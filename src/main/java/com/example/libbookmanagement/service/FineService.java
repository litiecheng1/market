package com.example.libbookmanagement.service;

import com.example.libbookmanagement.entity.BookCopy;
import com.example.libbookmanagement.entity.BorrowRec;
import com.example.libbookmanagement.entity.BorrowRecId;
import com.example.libbookmanagement.entity.Fine;
import com.example.libbookmanagement.repository.BookCopyRepository;
import com.example.libbookmanagement.repository.BorrowRecRepository;
import com.example.libbookmanagement.repository.FineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FineService {
    private static final String STATUS_UNRETURNED = "未归还";
    private static final String STATUS_RETURNED_OVERDUE = "超时还";
    private static final double FINE_PER_DAY = 0.5;

    @Autowired
    private BorrowRecRepository borrowRecRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private FineRepository fineRepository;

    /**
     * 更新所有未归还图书的罚款金额（定时任务调用）
     * @return 更新的记录数
     */
    @Transactional
    public int updateAllOverdueFines() {
        // 查询所有未归还的借阅记录
        List<BorrowRec> allUnreturned = borrowRecRepository.findByStatus(STATUS_UNRETURNED);
        int updatedCount = 0;

        for (BorrowRec rec : allUnreturned) {
            LocalDateTime retDate = rec.getRetDate();
            LocalDateTime now = LocalDateTime.now();

            if (retDate != null && retDate.isBefore(now)) {
                long overdueDays = calculateOverdueDays(rec);
                if (overdueDays > 0) {
                    Optional<Fine> existingFine = fineRepository.findBySnoAndBarCode(rec.getSno(), rec.getBarCode());
                    if (existingFine.isPresent()) {
                        Fine fine = existingFine.get();
                        if ("未缴纳".equals(fine.getStatus())) {
                            // 更新罚款金额
                            fine.setOverdueDays((int) overdueDays);
                            fine.setAmount(overdueDays * FINE_PER_DAY);
                            fine.setFineDate(LocalDateTime.now());
                            fineRepository.save(fine);
                            updatedCount++;
                        }
                    } else {
                        // 如果不存在罚款记录，创建一条
                        Fine fine = new Fine();
                        fine.setSno(rec.getSno());
                        fine.setBarCode(rec.getBarCode());
                        fine.setOverdueDays((int) overdueDays);
                        fine.setAmount(overdueDays * FINE_PER_DAY);
                        fine.setFineDate(LocalDateTime.now());
                        fine.setStatus("未缴纳");
                        fineRepository.save(fine);
                        updatedCount++;
                    }
                }
            }
        }

        System.out.println("定时任务更新罚款完成，共更新 " + updatedCount + " 条记录");
        return updatedCount;
    }

    /**
     * 获取学生的所有未缴罚款（优化N+1查询）
     */
    public List<Map<String, Object>> getUnpaidFines(String sno) {
        List<Map<String, Object>> fines = new ArrayList<>();

        // 从fine表查询未缴纳的罚款记录
        List<Fine> unpaidFines = fineRepository.findBySnoAndStatus(sno, "未缴纳");

        if (unpaidFines.isEmpty()) {
            return fines;
        }

        // 批量获取条形码列表
        List<String> barCodes = unpaidFines.stream()
                .map(Fine::getBarCode)
                .collect(Collectors.toList());

        // 批量查询图书副本信息
        List<BookCopy> bookCopies = bookCopyRepository.findAllById(barCodes);
        Map<String, BookCopy> bookCopyMap = bookCopies.stream()
                .collect(Collectors.toMap(BookCopy::getBarCode, copy -> copy));

        // 批量查询借阅记录
        List<BorrowRecId> borrowRecIds = unpaidFines.stream()
                .map(fine -> new BorrowRecId(fine.getSno(), fine.getBarCode()))
                .collect(Collectors.toList());
        List<BorrowRec> borrowRecs = borrowRecRepository.findAllById(borrowRecIds);
        Map<String, BorrowRec> borrowRecMap = borrowRecs.stream()
                .collect(Collectors.toMap(rec -> rec.getBarCode(), rec -> rec));

        for (Fine fine : unpaidFines) {
            Map<String, Object> fineMap = new HashMap<>();
            fineMap.put("id", fine.getId());
            fineMap.put("barCode", fine.getBarCode());
            fineMap.put("sno", fine.getSno());
            fineMap.put("overdueDays", fine.getOverdueDays());
            fineMap.put("fineAmount", fine.getAmount());
            fineMap.put("fineDate", formatDateTime(fine.getFineDate()));
            fineMap.put("paidDate", fine.getPaidDate() != null ? formatDateTime(fine.getPaidDate()) : null);
            fineMap.put("status", fine.getStatus());

            // 从缓存map中获取书名
            BookCopy copy = bookCopyMap.get(fine.getBarCode());
            if (copy != null && copy.getBook() != null) {
                fineMap.put("bookName", copy.getBook().getBName());
            } else {
                fineMap.put("bookName", getBookNameByBarCodeFallback(fine.getBarCode()));
            }

            // 从缓存map中获取借阅记录详情
            BorrowRec rec = borrowRecMap.get(fine.getBarCode());
            if (rec != null) {
                fineMap.put("borDate", formatDateTime(rec.getBorDate()));
                fineMap.put("retDate", formatDateTime(rec.getRetDate()));
                fineMap.put("realRetDate", formatDateTime(rec.getRealRetDate()));
            }

            fines.add(fineMap);
        }

        return fines;
    }

    /**
     * 为单条借阅记录生成罚款（仅当确实超期时才生成）
     */
    @Transactional
    public void generateFineForBorrowRec(BorrowRec rec) {
        // 计算超期天数
        long overdueDays = calculateOverdueDays(rec);

        // 只有超期天数 > 0 才需要罚款
        if (overdueDays <= 0) {
            System.out.println("未超期，不生成罚款：" + rec.getSno() + " - " + rec.getBarCode());
            return;
        }

        // 检查是否已经存在罚款记录
        Optional<Fine> existingFine = fineRepository.findBySnoAndBarCode(rec.getSno(), rec.getBarCode());
        if (existingFine.isPresent()) {
            // 如果已存在且未缴纳，更新超期天数
            Fine fine = existingFine.get();
            if ("未缴纳".equals(fine.getStatus())) {
                // 重新计算超期天数（因为时间可能继续推移）
                long newOverdueDays = calculateOverdueDays(rec);
                if (newOverdueDays > fine.getOverdueDays()) {
                    fine.setOverdueDays((int) newOverdueDays);
                    fine.setAmount(newOverdueDays * FINE_PER_DAY);
                    fine.setFineDate(LocalDateTime.now());
                    fineRepository.save(fine);
                    System.out.println("更新罚款记录：" + rec.getSno() + " - " + rec.getBarCode() +
                            " 超期" + newOverdueDays + "天，罚款" + fine.getAmount() + "元");
                }
            }
            return;
        }

        // 生成新的罚款记录
        Fine fine = new Fine();
        fine.setSno(rec.getSno());
        fine.setBarCode(rec.getBarCode());
        fine.setOverdueDays((int) overdueDays);
        fine.setAmount(overdueDays * FINE_PER_DAY);
        fine.setFineDate(LocalDateTime.now());
        fine.setStatus("未缴纳");

        fineRepository.save(fine);
        System.out.println("生成罚款记录：" + rec.getSno() + " - " + rec.getBarCode() +
                " 超期" + overdueDays + "天，罚款" + fine.getAmount() + "元");
    }

    /**
     * 为学生的所有超期记录生成罚款（批量优化版本）
     */
    @Transactional
    public void generateFinesForStudent(String sno) {
        int generatedCount = 0;

        // 批量查询所有超期记录（未归还 + 超时还）
        List<BorrowRec> overdueRecords = borrowRecRepository.findAllOverdueRecords(sno);

        if (overdueRecords.isEmpty()) {
            System.out.println("学号 " + sno + " 没有超期记录");
            return;
        }

        // 批量查询已存在的罚款记录
        List<Fine> existingFines = fineRepository.findBySno(sno);
        Map<String, Fine> existingFineMap = existingFines.stream()
                .collect(Collectors.toMap(fine -> fine.getBarCode(), fine -> fine));

        for (BorrowRec rec : overdueRecords) {
            long overdueDays = calculateOverdueDays(rec);

            if (overdueDays <= 0) {
                continue;
            }

            Fine existingFine = existingFineMap.get(rec.getBarCode());
            if (existingFine != null) {
                // 更新已存在的罚款记录
                if ("未缴纳".equals(existingFine.getStatus()) && overdueDays > existingFine.getOverdueDays()) {
                    existingFine.setOverdueDays((int) overdueDays);
                    existingFine.setAmount(overdueDays * FINE_PER_DAY);
                    existingFine.setFineDate(LocalDateTime.now());
                    fineRepository.save(existingFine);
                    generatedCount++;
                }
            } else {
                // 创建新的罚款记录
                Fine fine = new Fine();
                fine.setSno(rec.getSno());
                fine.setBarCode(rec.getBarCode());
                fine.setOverdueDays((int) overdueDays);
                fine.setAmount(overdueDays * FINE_PER_DAY);
                fine.setFineDate(LocalDateTime.now());
                fine.setStatus("未缴纳");
                fineRepository.save(fine);
                generatedCount++;
            }
        }

        System.out.println("为学号 " + sno + " 生成罚款完成，共处理 " + generatedCount + " 条罚款记录");
    }

    /**
     * 补生成所有已超期但无罚款记录的罚款
     * 用于修复历史数据
     * @return 生成的罚款记录数
     */
    @Transactional
    public int repairMissingFines() {
        // 查询所有"超时还"状态的借阅记录
        List<BorrowRec> overdueRecords = borrowRecRepository.findByStatus(STATUS_RETURNED_OVERDUE);
        int generatedCount = 0;

        for (BorrowRec rec : overdueRecords) {
            // 检查是否已有罚款记录
            Optional<Fine> existingFine = fineRepository.findBySnoAndBarCode(
                    rec.getSno(), rec.getBarCode()
            );
            if (existingFine.isPresent()) {
                // 已有记录，检查是否需要更新超期天数
                Fine fine = existingFine.get();
                if ("未缴纳".equals(fine.getStatus())) {
                    long overdueDays = calculateOverdueDays(rec);
                    if (overdueDays > fine.getOverdueDays()) {
                        fine.setOverdueDays((int) overdueDays);
                        fine.setAmount(overdueDays * FINE_PER_DAY);
                        fine.setFineDate(LocalDateTime.now());
                        fineRepository.save(fine);
                        generatedCount++;
                        System.out.println("更新罚款记录：" + rec.getSno() + " - " + rec.getBarCode() +
                                " 超期" + overdueDays + "天，罚款" + fine.getAmount() + "元");
                    }
                }
                continue;
            }

            long overdueDays = calculateOverdueDays(rec);
            if (overdueDays > 0) {
                Fine fine = new Fine();
                fine.setSno(rec.getSno());
                fine.setBarCode(rec.getBarCode());
                fine.setOverdueDays((int) overdueDays);
                fine.setAmount(overdueDays * FINE_PER_DAY);
                fine.setFineDate(LocalDateTime.now());
                fine.setStatus("未缴纳");
                fineRepository.save(fine);
                generatedCount++;

                System.out.println("补生成罚款记录：" + rec.getSno() + " - " + rec.getBarCode() +
                        " 超期" + overdueDays + "天，罚款" + fine.getAmount() + "元");
            }
        }

        // 也检查未归还但已超期的记录
        List<BorrowRec> unreturnedRecords = borrowRecRepository.findByStatus(STATUS_UNRETURNED);
        for (BorrowRec rec : unreturnedRecords) {
            LocalDateTime retDate = rec.getRetDate();
            LocalDateTime now = LocalDateTime.now();
            if (retDate != null && retDate.isBefore(now)) {
                // 检查是否已有罚款记录
                Optional<Fine> existingFine = fineRepository.findBySnoAndBarCode(
                        rec.getSno(), rec.getBarCode()
                );
                if (existingFine.isPresent()) {
                    continue;
                }

                long overdueDays = calculateOverdueDays(rec);
                if (overdueDays > 0) {
                    Fine fine = new Fine();
                    fine.setSno(rec.getSno());
                    fine.setBarCode(rec.getBarCode());
                    fine.setOverdueDays((int) overdueDays);
                    fine.setAmount(overdueDays * FINE_PER_DAY);
                    fine.setFineDate(LocalDateTime.now());
                    fine.setStatus("未缴纳");
                    fineRepository.save(fine);
                    generatedCount++;

                    System.out.println("补生成罚款记录（未归还）：" + rec.getSno() + " - " + rec.getBarCode() +
                            " 超期" + overdueDays + "天，罚款" + fine.getAmount() + "元");
                }
            }
        }

        System.out.println("补生成罚款完成，共生成/更新 " + generatedCount + " 条记录");
        return generatedCount;
    }

    /**
     * 计算超期天数（核心逻辑）
     * 修改：只要超期，不足1天按1天计算
     * @return 超期天数，如果未超期则返回0
     */
    private long calculateOverdueDays(BorrowRec rec) {
        LocalDateTime retDate = rec.getRetDate();        // 应还日期
        LocalDateTime realRetDate = rec.getRealRetDate(); // 实际归还日期
        LocalDateTime now = LocalDateTime.now();

        // 没有应还日期，不产生罚款
        if (retDate == null) {
            return 0;
        }

        long days = 0;

        if (STATUS_UNRETURNED.equals(rec.getStatus())) {
            // 未归还：只有当前时间超过应还日期才产生罚款
            if (now.isAfter(retDate)) {
                days = ChronoUnit.DAYS.between(retDate, now);
                if (days == 0 && now.isAfter(retDate)) {
                    days = 1; // 只要超时，至少算1天
                }
            }
        } else if (STATUS_RETURNED_OVERDUE.equals(rec.getStatus())) {
            // 超时还：只有实际归还日期超过应还日期才产生罚款
            if (realRetDate != null && realRetDate.isAfter(retDate)) {
                days = ChronoUnit.DAYS.between(retDate, realRetDate);
                if (days == 0 && realRetDate.isAfter(retDate)) {
                    days = 1; // 只要超时，至少算1天
                }
            }
        }
        // 按时还或其他情况：不产生罚款
        return days;
    }

    /**
     * 检查借阅记录是否超期
     */
    public boolean isOverdue(BorrowRec rec) {
        return calculateOverdueDays(rec) > 0;
    }

    /**
     * 获取学生的未缴罚款总金额
     */
    public double getTotalUnpaidFineAmount(String sno) {
        Double total = fineRepository.sumUnpaidFines(sno);
        return total != null ? total : 0.0;
    }

    /**
     * 获取学生的未缴罚款数量
     */
    public long getUnpaidFineCount(String sno) {
        return fineRepository.countUnpaidFines(sno);
    }

    /**
     * 缴纳所有未缴罚款
     */
    @Transactional
    public Map<String, Object> payAllFines(String sno) {
        Map<String, Object> result = new HashMap<>();

        double totalAmount = getTotalUnpaidFineAmount(sno);
        long fineCount = getUnpaidFineCount(sno);

        if (fineCount == 0) {
            result.put("success", true);
            result.put("message", "没有需要缴纳的罚款");
            result.put("amount", 0.0);
            result.put("count", 0);
            return result;
        }

        // 更新所有未缴纳罚款的状态
        int updatedCount = fineRepository.markAllAsPaid(sno);

        result.put("success", true);
        result.put("message", String.format("成功缴纳 %d 笔罚款，共计 %.2f 元", updatedCount, totalAmount));
        result.put("amount", totalAmount);
        result.put("count", updatedCount);

        System.out.println("缴纳罚款完成，学号：" + sno + "，共" + updatedCount + "笔，总金额：" + totalAmount + "元");
        return result;
    }

    /**
     * 缴纳单笔罚款
     */
    @Transactional
    public Map<String, Object> payFineById(Integer fineId) {
        Map<String, Object> result = new HashMap<>();

        Optional<Fine> fineOpt = fineRepository.findById(fineId);
        if (fineOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "罚款记录不存在");
            return result;
        }

        Fine fine = fineOpt.get();
        if ("已缴纳".equals(fine.getStatus())) {
            result.put("success", false);
            result.put("message", "该罚款已缴纳");
            return result;
        }

        int updatedCount = fineRepository.markAsPaid(fineId);

        if (updatedCount > 0) {
            result.put("success", true);
            result.put("message", String.format("成功缴纳罚款 %.2f 元", fine.getAmount()));
            result.put("amount", fine.getAmount());
            result.put("fineId", fineId);
        } else {
            result.put("success", false);
            result.put("message", "缴纳失败");
        }

        return result;
    }

    /**
     * 备用书名推断方法
     */
    private String getBookNameByBarCodeFallback(String barCode) {
        if (barCode == null) return "未知图书";
        if (barCode.startsWith("978001")) return "红楼梦";
        if (barCode.startsWith("978002")) return "西游记";
        if (barCode.startsWith("978003")) return "水浒传";
        if (barCode.startsWith("978004")) return "三国演义";
        if (barCode.startsWith("978005")) return "计算机组成原理";
        if (barCode.startsWith("978006")) return "活着";
        return "未知图书";
    }

    /**
     * 格式化日期时间
     */
    private String formatDateTime(LocalDateTime date) {
        if (date == null) return "";
        return date.toString().replace("T", " ");
    }
}
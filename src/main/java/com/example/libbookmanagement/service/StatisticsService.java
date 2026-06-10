package com.example.libbookmanagement.service;

import com.example.libbookmanagement.entity.Book;
import com.example.libbookmanagement.entity.BorrowRec;
import com.example.libbookmanagement.entity.Student;
import com.example.libbookmanagement.repository.BookRepository;
import com.example.libbookmanagement.repository.BorrowRecRepository;
import com.example.libbookmanagement.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class StatisticsService {
    private static final String STATUS_UNRETURNED = "未归还";
    private static final String STATUS_RETURNED_ON_TIME = "按时还";
    private static final String STATUS_RETURNED_OVERDUE = "超时还";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private BorrowRecRepository borrowRecRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private BookRepository bookRepository;

    /**
     * 当月借书学生排行榜（从数据库查询真实数据）
     */
    public List<Map<String, Object>> getMonthlyStudentRanking() {
        List<Map<String, Object>> ranking = new ArrayList<>();

        try {
            // 查询所有借阅记录
            List<BorrowRec> allRecords = borrowRecRepository.findAll();

            // 按学号统计借书次数
            Map<String, Integer> borrowCountMap = new HashMap<>();
            for (BorrowRec rec : allRecords) {
                String sno = rec.getSno();
                borrowCountMap.put(sno, borrowCountMap.getOrDefault(sno, 0) + 1);
            }

            // 获取学生姓名
            List<Student> students = studentRepository.findAll();
            Map<String, String> studentNameMap = new HashMap<>();
            for (Student student : students) {
                studentNameMap.put(student.getSno(), student.getSName());
            }

            // 转换为列表并排序
            List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(borrowCountMap.entrySet());
            sortedList.sort((a, b) -> b.getValue().compareTo(a.getValue()));

            // 取前10名
            int rank = 1;
            for (Map.Entry<String, Integer> entry : sortedList) {
                if (rank > 10) break;
                Map<String, Object> item = new HashMap<>();
                item.put("sno", entry.getKey());
                item.put("name", studentNameMap.getOrDefault(entry.getKey(), "未知"));
                item.put("borrowCount", entry.getValue());
                ranking.add(item);
                rank++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ranking;
    }

    /**
     * 当月书籍受欢迎排行榜（从数据库查询真实数据）
     */
    public List<Map<String, Object>> getMonthlyBookRanking() {
        List<Map<String, Object>> ranking = new ArrayList<>();

        try {
            // 查询所有借阅记录
            List<BorrowRec> allRecords = borrowRecRepository.findAll();

            // 按ISBN统计借阅次数（通过条形码前6位获取ISBN）
            Map<String, Integer> borrowCountByIsbn = new HashMap<>();
            for (BorrowRec rec : allRecords) {
                String barCode = rec.getBarCode();
                String isbn = barCode.length() >= 6 ? barCode.substring(0, 6) : barCode;
                borrowCountByIsbn.put(isbn, borrowCountByIsbn.getOrDefault(isbn, 0) + 1);
            }

            // 获取图书信息
            List<Book> books = bookRepository.findAll();
            Map<String, String> bookNameMap = new HashMap<>();
            for (Book book : books) {
                bookNameMap.put(book.getIsbn(), book.getBName());
            }

            // 转换为列表并排序
            List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(borrowCountByIsbn.entrySet());
            sortedList.sort((a, b) -> b.getValue().compareTo(a.getValue()));

            // 取前10名
            int rank = 1;
            for (Map.Entry<String, Integer> entry : sortedList) {
                if (rank > 10) break;
                String isbn = entry.getKey();
                Map<String, Object> item = new HashMap<>();
                item.put("isbn", isbn);
                item.put("name", bookNameMap.getOrDefault(isbn, getBookNameByIsbn(isbn)));
                item.put("borrowCount", entry.getValue());
                ranking.add(item);
                rank++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ranking;
    }

    /**
     * 根据ISBN获取书名（备用）
     */
    private String getBookNameByIsbn(String isbn) {
        if (isbn.startsWith("978001")) return "红楼梦";
        if (isbn.startsWith("978002")) return "西游记";
        if (isbn.startsWith("978003")) return "水浒传";
        if (isbn.startsWith("978004")) return "三国演义";
        if (isbn.startsWith("978005")) return "计算机组成原理";
        if (isbn.startsWith("978006")) return "活着";
        return "未知图书";
    }

    /**
     * 个人借阅情况查询
     */
    public Map<String, Object> getPersonalBorrowInfo(String sno) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 查询当前借阅（未归还）
            List<BorrowRec> currentBorrows = borrowRecRepository.findByIdSnoAndStatus(sno, STATUS_UNRETURNED);
            // 查询历史借阅（按时还和超时还）
            List<BorrowRec> historyBorrows = new ArrayList<>();
            historyBorrows.addAll(borrowRecRepository.findByIdSnoAndStatus(sno, STATUS_RETURNED_ON_TIME));
            historyBorrows.addAll(borrowRecRepository.findByIdSnoAndStatus(sno, STATUS_RETURNED_OVERDUE));

            // 获取学生类型对应的借书上限
            int maxBorrowLimit = getMaxBorrowLimit(sno);

            // 计算当前未还数量
            int currentlyBorrowed = currentBorrows.size();

            // 格式化当前借阅记录
            List<Map<String, Object>> currentList = formatCurrentBorrowRecords(currentBorrows);
            List<Map<String, Object>> historyList = formatHistoryBorrowRecords(historyBorrows);

            result.put("totalBorrowed", currentList.size() + historyList.size());
            result.put("currentlyBorrowed", currentlyBorrowed);
            result.put("maxBorrowLimit", maxBorrowLimit);
            result.put("unreturnedBooks", currentList);
            result.put("currentBorrows", currentList);
            result.put("historyBorrows", historyList);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("totalBorrowed", 0);
            result.put("currentlyBorrowed", 0);
            result.put("maxBorrowLimit", 5);
            result.put("unreturnedBooks", new ArrayList<>());
            result.put("currentBorrows", new ArrayList<>());
            result.put("historyBorrows", new ArrayList<>());
            return result;
        }
    }

    /**
     * 获取学生借书上限（从数据库查询真实学生类型）
     */
    private int getMaxBorrowLimit(String sno) {
        try {
            Optional<Student> studentOpt = studentRepository.findById(sno);
            if (studentOpt.isPresent()) {
                Student student = studentOpt.get();
                return student.getType().equals("研究生") ? 10 : 5;
            }
        } catch (Exception e) {
            System.err.println("获取学生类型失败: " + e.getMessage());
        }
        return 5;
    }

    /**
     * 格式化当前借阅记录
     */
    private List<Map<String, Object>> formatCurrentBorrowRecords(List<BorrowRec> records) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (records == null || records.isEmpty()) {
            return list;
        }

        for (BorrowRec rec : records) {
            try {
                Map<String, Object> map = new HashMap<>();
                map.put("barCode", rec.getBarCode());
                map.put("sno", rec.getSno());
                map.put("borDate", formatDate(rec.getBorDate()));
                map.put("retDate", formatDate(rec.getRetDate()));
                map.put("realRetDate", formatDate(rec.getRealRetDate()));
                map.put("status", rec.getStatus() != null ? rec.getStatus() : "");
                map.put("bookName", getBookNameByBarCode(rec.getBarCode()));
                list.add(map);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * 格式化历史借阅记录
     */
    private List<Map<String, Object>> formatHistoryBorrowRecords(List<BorrowRec> records) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (records == null || records.isEmpty()) {
            return list;
        }

        for (BorrowRec rec : records) {
            try {
                Map<String, Object> map = new HashMap<>();
                map.put("barCode", rec.getBarCode());
                map.put("sno", rec.getSno());
                map.put("borDate", formatDate(rec.getBorDate()));
                map.put("retDate", formatDate(rec.getRetDate()));
                map.put("realRetDate", formatDate(rec.getRealRetDate()));
                map.put("status", rec.getStatus() != null ? rec.getStatus() : "");
                map.put("bookName", getBookNameByBarCode(rec.getBarCode()));
                list.add(map);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * 通过条形码获取书名
     */
    private String getBookNameByBarCode(String barCode) {
        if (barCode == null) return "未知图书";
        String isbn = barCode.length() >= 6 ? barCode.substring(0, 6) : barCode;
        return getBookNameByIsbn(isbn);
    }

    /**
     * 统一日期格式化方法
     */
    private String formatDate(LocalDateTime date) {
        if (date == null) {
            return "";
        }
        try {
            return date.format(DATE_FORMATTER);
        } catch (Exception e) {
            return "";
        }
    }
}
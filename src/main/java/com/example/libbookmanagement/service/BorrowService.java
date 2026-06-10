package com.example.libbookmanagement.service;

import com.example.libbookmanagement.entity.BookCopy;
import com.example.libbookmanagement.entity.BorrowRec;
import com.example.libbookmanagement.entity.BorrowRecId;
import com.example.libbookmanagement.entity.LibCard;
import com.example.libbookmanagement.entity.Student;
import com.example.libbookmanagement.repository.BookCopyRepository;
import com.example.libbookmanagement.repository.BorrowRecRepository;
import com.example.libbookmanagement.repository.LibCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BorrowService {
    private static final int BORROW_DAYS = 30;
    private static final int GRADUATE_MAX_BORROW = 10;
    private static final int UNDERGRADUATE_MAX_BORROW = 5;

    // 数据库状态常量（字符串）
    private static final String STATUS_UNRETURNED = "未归还";
    private static final String STATUS_RETURNED_ON_TIME = "按时还";
    private static final String STATUS_RETURNED_OVERDUE = "超时还";

    @Autowired
    private LibCardRepository libCardRepository;

    @Autowired
    private BorrowRecRepository borrowRecRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private FineService fineService;

    @Transactional
    public String borrowBooks(String sno, List<String> barCodes) {
        // 1. 检查借书证
        LibCard card = libCardRepository.findById(sno).orElse(null);
        if (card == null) return "借书证不存在";
        if (!card.getStatus().equals("正常")) return "借书证状态异常，无法借书";

        // 2. 检查学生信息
        Student student = card.getStudent();
        if (student == null) return "学生信息不存在";

        // 3. 检查是否有未缴罚款（新增）
        double unpaidFine = fineService.getTotalUnpaidFineAmount(sno);
        if (unpaidFine > 0) {
            return "您有未缴罚款 " + String.format("%.2f", unpaidFine) + " 元，请先缴纳罚款后再借书";
        }

        // 4. 检查借书数量上限
        long unreturned = borrowRecRepository.countUnreturnedBySno(sno);
        int max = student.getType().equals("研究生") ? GRADUATE_MAX_BORROW : UNDERGRADUATE_MAX_BORROW;

        if (unreturned + barCodes.size() > max) {
            return "超出借书上限，可借：" + (max - unreturned) + "本";
        }

        // 5. 执行借书操作
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime ret = now.plusDays(BORROW_DAYS);

        for (String barCode : barCodes) {
            // 更新图书副本状态为"借出"
            Optional<BookCopy> copyOpt = bookCopyRepository.findById(barCode);
            if (copyOpt.isPresent()) {
                BookCopy copy = copyOpt.get();
                if (!"可借".equals(copy.getStatus())) {
                    return "图书条形码 " + barCode + " 状态异常，无法借出";
                }
                copy.setStatus("借出");
                bookCopyRepository.save(copy);
            } else {
                return "图书条形码 " + barCode + " 不存在";
            }

            BorrowRec rec = new BorrowRec();
            rec.setId(new BorrowRecId(sno, barCode));
            rec.setBorDate(now);
            rec.setRetDate(ret);
            rec.setStatus(STATUS_UNRETURNED);
            borrowRecRepository.save(rec);
        }
        return "借书成功！共借出" + barCodes.size() + "本";
    }

    @Transactional
    public String returnBook(String barCode) {
        BorrowRec rec = borrowRecRepository.findByIdBarCodeAndStatus(barCode, STATUS_UNRETURNED);
        if (rec == null) return "该图书未被借出";

        LocalDateTime now = LocalDateTime.now();
        rec.setRealRetDate(now);

        // 更新图书副本状态为"可借"
        Optional<BookCopy> copyOpt = bookCopyRepository.findById(barCode);
        if (copyOpt.isPresent()) {
            BookCopy copy = copyOpt.get();
            copy.setStatus("可借");
            bookCopyRepository.save(copy);
        }

        if (now.isAfter(rec.getRetDate())) {
            rec.setStatus(STATUS_RETURNED_OVERDUE);
            // 还书超时，生成罚款记录
            fineService.generateFineForBorrowRec(rec);
            return "还书成功！图书超时，已生成罚款记录，请及时缴纳罚款";
        } else {
            rec.setStatus(STATUS_RETURNED_ON_TIME);
            return "还书成功！";
        }
    }
}
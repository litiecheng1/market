package com.example.libbookmanagement.service;

import com.example.libbookmanagement.entity.CardRec;
import com.example.libbookmanagement.entity.LibCard;
import com.example.libbookmanagement.entity.Student;
import com.example.libbookmanagement.repository.CardRecRepository;
import com.example.libbookmanagement.repository.LibCardRepository;
import com.example.libbookmanagement.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class LibCardService {
    @Autowired
    private LibCardRepository libCardRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CardRecRepository cardRecRepository;

    // 生成4位卡号
    private String generateCardNo() {
        Random random = new Random();
        int num = random.nextInt(9000) + 1000;
        return String.valueOf(num);
    }

    // 生成5位流水号
    private String generateSerNum() {
        long count = cardRecRepository.count();
        return String.format("c%04d", count + 1);
    }

    // 新办借书证
    public String createLibraryCard(String sno) {
        Optional<Student> optionalStudent = studentRepository.findById(sno);
        if (optionalStudent.isEmpty()) {
            return "学生不存在";
        }

        Optional<LibCard> optionalCard = libCardRepository.findById(sno);
        if (optionalCard.isPresent() && !optionalCard.get().getStatus().equals("注销")) {
            return "该学生已有有效借书证";
        }

        String cardNo = generateCardNo();

        LibCard card = new LibCard();
        card.setSno(sno);
        card.setCardNo(cardNo);
        card.setPassword("123456"); // 默认密码
        card.setStatus("正常");

        libCardRepository.save(card);

        // 记录操作日志
        CardRec cardRec = new CardRec();
        cardRec.setSerNum(generateSerNum());
        cardRec.setSno(sno);
        cardRec.setNewCardNo(cardNo);
        cardRec.setOpType("新办");
        cardRec.setOpTime(LocalDateTime.now());

        cardRecRepository.save(cardRec);

        return "借书证办理成功，卡号：" + cardNo;
    }

    // 挂失借书证
    public String loseLibraryCard(String sno) {
        Optional<LibCard> optionalCard = libCardRepository.findById(sno);
        if (optionalCard.isEmpty()) {
            return "借书证不存在";
        }

        LibCard card = optionalCard.get();
        if (!card.getStatus().equals("正常")) {
            return "借书证状态异常，无法挂失";
        }

        card.setStatus("挂失");
        libCardRepository.save(card);

        // 记录操作日志
        CardRec cardRec = new CardRec();
        cardRec.setSerNum(generateSerNum());
        cardRec.setSno(sno);
        cardRec.setOriginCardNo(card.getCardNo());
        cardRec.setOpType("挂失");
        cardRec.setOpTime(LocalDateTime.now());

        cardRecRepository.save(cardRec);

        return "借书证挂失成功";
    }

    // 补办借书证
    public String reissueLibraryCard(String sno) {
        Optional<LibCard> optionalCard = libCardRepository.findById(sno);
        if (optionalCard.isEmpty()) {
            return "借书证不存在";
        }

        LibCard oldCard = optionalCard.get();
        if (!oldCard.getStatus().equals("挂失")) {
            return "只有挂失状态的借书证才能补办";
        }

        String newCardNo = generateCardNo();

        // 注销旧卡
        oldCard.setStatus("注销");
        libCardRepository.save(oldCard);

        // 创建新卡
        LibCard newCard = new LibCard();
        newCard.setSno(sno);
        newCard.setCardNo(newCardNo);
        newCard.setPassword("123456"); // 重置密码
        newCard.setEmail(oldCard.getEmail());
        newCard.setStatus("正常");

        libCardRepository.save(newCard);

        // 记录操作日志
        CardRec cardRec = new CardRec();
        cardRec.setSerNum(generateSerNum());
        cardRec.setSno(sno);
        cardRec.setOriginCardNo(oldCard.getCardNo());
        cardRec.setNewCardNo(newCardNo);
        cardRec.setOpType("补办");
        cardRec.setOpTime(LocalDateTime.now());

        cardRecRepository.save(cardRec);

        return "借书证补办成功，新卡号：" + newCardNo;
    }

    // 注销借书证
    public String cancelLibraryCard(String sno) {
        Optional<LibCard> optionalCard = libCardRepository.findById(sno);
        if (optionalCard.isEmpty()) {
            return "借书证不存在";
        }

        LibCard card = optionalCard.get();
        if (card.getStatus().equals("注销")) {
            return "借书证已注销";
        }

        card.setStatus("注销");
        libCardRepository.save(card);

        // 记录操作日志
        CardRec cardRec = new CardRec();
        cardRec.setSerNum(generateSerNum());
        cardRec.setSno(sno);
        cardRec.setOriginCardNo(card.getCardNo());
        cardRec.setOpType("注销");
        cardRec.setOpTime(LocalDateTime.now());

        cardRecRepository.save(cardRec);

        return "借书证注销成功";
    }
}
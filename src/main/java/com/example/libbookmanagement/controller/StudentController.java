package com.example.libbookmanagement.controller;

import com.example.libbookmanagement.entity.LibCard;
import com.example.libbookmanagement.entity.Student;
import com.example.libbookmanagement.repository.LibCardRepository;
import com.example.libbookmanagement.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "*")
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private LibCardRepository libCardRepository;

    /**
     * 学生登录
     * POST /api/student/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String sno = loginRequest.get("username");
        String password = loginRequest.get("password");

        System.out.println("=== 学生登录请求 ===");
        System.out.println("学号: " + sno);
        System.out.println("密码: " + password);

        if (sno == null || password == null || sno.isEmpty() || password.isEmpty()) {
            return ResponseEntity.badRequest().body("学号和密码不能为空");
        }

        Optional<Student> studentOpt = studentRepository.findById(sno);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("学号不存在");
        }

        Optional<LibCard> cardOpt = libCardRepository.findById(sno);
        if (cardOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("借书证不存在，请联系管理员办理");
        }

        LibCard card = cardOpt.get();
        if (!password.equals(card.getPassword())) {
            return ResponseEntity.badRequest().body("密码错误");
        }

        if (!"正常".equals(card.getStatus())) {
            return ResponseEntity.badRequest().body("借书证状态异常：" + card.getStatus());
        }

        Student student = studentOpt.get();
        Map<String, Object> result = new HashMap<>();
        result.put("sno", student.getSno());
        result.put("sName", student.getSName());
        result.put("sSex", student.getSSex());
        result.put("type", student.getType());
        result.put("college", student.getCollege());
        result.put("major", student.getMajor());
        result.put("cardNo", card.getCardNo());
        result.put("cardStatus", card.getStatus());
        result.put("email", card.getEmail());
        result.put("userType", "student");

        System.out.println("学生登录成功: " + student.getSName());
        return ResponseEntity.ok(result);
    }

    /**
     * 获取学生个人信息
     * GET /api/student/info/{sno}
     */
    @GetMapping("/info/{sno}")
    public ResponseEntity<?> getStudentInfo(@PathVariable String sno) {
        Optional<Student> studentOpt = studentRepository.findById(sno);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("学生不存在");
        }

        Optional<LibCard> cardOpt = libCardRepository.findById(sno);
        Map<String, Object> result = new HashMap<>();

        Student student = studentOpt.get();
        result.put("sno", student.getSno());
        result.put("sName", student.getSName());
        result.put("sSex", student.getSSex());
        result.put("type", student.getType());
        result.put("college", student.getCollege());
        result.put("major", student.getMajor());
        result.put("sDorm", student.getSDorm());
        result.put("sAge", student.getSAge());
        result.put("originPlace", student.getOriginPlace());

        if (cardOpt.isPresent()) {
            LibCard card = cardOpt.get();
            result.put("cardNo", card.getCardNo());
            result.put("cardStatus", card.getStatus());
            result.put("email", card.getEmail());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 修改密码
     * PUT /api/student/password/{sno}
     */
    @PutMapping("/password/{sno}")
    public ResponseEntity<?> changePassword(@PathVariable String sno,
                                            @RequestBody Map<String, String> request) {
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        if (oldPassword == null || newPassword == null ||
                oldPassword.isEmpty() || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().body("旧密码和新密码不能为空");
        }

        Optional<LibCard> cardOpt = libCardRepository.findById(sno);
        if (cardOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("借书证不存在");
        }

        LibCard card = cardOpt.get();
        if (!oldPassword.equals(card.getPassword())) {
            return ResponseEntity.badRequest().body("原密码错误");
        }

        card.setPassword(newPassword);
        libCardRepository.save(card);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "密码修改成功");
        return ResponseEntity.ok(result);
    }
}
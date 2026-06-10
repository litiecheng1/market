package com.example.libbookmanagement.repository;

import com.example.libbookmanagement.entity.Fine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface FineRepository extends JpaRepository<Fine, Integer> {

    // 查询学生未缴纳的罚款
    List<Fine> findBySnoAndStatus(String sno, String status);

    // 查询学生所有罚款
    List<Fine> findBySno(String sno);

    // 查询特定借阅记录的罚款
    Optional<Fine> findBySnoAndBarCode(String sno, String barCode);

    // 查询未缴纳的罚款总数
    @Query("SELECT COUNT(f) FROM Fine f WHERE f.sno = :sno AND f.status = '未缴纳'")
    long countUnpaidFines(@Param("sno") String sno);

    // 查询未缴纳罚款总金额
    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM Fine f WHERE f.sno = :sno AND f.status = '未缴纳'")
    Double sumUnpaidFines(@Param("sno") String sno);

    // 更新所有未缴纳罚款为已缴纳
    @Modifying
    @Transactional
    @Query("UPDATE Fine f SET f.status = '已缴纳', f.paidDate = CURRENT_TIMESTAMP WHERE f.sno = :sno AND f.status = '未缴纳'")
    int markAllAsPaid(@Param("sno") String sno);

    // 更新特定罚款为已缴纳
    @Modifying
    @Transactional
    @Query("UPDATE Fine f SET f.status = '已缴纳', f.paidDate = CURRENT_TIMESTAMP WHERE f.id = :id AND f.status = '未缴纳'")
    int markAsPaid(@Param("id") Integer id);
}
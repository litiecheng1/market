package com.example.libbookmanagement.repository;

import com.example.libbookmanagement.entity.BorrowRec;
import com.example.libbookmanagement.entity.BorrowRecId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowRecRepository extends JpaRepository<BorrowRec, BorrowRecId> {

    // 按学号和状态查询
    List<BorrowRec> findByIdSnoAndStatus(String sno, String status);

    // 按学号查询所有记录
    List<BorrowRec> findByIdSno(String sno);

    // 按条形码查询
    List<BorrowRec> findByIdBarCode(String barCode);

    // 按条形码和状态查询
    BorrowRec findByIdBarCodeAndStatus(String barCode, String status);

    // 按状态查询
    List<BorrowRec> findByStatus(String status);

    // 统计学号下未归还的图书数量
    @Query("SELECT COUNT(b) FROM BorrowRec b WHERE b.id.sno = :sno AND b.status = '未归还'")
    long countUnreturnedBySno(@Param("sno") String sno);

    // 查询学号下所有超期记录（未归还+超时还）
    @Query("SELECT b FROM BorrowRec b WHERE b.id.sno = :sno AND (b.status = '未归还' OR b.status = '超时还')")
    List<BorrowRec> findAllOverdueRecords(@Param("sno") String sno);
}
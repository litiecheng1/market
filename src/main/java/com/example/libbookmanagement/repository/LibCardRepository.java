package com.example.libbookmanagement.repository;

import com.example.libbookmanagement.entity.LibCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LibCardRepository extends JpaRepository<LibCard, String> {
    Optional<LibCard> findByCardNo(String cardNo);
}
package com.example.libbookmanagement.repository;

import com.example.libbookmanagement.entity.CardRec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRecRepository extends JpaRepository<CardRec, String> {
}
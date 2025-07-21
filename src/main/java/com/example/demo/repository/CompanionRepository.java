package com.example.demo.repository;

import com.example.demo.module.Companion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanionRepository extends JpaRepository<Companion, Long> {
    // 根据访客ID查找所有同行人
    List<Companion> findByVisitorId(Long visitorId);
}
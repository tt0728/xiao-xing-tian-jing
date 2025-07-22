package com.example.demo.repository;

import com.example.demo.module.Approval;
import com.example.demo.module.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VisitorRepository extends JpaRepository<Visitor, Long> {
    List<Visitor> findByStatusIn(List<Approval> statuses);

    List<Visitor> findByVisitorAccountId(Long visitorAccountId);

    List<Visitor> findAll();
}
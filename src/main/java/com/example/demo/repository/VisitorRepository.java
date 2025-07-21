package com.example.demo.repository;

import com.example.demo.module.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VisitorRepository extends JpaRepository<Visitor, Long> {
    List<Visitor> findByStatus(String status);

    Optional<Visitor> findByQrCodeContent(String qrCodeContent);

    // 如果需要，可以添加根据受访人或部门查询的方法
    List<Visitor> findByVisitedPersonContainingIgnoreCaseOrVisitedPersonDepartmentContainingIgnoreCase(
            String visitedPersonKeyword, String departmentKeyword);
}
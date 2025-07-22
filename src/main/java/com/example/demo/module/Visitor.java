package com.example.demo.module;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Visitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long visitorAccountId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false, unique = true) // 身份证号通常是唯一的
    private String idCard;
    @Column(nullable = false) // 如果数据库要求非空，这里也设置非空
    private String visitedPerson;
    @Column(nullable = false) // 如果数据库要求非空，这里也设置非空
    private String visitedPersonDepartment;
    private LocalDate visitDate;
    private LocalTime visitTimeStart;
    private LocalTime visitTimeEnd;
    private String purpose;

    @Enumerated(EnumType.STRING)
    private Approval status = Approval.PENDING; // 默认待审核

    private String comments; // 审批意见

    // 进出记录
    private LocalTime entryTime;
    private LocalTime exitTime;

    @OneToMany(mappedBy = "visitor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Companion> companions;
}
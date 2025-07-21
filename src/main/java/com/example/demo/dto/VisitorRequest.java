package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// 这个DTO用于接收访客登记页面提交的所有数据
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisitorRequest {
    // 预约人信息
    private String name;
    private String phone;
    private String idCard;
    private String workUnit;
    private LocalDate visitDate;
    private LocalTime visitTimeStart;
    private LocalTime visitTimeEnd;
    private String purpose;

    // 受访人信息
    private String visitedPerson;
    private String visitedPersonDepartment;

    // 同行人信息列表
    private List<CompanionDTO> companions;
}
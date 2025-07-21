package com.example.demo.module;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List; // 导入List

@Entity
@Table(name = "visitor")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Visitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- 预约人信息 ---
    @Column(nullable = false)
    private String name; // 预约人姓名

    @Column(nullable = false)
    private String phone; // 预约人电话

    @Column(name = "id_card") // 证件号码
    private String idCard;

    @Column(name = "work_unit") // 工作单位
    private String workUnit;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "visit_time_start")
    private LocalTime visitTimeStart;

    @Column(name = "visit_time_end")
    private LocalTime visitTimeEnd;

    @Column(name = "purpose") // 拜访事由
    private String purpose;

    // --- 受访人信息 ---
    @Column(name = "visited_person", nullable = false) // 受访人姓名
    private String visitedPerson;

    @Column(name = "visited_person_department") // 受访人部门
    private String visitedPersonDepartment;

    // --- 访客状态与凭证 ---
    @Column(nullable = false)
    private String status; // PENDING, APPROVED, REJECTED, IN_VISIT, DEPARTED

    @Column(name = "qr_code_content")
    private String qrCodeContent;

    @Column(name = "entry_time")
    private LocalDateTime entryTime;

    @Column(name = "exit_time")
    private LocalDateTime exitTime;

    // --- 同行人信息 (一对多关联) ---
    // mappedBy 指向 Companion 实体中拥有外键的字段 (即 Companion 中的 visitor)
    // CascadeType.ALL 表示对 Visitor 的操作（如保存、删除）会级联到 Companion
    // orphanRemoval = true 表示如果从 companions 列表中移除一个 Companion，它也会从数据库中删除
    @OneToMany(mappedBy = "visitor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Companion> companions; // 同行人列表

    // --- 时间戳 ---
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        if (this.status == null) { // 确保在保存新预约时默认状态为 PENDING
            this.status = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}
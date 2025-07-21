package com.example.demo.module;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore; // 避免循环引用

import java.time.LocalDateTime;

@Entity
@Table(name = "companion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Companion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- 同行人信息 ---
    @Column(nullable = false)
    private String name;

    private String phone;

    @Column(name = "id_card")
    private String idCard;

    // --- 与 Visitor 的多对一关联 ---
    @ManyToOne(fetch = FetchType.LAZY) // 多个 Companion 对应一个 Visitor
    @JoinColumn(name = "visitor_id", nullable = false) // 外键列名
    @JsonIgnore // 避免在序列化时 Visitor 和 Companion 之间产生无限循环
    private Visitor visitor;

    // --- 时间戳 ---
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}
package com.example.demo.module;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "visitor_account") // 数据库表名
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisitorAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username; // 可以是手机号或自定义用户名

    @Column(nullable = false)
    private String password; // 生产环境请务必加密存储密码！

    // 可以添加其他访客信息，例如与 Visitor 实体的关联
    // @OneToOne(mappedBy = "visitorAccount", cascade = CascadeType.ALL, fetch =
    // FetchType.LAZY)
    // private Visitor visitorProfile; // 如果一个VisitorAccount对应一个Visitor个人信息
}
package com.example.demo.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

import java.util.concurrent.atomic.AtomicInteger; // 用于生成简单递增ID

@Controller
public class RegistrationController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 简单模拟ID生成器。在实际生产环境应使用更健壮的方案 (如数据库序列、UUID等)
    // 注意：AtomicInteger 只能保证内存中的线程安全，重启应用后会重置。
    // 如果您希望ID在数据库中持久化且唯一，且不使用数据库的自增，您可能需要手动查询最大ID+1。
    // 但这通常需要更复杂的数据库交互，超出了当前“不改变功能”的范畴。
    // 如果是开发测试，可以先用这个。在生产环境，强烈建议使用数据库的序列或UUID。
    private static final AtomicInteger idCounter = new AtomicInteger(0);

    public RegistrationController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    @ResponseBody
    public String registerUser(@RequestParam String username,
            @RequestParam String password) {
        User existingUser = userRepository.findByUsername(username);
        if (existingUser != null) {
            return "注册失败：用户名 '" + username + "' 已存在！";
        }

        User newUser = new User();
        // 手动设置ID。为了确保不重复，这里使用 AtomicInteger。
        // **警告：这种方式只适用于内存模拟，如果应用重启，ID可能重复。
        // 生产环境请考虑使用数据库的sequence/UUID或查询当前最大ID+1。**
        newUser.setId(idCounter.incrementAndGet()); // <--- 在这里手动为id赋值
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setFailedAttempt(0);
        newUser.setLockTime(null);
        userRepository.save(newUser);

        return "注册成功！欢迎新用户 " + username + "！";
    }

    @GetMapping("/success")
    @ResponseBody
    public String loginSuccess() {
        return "<h1>登录成功！欢迎来到主页！</h1><p><a href=\"/logout\">点击这里登出</a></p>";
    }
}
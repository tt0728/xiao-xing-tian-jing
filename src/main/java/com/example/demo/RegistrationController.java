package com.example.demo;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RegistrationController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password)); // 密码哈希加密
        userRepository.save(newUser);

        return "注册成功！欢迎新用户 " + username + "！";
    }

    @GetMapping("/success")
    @ResponseBody
    public String loginSuccess() {
        return "<h1>登录成功！欢迎来到主页！</h1><p><a href=\"/logout\">点击这里登出</a></p>";
    }
}
package com.example.demo.service;

import com.example.demo.module.VisitorAccount;
import com.example.demo.repository.VisitorAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession; // 导入 HttpSession

@Service
public class VisitorAccountService {

    private final VisitorAccountRepository visitorAccountRepository;

    @Autowired
    public VisitorAccountService(VisitorAccountRepository visitorAccountRepository) {
        this.visitorAccountRepository = visitorAccountRepository;
    }

    /**
     * 注册新的访客账号
     * 
     * @param username 用户名
     * @param password 密码
     * @return 注册成功返回 VisitorAccount，否则返回 null（例如用户名已存在）
     */
    public VisitorAccount register(String username, String password) {
        if (visitorAccountRepository.findByUsername(username).isPresent()) {
            return null; // 用户名已存在
        }
        VisitorAccount newAccount = new VisitorAccount();
        newAccount.setUsername(username);
        newAccount.setPassword(password); // 生产环境请加密密码！
        return visitorAccountRepository.save(newAccount);
    }

    /**
     * 认证访客登录
     * 
     * @param username 用户名
     * @param password 密码
     * @param session  HTTP Session
     * @return 登录成功返回 VisitorAccount，否则返回 null
     */
    public VisitorAccount authenticate(String username, String password, HttpSession session) {
        return visitorAccountRepository.findByUsername(username)
                .filter(account -> account.getPassword().equals(password)) // 生产环境请加密比较
                .map(account -> {
                    // 登录成功，将用户ID存储到Session中
                    session.setAttribute("visitorAccountId", account.getId());
                    session.setAttribute("visitorUsername", account.getUsername());
                    System.out.println(
                            "Visitor logged in: " + account.getUsername() + ", Session ID: " + session.getId());
                    return account;
                })
                .orElse(null);
    }

    /**
     * 从 Session 获取当前登录的访客账号ID
     * 
     * @param session HTTP Session
     * @return 访客账号ID，如果未登录则返回 null
     */
    public Long getCurrentVisitorAccountId(HttpSession session) {
        return (Long) session.getAttribute("visitorAccountId");
    }

    /**
     * 登出访客账号
     * 
     * @param session HTTP Session
     */
    public void logout(HttpSession session) {
        session.invalidate(); // 使当前会话失效
    }
}
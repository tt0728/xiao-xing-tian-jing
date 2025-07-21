package com.example.demo.service;

import com.example.demo.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class AdminService {
    private final AdminRepository adminRepository;

    @Autowired
    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public boolean authenticate(String username, String password) {
        String hashedPassword = DigestUtils.md5DigestAsHex(password.getBytes());
        return adminRepository.findByUsername(username)
                .map(admin -> admin.getPassword().equals(hashedPassword))
                .orElse(false);
    }
}
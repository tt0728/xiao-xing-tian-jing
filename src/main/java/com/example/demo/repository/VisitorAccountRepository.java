package com.example.demo.repository;

import com.example.demo.module.VisitorAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VisitorAccountRepository extends JpaRepository<VisitorAccount, Long> {
    Optional<VisitorAccount> findByUsername(String username);
}
package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.LockedException;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.core.Authentication;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final UserRepository userRepository;

    public WebSecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepository.findByUsername(username);

            if (user == null) {
                throw new UsernameNotFoundException("用户未找到: " + username);
            }

            if (user.getLockTime() != null) {
                if (LocalDateTime.now().isBefore(user.getLockTime())) {
                    throw new LockedException("账户已锁定，请稍后再试。");
                } else {
                    user.setFailedAttempt(0);
                    user.setLockTime(null);
                    userRepository.save(user);
                }
            }

            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    Collections.emptyList());
        };
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationFailureHandler customAuthenticationFailureHandler() {
        return (request, response, exception) -> {
            String username = request.getParameter("username");
            User user = userRepository.findByUsername(username);

            if (user != null) {
                int newAttempts = user.getFailedAttempt() + 1;
                user.setFailedAttempt(newAttempts);

                if (newAttempts >= 3) {
                    user.setLockTime(LocalDateTime.now().plus(10, ChronoUnit.MINUTES));
                    userRepository.save(user);
                    response.sendRedirect("/login?locked");
                } else {
                    userRepository.save(user);
                    response.sendRedirect("/login?error");
                }
            } else {
                response.sendRedirect("/login?error");
            }
        };
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);

            if (user != null) {
                user.setFailedAttempt(0);
                user.setLockTime(null);
                userRepository.save(user);
            }
            response.sendRedirect("/departments");
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(authorize -> authorize
                        // 确保 /login 路径是公开可访问的，这是解决重定向循环的关键
                        .requestMatchers(
                                "/login**",
                                "/register",
                                "/register.html",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/error",
                                "/departmentList", // 允许转发到 departmentList 视图名
                                "/userList" // 如果有 userList 视图，也一并添加
                        )
                        .permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/departments", true)
                        .failureHandler(customAuthenticationFailureHandler())
                        .successHandler(customAuthenticationSuccessHandler())
                        .permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("/departments", true)
                        .failureUrl("/login?oauth2error"))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
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
            response.sendRedirect("/success");
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(authorize -> authorize
                        // 确保 /login 路径是公开可访问的，这是解决重定向循环的关键
                        .requestMatchers("/login**", "/register", "/register.html", "/css/**", "/js/**", "/images/**",
                                "/error")
                        .permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login") // 指定登录页面的URL
                        .loginProcessingUrl("/login") // 指定处理登录表单提交的URL，与login.html的form action一致
                        .defaultSuccessUrl("/success", true)
                        .failureHandler(customAuthenticationFailureHandler())
                        .successHandler(customAuthenticationSuccessHandler())
                        .permitAll() // 确保登录相关的URL在formLogin中也被允许访问
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login") // 自定义登录页
                        .defaultSuccessUrl("/success", true) // 登录成功跳转
                        .failureUrl("/login?oauth2error") // 登录失败跳转
                // 可以扩展 successHandler 和 failureHandler，自定义登录逻辑
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login") // 如果需要，也可以指定 OAuth2 的登录页面
                        .defaultSuccessUrl("/success", true) // OAuth2 登录成功后的默认跳转
                        .failureUrl("/login?oauth2error") // OAuth2 登录失败后的跳转
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
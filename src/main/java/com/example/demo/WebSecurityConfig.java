package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final UserRepository userRepository;

    // 注入 UserRepository
    public WebSecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 定义密码编码器 Bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 配置 UserDetailsService，从数据库加载用户信息
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepository.findByUsername(username); // 通过用户名查找用户
            if (user == null) {
                throw new UsernameNotFoundException("用户未找到: " + username);
            }
            // 返回 Spring Security 的 UserDetails 对象
            // user.getPassword() 返回的是哈希后的密码，BCryptPasswordEncoder 会自动处理验证
            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    Collections.emptyList() // 暂时不处理权限/角色，留空列表
            );
        };
    }

    // 配置安全过滤链
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // 允许所有人访问登录、注册页面及相关 POST 接口，以及静态资源
                        .requestMatchers("/login", "/register", "/login.html", "/register.html", "/css/**", "/js/**",
                                "/images/**")
                        .permitAll() // 确保 /static 下的资源也能访问
                        .anyRequest().authenticated() // 所有其他请求都需要认证
                )
                .formLogin(form -> form
                        .loginPage("/login.html") // 指定自定义登录页面 URL
                        .loginProcessingUrl("/login") // 处理登录表单提交的 URL
                        .defaultSuccessUrl("/success", true) // 登录成功后的默认跳转页面
                        .failureUrl("/login.html?error") // 登录失败后跳转回登录页面并带上 error 参数
                        .permitAll() // 允许所有人访问登录相关的URL
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // 指定登出 URL (默认为 /logout)
                        .logoutSuccessUrl("/login.html?logout") // 登出成功后跳转到登录页面并带上 logout 参数
                        .permitAll() // 允许所有人访问登出URL
                )
                .csrf(csrf -> csrf.disable()); // 生产环境请启用并正确配置CSRF保护

        return http.build();
    }
}
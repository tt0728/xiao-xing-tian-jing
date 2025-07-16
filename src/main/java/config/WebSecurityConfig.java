package config;

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

import model.User;
import repository.UserRepository;

import org.springframework.security.authentication.LockedException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import java.util.Collections;

@Configuration
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

            // 检查账户是否被锁定
            if (user.getLockTime() != null) {
                if (LocalDateTime.now().isBefore(user.getLockTime())) {
                    throw new LockedException("账户已锁定，请 10 分钟后再试。");
                } else {
                    // 锁定时间已过，重置失败次数和锁定时间
                    user.setFailedAttempt(0);
                    user.setLockTime(null);
                    userRepository.save(user); // 确保保存更改
                }
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

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        // 可以移除这两行，或设置为 null。默认情况下，preAuthenticationChecks 和 postAuthenticationChecks
        // 是自动处理的。
        // authProvider.setPreAuthenticationChecks(null);
        // authProvider.setPostAuthenticationChecks(null);
        return authProvider;
    }

    // 登录失败处理
    private void handleLoginFailure(User user) {
        int newAttempts = user.getFailedAttempt() + 1;
        user.setFailedAttempt(newAttempts);
        if (newAttempts >= 3) {
            user.setLockTime(LocalDateTime.now().plus(10, ChronoUnit.MINUTES));
        }
        userRepository.save(user);
    }

    // 登录成功处理
    private void handleLoginSuccess(User user) {
        user.setFailedAttempt(0);
        user.setLockTime(null);
        userRepository.save(user);
    }

    // 配置安全过滤链
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(authorize -> authorize
                        // 允许所有人访问登录、注册页面及相关 POST 接口，以及静态资源
                        // 这里将 /login 明确地设置为任何人都可以访问，因为它是您自定义登录页面的入口。
                        .requestMatchers("/login", "/register", "/register.html", "/css/**", "/js/**", "/images/**")
                        .permitAll()
                        .anyRequest().authenticated() // 所有其他请求都需要认证
                )
                .formLogin(form -> form
                        .loginPage("/login") // 关键修改：将这里改为 "/login"
                        .loginProcessingUrl("/login") // 处理登录表单提交的 URL (保持不变，与loginPage一致)
                        .defaultSuccessUrl("/success", true) // 登录成功后的默认跳转页面
                        .failureHandler((request, response, exception) -> {
                            String username = request.getParameter("username");
                            User user = userRepository.findByUsername(username);
                            if (user != null) {
                                handleLoginFailure(user);
                                if (user.getFailedAttempt() >= 3) {
                                    response.sendRedirect("/login?locked"); // 注意这里也改为 /login
                                } else {
                                    response.sendRedirect("/login?error"); // 注意这里也改为 /login
                                }
                            } else {
                                response.sendRedirect("/login?error"); // 注意这里也改为 /login
                            }
                        })
                        .successHandler((request, response, authentication) -> {
                            String username = authentication.getName();
                            User user = userRepository.findByUsername(username);
                            handleLoginSuccess(user);
                            response.sendRedirect("/success");
                        })
                        .permitAll() // 允许所有人访问登录相关的URL
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // 指定登出 URL (默认为 /logout)
                        .logoutSuccessUrl("/login?logout") // 关键修改：登出成功后跳转到 /login
                        .permitAll() // 允许所有人访问登出URL
                )
                .csrf(csrf -> csrf.disable()); // 生产环境请启用并正确配置CSRF保护

        return http.build();
    }
}
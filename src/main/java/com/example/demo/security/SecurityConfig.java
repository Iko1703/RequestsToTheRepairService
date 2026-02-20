package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/dispatcher/**", "/api/dispatcher/**").hasRole("DISPATCHER")
                        .requestMatchers("/master/**", "/api/master/**").hasRole("MASTER")
                        .requestMatchers("/api/tickets/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                // Для упрощения разработки и тестового задания отключаем CSRF.
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /**
     * Для тестового задания используем in-memory пользователей.
     * Позже можно заменить на UserRepository + UserDetailsService.
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails dispatcher = User.withUsername("dispatcher")
                .password(passwordEncoder.encode("password"))
                .roles("DISPATCHER")
                .build();

        UserDetails master1 = User.withUsername("master1")
                .password(passwordEncoder.encode("password"))
                .roles("MASTER")
                .build();

        UserDetails master2 = User.withUsername("master2")
                .password(passwordEncoder.encode("password"))
                .roles("MASTER")
                .build();

        return new InMemoryUserDetailsManager(dispatcher, master1, master2);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}





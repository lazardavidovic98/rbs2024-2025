package com.zuehlke.securesoftwaredevelopment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // DatabaseAuthenticationProvider is the only AuthenticationProvider bean, so Spring Boot wires it
    // into the global AuthenticationManager. Registering it here as well would run it twice per login.

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // The token is mirrored into a JS-readable cookie so fetch() calls can send it back as a header
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .authorizeRequests(auth -> auth
                        .antMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform-login")
                        .defaultSuccessUrl("/books", true)
                        .failureHandler((request, response, exception) -> response.sendRedirect(
                                request.getContextPath() + (exception instanceof LockedException ? "/login?locked" : "/login?error"))))
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"))
                // New session id after login prevents session fixation
                .sessionManagement(session -> session.sessionFixation().changeSessionId())
                .headers(headers -> headers.frameOptions().deny());
        return http.build();
    }
}

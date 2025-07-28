package com.ddi.newsapi.config;

import com.ddi.newsapi.handler.OAuth2LoginSuccessHandler;
import com.ddi.newsapi.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF는 기본적으로 활성화하는 것이 좋습니다. Spring Security가 form과 OAuth2에서 알아서 처리해 줍니다.
                // .csrf(csrf -> csrf.disable()) // 특별한 이유가 없다면 비활성화하지 마세요.

                .authorizeHttpRequests(authorize -> authorize
                        // 로그인, 회원가입 관련 경로는 누구나 접근 가능
                        .requestMatchers("/", "/login", "/signup", "/signup-proc", "/css/**", "/js/**", "/images/**", "/login-error","/favicon.ico").permitAll()
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login-proc")
                        .defaultSuccessUrl("/news/home", true)
                        .failureUrl("/login?error=true")
                )
                .oauth2Login(oauth2 -> oauth2
                        // [중요] OAuth2 로그인 시에도 우리의 커스텀 로그인 페이지를 사용하도록 명시
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .defaultSuccessUrl("/news/home", true)
                        // OAuth2 과정에서 실패 시 이동할 URL
                        .failureUrl("/login-error")
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

                // [핵심] CSRF 보호 설정 수정
                http.csrf(csrf -> csrf
                        // 회원가입 처리 URL에 대해서만 CSRF 보호를 비활성화합니다.
                        .ignoringRequestMatchers("/signup-proc")
                );

        return http.build();
    }
    /**
     * [추가] 정적 리소스에 대한 시큐리티 설정을 무시합니다.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // /favicon.ico 요청은 시큐리티 필터를 거치지 않고 통과시킵니다.
        // 다른 정적 리소스(css, js 등)도 함께 추가하면 좋습니다.
        return (web) -> web.ignoring().requestMatchers("/favicon.ico", "/css/**", "/js/**", "/images/**");
    }
}
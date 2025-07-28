package com.ddi.newsapi.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        try{
            // 이 핸들러가 호출되었다는 것은 OAuth2 로그인이 성공했다는 의미입니다.
            log.info("### OAuth2 Login Succeeded! ###");

            // 인증 객체에서 사용자 정보를 가져옵니다.
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            // 사용자 정보를 로그로 출력하여 확인합니다. (디버깅에 매우 중요!)
            log.info("User Attributes: {}", oAuth2User.getAttributes());

            // 여기서 DB에 사용자 정보를 저장하거나 업데이트하는 로직을 추가할 수 있습니다.
            // 예: String email = oAuth2User.getAttribute("email");
            //     User user = userRepository.findByEmail(email).orElse(null);
            //     if (user == null) { ... 새로 저장 ... }

            // 로그인 성공 후 리디렉션할 URL을 지정합니다.
            String targetUrl = "/news/home";
            response.sendRedirect(targetUrl);
        }catch (Exception e){
            log.error(" CustomOAuth2UserService Error... [{}], [{}}", e , e.getMessage());
        }
    }
}
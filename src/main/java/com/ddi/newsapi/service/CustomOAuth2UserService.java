package com.ddi.newsapi.service;

import com.ddi.newsapi.repository.UserMapper; // Mapper를 import
import com.ddi.newsapi.model.User;
import com.ddi.newsapi.config.auth.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * 구글 OAuth2 로그인을 성공적으로 마친 후, Spring Security에 의해 자동으로 호출되는 핵심 메소드
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    //이건 redis

    private final UserMapper userMapper;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 부모 클래스의 loadUser를 호출하여 공급자로부터 사용자 정보를 가져옴
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("OAuth2 User Attributes: {}", oAuth2User.getAttributes());

        // 2. 사용자 정보 추출
        String provider = userRequest.getClientRegistration().getRegistrationId(); // "google"
        String providerId = oAuth2User.getAttribute("sub"); // 구글의 고유 식별자
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String username = provider + "_" + providerId; // 고유한 사용자 이름 생성 (예: "google_12345")

        // 3. DB에서 사용자 조회
        Optional<User> userOptional = userMapper.findByProviderAndProviderId(provider, providerId);
        User user;

        if (userOptional.isPresent()) {
            // 4-1. 이미 가입된 사용자: 정보 업데이트
            user = userOptional.get();
            log.info("기존 사용자입니다: {}", user.getUsername());
            // 이름이나 이메일이 변경되었을 수 있으므로 업데이트
            user.setName(name);
            user.setEmail(email);
            userMapper.update(user); // Mapper의 update 메소드 호출
        } else {
            // 4-2. 처음 로그인하는 사용자: 새로 생성하여 DB에 저장
            log.info("신규 사용자입니다. DB에 저장합니다.");
            user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setName(name);
            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setRole("ROLE_USER"); // 기본 권한 부여
            // 소셜 로그인 사용자는 우리 시스템의 비밀번호가 없으므로 password 필드는 null
            userMapper.save(user); // Mapper의 save 메소드 호출
        }

        // 5. Spring Security 세션에 저장될 PrincipalDetails 객체 반환
        return new PrincipalDetails(user, oAuth2User.getAttributes());
    }

    private User saveOrUpdate(Map<String, Object> attributes, String provider) {
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String providerId = (String) attributes.get("sub");

        Optional<User> userOptional = userMapper.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            // 이미 가입된 사용자
            user = userOptional.get();
            user.setName(name); // 이름 업데이트
            userMapper.update(user); // Mapper의 update 메소드 호출
        } else {
            // 새로 가입하는 사용자
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setUsername(provider + "_" + providerId); // 고유한 username 생성
            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setRole("ROLE_USER");
            userMapper.save(user); // Mapper의 save 메소드 호출
        }
        return user;
    }
}
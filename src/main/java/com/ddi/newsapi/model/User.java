package com.ddi.newsapi.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class User {

    private Long id;
    private String username;
    private String password; // 일반 회원가입 시 사용, 소셜 로그인은 null
    private String email;
    private String name; // 사용자 이름 (예: "홍길동")
    private String role; // 사용자 권한 (예: "ROLE_USER")

    // OAuth2 제공자 정보
    private String provider; // 예: "google"
    private String providerId; // 예: 구글이 부여한 고유 ID
}
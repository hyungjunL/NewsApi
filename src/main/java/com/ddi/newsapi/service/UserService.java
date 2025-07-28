package com.ddi.newsapi.service;

import com.ddi.newsapi.dto.SignUpRequestDto; // 회원가입 요청 DTO
import com.ddi.newsapi.repository.UserMapper;   // MyBatis UserMapper
import com.ddi.newsapi.model.User;         // User 모델 (DTO/VO)
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder; // 비밀번호 암호화
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 트랜잭션 처리

@Slf4j // Lombok을 이용한 로거
@Service // Spring 서비스 빈으로 등록
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성 (의존성 주입)
public class UserService {

    private final UserMapper userMapper; // UserMapper 주입
    private final PasswordEncoder passwordEncoder; // PasswordEncoder 주입

    /**
     * 새로운 사용자를 등록하는 메소드입니다.
     *
     * @param requestDto 회원가입 요청 데이터 (아이디, 이메일, 비밀번호)
     * @throws IllegalArgumentException 아이디 또는 이메일이 이미 사용 중일 경우 발생
     */
    @Transactional // 이 메소드 내의 DB 작업은 하나의 트랜잭션으로 묶입니다.
    public void registerUser(SignUpRequestDto requestDto) throws IllegalArgumentException{
        log.info("Attempting to register user: {}", requestDto.getUsername());

        // 1. 아이디 중복 확인
        if (userMapper.findByUsername(requestDto.getUsername()).isPresent()) {
            log.warn("Registration failed: Username '{}' already exists.", requestDto.getUsername());
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 2. 이메일 중복 확인 (선택 사항이지만, 중복 이메일을 허용하지 않는다면 필요)
        if (requestDto.getEmail() != null && userMapper.findByEmail(requestDto.getEmail()).isPresent()) {
            log.warn("Registration failed: Email '{}' already exists.", requestDto.getEmail());
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 3. User 모델 객체 생성 및 데이터 설정
        User user = new User();
        user.setUsername(requestDto.getUsername());
        user.setEmail(requestDto.getEmail());
        // 비밀번호는 반드시 암호화하여 저장합니다.
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        user.setName(requestDto.getUsername()); // 기본적으로 아이디를 이름으로 설정하거나, 별도 필드 추가
        user.setRole("ROLE_USER"); // 기본 권한 설정 (예: 일반 사용자)
        user.setProvider(null); // 일반 로그인 사용자는 provider 없음
        user.setProviderId(null); // 일반 로그인 사용자는 providerId 없음

        // 4. UserMapper를 통해 DB에 저장
        userMapper.save(user);
        log.info("User '{}' registered successfully with ID: {}", user.getUsername(), user.getId());
    }
}
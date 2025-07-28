package com.ddi.newsapi.service;

import com.ddi.newsapi.repository.UserMapper;
import com.ddi.newsapi.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    /**
     * Spring Security가 /login-proc 요청을 받으면 이 메소드를 호출합니다.
     * @param username 사용자가 로그인 폼에 입력한 아이디
     * @return DB에서 찾은 사용자 정보 (아이디, 암호화된 비밀번호, 권한)
     * @throws UsernameNotFoundException 사용자를 찾지 못했을 때 발생
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Mapper를 통해 DB에서 사용자 정보를 조회합니다.
        User user = userMapper.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        // 2. Spring Security가 이해할 수 있는 UserDetails 객체로 변환하여 반환합니다.
        //    이 객체에 담긴 암호화된 비밀번호를 Spring Security가 알아서 비교해줍니다.
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority(user.getRole()))
        );
    }
}
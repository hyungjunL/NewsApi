package com.ddi.newsapi.repository;

import com.ddi.newsapi.model.User;
import org.apache.ibatis.annotations.Mapper;
import java.util.Optional;

@Mapper // Spring이 MyBatis Mapper로 인식하도록 어노테이션을 붙입니다.
public interface UserMapper {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    void save(User user);
    void update(User user);

    // [추가] OAuth 제공자와 제공자 ID로 사용자를 찾는 메소드
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
}
package com.ddi.newsapi.repository;

import com.ddi.newsapi.model.UserKeyword;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserKeywordMapper {
    // 특정 사용자의 모든 키워드 조회
    List<UserKeyword> findByUserUsername(String userUsername);

    // 키워드 추가
    void insert(UserKeyword userKeyword);

    // 특정 사용자의 모든 키워드 삭제 (업데이트 전 기존 데이터 삭제용)
    void deleteByUserUsername(String userUsername);

    // 특정 사용자의 특정 키워드 삭제 (선택적)
    void deleteByUserUsernameAndKeyword(@Param("userUsername") String userUsername, @Param("keyword") String keyword);
}
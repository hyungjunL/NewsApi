package com.ddi.newsapi.repository;

import com.ddi.newsapi.model.UserNewsSite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserNewsSiteMapper {
    // 특정 사용자의 모든 뉴스 사이트 조회
    List<UserNewsSite> findByUserUsername(String userUsername);

    // 뉴스 사이트 추가
    void insert(UserNewsSite userNewsSite);

    // 특정 사용자의 모든 뉴스 사이트 삭제 (업데이트 전 기존 데이터 삭제용)
    void deleteByUserUsername(String userUsername);

    // 특정 사용자의 특정 사이트 삭제 (선택적)
    void deleteByUserUsernameAndSiteUrl(@Param("userUsername") String userUsername, @Param("siteUrl") String siteUrl);
}
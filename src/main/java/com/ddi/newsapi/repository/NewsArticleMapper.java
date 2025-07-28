package com.ddi.newsapi.repository;

import com.ddi.newsapi.model.NewsArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface NewsArticleMapper {

    /**
     * [수정] 페이징 파라미터(limit, offset)를 추가합니다.
     */
    List<NewsArticle> findArticlesBySettings(
            @Param("sites") List<String> sites,
            @Param("keywords") List<String> keywords,
            @Param("limit") int limit,
            @Param("offset") long offset
    );

    /**
     * [신규] 필터 조건에 맞는 전체 기사 수를 조회합니다.
     */
    long countArticlesBySettings(
            @Param("sites") List<String> sites,
            @Param("keywords") List<String> keywords
    );
}
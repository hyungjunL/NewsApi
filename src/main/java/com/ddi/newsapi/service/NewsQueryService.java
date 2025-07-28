package com.ddi.newsapi.service;

import com.ddi.newsapi.dto.NewsSettingsRequestDto;
import com.ddi.newsapi.repository.NewsArticleMapper; // 경로 확인 필요
import com.ddi.newsapi.model.NewsArticle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NewsQueryService {

    private final NewsArticleMapper newsArticleMapper;

    /**
     * [수정] 페이징 파라미터를 받아 DB에서 뉴스를 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<NewsArticle> findNewsBySettings(NewsSettingsRequestDto settings, int limit, long offset) {
        log.info("Querying news from DB with settings. Sites: {}, Keywords: {}, Limit: {}, Offset: {}",
                settings.getSites(), settings.getKeywords(), limit, offset);

        return newsArticleMapper.findArticlesBySettings(
                settings.getSites(),
                settings.getKeywords(),
                limit,
                offset
        );
    }

    /**
     * [신규] 필터 조건에 맞는 전체 기사 수를 조회합니다.
     */
    @Transactional(readOnly = true)
    public long countNewsBySettings(NewsSettingsRequestDto settings) {
        return newsArticleMapper.countArticlesBySettings(
                settings.getSites(),
                settings.getKeywords()
        );
    }
}
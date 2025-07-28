package com.ddi.newsapi.service;

import com.ddi.newsapi.dto.NewsSettingsRequestDto;
import com.ddi.newsapi.repository.UserKeywordMapper; // 새 매퍼 import
import com.ddi.newsapi.repository.UserNewsSiteMapper; // 새 매퍼 import
import com.ddi.newsapi.model.UserKeyword; // 새 모델 import
import com.ddi.newsapi.model.UserNewsSite; // 새 모델 import
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsSettingService {

    private final UserNewsSiteMapper userNewsSiteMapper; // 주입
    private final UserKeywordMapper userKeywordMapper;   // 주입

    /**
     * 사용자별 뉴스 설정을 저장하거나 업데이트합니다.
     * 기존 설정을 모두 삭제하고 새로운 설정으로 대체합니다.
     * @param username 설정할 사용자의 아이디
     * @param requestDto 클라이언트로부터 받은 뉴스 설정 데이터
     */
    @Transactional
    public void saveOrUpdateNewsSettings(String username, NewsSettingsRequestDto requestDto) {
        log.info("Saving/Updating news settings for user: {}", username);

        // 1. 기존 뉴스 사이트 설정 삭제
        userNewsSiteMapper.deleteByUserUsername(username);
        log.debug("Deleted existing news sites for user: {}", username);

        // 2. 새로운 뉴스 사이트 설정 삽입
        if (requestDto.getSites() != null && !requestDto.getSites().isEmpty()) {
            for (String siteUrl : requestDto.getSites()) {
                UserNewsSite newsSite = new UserNewsSite(null, username, siteUrl);
                userNewsSiteMapper.insert(newsSite);
            }
            log.debug("Inserted {} new news sites for user: {}", requestDto.getSites().size(), username);
        }

        // 3. 기존 키워드 설정 삭제
        userKeywordMapper.deleteByUserUsername(username);
        log.debug("Deleted existing keywords for user: {}", username);

        // 4. 새로운 키워드 설정 삽입
        if (requestDto.getKeywords() != null && !requestDto.getKeywords().isEmpty()) {
            for (String keyword : requestDto.getKeywords()) {
                UserKeyword userKeyword = new UserKeyword(null, username, keyword);
                userKeywordMapper.insert(userKeyword);
            }
            log.debug("Inserted {} new keywords for user: {}", requestDto.getKeywords().size(), username);
        }
        log.info("News settings saved/updated successfully for user: {}", username);
    }

    /**
     * 사용자별 뉴스 설정을 조회합니다.
     * @param username 조회할 사용자의 아이디
     * @return 뉴스 설정 DTO (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<NewsSettingsRequestDto> getNewsSettings(String username) {
        log.info("Retrieving news settings for user: {}", username);

        // 1. 뉴스 사이트 조회
        List<UserNewsSite> sites = userNewsSiteMapper.findByUserUsername(username);
        List<String> siteUrls = sites.stream()
                .map(UserNewsSite::getSiteUrl)
                .collect(Collectors.toList());

        // 2. 키워드 조회
        List<UserKeyword> keywords = userKeywordMapper.findByUserUsername(username);
        List<String> keywordStrings = keywords.stream()
                .map(UserKeyword::getKeyword)
                .collect(Collectors.toList());

        // 3. DTO로 조합하여 반환
        if (siteUrls.isEmpty() && keywordStrings.isEmpty()) {
            log.info("No news settings found for user: {}", username);
            return Optional.empty(); // 설정이 전혀 없으면 Optional.empty() 반환
        } else {
            NewsSettingsRequestDto dto = new NewsSettingsRequestDto(siteUrls, keywordStrings);
            log.info("Found news settings for user: {}", username);
            return Optional.of(dto);
        }
    }
}
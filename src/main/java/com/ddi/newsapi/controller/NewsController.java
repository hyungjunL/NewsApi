package com.ddi.newsapi.controller;

import com.ddi.newsapi.dto.NewsSettingsRequestDto;
import com.ddi.newsapi.model.NewsArticle;
import com.ddi.newsapi.service.NewsQueryService;
import com.ddi.newsapi.service.NewsSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller; // [수정] @Controller로 변경
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/news") // 기본 경로 유지
public class NewsController {

    private final NewsSettingService newsSettingService;
    private final NewsQueryService newsQueryService;
    private final WebClient webClient; // [추가] WebClient 주입

    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<?> getNewsList(
            @AuthenticationPrincipal UserDetails userDetails,
            // [수정] 클라이언트가 페이지와 사이즈를 요청할 수 있도록 파라미터 추가
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증된 사용자 정보가 없습니다.");
        }

        String username = userDetails.getUsername();
        Optional<NewsSettingsRequestDto> settingsOptional = newsSettingService.getNewsSettings(username);

        if (settingsOptional.isPresent()) {
            NewsSettingsRequestDto settings = settingsOptional.get();
            if (settings.getSites().isEmpty() && settings.getKeywords().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("뉴스 설정을 찾을 수 없습니다. 사이트나 키워드를 하나 이상 등록해주세요.");
            }

            // --- [수정] 페이징 로직 추가 ---
            long offset = (long) (page - 1) * size;

            // 1. 올바른 파라미터로 서비스 메소드 호출
            List<NewsArticle> articles = newsQueryService.findNewsBySettings(settings, size, offset);

            // 2. 전체 개수와 페이지 수 계산
            long totalArticles = newsQueryService.countNewsBySettings(settings);
            long totalPages = (long) Math.ceil((double) totalArticles / size);

            // 3. API 응답을 위한 구조화된 데이터 생성
            Map<String, Object> response = new HashMap<>();
            response.put("articles", articles);
            response.put("currentPage", page);
            response.put("totalItems", totalArticles);
            response.put("totalPages", totalPages);

            return ResponseEntity.ok(response);

        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("뉴스 설정을 찾을 수 없습니다. 먼저 설정을 등록해주세요.");
        }
    }

    @PostMapping("/settings")
    @ResponseBody
    public ResponseEntity<String> saveNewsSettings(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody NewsSettingsRequestDto requestDto) {
        // ... 기존 saveNewsSettings 로직 ...
        String username = getUsername(userDetails, null);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증된 사용자 정보가 없습니다.");
        }
        newsSettingService.saveOrUpdateNewsSettings(username, requestDto);
        return ResponseEntity.ok("뉴스 설정이 성공적으로 저장되었습니다.");
    }

    @GetMapping("/settings")
    @ResponseBody
    public ResponseEntity<?> getNewsSettings(@AuthenticationPrincipal UserDetails userDetails) {
        // ... 기존 getNewsSettings 로직 ...
        String username = getUsername(userDetails, null);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증된 사용자 정보가 없습니다.");
        }
        Optional<NewsSettingsRequestDto> settingsOptional = newsSettingService.getNewsSettings(username);
        if (settingsOptional.isPresent()) {
            return ResponseEntity.ok(settingsOptional.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 사용자의 뉴스 설정을 찾을 수 없습니다.");
        }
    }

    // --- [신규] HTML 폼을 위한 API ---

    /**
     * 뉴스 설정을 위한 HTML 폼 페이지를 보여줍니다.
     * GET /api/news/settings-form
     */
    @GetMapping("/settings-form")
    public String showSettingsForm(@AuthenticationPrincipal UserDetails userDetails,
                                   @AuthenticationPrincipal OAuth2User oauth2User,
                                   Model model) {
        String username = getUsername(userDetails, oauth2User);
        if (username == null) {
            return "redirect:/login";
        }

        // 기존 설정을 조회하여 모델에 추가 (폼에 기본값으로 채워넣기 위함)
        Optional<NewsSettingsRequestDto> settingsOptional = newsSettingService.getNewsSettings(username);
        model.addAttribute("currentSettings", settingsOptional.orElse(new NewsSettingsRequestDto(Collections.emptyList(), Collections.emptyList())));

        return "news_settings"; // "news_settings.html" 템플릿을 렌더링
    }

    /**
     * HTML 폼에서 제출된 뉴스 설정을 저장합니다.
     * POST /api/news/settings-form
     */
    @PostMapping("/settings-form")
    public String processSettingsForm(@AuthenticationPrincipal UserDetails userDetails,
                                      @AuthenticationPrincipal OAuth2User oauth2User,
                                      @RequestParam(value = "sites", required = false) List<String> sites,
                                      // [수정] 여러 개의 input 필드 값을 List<String>으로 직접 받습니다.
                                      @RequestParam(value = "keywords", required = false) List<String> keywords) {
        String username = getUsername(userDetails, oauth2User);
        if (username == null) {
            return "redirect:/login";
        }

        // [수정] 파라미터가 null일 경우 (아무것도 체크/입력하지 않았을 때) 빈 리스트로 처리합니다.
        List<String> siteList = (sites != null) ? sites : Collections.emptyList();
        List<String> keywordList = (keywords != null) ? keywords.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList())
                : Collections.emptyList();

        // DTO를 생성하여 서비스 호출
        NewsSettingsRequestDto settingsDto = new NewsSettingsRequestDto(siteList, keywordList);
        newsSettingService.saveOrUpdateNewsSettings(username, settingsDto);

        // --- newBridge 프로젝트의 /keywords/register API 호출 ---
        /**
         * WebClient를 사용한 직접적인 HTTP 호출(동기 방식)은 두 서비스 간의 **강한 결합(Tight Coupling)**을 만듭
         * 추후 MessageBroker를 활용하여 서비스 확장성 고려 필요
         */
        Set<String> keywordsForBridge = new java.util.HashSet<>(keywordList);

        try {
            String newBridgeApiUrl = "http://localhost:8080/api/bridge/keywords/register";

            String responseBody = webClient.post()
                    .uri(newBridgeApiUrl)
                    .bodyValue(keywordsForBridge)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("newBridge /keywords/register API 호출 성공. 응답: {}", responseBody);
        } catch (Exception e) {
            log.error("newBridge /keywords/register API 호출 실패: {}", e.getMessage(), e);
        }
        // --- API 호출 끝 ---

        return "redirect:/news/home";
    }

    /**
     * 인증된 사용자의 이름을 가져오는 헬퍼 메소드
     */
    private String getUsername(UserDetails userDetails, OAuth2User oauth2User) {
        if (userDetails != null) {
            return userDetails.getUsername();
        } else if (oauth2User != null) {
            // CustomOAuth2UserService에서 사용자를 식별하기 위해 사용한 속성을 적어주세요.
            // 보통 email을 많이 사용합니다.
            return oauth2User.getAttribute("email");
        }
        return null;
    }
}
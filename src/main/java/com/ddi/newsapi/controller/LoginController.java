package com.ddi.newsapi.controller;

import com.ddi.newsapi.dto.NewsSettingsRequestDto;
import com.ddi.newsapi.dto.SignUpRequestDto;
import com.ddi.newsapi.model.NewsArticle;
import com.ddi.newsapi.service.NewsQueryService;
import com.ddi.newsapi.service.NewsSettingService;
import com.ddi.newsapi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;
    private final NewsSettingService newsSettingService;
    private final NewsQueryService newsQueryService;

    // ... loginPage, signupPage, processSignUp, loginError 메소드 ...

    //    @GetMapping("/login")
    //    public String loginPage() {
    //        return "login";
    //    }
    //
    //    @GetMapping("/signup")
    //    public String signupPage() {
    //        return "signup";
    //    }
    @GetMapping("/login")
    public String loginPage(Principal principal) {
        // Principal 객체가 존재한다는 것은 사용자가 이미 로그인했다는 의미입니다.
        if (principal != null) {
            // 이미 로그인한 사용자는 뉴스 대시보드로 리다이렉트합니다.
            return "redirect:/news/home";
        }
        // 로그인하지 않은 사용자에게만 로그인 페이지를 보여줍니다.
        return "login";
    }

    @GetMapping("/signup")
    public String signupPage(Principal principal) {
        // 로그인 페이지와 동일한 로직을 적용합니다.
        if (principal != null) {
            return "redirect:/news/home";
        }
        return "signup";
    }

    @PostMapping("/signup-proc")
    @ResponseBody // 이 메소드는 더 이상 HTML 뷰를 반환하지 않고, HTTP 응답 본문(Body)을 직접 작성합니다.
    public ResponseEntity<?> processSignUp(@RequestBody SignUpRequestDto signUpRequestDto) {
        try {
            userService.registerUser(signUpRequestDto);
            // 성공 시: HTTP 200 OK 상태 코드와 함께 성공 메시지를 반환
            return ResponseEntity.ok().body(Map.of("message", "회원가입이 성공적으로 완료되었습니다."));

        } catch (IllegalArgumentException e) {
            // 실패 시: 서비스 로직에서 발생한 예외 메시지를 가져옵니다.
            log.warn("Sign up failed: {}", e.getMessage());
            // HTTP 409 Conflict 상태 코드와 함께 에러 메시지를 반환
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            // 그 외 예상치 못한 에러 처리
            log.error("Unexpected error during sign up", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "서버 내부 오류가 발생했습니다."));
        }
    }

    @GetMapping("/login-error")
    public String loginError() {
        return "redirect:/login";
    }

    @GetMapping("/news/home")
    public String newsHome(@AuthenticationPrincipal UserDetails userDetails,
                           @AuthenticationPrincipal OAuth2User oauth2User,
                           @RequestParam(value = "page", defaultValue = "1") int page,
                           Model model) {

        String username = getUsername(userDetails, oauth2User);
        if (username == null) {
            return "redirect:/login";
        }

        log.info("Loading news dashboard for user: {}, page: {}", username, page);

        Optional<NewsSettingsRequestDto> settingsOptional = newsSettingService.getNewsSettings(username);

        if (settingsOptional.isPresent()) {
            NewsSettingsRequestDto settings = settingsOptional.get();

            // --- 기존 페이징 로직 ---
            int pageSize = 10; // 한 페이지에 보여줄 게시글 수
            long offset = (long) (page - 1) * pageSize;
            List<NewsArticle> articles = newsQueryService.findNewsBySettings(settings, pageSize, offset);
            long totalArticles = newsQueryService.countNewsBySettings(settings);
            long totalPages = (long) Math.ceil((double) totalArticles / pageSize);

            // --- [신규] 페이징 블록 계산 로직 ---
            int pageBlockSize = 10; // 한 블록에 표시할 페이지 번호 개수
            // 현재 페이지가 속한 블록의 시작 페이지를 계산합니다. (예: 1, 11, 21, ...)
            int startPage = ((page - 1) / pageBlockSize) * pageBlockSize + 1;
            // 현재 페이지가 속한 블록의 끝 페이지를 계산합니다. (예: 10, 20, 30, ...)
            int endPage = startPage + pageBlockSize - 1;
            // 끝 페이지가 전체 페이지 수를 넘지 않도록 조정합니다.
            if (endPage > totalPages) {
                endPage = (int) totalPages;
            }
            // --- 페이징 블록 계산 끝 ---

            // 뷰에 데이터 전달
            model.addAttribute("articles", articles);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalArticles", totalArticles);
            // [신규] 계산된 시작/끝 페이지를 모델에 추가
            model.addAttribute("startPage", startPage);
            model.addAttribute("endPage", endPage);

        } else {
            model.addAttribute("articles", Collections.emptyList());
            model.addAttribute("needsSetup", true);
        }

        return "news_dashboard";
    }

    /**
     * [추가된 헬퍼 메소드]
     * 인증된 사용자의 이름을 가져옵니다.
     * 일반 로그인(UserDetails)과 소셜 로그인(OAuth2User)을 모두 처리합니다.
     *
     * @param userDetails  일반 로그인 시 주입되는 객체
     * @param oauth2User   소셜 로그인 시 주입되는 객체
     * @return 사용자 이름(String)
     */
    private String getUsername(UserDetails userDetails, OAuth2User oauth2User) {
        if (userDetails != null) {
            // 일반 폼 로그인의 경우, UserDetails에서 username을 가져옵니다.
            return userDetails.getUsername();
        } else if (oauth2User != null) {
            // OAuth2 소셜 로그인의 경우, attributes에서 고유 식별자를 가져옵니다.
            // Google, Naver, Kakao 등 대부분 'email'을 제공합니다.
            // CustomOAuth2UserService에서 저장한 방식과 일치해야 합니다.
            return oauth2User.getAttribute("email");
        }
        // 로그인 정보가 없는 경우
        return null;
    }
}
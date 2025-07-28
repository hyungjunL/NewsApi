package com.ddi.newsapi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserKeyword {
    private Long id;
    private String username; // users 테이블의 username과 매핑
    private String keyword;      // 검색 키워드
}
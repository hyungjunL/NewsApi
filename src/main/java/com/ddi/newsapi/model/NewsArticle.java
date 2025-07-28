package com.ddi.newsapi.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewsArticle {
    private String id;
    private String title;
    private String link;
    private String summary;
    private String source;
    private String pubDate;
    private String regDate;
}
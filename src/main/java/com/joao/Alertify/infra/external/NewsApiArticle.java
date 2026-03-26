package com.joao.Alertify.infra.external;

import java.util.List;

/**
 * DTO representing a single news article returned by the Massive News API
 * (GET /v2/reference/news).
 */
public record NewsApiArticle(
        String title,
        String description,
        String url,
        String publishedAt,
        String publisher,
        List<String> tickers,
        String sentiment
) {}


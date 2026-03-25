package com.joao.Alertify.infra.external;

/**
 * DTO representing a single news headline returned by the News API.
 */
public record NewsApiArticle(
        String title,
        String description,
        String url,
        String publishedAt,
        NewsApiSource source
) {
    public record NewsApiSource(String id, String name) {}
}


package com.joao.Alertify.infra.external;

import java.util.List;

/**
 * Wrapper for the NewsAPI /everything response envelope.
 */
public record NewsApiResponse(
        String status,
        int totalResults,
        List<NewsApiArticle> articles
) {}


package com.joao.Alertify.infra.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO that maps the raw JSON from GET /v2/reference/news (Massive API).
 *
 * Top-level envelope:
 * {
 *   "count": 1,
 *   "next_url": "...",
 *   "request_id": "...",
 *   "results": [ { ... } ],
 *   "status": "OK"
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MassiveNewsResponse(

        int count,

        @JsonProperty("next_url")
        String nextUrl,

        @JsonProperty("request_id")
        String requestId,

        List<NewsResult> results,

        String status
) {

    /**
     * Each element inside the "results" array.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NewsResult(

            String id,

            String title,

            String description,

            @JsonProperty("article_url")
            String articleUrl,

            @JsonProperty("amp_url")
            String ampUrl,

            @JsonProperty("image_url")
            String imageUrl,

            String author,

            @JsonProperty("published_utc")
            String publishedUtc,

            Publisher publisher,

            List<String> tickers,

            List<String> keywords,

            List<Insight> insights
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Publisher(
            String name,

            @JsonProperty("homepage_url")
            String homepageUrl,

            @JsonProperty("logo_url")
            String logoUrl,

            @JsonProperty("favicon_url")
            String faviconUrl
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Insight(
            String ticker,
            String sentiment,

            @JsonProperty("sentiment_reasoning")
            String sentimentReasoning
    ) {}
}


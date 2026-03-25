package com.joao.Alertify.infra.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO that maps the raw JSON from GET /v3/reference/tickers (Massive API).
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
public record MassiveTickerResponse(

        int count,

        @JsonProperty("next_url")
        String nextUrl,

        @JsonProperty("request_id")
        String requestId,

        List<TickerResult> results,

        String status
) {

    /**
     * Each element inside the "results" array.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TickerResult(

            boolean active,

            @JsonProperty("currency_name")
            String currencyName,

            @JsonProperty("currency_symbol")
            String currencySymbol,

            @JsonProperty("last_updated_utc")
            String lastUpdatedUtc,

            String locale,

            String market,

            String name,

            @JsonProperty("primary_exchange")
            String primaryExchange,

            String ticker,

            String type
    ) {}
}


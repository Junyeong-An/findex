package com.sprint.mission.findex.domain.syncclient.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.findex.domain.syncclient.dto.IndexDataApiResponse;
import com.sprint.mission.findex.domain.syncclient.dto.KrxApiResponseWrapper;
import com.sprint.mission.findex.global.exception.ApiException;
import com.sprint.mission.findex.global.exception.ApiException.ERROR;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

@Slf4j
@Component
public class KrxOpenApiClientImpl implements KrxOpenApiClient {

    private static final int NUM_OF_ROWS = 1000;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String apiKey;

    public KrxOpenApiClientImpl(
            @Value("${public-data.base-url}") String baseUrl,
            @Value("${public-data.api-key}") String apiKey,
            ObjectMapper objectMapper
    ) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
    }

    @Cacheable(
        value = "krxDailyData",
        key = "#indexName + ':' + #from + ':' + #to",
        condition = "#to != null && #from != null && #to.isBefore(T(java.time.LocalDate).now(T(java.time.ZoneId).of('Asia/Seoul')))"
    )
    @Override
    public List<IndexDataApiResponse> fetchByDateRange(String indexName, LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new ApiException(ERROR.COMMON_INVALID_REQUEST);
        }
        return fetchAllPages(from, to, indexName);
    }

    private List<IndexDataApiResponse> fetchAllPages(LocalDate from, LocalDate to, String indexName) {
        List<IndexDataApiResponse> allItems = new ArrayList<>();
        int pageNo = 1;

        try {
            while (true) {
                List<IndexDataApiResponse> pageItems = fetchPage(from, to, indexName, pageNo);

                if (pageItems.isEmpty()) break;

                allItems.addAll(pageItems);

                if (pageItems.size() < NUM_OF_ROWS) break;

                pageNo++;
            }
        } catch (ApiException e) {
            throw e;
        } catch (RestClientException e) {
            log.error("[KRX API] HTTP 호출 실패: {}", e.getMessage(), e);
            throw new ApiException(ERROR.SYNC_JOB_OPEN_API_ERROR, e);
        }

        return allItems.isEmpty() ? Collections.emptyList() : allItems;
    }

    private List<IndexDataApiResponse> fetchPage(LocalDate from, LocalDate to, String indexName, int pageNo) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl + "/getStockMarketIndex")
                .queryParam("serviceKey", apiKey)
                .queryParam("resultType", "json")
                .queryParam("numOfRows", NUM_OF_ROWS)
                .queryParam("pageNo", pageNo)
                .queryParam("beginBasDt", from.format(DateTimeFormatter.BASIC_ISO_DATE))
                .queryParam("endBasDt", to.plusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE));

        if (indexName != null && !indexName.isBlank()) {
            builder.queryParam("idxNm", UriUtils.encode(indexName, StandardCharsets.UTF_8));
        }

        URI uri = builder.build(true).toUri();
        log.debug("[KRX API] 요청 - pageNo: {}, from: {}, to: {}, indexName: {}", pageNo, from, to, indexName);

        String responseBody = restClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);

        log.debug("[KRX API] 응답 body (앞 200자): {}", responseBody != null ? responseBody.substring(0, Math.min(200, responseBody.length())) : "null");

        if (responseBody == null || responseBody.isBlank()) {
            return Collections.emptyList();
        }

        KrxApiResponseWrapper wrapper;
        try {
            wrapper = objectMapper.readValue(responseBody, KrxApiResponseWrapper.class);
        } catch (JsonProcessingException e) {
            log.error("[KRX API] JSON 파싱 실패: {}", e.getMessage());
            throw new ApiException(ERROR.SYNC_JOB_OPEN_API_ERROR, e);
        }

        validateResponse(wrapper);

        if (wrapper.response().body() == null || wrapper.response().body().items() == null) {
            return Collections.emptyList();
        }

        List<IndexDataApiResponse> items = wrapper.response().body().items().item();
        return items == null ? Collections.emptyList() : items;
    }

    private void validateResponse(KrxApiResponseWrapper wrapper) {
        if (wrapper == null || wrapper.response() == null || wrapper.response().header() == null) {
            throw new ApiException(ERROR.SYNC_JOB_OPEN_API_ERROR);
        }
        if (!"00".equals(wrapper.response().header().resultCode())) {
            log.error("[KRX API] resultCode 오류: {} - {}", wrapper.response().header().resultCode(), wrapper.response().header().resultMsg());
            throw new ApiException(ERROR.SYNC_JOB_OPEN_API_ERROR);
        }
    }
}
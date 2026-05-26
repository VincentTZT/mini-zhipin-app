package com.cn.part.time.service;

import com.cn.part.time.exception.MiniZhipinException;
import com.cn.part.time.model.payload.MiniZhiPinPayload;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZhiPinService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, Integer> errorTimesMap = new ConcurrentHashMap<>();

    private final static String ZHIPIN_URL = "https://www.zhipin.com";

    @Cacheable(value = "zhi.pin.service.valid.account", key = "#phone", unless = "#result==false")
    public boolean validAccountByPhone(HttpHeaders headers, String phone) {
        try {
            String response = proxyRequest(headers, new MiniZhiPinPayload(
                    MiniZhiPinPayload.Method.GET,
                    "/wapi/zppassport/user/accountStatus",
                    null,
                    null
            ), phone);

            JsonNode accountInfoStr = objectMapper.readTree(response);
            String hidPhone = Optional.ofNullable(accountInfoStr.get("zpData").get("phone")).map(JsonNode::asText).orElse(null);
//        log.info("Hid phone is {}", hidPhone);
            if (!StringUtils.hasLength(hidPhone)) {
                return false;
            }
            return phone.startsWith(hidPhone.substring(0, 3)) && phone.endsWith(hidPhone.substring(hidPhone.length() - 2));
        } catch (Exception e) {
            throw new MiniZhipinException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public String proxyRequest(HttpHeaders headers, MiniZhiPinPayload payload, String phone) {
        if (errorTimesMap.getOrDefault(phone, 0) >= 5) {
            throw new MiniZhipinException(HttpStatus.BAD_REQUEST,
                    "BOSS账号 " + phone + " 请求错误达到5次了，请重启程序后再重试，或联系系统管理员。");
        }
        StringBuilder targetUrl = new StringBuilder(ZHIPIN_URL).append(payload.targetUrl());
        String body = null;
        MultiValueMap<String, Object> paramMap = null;

        try {
            if (!CollectionUtils.isEmpty(payload.params())) {
                if (!CollectionUtils.isEmpty(payload.headers()) && payload.headers().entrySet().stream()
                        .anyMatch(entry ->
                                "content-type".equalsIgnoreCase(entry.getKey())
                                        && "application/x-www-form-urlencoded".equalsIgnoreCase(entry.getValue())
                        )) {
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    paramMap = new LinkedMultiValueMap<>();
                    payload.params().forEach(paramMap::set);
                } else {
                    switch (payload.method()) {
                        case MiniZhiPinPayload.Method.PUT, MiniZhiPinPayload.Method.POST -> {
                            body = objectMapper.writeValueAsString(payload.params());
                            headers.setContentType(MediaType.APPLICATION_JSON);
                        }
                        default -> {
                            body = buildGetParameters(payload.params());
                            if (StringUtils.hasLength(body)) {
                                targetUrl.append("?").append(body);
                                body = null;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
//            log.error("Error reading request payload params: {}", payload.params(), e);
            throw new MiniZhipinException(HttpStatus.BAD_REQUEST, "Error reading payload params: " + payload.params());
        }

        if (!CollectionUtils.isEmpty(payload.headers())) {
            payload.headers().forEach(headers::set);
        }
        headers.set("referer", targetUrl.toString());

//        try {
//            log.info("Request Header: {}", objectMapper.writeValueAsString(headers));
//            log.info("Request Body: {}", CollectionUtils.isEmpty(paramMap) ? body : objectMapper.writeValueAsString(paramMap));
//            log.info("Request url: {}", targetUrl);
//        } catch (JsonProcessingException e) {
//            log.error("Error parsing objects.", e);
//        }

        try {
            // 构建目标URI
            URI targetUri = new URI(targetUrl.toString());

            // 创建请求实体
            HttpEntity<Object> requestEntity = CollectionUtils.isEmpty(paramMap) ? new HttpEntity<>(body, headers) : new HttpEntity<>(paramMap, headers);

            // 转发请求并获取响应
            ResponseEntity<String> response = restTemplate.exchange(
                    targetUri,
                    HttpMethod.valueOf(payload.method().name()),
                    requestEntity,
                    String.class
            );
//            log.info("Response: {}", response);

            return response.getBody();
        } catch (Exception e) {
            errorTimesMap.put(phone, errorTimesMap.getOrDefault(phone, 0) + 1);
            throw new MiniZhipinException(HttpStatus.BAD_REQUEST, "Error proxying request: " + e.getMessage());
        }
    }

    private String buildGetParameters(Map<String, Object> params) {
        return params.entrySet().stream()
                .map(entry -> {
                    String key = UriUtils.encode(entry.getKey(), StandardCharsets.UTF_8);
                    Object value = entry.getValue();

                    if (value == null) {
                        return null;
                    }
                    if (value.getClass().isArray()) {
                        String joinedValues = Arrays.stream((Object[]) value)
                                .map(this::encodeValue).filter(StringUtils::hasLength)
                                .collect(Collectors.joining(","));
                        return StringUtils.hasLength(joinedValues) ? key + "=" + joinedValues : null;
                    } else if (value instanceof Iterable<?>) {
                        String joinedValues = StreamSupport.stream(((Iterable<?>) value).spliterator(), false)
                                .map(this::encodeValue).filter(StringUtils::hasLength)
                                .collect(Collectors.joining(","));
                        return StringUtils.hasLength(joinedValues) ? key + "=" + joinedValues : null;
                    } else {
                        String encodeValue = encodeValue(value);
                        return StringUtils.hasLength(encodeValue) ? key + "=" + encodeValue : null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining("&"));
    }

    private String encodeValue(Object value) {
        if (value == null) return "";
        return UriUtils.encode(value.toString(), StandardCharsets.UTF_8);
    }
}

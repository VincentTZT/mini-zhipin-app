package com.cn.part.time.service;

import com.cn.part.time.exception.MiniZhipinException;
import com.cn.part.time.model.response.AccountDto;
import com.cn.part.time.model.response.AccountVo;
import com.cn.part.time.model.service.AccountBo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final static String ACCOUNT_LIST_URL = "https://api.txttool.cn/netcut/note/info/";
    private final static String ACCOUNT_PASSWORD = "xin-xin";

    @Cacheable(value = "login.service.account", key = "#noteName + '_' + #phone", unless = "#result==null")
    public AccountVo accountStatus(String noteName, String phone) {
        HttpHeaders headers = buildHeaders();
        MultiValueMap<String, String> parameters = buildParameters(noteName);

//        try {
//            log.info("Request Header: {}", objectMapper.writeValueAsString(headers));
//            log.info("Request Parameters: {}", objectMapper.writeValueAsString(parameters));
//        } catch (JsonProcessingException e) {
//            log.error("Error parsing objects.", e);
//        }

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parameters, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                ACCOUNT_LIST_URL,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
//            log.error("get account list failed: {}", response);
            throw new MiniZhipinException(HttpStatus.valueOf(response.getStatusCode().value()), "get account list failed");
        }
        String responseBody = response.getBody();
//        log.info("response: {}", responseBody);

        if (StringUtils.hasLength(responseBody)) {
            String accountJson;
            try {
                AccountDto accountDto = objectMapper.readValue(responseBody, AccountDto.class);
                ArrayNode arrayNode = objectMapper.readValue(accountDto.data().noteContent(), ArrayNode.class);
                accountJson = arrayNode.get(0).get("content").asText();
            } catch (Exception e) {
//                log.error("parse account list failed", e);
                throw new MiniZhipinException(HttpStatus.BAD_REQUEST, "parse account list failed: " + responseBody, e);
            }
            try {
                List<AccountBo> accountBos = objectMapper.readValue(accountJson, new TypeReference<>() {
                });
                return accountBos.stream()
                        .filter(account -> account.phone().equals(phone))
                        .findFirst()
                        .map(bo -> new AccountVo(bo.phone(), bo.expireDate(), LocalDate.now().isAfter(bo.expireDate())))
                        .orElseGet(() -> {
                            log.error("account not found: {}", phone);
                            return null;
                        });
            } catch (Exception e) {
//                log.error("parse account list failed", e);
                throw new MiniZhipinException(HttpStatus.BAD_REQUEST, "parse account list failed: " + accountJson, e);
            }
        } else {
            throw new MiniZhipinException(HttpStatus.BAD_REQUEST, "account list is empty");
        }
    }

    private static MultiValueMap<String, String> buildParameters(String noteName) {
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("note_name", noteName);
        paramMap.add("note_pwd", ACCOUNT_PASSWORD);
        return paramMap;
    }

    private static HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        MediaType mediaType = new MediaType(MediaType.APPLICATION_FORM_URLENCODED, StandardCharsets.UTF_8);
        headers.setContentType(mediaType);
        headers.set("accept-language", "en-US,en;q=0.9");
        return headers;
    }
}

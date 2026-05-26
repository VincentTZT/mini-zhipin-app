package com.cn.part.time.controller;


import com.cn.part.time.exception.MiniZhipinException;
import com.cn.part.time.model.payload.MiniZhiPinPayload;
import com.cn.part.time.model.response.AccountVo;
import com.cn.part.time.model.response.ResponseVo;
import com.cn.part.time.service.LoginService;
import com.cn.part.time.service.ZhiPinService;
import com.cn.part.time.util.AESUtil;
import com.cn.part.time.util.Const;
import com.cn.part.time.util.ProxyUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ProxyController {
    private final LoginService loginService;
    private final ZhiPinService zhiPinService;
    private final ObjectMapper objectMapper;

    @PostMapping("/proxy/zhipin")
    public ResponseVo proxyRequest(HttpServletRequest request, @RequestBody Map<String, String> map) {
        String phone = request.getHeader(Const.ZHIPIN_PHONE);
        String noteName = request.getHeader(Const.ZHIPIN_NOTE_NAME);
        MiniZhiPinPayload payload;
        try {
            payload = objectMapper.readValue(AESUtil.decrypt(noteName, map.get("msg")), MiniZhiPinPayload.class);
        } catch (Exception e) {
            throw new MiniZhipinException(HttpStatus.BAD_REQUEST, "Decryption failed.", e);
        }
        if (!StringUtils.hasLength(phone)) {
            log.error("手机号码不能为空: {}", phone);
            return new ResponseVo(false, "手机号码不能为空: " + phone, null);
        }
        AccountVo accountVo = loginService.accountStatus(noteName, phone);
        if (accountVo == null) {
            log.error("无效账号: {}", phone);
            return new ResponseVo(false, "无效账号: " + phone, null);
        }
        if (accountVo.isExpired()) {
            log.error("账号已过期: {}", phone);
            return new ResponseVo(false, "账号已过期: " + phone, null);
        }

        HttpHeaders headers = ProxyUtil.copyRequestHeaders(request);
        if (!zhiPinService.validAccountByPhone(headers, phone)) {
            log.error("BOSS账号认证失败: {}", phone);
            return new ResponseVo(false, "BOSS账号认证失败: " + phone, null);
        }
        return new ResponseVo(true, "success", AESUtil.encrypt(noteName, zhiPinService.proxyRequest(headers, payload, phone)));
    }

    @GetMapping("/account")
    public ResponseVo accountStatus(@RequestHeader(Const.ZHIPIN_NOTE_NAME) String noteName, @RequestHeader(Const.ZHIPIN_PHONE) String phone) {
        AccountVo accountVo = loginService.accountStatus(noteName, phone);
        return accountVo == null
                ? new ResponseVo(false, "无效账号: " + phone, null)
                : new ResponseVo(true, "success", accountVo);
    }
}

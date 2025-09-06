package com.cn.past.time.controller;


import com.cn.past.time.exception.MiniZhipinException;
import com.cn.past.time.model.payload.MiniZhiPinPayload;
import com.cn.past.time.model.response.AccountVo;
import com.cn.past.time.service.LoginService;
import com.cn.past.time.service.ZhiPinService;
import com.cn.past.time.util.Const;
import com.cn.past.time.util.ProxyUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/proxy")
public class ProxyController {
    private final LoginService loginService;
    private final ZhiPinService zhiPinService;

    @PostMapping("/zhipin")
    public String proxyRequest(HttpServletRequest request, @Valid @RequestBody MiniZhiPinPayload payload) {
        String phone = request.getHeader(Const.ZHIPIN_PHONE);
        String noteId = request.getHeader(Const.ZHIPIN_NOTE_ID);
        if (!StringUtils.hasLength(phone)) {
            throw new MiniZhipinException(HttpStatus.UNAUTHORIZED, "手机号码不能为空: " + phone);
        }
        AccountVo accountVo = loginService.accountStatus(noteId, phone);
        if (accountVo == null || accountVo.isExpired()) {
            log.error("无效账号: {}", accountVo);
            throw new MiniZhipinException(HttpStatus.UNAUTHORIZED, "无效账号: " + phone);
        }
        HttpHeaders headers = ProxyUtil.copyRequestHeaders(request);
        if (!zhiPinService.validAccountByPhone(headers, phone)) {
            throw new MiniZhipinException(HttpStatus.UNAUTHORIZED, "无效账号: " + phone);
        }
        return zhiPinService.proxyRequest(headers, payload);
    }

    @GetMapping("/account/status")
    public AccountVo accountStatus(@RequestHeader(Const.ZHIPIN_NOTE_ID) String nodeId, @RequestHeader(Const.ZHIPIN_PHONE) String phone) {
        return loginService.accountStatus(nodeId, phone);
    }
}

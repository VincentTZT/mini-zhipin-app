package com.cn.past.time.controller;


import com.cn.past.time.model.payload.MiniZhiPinPayload;
import com.cn.past.time.model.response.AccountVo;
import com.cn.past.time.model.response.ResponseDto;
import com.cn.past.time.service.LoginService;
import com.cn.past.time.service.ZhiPinService;
import com.cn.past.time.util.Const;
import com.cn.past.time.util.ProxyUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
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
    public ResponseDto proxyRequest(HttpServletRequest request, @Valid @RequestBody MiniZhiPinPayload payload) {
        String phone = request.getHeader(Const.ZHIPIN_PHONE);
        String noteId = request.getHeader(Const.ZHIPIN_NOTE_ID);
        if (!StringUtils.hasLength(phone)) {
            log.error("手机号码不能为空: {}", phone);
            return new ResponseDto(false, "手机号码不能为空: " + phone, null);
        }
        AccountVo accountVo = loginService.accountStatus(noteId, phone);
        if (accountVo == null) {
            log.error("无效账号: {}", phone);
            return new ResponseDto(false, "无效账号: " + phone, null);
        }
        if (accountVo.isExpired()) {
            log.error("账号已过期: {}", phone);
            return new ResponseDto(false, "账号已过期: " + phone, null);
        }

        HttpHeaders headers = ProxyUtil.copyRequestHeaders(request);
        if (!zhiPinService.validAccountByPhone(headers, phone)) {
            log.error("无效账号: {}", phone);
            return new ResponseDto(false, "无效账号: " + phone, null);
        }
        return new ResponseDto(true, "success", zhiPinService.proxyRequest(headers, payload));
    }

    @GetMapping("/account/status")
    public AccountVo accountStatus(@RequestHeader(Const.ZHIPIN_NOTE_ID) String nodeId, @RequestHeader(Const.ZHIPIN_PHONE) String phone) {
        return loginService.accountStatus(nodeId, phone);
    }
}

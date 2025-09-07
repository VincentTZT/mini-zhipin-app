package com.cn.past.time.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;

import java.util.Enumeration;
import java.util.List;

public class ProxyUtil {
    public static HttpHeaders copyRequestHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            // 跳过一些不需要转发的头
            if (List.of("content-length", "host", "referer", "origin", "content-type", Const.ZHIPIN_NOTE_ID, Const.ZHIPIN_PHONE).contains(headerName)) {
                continue;
            }

            String headerValue = request.getHeader(headerName);
            headers.set(headerName, headerValue);
        }
        headers.set("X-Requested-With", "XMLHttpRequest");
        headers.set("connection", "keep-alive");
        return headers;
    }
}

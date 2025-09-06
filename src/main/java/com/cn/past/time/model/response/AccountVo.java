package com.cn.past.time.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record AccountVo(
        String phone,
        @JsonProperty("expire_date")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate expireDate,
        boolean isExpired
) {
}

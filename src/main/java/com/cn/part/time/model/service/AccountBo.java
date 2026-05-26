package com.cn.part.time.model.service;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record AccountBo(
        String phone,
        @JsonProperty("expire_date")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate expireDate
) {
}

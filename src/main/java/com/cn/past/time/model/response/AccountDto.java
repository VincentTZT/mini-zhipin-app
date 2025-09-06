package com.cn.past.time.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AccountDto(
        Data data
) {
    public record Data(
            @JsonProperty("note_content")
            String noteContent
    ) {
    }
}

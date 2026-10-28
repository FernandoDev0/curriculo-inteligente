package com.curriculo.inteligente.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        String code,
        String message,
        Map<String, String> fields,
        Integer pageCount,
        List<String> hints) {

    public ApiError(String code, String message, Map<String, String> fields) {
        this(code, message, fields, null, null);
    }
}

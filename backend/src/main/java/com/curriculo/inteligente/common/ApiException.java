package com.curriculo.inteligente.common;

import java.util.Map;

public class ApiException extends RuntimeException {

    private final int status;
    private final String code;
    private final Map<String, String> fields;
    private final Integer pageCount;
    private final java.util.List<String> hints;

    public ApiException(int status, String code, String message) {
        this(status, code, message, Map.of(), null, null);
    }

    public ApiException(int status, String code, String message, Map<String, String> fields) {
        this(status, code, message, fields, null, null);
    }

    public ApiException(int status, String code, String message, Integer pageCount, java.util.List<String> hints) {
        this(status, code, message, Map.of(), pageCount, hints);
    }

    public ApiException(int status, String code, String message, Map<String, String> fields,
                        Integer pageCount, java.util.List<String> hints) {
        super(message);
        this.status = status;
        this.code = code;
        this.fields = fields;
        this.pageCount = pageCount;
        this.hints = hints;
    }

    public int status() {
        return status;
    }

    public String code() {
        return code;
    }

    public Map<String, String> fields() {
        return fields;
    }

    public Integer pageCount() {
        return pageCount;
    }

    public java.util.List<String> hints() {
        return hints;
    }
}

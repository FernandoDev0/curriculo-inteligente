package com.curriculo.inteligente.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> api(ApiException ex) {
        return ResponseEntity.status(ex.status())
                .body(new ApiError(ex.code(), ex.getMessage(), ex.fields(), ex.pageCount(), ex.hints()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> fields.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ApiError("VALIDATION", "Revise os campos.", fields));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> unreadable() {
        return ResponseEntity.badRequest().body(new ApiError("VALIDATION", "O corpo da requisição não pôde ser lido.", Map.of()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> tooLarge() {
        return ResponseEntity.status(413).body(new ApiError("PAYLOAD_TOO_LARGE", "A foto passa de 5 MB.", Map.of()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception ex) {
        log.error("Falha nao tratada", ex);
        String detail = ex.getMessage();
        if (detail == null || detail.isBlank()) {
            detail = "O servidor não conseguiu concluir o pedido.";
        }
        int line = detail.indexOf('\n');
        if (line > 0) {
            detail = detail.substring(0, line);
        }
        if (detail.length() > 240) {
            detail = detail.substring(0, 240);
        }
        return ResponseEntity.status(500).body(new ApiError("UNAVAILABLE", detail, Map.of()));
    }
}

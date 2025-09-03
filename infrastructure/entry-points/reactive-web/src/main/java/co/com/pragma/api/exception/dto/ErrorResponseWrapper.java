package co.com.pragma.api.exception.dto;

import org.springframework.http.HttpStatus;

public record ErrorResponseWrapper(HttpStatus status, ErrorBody body) {
}
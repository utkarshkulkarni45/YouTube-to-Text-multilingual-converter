package com.utkarsh.yt2text.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    List<String> detail,
    String error
) {
    public static ErrorResponse ofDetail(String message) {
        return new ErrorResponse(List.of(message), null);
    }

    public static ErrorResponse ofError(String error) {
        return new ErrorResponse(null, error);
    }
}


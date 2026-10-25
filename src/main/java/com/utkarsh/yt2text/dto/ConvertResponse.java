package com.utkarsh.yt2text.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ConvertResponse(
    boolean success,
    @JsonProperty("english") String english,
    @JsonProperty("hindi") String hindi,
    @JsonProperty("marathi") String marathi
) {}


package com.utkarsh.yt2text.dto;

import java.util.List;

public record TranscriptSegment(
    double start,
    double end,
    String text
) {}


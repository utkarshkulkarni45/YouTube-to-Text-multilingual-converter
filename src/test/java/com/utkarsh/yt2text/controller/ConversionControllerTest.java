package com.utkarsh.yt2text.controller;

import com.utkarsh.yt2text.dto.ConvertResponse;
import com.utkarsh.yt2text.exception.InvalidUrlException;
import com.utkarsh.yt2text.service.ConversionPipelineService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConversionController.class)
class ConversionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConversionPipelineService pipelineService;

    @Test
    @DisplayName("GET /api/health returns 200 and status UP")
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("youtube-to-text"));
    }

    @Test
    @DisplayName("POST /api/convert returns success with filenames")
    void testConvertEndpointSuccess() throws Exception {
        when(pipelineService.process("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
                .thenReturn(new ConvertResponse(true, "abc_english.txt", "abc_hindi.txt", "abc_marathi.txt"));

        mockMvc.perform(post("/api/convert")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("youtube_url", "https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.english").value("abc_english.txt"))
                .andExpect(jsonPath("$.hindi").value("abc_hindi.txt"))
                .andExpect(jsonPath("$.marathi").value("abc_marathi.txt"));
    }

    @Test
    @DisplayName("POST /api/convert returns 400 when URL is invalid")
    void testConvertEndpointInvalidUrl() throws Exception {
        when(pipelineService.process(anyString()))
                .thenThrow(new InvalidUrlException("Invalid YouTube URL format."));

        mockMvc.perform(post("/api/convert")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("youtube_url", "https://invalid-url.com"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail[0]").value("Invalid YouTube URL format."));
    }

    @Test
    @DisplayName("GET /{filename} returns file content safely")
    void testDownloadFileSuccess() throws Exception {
        byte[] content = "Hello world transcript".getBytes();
        when(pipelineService.getFileContent("test_file.txt")).thenReturn(content);

        mockMvc.perform(get("/test_file.txt"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"test_file.txt\""))
                .andExpect(content().string("Hello world transcript"));
    }
}


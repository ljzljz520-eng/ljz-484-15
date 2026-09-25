package com.novel.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 作者工作台统计口径的集成测试：
 * 统计必须由后端基于本地数据计算，且草稿对读者不可见。
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthorWorkbenchTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void statsAreComputedFromLocalData() throws Exception {
        mockMvc.perform(get("/api/author/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.novelCount").value(3))
                .andExpect(jsonPath("$.draftChapterCount").value(3))
                .andExpect(jsonPath("$.publishedChapterCount").value(6))
                .andExpect(jsonPath("$.totalChapterCount").value(9))
                .andExpect(jsonPath("$.lastUpdatedAt").isNotEmpty())
                .andExpect(jsonPath("$.weeklyNewWords").isNumber())
                .andExpect(jsonPath("$.calculatedAt").isNotEmpty());
    }

    @Test
    void weeklyWordsEqualSumOfChaptersCreatedSinceMonday() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper mapper =
                new com.fasterxml.jackson.databind.ObjectMapper();

        MvcResult statsResult = mockMvc.perform(get("/api/author/stats"))
                .andExpect(status().isOk())
                .andReturn();
        long weeklyWords = mapper.readTree(statsResult.getResponse().getContentAsString())
                .get("weeklyNewWords").asLong();

        MvcResult weekResult = mockMvc.perform(get("/api/author/chapters")
                        .param("thisWeek", "true")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andReturn();
        long summed = 0L;
        for (com.fasterxml.jackson.databind.JsonNode item :
                mapper.readTree(weekResult.getResponse().getContentAsString()).get("data")) {
            summed += item.get("wordCount").asLong();
        }

        org.junit.jupiter.api.Assertions.assertEquals(summed, weeklyWords,
                "本周新增字数必须等于本周新增章节字数之和");
    }

    @Test
    void draftChaptersAreHiddenFromReaders() throws Exception {
        // id=4 是种子数据中的草稿章节
        mockMvc.perform(get("/api/chapters/4"))
                .andExpect(status().isNotFound());
        // 作品1的读者目录只包含已发布章节（4章中的3章）
        mockMvc.perform(get("/api/novels/1/chapters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void invalidStatusFilterIsRejected() throws Exception {
        mockMvc.perform(get("/api/author/chapters").param("status", "FOO"))
                .andExpect(status().isBadRequest());
    }
}

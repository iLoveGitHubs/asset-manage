package com.assetmanage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AssetSearchAndPaginationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, Object> newAssetPayload(String code) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Asset " + code);
        payload.put("code", code);
        payload.put("status", "AVAILABLE");
        payload.put("purchaseDate", "2022-01-01");
        payload.put("purchaseValue", 1000.00);
        payload.put("currentValue", 1000.00);
        payload.put("categoryId", 1);
        payload.put("usefulLifeYears", 5);
        return payload;
    }

    private Long createAsset(String code) throws Exception {
        String response = mockMvc.perform(post("/api/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAssetPayload(code))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    void pagination_page0_size2_returns200WithPage() throws Exception {
        mockMvc.perform(get("/api/assets")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.totalPages").exists())
                .andExpect(jsonPath("$.pageable.pageSize").value(2))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @Test
    void pagination_default_returns200() throws Exception {
        mockMvc.perform(get("/api/assets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.totalPages").exists())
                .andExpect(jsonPath("$.pageable.pageSize").value(20))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @Test
    void search_qDell_returns200WithResults() throws Exception {
        mockMvc.perform(get("/api/assets/search").param("q", "Dell"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").exists());
    }

    @Test
    void search_qNonexistent_returns200WithEmptyArray() throws Exception {
        mockMvc.perform(get("/api/assets/search").param("q", "nonexistentxyz123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void depreciation_returns200WithInfo() throws Exception {
        Long id = createAsset("LAP-DEP-INFO-001");

        mockMvc.perform(get("/api/assets/" + id + "/depreciation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assetId").value(id))
                .andExpect(jsonPath("$.code").value("LAP-DEP-INFO-001"))
                .andExpect(jsonPath("$.purchaseValue").exists())
                .andExpect(jsonPath("$.usefulLifeYears").exists())
                .andExpect(jsonPath("$.yearsElapsed").exists())
                .andExpect(jsonPath("$.accumulatedDepreciation").exists())
                .andExpect(jsonPath("$.currentValue").exists());
    }
}

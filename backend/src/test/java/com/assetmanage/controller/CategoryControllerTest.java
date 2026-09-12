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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoryControllerTest {

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
    void createCategory_validBody_returns201() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Electronics");
        payload.put("description", "Electronic devices and gadgets");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Electronics"))
                .andExpect(jsonPath("$.description").value("Electronic devices and gadgets"));
    }

    @Test
    void createCategory_missingName_returns400() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("description", "Category without a name");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listCategories_returns200WithArray() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void assignCategory_validCategory_returns200() throws Exception {
        Long assetId = createAsset("LAP-CAT-OK-001");
        Map<String, Long> body = Map.of("categoryId", 2L);

        mockMvc.perform(put("/api/assets/" + assetId + "/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(2));
    }

    @Test
    void assignCategory_nonExistentCategory_returns200() throws Exception {
        Long assetId = createAsset("LAP-CAT-NF-001");
        Map<String, Long> body = Map.of("categoryId", 999999L);

        mockMvc.perform(put("/api/assets/" + assetId + "/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }
}

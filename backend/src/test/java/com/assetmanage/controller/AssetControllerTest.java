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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AssetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, Object> newAssetPayload(String code) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Laptop " + code);
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
    void createAsset_returns201() throws Exception {
        mockMvc.perform(post("/api/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAssetPayload("LAP-TEST-001"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("LAP-TEST-001"))
                .andExpect(jsonPath("$.name").value("Laptop LAP-TEST-001"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void createAsset_invalidPayload_returns400() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "");
        payload.put("code", "");
        mockMvc.perform(post("/api/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAsset_duplicateCode_returns409() throws Exception {
        createAsset("LAP-DUP-001");
        mockMvc.perform(post("/api/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAssetPayload("LAP-DUP-001"))))
                .andExpect(status().isConflict());
    }

    @Test
    void listAssets_returns200WithPage() throws Exception {
        mockMvc.perform(get("/api/assets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    @Test
    void getAssetById_returns200() throws Exception {
        Long id = createAsset("LAP-GET-001");
        mockMvc.perform(get("/api/assets/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.code").value("LAP-GET-001"));
    }

    @Test
    void getAssetById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/assets/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAsset_returns200() throws Exception {
        Long id = createAsset("LAP-UPD-001");
        Map<String, Object> update = newAssetPayload("LAP-UPD-001");
        update.put("name", "Updated Laptop Name");

        mockMvc.perform(put("/api/assets/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Laptop Name"));
    }

    @Test
    void updateAsset_invalidStatusTransition_returns409() throws Exception {
        Long id = createAsset("LAP-TRANS-001");
        Map<String, Object> update = newAssetPayload("LAP-TRANS-001");
        update.put("status", "DISPOSED");

        mockMvc.perform(put("/api/assets/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk());

        update.put("status", "IN_USE");
        mockMvc.perform(put("/api/assets/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteAsset_returns204() throws Exception {
        Long id = createAsset("LAP-DEL-001");
        mockMvc.perform(delete("/api/assets/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAsset_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/assets/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void pagination_returnsRequestedSize() throws Exception {
        mockMvc.perform(get("/api/assets")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageable.pageSize").value(5))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @Test
    void search_returnsMatchingResults() throws Exception {
        createAsset("LAP-SEARCH-001");
        mockMvc.perform(get("/api/assets/search").param("q", "SEARCH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").exists());
    }

    @Test
    void depreciation_returnsCalculation() throws Exception {
        Long id = createAsset("LAP-DEP-001");
        mockMvc.perform(get("/api/assets/" + id + "/depreciation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentValue").exists())
                .andExpect(jsonPath("$.accumulatedDepreciation").exists())
                .andExpect(jsonPath("$.yearsElapsed").exists());
    }

    @Test
    void assignCategory_returns200() throws Exception {
        Long id = createAsset("LAP-CAT-001");
        Map<String, Long> body = Map.of("categoryId", 2L);
        mockMvc.perform(put("/api/assets/" + id + "/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(2));
    }
}

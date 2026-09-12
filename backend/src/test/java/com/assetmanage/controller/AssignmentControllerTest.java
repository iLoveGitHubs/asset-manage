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
class AssignmentControllerTest {

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

    private Map<String, Object> assignPayload(Long assetId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("assetId", assetId);
        payload.put("employeeId", 1);
        payload.put("assignDate", "2024-01-15");
        return payload;
    }

    @Test
    void assign_validBody_returns201() throws Exception {
        Long assetId = createAsset("LAP-ASG-001");

        mockMvc.perform(post("/api/assets/" + assetId + "/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignPayload(assetId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assetId").value(assetId))
                .andExpect(jsonPath("$.employeeId").value(1))
                .andExpect(jsonPath("$.assignDate").exists())
                .andExpect(jsonPath("$.returnDate").doesNotExist());
    }

    @Test
    void assign_alreadyAssignedAsset_returns409() throws Exception {
        Long assetId = createAsset("LAP-ASG-DUP-001");

        mockMvc.perform(post("/api/assets/" + assetId + "/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignPayload(assetId))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/assets/" + assetId + "/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignPayload(assetId))))
                .andExpect(status().isConflict());
    }

    @Test
    void assign_nonExistentAsset_returns404() throws Exception {
        mockMvc.perform(post("/api/assets/999999/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignPayload(999999L))))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnAsset_returns200() throws Exception {
        Long assetId = createAsset("LAP-RET-001");

        mockMvc.perform(post("/api/assets/" + assetId + "/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignPayload(assetId))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/assets/" + assetId + "/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assetId").value(assetId))
                .andExpect(jsonPath("$.returnDate").exists());
    }

    @Test
    void returnAsset_notAssigned_returns404() throws Exception {
        Long assetId = createAsset("LAP-RET-NA-001");

        mockMvc.perform(post("/api/assets/" + assetId + "/return"))
                .andExpect(status().isNotFound());
    }

    @Test
    void findAllAssignments_returns200WithArray() throws Exception {
        mockMvc.perform(get("/api/assignments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}

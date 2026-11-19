package org.example.requests.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RequestControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullLifecycle() throws Exception {
        long id = create("Grant", "Please approve");

        mvc.perform(put("/api/requests/{id}/content", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated"));

        action(id, "verify").andExpect(jsonPath("$.state").value("VERIFIED"));
        action(id, "accept").andExpect(jsonPath("$.state").value("ACCEPTED"));
        action(id, "publish")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("PUBLISHED"))
                .andExpect(jsonPath("$.publicationNumber").isNumber());

        mvc.perform(get("/api/requests/{id}", id))
                .andExpect(jsonPath("$.name").value("Grant"))
                .andExpect(jsonPath("$.state").value("PUBLISHED"));

        mvc.perform(get("/api/requests/{id}/history", id))
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[3].fromState").value("ACCEPTED"))
                .andExpect(jsonPath("$[3].toState").value("PUBLISHED"));
    }

    @Test
    void publicationNumbersAreUnique() throws Exception {
        long first = publishedNumber(create("a", "b"));
        long second = publishedNumber(create("c", "d"));
        org.assertj.core.api.Assertions.assertThat(first).isNotEqualTo(second);
    }

    @Test
    void createRequiresNameAndContent() throws Exception {
        mvc.perform(post("/api/requests").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteAndRejectRequireReason() throws Exception {
        long id = create("n", "c");
        mvc.perform(post("/api/requests/{id}/delete", id).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/requests/{id}/delete", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"duplicate\"}"))
                .andExpect(jsonPath("$.state").value("DELETED"))
                .andExpect(jsonPath("$.reason").value("duplicate"));

        long other = create("n", "c");
        action(other, "verify");
        mvc.perform(post("/api/requests/{id}/reject", other).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/requests/{id}/reject", other).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"incomplete\"}"))
                .andExpect(jsonPath("$.state").value("REJECTED"));
    }

    @Test
    void invalidTransitionAndNonEditableContentReturnConflict() throws Exception {
        long id = create("n", "c");
        action(id, "publish").andExpect(status().isConflict());

        action(id, "verify");
        action(id, "accept");
        mvc.perform(put("/api/requests/{id}/content", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"x\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void unknownRequestReturnsNotFound() throws Exception {
        mvc.perform(get("/api/requests/{id}", 999_999)).andExpect(status().isNotFound());
    }

    @Test
    void listIsPaginatedAndFilterable() throws Exception {
        String prefix = UUID.randomUUID().toString();
        for (int i = 0; i < 12; i++) {
            create(prefix + " report " + i, "c");
        }
        action(create(prefix + " memo", "c"), "verify");

        mvc.perform(get("/api/requests").param("name", prefix))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.totalElements").value(13));

        mvc.perform(get("/api/requests").param("name", prefix.toUpperCase()).param("page", "1"))
                .andExpect(jsonPath("$.content", hasSize(3)));

        mvc.perform(get("/api/requests").param("name", prefix).param("state", "VERIFIED"))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value(prefix + " memo"));
    }

    private long create(String name, String content) throws Exception {
        String response = mvc.perform(post("/api/requests").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", name, "content", content))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.state").value("CREATED"))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private ResultActions action(long id, String action) throws Exception {
        return mvc.perform(post("/api/requests/{id}/{action}", id, action));
    }

    private long publishedNumber(long id) throws Exception {
        action(id, "verify");
        action(id, "accept");
        String response = action(id, "publish").andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("publicationNumber").asLong();
    }
}

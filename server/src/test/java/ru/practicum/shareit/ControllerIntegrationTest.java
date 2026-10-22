package ru.practicum.shareit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ControllerIntegrationTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void usersCanBeCreatedUpdatedAndDeletedWithEmailValidation() throws Exception {
        long id = createUser("Alice", "alice.api@example.com");

        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Other\",\"email\":\"alice.api@example.com\"}"))
                .andExpect(status().isConflict());
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Other\",\"email\":\"bad-email\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/users/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Updated"))
                .andExpect(jsonPath("$.email").value("alice.api@example.com"));
        mvc.perform(get("/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Updated"));
        mvc.perform(delete("/users/{id}", id)).andExpect(status().isOk());
        mvc.perform(get("/users/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void itemsAreOwnedAndSearchReturnsOnlyAvailableMatches() throws Exception {
        long owner = createUser("Owner", "owner.api@example.com");
        long other = createUser("Other", "other.api@example.com");
        long drill = createItem(owner, "Drill", "Cordless tool", true);
        createItem(owner, "Saw", "Cordless tool", false);

        mvc.perform(post("/items").header("X-Sharer-User-Id", owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Broken\",\"description\":\"No status\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/items").header("X-Sharer-User-Id", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Lost\",\"description\":\"Lost\",\"available\":true}"))
                .andExpect(status().isNotFound());
        mvc.perform(patch("/items/{id}", drill).header("X-Sharer-User-Id", other)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Stolen\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/items/{id}", drill).header("X-Sharer-User-Id", owner)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Power drill\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Power drill"));
        mvc.perform(get("/items/{id}", drill)).andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Cordless tool"));
        mvc.perform(get("/items").header("X-Sharer-User-Id", owner))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/items/search").param("text", "CORDLESS"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(drill));
        mvc.perform(get("/items/search").param("text", ""))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
    }

    private long createUser(String name, String email) throws Exception {
        String response = mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new UserInput(name, email))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).get("id").asLong();
    }

    private long createItem(long ownerId, String name, String description, boolean available) throws Exception {
        String response = mvc.perform(post("/items").header("X-Sharer-User-Id", ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ItemInput(name, description, available))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode item = mapper.readTree(response);
        return item.get("id").asLong();
    }

    private record UserInput(String name, String email) {
    }

    private record ItemInput(String name, String description, boolean available) {
    }
}

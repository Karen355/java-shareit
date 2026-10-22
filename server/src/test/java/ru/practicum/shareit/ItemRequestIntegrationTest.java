package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.ItemRequestService;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ItemRequestIntegrationTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ItemRequestRepository requests;

    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void usersCanCreateRequestsAndAnswerOtherUsersRequests() throws Exception {
        long requestorId = createUser("Requestor", "requestor.flow@example.com");
        long ownerId = createUser("Owner", "owner.flow@example.com");
        long firstRequestId = createRequest(requestorId, "Need a drill");
        long secondRequestId = createRequest(requestorId, "Need a saw");
        long ownerRequestId = createRequest(ownerId, "Need a ladder");
        LocalDateTime sameTime = LocalDateTime.now().minusHours(1);
        var firstRequest = requests.findById(firstRequestId).orElseThrow();
        var secondRequest = requests.findById(secondRequestId).orElseThrow();
        firstRequest.setCreated(sameTime);
        secondRequest.setCreated(sameTime);
        requests.save(firstRequest);
        requests.save(secondRequest);

        String itemResponse = mvc.perform(post("/items").header("X-Sharer-User-Id", ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ItemInput("Drill", "Cordless", true, firstRequestId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(firstRequestId))
                .andReturn().getResponse().getContentAsString();
        long itemId = mapper.readTree(itemResponse).get("id").asLong();

        mvc.perform(get("/requests").header("X-Sharer-User-Id", requestorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(secondRequestId))
                .andExpect(jsonPath("$[1].items[0].id").value(itemId))
                .andExpect(jsonPath("$[1].items[0].name").value("Drill"))
                .andExpect(jsonPath("$[1].items[0].ownerId").value(ownerId));
        mvc.perform(get("/requests/all").header("X-Sharer-User-Id", ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(secondRequestId));
        mvc.perform(get("/requests/{requestId}", firstRequestId)
                        .header("X-Sharer-User-Id", ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(itemId));
        mvc.perform(get("/requests/all").header("X-Sharer-User-Id", requestorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ownerRequestId));

        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();
        try {
            assertEquals(2, requestService.getOwn(requestorId).size());
            assertEquals(3, statistics.getPrepareStatementCount());
        } finally {
            statistics.setStatisticsEnabled(false);
        }
    }

    @Test
    void invalidRequestsAndAnswersAreRejected() throws Exception {
        long requestorId = createUser("Requestor", "requestor.invalid@example.com");
        long ownerId = createUser("Other owner", "owner.invalid@example.com");
        long requestId = createRequest(requestorId, "Need a camera");

        mvc.perform(post("/requests").header("X-Sharer-User-Id", requestorId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"description\":\"  \"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/items").header("X-Sharer-User-Id", requestorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ItemInput("Camera", "Digital", true, requestId))))
                .andExpect(status().isForbidden());
        mvc.perform(get("/requests/{requestId}", 999999)
                        .header("X-Sharer-User-Id", requestorId))
                .andExpect(status().isNotFound());
        mvc.perform(post("/requests").header("X-Sharer-User-Id", 999999)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"description\":\"Camera\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/items").header("X-Sharer-User-Id", ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ItemInput("Camera", "Digital", true, 999999L))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(containsString("999999")));
    }

    private long createUser(String name, String email) throws Exception {
        String response = mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new UserInput(name, email))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).get("id").asLong();
    }

    private long createRequest(long userId, String description) throws Exception {
        String response = mvc.perform(post("/requests").header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new RequestInput(description))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).get("id").asLong();
    }

    private record UserInput(String name, String email) {
    }

    private record RequestInput(String description) {
    }

    private record ItemInput(String name, String description, boolean available, Long requestId) {
    }
}

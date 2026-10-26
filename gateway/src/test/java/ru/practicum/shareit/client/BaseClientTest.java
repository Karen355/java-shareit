package ru.practicum.shareit.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;

class BaseClientTest {
    @Test
    void forwardsUserHeaderAndResponseBody() {
        RestTemplate rest = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(rest);
        server.expect(requestTo("http://shareit/users/7"))
                .andExpect(method(GET))
                .andExpect(header("X-Sharer-User-Id", "7"))
                .andRespond(withSuccess("{\"id\":7}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = new BaseClient(rest).get("http://shareit/users/7", 7);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(7, ((Map<?, ?>) response.getBody()).get("id"));
        server.verify();
    }

    @Test
    void preservesServerErrorStatusAndBody() {
        RestTemplate rest = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(rest);
        server.expect(requestTo("http://shareit/items/9"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"Item with id 9 not found\"}"));

        ResponseEntity<Object> response = new BaseClient(rest).get("http://shareit/items/9");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("{\"error\":\"Item with id 9 not found\"}", response.getBody());
        server.verify();
    }

    @Test
    void returnsServiceUnavailableWhenServerCannotBeReached() {
        RestTemplate rest = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(rest);
        server.expect(requestTo("http://shareit/requests"))
                .andRespond(withException(new IOException("connection refused")));

        ResponseEntity<Object> response = new BaseClient(rest).get("http://shareit/requests");

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("ShareIt server unavailable", ((Map<?, ?>) response.getBody()).get("error"));
        server.verify();
    }
}

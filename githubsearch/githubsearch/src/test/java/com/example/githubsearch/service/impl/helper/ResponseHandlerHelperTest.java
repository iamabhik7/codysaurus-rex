package com.example.githubsearch.service.impl.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ResponseHandlerHelper}.
 */
class ResponseHandlerHelperTest {

    /**
     * Helper to create a ClientResponse with a JSON body.
     */
    private ClientResponse createClientResponse(String json, HttpStatus status) {
        return ClientResponse.create(status)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(json)
                .build();
    }

    @Test
    @DisplayName("handleResponse: valid response with items")
    void testHandleResponse_validItems() {
        String json = "{" +
                "\"items\":[{" +
                "\"id\":1,\"name\":\"repo1\"},{\"id\":2,\"name\":\"repo2\"}]," +
                "\"total_count\":2}";
        ClientResponse response = createClientResponse(json, HttpStatus.OK);
        StepVerifier.create(ResponseHandlerHelper.handleResponse(response))
                .assertNext(result -> {
                    assertEquals(2, result.getTotalCount());
                    assertEquals(2, result.getItems().size());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("handleResponse: empty items list")
    void testHandleResponse_emptyItems() {
        String json = "{\"items\":[],\"totalCount\":0}";
        ClientResponse response = createClientResponse(json, HttpStatus.OK);
        StepVerifier.create(ResponseHandlerHelper.handleResponse(response))
                .assertNext(result -> {
                    assertEquals(0, result.getTotalCount());
                    assertTrue(result.getItems().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("handleResponse: non-OK status propagates error")
    void testHandleResponse_nonOkStatus() {
        String json = "{}";
        ClientResponse response = createClientResponse(json, HttpStatus.BAD_REQUEST);
        StepVerifier.create(ResponseHandlerHelper.handleResponse(response))
                .expectError()
                .verify();
    }

    @Test
    @DisplayName("handleResponse: malformed JSON propagates error")
    void testHandleResponse_malformedJson() {
        String json = "{not valid json}";
        ClientResponse response = createClientResponse(json, HttpStatus.OK);
        StepVerifier.create(ResponseHandlerHelper.handleResponse(response))
                .expectError()
                .verify();
    }

}

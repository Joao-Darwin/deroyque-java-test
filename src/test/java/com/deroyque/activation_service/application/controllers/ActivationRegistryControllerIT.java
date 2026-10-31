package com.deroyque.activation_service.application.controllers;

import com.deroyque.activation_service.AbstractIntegrationTest;
import com.deroyque.activation_service.models.repositories.ActivationRegistryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ActivationRegistryControllerIT extends AbstractIntegrationTest {

    private static final String BASE_URL = "/v1/requests";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ActivationRegistryRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldCreateActivationRequestSuccessfully() throws Exception {
        String request = """
                {
                    "externalId": "external-123",
                    "customerId": "customer-123",
                    "productCode": "product-123"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.externalId").value("external-123"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        assert repository.count() == 1;
    }

    @Test
    void shouldReturnBadRequestWhenRequestHasInvalidData() throws Exception {
        String request = """
                {
                    "externalId": "",
                    "customerId": "",
                    "productCode": ""
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        assert repository.count() == 0;
    }

    @Test
    void shouldReturnBadRequestWhenRequestHasMissingFields() throws Exception {
        String request = """
                {
                    "externalId": "external-123"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        assert repository.count() == 0;
    }

    @Test
    void shouldNotCreateDuplicatedActivationWhenExternalIdAlreadyExists()
            throws Exception {

        String request = """
                {
                    "externalId": "external-123",
                    "customerId": "customer-123",
                    "productCode": "product-123"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        assert repository.count() == 1;
    }

    @Test
    void shouldReturnConflictWhenExternalIdHasDifferentRequestData()
            throws Exception {

        String firstRequest = """
                {
                    "externalId": "external-123",
                    "customerId": "customer-123",
                    "productCode": "product-123"
                }
                """;

        String secondRequest = """
                {
                    "externalId": "external-123",
                    "customerId": "customer-123",
                    "productCode": "another-product"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstRequest))
                .andExpect(status().isCreated());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondRequest))
                .andExpect(status().isConflict());

        assert repository.count() == 1;
    }

    @Test
    void shouldFindActivationRequestById() throws Exception {
        String request = """
                {
                    "externalId": "external-123",
                    "customerId": "customer-123",
                    "productCode": "product-123"
                }
                """;

        String response = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = com.jayway.jsonpath.JsonPath.read(response, "$.id");

        mockMvc.perform(get(BASE_URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.externalId").value("external-123"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldReturnNotFoundWhenActivationRequestDoesNotExist()
            throws Exception {

        mockMvc.perform(get(BASE_URL + "/" + java.util.UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnPagedActivationRequests() throws Exception {
        createRequest("external-001", "customer-001", "product-001");
        createRequest("external-002", "customer-002", "product-002");
        createRequest("external-003", "customer-003", "product-003");

        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void shouldFilterActivationRequestsByStatus() throws Exception {
        createRequest("external-001", "customer-001", "product-001");
        createRequest("external-002", "customer-002", "product-002");

        mockMvc.perform(get(BASE_URL)
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].status",
                        everyItem(is("PENDING"))));
    }

    @Test
    void shouldFilterActivationRequestsByCustomerId() throws Exception {
        createRequest("external-001", "customer-001", "product-001");
        createRequest("external-002", "customer-001", "product-002");
        createRequest("external-003", "customer-002", "product-003");

        mockMvc.perform(get(BASE_URL)
                        .param("customerId", "customer-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].customerId",
                        everyItem(is("customer-001"))));
    }

    private void createRequest(
            String externalId,
            String customerId,
            String productCode
    ) throws Exception {

        String request = """
                {
                    "externalId": "%s",
                    "customerId": "%s",
                    "productCode": "%s"
                }
                """.formatted(externalId, customerId, productCode);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldCreateOnlyOneRegistryWhenMultipleRequestsArriveSimultaneously()
            throws Exception {

        int numberOfRequests = 10;

        String request = """
            {
                "externalId": "external-concurrent",
                "customerId": "customer-123",
                "productCode": "product-123"
            }
            """;

        ExecutorService executor = Executors.newFixedThreadPool(numberOfRequests);

        CountDownLatch ready = new CountDownLatch(numberOfRequests);
        CountDownLatch start = new CountDownLatch(1);

        try {
            List<Future<Integer>> futures = new ArrayList<>();

            for (int i = 0; i < numberOfRequests; i++) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();

                    return mockMvc.perform(post(BASE_URL)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(request))
                            .andReturn()
                            .getResponse()
                            .getStatus();
                }));
            }

            ready.await();
            start.countDown();

            List<Integer> statuses = new ArrayList<>();

            for (Future<Integer> future : futures) {
                statuses.add(future.get());
            }

            assertEquals(1, repository.count());

        } finally {
            executor.shutdownNow();
        }
    }
}
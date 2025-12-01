package com.niyiment.facilityservice;

import com.niyiment.facilityservice.dto.request.CreateOrganisationRequest;
import com.niyiment.facilityservice.dto.response.OrganisationResponse;
import com.niyiment.facilityservice.enums.OrganisationType;
import com.niyiment.facilityservice.repository.OrganisationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.ExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class OrganisationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String jdbcUrl = postgres.getJdbcUrl();
        // Remove any problematic query parameters
        jdbcUrl = jdbcUrl.split("\\?")[0];

        String finalJdbcUrl = jdbcUrl;
        registry.add("spring.datasource.url", () -> finalJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        // Additional properties for better compatibility
        registry.add("spring.jpa.properties.hibernate.dialect",
                () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }


    @LocalServerPort
    private int port;

    private RestTestClient client;

    @Autowired
    private OrganisationRepository organisationRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/";
        organisationRepository.deleteAll();
        client = RestTestClient.bindToServer()
                .baseUrl(baseUrl)
                .build();
    }

    @Test
    void createState_WithValidRequest_ShouldReturn201() {
        CreateOrganisationRequest request = new CreateOrganisationRequest(
                "Test State",
                "NG-TS",
                null,
                OrganisationType.STATE,
                "Test Address"
        );

        EntityExchangeResult<OrganisationResponse> result = client.post()
                .uri("organisations")
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrganisationResponse.class)
                .returnResult();

        OrganisationResponse response = result.getResponseBody();
        assertNotNull(response);
        assertEquals("Test State", response.name());
        assertEquals(OrganisationType.STATE, response.organisationType());

    }
//
    @Test
    void createLga_WithValidStateParent_ShouldReturn201() {
        // First create a state
        CreateOrganisationRequest stateRequest = new CreateOrganisationRequest(
            "Test State",
            "NG-TS",
            null,
            OrganisationType.STATE,
            "Test Address"
        );

        EntityExchangeResult<OrganisationResponse> stateResponse = client.post()
                .uri("organisations")
                .body(stateRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrganisationResponse.class)
                .returnResult();

        UUID stateId = stateResponse.getResponseBody().id();

        // Then create an LGA
        CreateOrganisationRequest lgaRequest = new CreateOrganisationRequest(
            "Test LGA",
            "NG-TS-TL",
            stateId,
            OrganisationType.LGA,
            "Test LGA Address"
        );

      EntityExchangeResult<OrganisationResponse> result = client.post()
                .uri("organisations")
                .body(lgaRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrganisationResponse.class)
                .returnResult();

        OrganisationResponse lgaResponse = result.getResponseBody();
        assertNotNull(lgaResponse);
        assertEquals("Test LGA", lgaResponse.name());
        assertEquals(OrganisationType.LGA, lgaResponse.organisationType());
        assertEquals(stateId, lgaResponse.parentId());
    }
//
    @Test
    void createFacility_WithValidLgaParent_ShouldReturn201() {
        // Create state
        CreateOrganisationRequest stateRequest = new CreateOrganisationRequest(
            "Test State", "NG-TS", null, OrganisationType.STATE, "Test Address"
        );
        EntityExchangeResult<OrganisationResponse> stateResponse = client.post()
                .uri("organisations")
                .body(stateRequest)
                .exchange()
                .expectBody(OrganisationResponse.class)
                .returnResult();
        UUID stateId = stateResponse.getResponseBody().id();

        // Create LGA
        CreateOrganisationRequest lgaRequest = new CreateOrganisationRequest(
            "Test LGA", "NG-TS-TL", stateId, OrganisationType.LGA, "Test LGA Address"
        );
        EntityExchangeResult<OrganisationResponse> lgaResponse = client.post()
                .uri("organisations")
                .body(lgaRequest)
                .exchange()
                .expectBody(OrganisationResponse.class)
                .returnResult();
        UUID lgaId = lgaResponse.getResponseBody().id();

        // Create Facility
        CreateOrganisationRequest facilityRequest = new CreateOrganisationRequest(
            "Test Facility", "FAC-TS-TF", lgaId, OrganisationType.FACILITY, "Test Facility Address"
        );

        EntityExchangeResult<OrganisationResponse> result = client.post()
                .uri("organisations")
                .body(facilityRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrganisationResponse.class)
                .returnResult();
        OrganisationResponse facilityResponse = result.getResponseBody();

        assertNotNull(facilityResponse);
        assertEquals("Test Facility", facilityResponse.name());
        assertEquals(OrganisationType.FACILITY, facilityResponse.organisationType());
        assertEquals(lgaId, facilityResponse.parentId());
    }
//
    @Test
    void getById_WithValidId_ShouldReturn200() {
        // Create a state first
        CreateOrganisationRequest request = new CreateOrganisationRequest(
            "Test State", "NG-TS", null, OrganisationType.STATE, "Test Address"
        );
        EntityExchangeResult<OrganisationResponse> result = client.post()
                .uri("organisations")
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrganisationResponse.class)
                .returnResult();
        OrganisationResponse createResponse = result.getResponseBody();
        UUID id = createResponse.id();

        // Get by ID
        EntityExchangeResult<OrganisationResponse> response = client.get()
                .uri("organisations/" + id)
                .exchange()
                .expectBody(OrganisationResponse.class)
                .returnResult();

        assertEquals(HttpStatus.OK, response.getStatus());
        OrganisationResponse getResponse = response.getResponseBody();
        assertNotNull(getResponse);
        assertEquals("Test State", getResponse.name());
    }
//
    @Test
    void getById_WithInvalidId_ShouldReturn404() {
        UUID invalidId = UUID.randomUUID();

        ExchangeResult response = client.get()
                .uri("organisations/" + invalidId)
                .exchange()
                .expectStatus().isNotFound().returnResult();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
    }

    @Test
    void update_WithValidRequest_ShouldReturn200() {
        // Create a state first
        CreateOrganisationRequest createRequest = new CreateOrganisationRequest(
            "Test State", "NG-TS", null, OrganisationType.STATE, "Test Address"
        );
        EntityExchangeResult<OrganisationResponse> result = client.post()
                .uri("organisations")
                .body(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrganisationResponse.class)
                .returnResult();
        OrganisationResponse createResponse = result.getResponseBody();
        UUID id = createResponse.id();

        // Update it
        CreateOrganisationRequest updateRequest = new CreateOrganisationRequest(
            "Updated State", "NG-TS", null, OrganisationType.STATE, "Updated Address"
        );

        EntityExchangeResult<OrganisationResponse> response = client.put()
                .uri("organisations/" + id)
                .body(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrganisationResponse.class)
                .returnResult();

        OrganisationResponse updateResponse = response.getResponseBody();
        assertNotNull(updateResponse);
        assertEquals("Updated State", updateResponse.name());
    }

    @Test
    void delete_WithNoChildren_ShouldReturn204() {
        // Create a state first
        CreateOrganisationRequest request = new CreateOrganisationRequest(
            "Test State", "NG-TS", null, OrganisationType.STATE, "Test Address"
        );
        EntityExchangeResult<OrganisationResponse> result = client.post()
                .uri("organisations")
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrganisationResponse.class)
                .returnResult();
        OrganisationResponse createResponse = result.getResponseBody();
        UUID id = createResponse.id();

        EntityExchangeResult<Void> response = client.delete()
                .uri("organisations/" + id)
                .exchange()
                .expectBody(Void.class)
                .returnResult();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
    }

    @Test
    void getAllStates_ShouldReturnStatesOnly() {
        // Create a state and an LGA
        CreateOrganisationRequest stateRequest = new CreateOrganisationRequest(
            "Test State", "NG-TS", null, OrganisationType.STATE, "Test Address"
        );
        EntityExchangeResult<OrganisationResponse> result = client.post()
                .uri("organisations")
                .body(stateRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrganisationResponse.class)
                .returnResult();
        OrganisationResponse stateResponse = result.getResponseBody();
        UUID stateId = stateResponse.id();

        CreateOrganisationRequest lgaRequest = new CreateOrganisationRequest(
            "Test LGA", "NG-TS-TL", stateId, OrganisationType.LGA, "Test LGA Address"
        );
        client.post()
                .uri("organisations")
                .body(lgaRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrganisationResponse.class)
                .returnResult();

        // Get all states
        EntityExchangeResult<OrganisationResponse[]> response = client.get()
                .uri("organisations/states")
                .exchange()
                .expectBody(OrganisationResponse[].class)
                .returnResult();

        assertEquals(HttpStatus.OK, response.getStatus());
        OrganisationResponse[] records = response.getResponseBody();
        assertNotNull(response);
        assertTrue(records.length > 0);

        // Verify all returned items are states
        for (OrganisationResponse org : records) {
            assertEquals(OrganisationType.STATE, org.organisationType());
        }
    }
}
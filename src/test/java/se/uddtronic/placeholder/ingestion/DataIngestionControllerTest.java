package se.uddtronic.placeholder.ingestion;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "placeholder.config.file=src/test/resources/test-mocks.json"
})
public class DataIngestionControllerTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    public void testRegularMockCall() {
        ResponseEntity<String> response = restTemplate.getForEntity(getBaseUrl() + "/api/users", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"message\":\"Hello world\"");
    }

    @Test
    public void testMissingMockReturns404() {
        try {
            restTemplate.getForEntity(getBaseUrl() + "/api/missing", String.class);
            fail("Expected HttpClientErrorException.NotFound to be thrown");
        } catch (HttpClientErrorException.NotFound e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Test
    public void testPriorityFeature() {
        ResponseEntity<String> response = restTemplate.getForEntity(getBaseUrl() + "/api/priority", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"message\":\"High priority\"");
    }

    @Test
    public void testTemplateFeature() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"name\":\"Alice\"}";
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl() + "/api/template/123?myParam=paramValue", entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        assertThat(response.getBody()).contains("\"hello\":\"Alice\"");
        assertThat(response.getBody()).contains("\"var\":\"value1\"");
        assertThat(response.getBody()).contains("\"param1\":\"paramValue\"");
    }

    @Test
    public void testValidationWarn() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Invalid body (name is too short)
        String body = "{\"name\":\"Al\"}";
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl() + "/api/validated/warn", entity, String.class);
        // Should still return 200 because action is WARN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"result\":\"success\"");
    }

    @Test
    public void testValidationFail() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"name\":\"Al\"}";
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(getBaseUrl() + "/api/validated/fail", entity, String.class);
            fail("Expected 422 Unprocessable Entity");
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode().value()).isEqualTo(422);
            assertThat(e.getResponseBodyAsString()).contains("Validation failed");
        }
    }

    @Test
    public void testValidationCustomError() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"name\":\"Al\"}";
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(getBaseUrl() + "/api/validated/custom", entity, String.class);
            fail("Expected 400 Bad Request");
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(e.getResponseBodyAsString()).contains("\"my_error\":\"failed_validation\"");
        }
    }

    @Test
    public void testValidationPass() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Valid body
        String body = "{\"name\":\"Alice\"}";
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl() + "/api/validated/fail", entity, String.class);
        // Should return 200 because validation passes
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"result\":\"success\"");
    }
}

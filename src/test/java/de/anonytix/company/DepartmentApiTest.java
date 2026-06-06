package de.anonytix.company;

import static org.assertj.core.api.Assertions.assertThat;

import de.anonytix.shared.error.ApiError;
import de.anonytix.support.AbstractIntegrationTest;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

class DepartmentApiTest extends AbstractIntegrationTest {

    private static final UUID COMPANY_ID =
            UUID.fromString("10729623-735e-4382-854f-33e3450bdac7");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUpCompany() {
        cleanDatabase(jdbcTemplate);
        jdbcTemplate.update(
                """
                INSERT INTO companies(id, name, slug, minimum_group_size, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                COMPANY_ID,
                "Anonytix Demo GmbH",
                "anonytix-demo",
                5,
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now()));
    }

    @Test
    void createsAndListsDepartment() {
        ResponseEntity<DepartmentPayload> created = restTemplate.postForEntity(
                "/api/v1/companies/{companyId}/departments",
                Map.of("name", "Softwareentwicklung", "code", "DEV"),
                DepartmentPayload.class,
                COMPANY_ID);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        assertThat(created.getBody().name()).isEqualTo("Softwareentwicklung");
        assertThat(created.getBody().code()).isEqualTo("DEV");
        assertThat(created.getBody().active()).isTrue();

        ResponseEntity<DepartmentPayload[]> listed = restTemplate.getForEntity(
                "/api/v1/companies/{companyId}/departments",
                DepartmentPayload[].class,
                COMPANY_ID);

        assertThat(listed.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listed.getBody()).hasSize(1);
    }

    @Test
    void updatesDepartment() {
        DepartmentPayload created = restTemplate.postForObject(
                "/api/v1/companies/{companyId}/departments",
                Map.of("name", "Softwareentwicklung", "code", "DEV"),
                DepartmentPayload.class,
                COMPANY_ID);

        ResponseEntity<DepartmentPayload> updated = restTemplate.exchange(
                "/api/v1/companies/{companyId}/departments/{departmentId}",
                HttpMethod.PATCH,
                new HttpEntity<>(Map.of("name", "Engineering", "active", false)),
                DepartmentPayload.class,
                COMPANY_ID,
                created.id());

        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody()).isNotNull();
        assertThat(updated.getBody().name()).isEqualTo("Engineering");
        assertThat(updated.getBody().active()).isFalse();
    }

    @Test
    void rejectsDuplicateCodeInsideCompany() {
        restTemplate.postForEntity(
                "/api/v1/companies/{companyId}/departments",
                Map.of("name", "Softwareentwicklung", "code", "DEV"),
                DepartmentPayload.class,
                COMPANY_ID);

        ResponseEntity<ApiError> duplicate = restTemplate.postForEntity(
                "/api/v1/companies/{companyId}/departments",
                Map.of("name", "IT", "code", "dev"),
                ApiError.class,
                COMPANY_ID);

        assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(duplicate.getBody()).isNotNull();
        assertThat(duplicate.getBody().code()).isEqualTo("DEPARTMENT_CODE_EXISTS");
    }

    record DepartmentPayload(UUID id, String name, String code, boolean active) {
    }
}

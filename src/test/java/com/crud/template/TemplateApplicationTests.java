package com.crud.template;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TemplateApplicationTests {
    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void shouldReturnEntityTemplateWhenGetFromId() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("user_test1", "abc123")
                .getForEntity("/entities/99", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        Number id = documentContext.read("$.id");
        assertThat(id).isEqualTo(99);

        String name = documentContext.read("$.name");
        assertThat(name).isEqualTo("Dennis Richie");
    }

    @Test
    void shouldNotReturnEntityWithAnUnknownId() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("user_test1", "abc123")
                .getForEntity("/entities/1000", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isBlank();
    }

    @Test
    void shouldNotReturnEntityWhenUsingBadCredentials() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("BAD-USER", "abc123")
                .getForEntity("/entities/99", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        response = restTemplate
                .withBasicAuth("user_test1", "BAD-PASSWORD")
                .getForEntity("/entities/99", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldNotAllowAccessToEntitysTheyDoNotOwn() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("user_test1", "abc123")
                .getForEntity("/entities/102", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    void shouldCreateANewEntity() {
        EntityTemplate newEntity = new EntityTemplate(null, "Eric Evans", null);
        ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth("user_test1", "abc123")
                .postForEntity("/entities", newEntity, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfNewEntity = createResponse.getHeaders().getLocation();
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("user_test1", "abc123")
                .getForEntity(locationOfNewEntity, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        Number id = documentContext.read("$.id");
        String name = documentContext.read("$.name");

        assertThat(id).isNotNull();
        assertThat(name).isEqualTo("Eric Evans");
    }

    @Test
    void shouldReturnAllEntitiesWhenListIsRequested() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("user_test1", "abc123")
                .getForEntity("/entities", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        int entityCount = documentContext.read("$.length()");
        assertThat(entityCount).isEqualTo(3);

        JSONArray ids = documentContext.read("$..id");
        assertThat(ids).containsExactlyInAnyOrder(99, 100, 101);

        JSONArray names = documentContext.read("$..name");
        assertThat(names).containsExactlyInAnyOrder("Dennis Richie", "Ada Lovelace", "Linus Torvalds");
    }

    @Test
    void shouldReturnAPageOfEntities() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("user_test1", "abc123")
                .getForEntity("/entities?page=0&size=1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnASortedPageOfEntities() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("user_test1", "abc123")
                .getForEntity("/entities?page=0&size=1&sort=name,desc", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray read = documentContext.read("$[*]");
        assertThat(read.size()).isEqualTo(1);

        String name = documentContext.read("$[0].name");
        assertThat(name).isEqualTo("Linus Torvalds");
    }

    @Test
    @DirtiesContext
    void shouldUpdateAnExistingEntityTemplate() {
        EntityTemplate entityUpdate = new EntityTemplate(null, "Denis Capeto", null);
        HttpEntity<EntityTemplate> request = new HttpEntity<>(entityUpdate);
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("user_test1", "abc123")
                .exchange("/entities/99", HttpMethod.PUT, request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("user_test1", "abc123")
                .getForEntity("/entities/99", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        Number id = documentContext.read("$.id");
        String name = documentContext.read("$.name");
        assertThat(id).isEqualTo(99);
        assertThat(name).isEqualTo("Denis Capeto");
    }

    @Test
    void shouldNotUpdateAEntityTemplateThatDoesNotExist() {
        EntityTemplate unknownCard = new EntityTemplate(null, "Eric Evans", null);
        HttpEntity<EntityTemplate> request = new HttpEntity<>(unknownCard);
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("user_test1", "abc123")
                .exchange("/entities/99999", HttpMethod.PUT, request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldNotUpdateAEntityTemplateThatIsOwnedBySomeoneElse() {
        EntityTemplate user2ownedEntity = new EntityTemplate(null, "José Valim Elixir", null);
        HttpEntity<EntityTemplate> request = new HttpEntity<>(user2ownedEntity);
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("user_test1", "abc123")
                .exchange("/entities/102", HttpMethod.PUT, request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldNotAllowDeletionOfEntitiesTheyDoNotOwn() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("user_test1", "abc123")
                .exchange("/entities/102", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("user_test2", "xyz789")
                .getForEntity("/entities/102", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DirtiesContext
    void shouldDeleteAnExistingEntity() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("user_test1", "abc123")
                .exchange("/entities/99", HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("user_test1", "abc123")
                .getForEntity("/entities/99", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}

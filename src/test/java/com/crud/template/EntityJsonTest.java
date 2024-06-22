package com.crud.template;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class EntityJsonTest {

    @Autowired
    private JacksonTester<EntityTemplate> json;

    @Autowired
    private JacksonTester<EntityTemplate[]> jsonList;

    private EntityTemplate[] entities;

    @BeforeEach
    void setUp() {
        entities = Arrays.array(
                new EntityTemplate(99L, "Dennis Richie", "user_test1"),
                new EntityTemplate(100L, "Ada Lovelace", "user_test1"),
                new EntityTemplate(101L, "Linus Torvalds", "user_test1"));
    }

    @Test
    void entitySerializationTest() throws IOException {
        EntityTemplate entity = entities[0];
        assertThat(json.write(entity)).isStrictlyEqualToJson("single.json");
        assertThat(json.write(entity)).hasJsonPathNumberValue("@.id");
        assertThat(json.write(entity)).extractingJsonPathNumberValue("@.id")
                .isEqualTo(99);
        assertThat(json.write(entity)).hasJsonPathStringValue("@.name");
        assertThat(json.write(entity)).extractingJsonPathStringValue("@.name")
                .isEqualTo("Dennis Richie");
    }

    @Test
    void entityDeserializationTest() throws IOException {
        String expected = """
                {
                    "id": 99,
                    "name": "Dennis Richie",
                    "owner": "user_test1"
                }
                """;
        assertThat(json.parse(expected))
                .isEqualTo(new EntityTemplate(99L, "Dennis Richie", "user_test1"));
        assertThat(json.parseObject(expected).name()).isEqualTo("Dennis Richie");
    }

    @Test
    void entityListSerializationTest() throws IOException {
        assertThat(jsonList.write(entities)).isStrictlyEqualToJson("list.json");
    }

    @Test
    void entityListDeserializationTest() throws IOException {
        String expected = """
                [
                     {"id": 99, "name": "Dennis Richie", "owner": "user_test1"},
                     {"id": 100, "name": "Ada Lovelace", "owner": "user_test1"},
                     {"id": 101, "name": "Linus Torvalds", "owner": "user_test1"}
                ]
                """;
        assertThat(jsonList.parse(expected)).isEqualTo(entities);
    }
}

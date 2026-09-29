package com.icthh.xm.tmf.ms.document.web.rest.errors;

import static org.assertj.core.api.Assertions.assertThat;

import com.icthh.xm.tmf.ms.document.AbstractSpringBootTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Checks that the application {@link JsonMapper} (Jackson 3) has {@link ProblemModule} registered, so
 * error responses built by {@link ExceptionTranslator} keep their RFC 7807 shape instead of being
 * serialized as plain throwables.
 */
public class ProblemModuleIntTest extends AbstractSpringBootTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    public void problemIsSerializedAsRfc7807() {
        Problem problem = Problem.builder()
            .withType(ErrorConstants.DEFAULT_TYPE)
            .withTitle("Bad Request")
            .withStatus(Status.BAD_REQUEST)
            .withDetail("details")
            .with("message", "error.test")
            .with("path", "/api/test")
            .build();

        JsonNode json = jsonMapper.readTree(jsonMapper.writeValueAsString(problem));

        assertThat(json.get("type").asString()).isEqualTo(ErrorConstants.DEFAULT_TYPE.toString());
        assertThat(json.get("title").asString()).isEqualTo("Bad Request");
        assertThat(json.get("status").asInt()).isEqualTo(400);
        assertThat(json.get("detail").asString()).isEqualTo("details");
        assertThat(json.get("message").asString()).isEqualTo("error.test");
        assertThat(json.get("path").asString()).isEqualTo("/api/test");
        assertThat(json.has("stackTrace")).isFalse();
    }

    @Test
    public void constraintViolationProblemContainsViolations() {
        ConstraintViolationProblem problem = new ConstraintViolationProblem(Status.BAD_REQUEST,
            List.of(new Violation("field", "must not be null")));

        JsonNode json = jsonMapper.readTree(jsonMapper.writeValueAsString(problem));

        assertThat(json.get("status").asInt()).isEqualTo(400);
        assertThat(json.get("violations").get(0).get("field").asString()).isEqualTo("field");
        assertThat(json.get("violations").get(0).get("message").asString()).isEqualTo("must not be null");
    }
}

package com.icthh.xm.tmf.ms.document.web.rest.errors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.icthh.xm.tmf.ms.document.AbstractSpringBootTest;
import com.icthh.xm.tmf.ms.document.web.api.DocumentApiController;
import com.icthh.xm.tmf.ms.document.web.rest.DocumentApiImpl;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Error bodies must stay as they were before the Spring Boot 4 migration: the expected JSON below is what
 * master (Boot 2.1, zalando ProblemHandling, Jackson 2) returned for the same requests, byte for byte.
 */
public class ExceptionTranslatorIntTest extends AbstractSpringBootTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new DocumentApiController(new DocumentApiImpl()), new ErrorsController())
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(new JacksonJsonHttpMessageConverter(jsonMapper))
            .build();
    }

    @Test
    public void methodArgumentNotValid() throws Exception {
        MvcResult result = perform(post("/api/documentManagement/document")
            .contentType(MediaType.APPLICATION_JSON).content("{\"type\":\"t\",\"name\":\"x\"}"), 400);

        assertThat(body(result)).isEqualTo("{\"type\":\"https://www.jhipster.tech/problem/constraint-violation\","
            + "\"title\":\"Method argument not valid\",\"status\":400,\"path\":\"/api/documentManagement/document\","
            + "\"message\":\"error.validation\",\"fieldErrors\":[{\"objectName\":\"documentCreate\","
            + "\"field\":\"description\",\"message\":\"NotNull\"}]}");
    }

    @Test
    public void badRequestAlert() throws Exception {
        MvcResult result = perform(get("/api/errors/bad-request-alert"), 400);

        assertThat(body(result)).isEqualTo("{\"entityName\":\"document\",\"errorKey\":\"badthing\","
            + "\"type\":\"https://www.jhipster.tech/problem/problem-with-message\",\"title\":\"Bad thing\","
            + "\"status\":400,\"message\":\"error.badthing\",\"params\":\"document\"}");
        assertThat(result.getResponse().getHeader("X-documentApp-error")).isEqualTo("Bad thing");
    }

    @Test
    public void customParameterized() throws Exception {
        MvcResult result = perform(get("/api/errors/custom-parameterized"), 400);

        JsonNode json = jsonMapper.readTree(body(result));
        assertThat(json.propertyNames()).containsExactly("type", "title", "status", "message", "params");
        assertThat(json.get("type").asString()).isEqualTo("https://www.jhipster.tech/problem/parameterized");
        assertThat(json.get("title").asString()).isEqualTo("Parameterized Exception");
        assertThat(json.get("message").asString()).isEqualTo("error.custom");
        assertThat(json.get("params").get("param0").asString()).isEqualTo("p1");
        assertThat(json.get("params").get("param1").asString()).isEqualTo("p2");
    }

    @Test
    public void problemBuiltByBuilder() throws Exception {
        MvcResult result = perform(get("/api/errors/built-problem"), 400);

        assertThat(body(result)).isEqualTo("{\"type\":\"https://www.jhipster.tech/problem/problem-with-message\","
            + "\"title\":\"Built\",\"status\":400,\"detail\":\"details\",\"path\":\"/api/errors/built-problem\","
            + "\"code\":\"c1\",\"message\":\"error.http.400\"}");
    }

    @Test
    public void internalServerErrorException() throws Exception {
        assertThat(body(perform(get("/api/errors/internal"), 500))).isEqualTo(
            "{\"type\":\"https://www.jhipster.tech/problem/problem-with-message\",\"title\":\"Something broke\",\"status\":500}");
    }

    @Test
    public void noSuchElement() throws Exception {
        assertThat(body(perform(get("/api/errors/no-such-element"), 404))).isEqualTo(
            "{\"type\":\"https://www.jhipster.tech/problem/problem-with-message\",\"status\":404,"
                + "\"path\":\"/api/errors/no-such-element\",\"message\":\"https://www.jhipster.tech/problem/entity-not-found\"}");
    }

    @Test
    public void concurrencyFailure() throws Exception {
        assertThat(body(perform(get("/api/errors/concurrency"), 409))).isEqualTo(
            "{\"type\":\"https://www.jhipster.tech/problem/problem-with-message\",\"status\":409,"
                + "\"path\":\"/api/errors/concurrency\",\"message\":\"error.concurrencyFailure\"}");
    }

    @Test
    public void constraintViolation() throws Exception {
        MvcResult result = perform(get("/api/errors/constraint-violation"), 400);

        JsonNode json = jsonMapper.readTree(body(result));
        assertThat(json.propertyNames()).containsExactly("type", "title", "status", "path", "violations", "message");
        assertThat(json.get("type").asString()).isEqualTo("https://zalando.github.io/problem/constraint-violation");
        assertThat(json.get("title").asString()).isEqualTo("Constraint Violation");
        assertThat(json.get("violations").get(0).get("field").asString()).isEqualTo("field");
        assertThat(json.get("message").asString()).isEqualTo("error.validation");
    }

    @Test
    public void unexpectedException() throws Exception {
        assertThat(body(perform(get("/api/errors/illegal-argument"), 500))).isEqualTo(
            "{\"type\":\"https://www.jhipster.tech/problem/problem-with-message\",\"title\":\"Internal Server Error\","
                + "\"status\":500,\"detail\":\"bad argument\",\"path\":\"/api/errors/illegal-argument\","
                + "\"message\":\"error.http.500\"}");
    }

    @Test
    public void springMvcException() throws Exception {
        MvcResult result = perform(post("/api/documentManagement/document")
            .contentType(MediaType.TEXT_PLAIN).content("x"), 415);

        JsonNode json = jsonMapper.readTree(body(result));
        assertThat(json.propertyNames()).containsExactly("type", "title", "status", "detail", "path", "message");
        assertThat(json.get("title").asString()).isEqualTo("Unsupported Media Type");
        assertThat(json.get("message").asString()).isEqualTo("error.http.415");
    }

    private MvcResult perform(RequestBuilder request, int expectedStatus) throws Exception {
        MvcResult result = mockMvc.perform(request).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(expectedStatus);
        assertThat(result.getResponse().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        return result;
    }

    private static String body(MvcResult result) {
        return new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
    }

    @RestController
    public static class ErrorsController {
        @GetMapping("/api/errors/bad-request-alert")
        public void badRequestAlert() {
            throw new BadRequestAlertException("Bad thing", "document", "badthing");
        }

        @GetMapping("/api/errors/custom-parameterized")
        public void customParameterized() {
            throw new CustomParameterizedException("error.custom", "p1", "p2");
        }

        @GetMapping("/api/errors/built-problem")
        public void builtProblem() {
            throw Problem.builder().withTitle("Built").withStatus(Status.BAD_REQUEST).withDetail("details")
                .with("code", "c1").build();
        }

        @GetMapping("/api/errors/internal")
        public void internal() {
            throw new InternalServerErrorException("Something broke");
        }

        @GetMapping("/api/errors/no-such-element")
        public void noSuchElement() {
            throw new NoSuchElementException("nothing");
        }

        @GetMapping("/api/errors/concurrency")
        public void concurrency() {
            throw new ConcurrencyFailureException("conflict");
        }

        @GetMapping("/api/errors/constraint-violation")
        public void constraintViolation() {
            throw new ConstraintViolationException(
                Validation.buildDefaultValidatorFactory().getValidator().validate(new Bean()));
        }

        @GetMapping("/api/errors/illegal-argument")
        public void illegalArgument() {
            throw new IllegalArgumentException("bad argument");
        }
    }

    public static class Bean {
        @NotNull
        public String field;
    }
}

package com.icthh.xm.tmf.ms.document.web.rest.errors;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Problem;
import org.zalando.problem.StatusType;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Set;

/**
 * Jackson 3 module that serializes Zalando {@link Problem} instances following RFC 7807.
 *
 * <p>It replaces the Jackson 2 modules shipped by {@code problem-spring-web}
 * ({@code ProblemModule} and {@code ConstraintViolationProblemModule}), which are not compatible
 * with the {@code tools.jackson} (Jackson 3) mapper used after the Spring Boot 4 migration.
 * Without it {@link Problem} objects (which extend {@link Throwable}) are serialized as plain
 * throwables and lose their RFC 7807 fields and custom parameters, and
 * {@link ConstraintViolationProblem}/{@link Violation} lose their {@code violations} payload.
 */
public class ProblemModule extends SimpleModule {

    /** RFC 7807 fields written by {@link #writeProblemFields}; custom parameters must not shadow them. */
    private static final Set<String> RESERVED_FIELDS = Set.of("type", "title", "status", "detail", "instance");

    public ProblemModule() {
        super("ZalandoProblemModule");
        addSerializer(Problem.class, new ProblemSerializer());
        addSerializer(ConstraintViolationProblem.class, new ConstraintViolationProblemSerializer());
        addSerializer(Violation.class, new ViolationSerializer());
    }

    private static void writeProblemFields(Problem problem, JsonGenerator gen) {
        URI type = problem.getType();
        if (type != null && !Problem.DEFAULT_TYPE.equals(type)) {
            gen.writeStringProperty("type", type.toString());
        }
        if (problem.getTitle() != null) {
            gen.writeStringProperty("title", problem.getTitle());
        }
        StatusType status = problem.getStatus();
        if (status != null) {
            gen.writeNumberProperty("status", status.getStatusCode());
        }
        if (problem.getDetail() != null) {
            gen.writeStringProperty("detail", problem.getDetail());
        }
        if (problem.getInstance() != null) {
            gen.writeStringProperty("instance", problem.getInstance().toString());
        }
    }

    /**
     * The Jackson 2 zalando module serialized problems as beans, so properties exposed by a problem subclass
     * (e.g. {@code entityName} and {@code errorKey} of {@code BadRequestAlertException}) were part of the body.
     * Written first, in field declaration order, as the old module did.
     */
    private static void writeSubclassProperties(Problem problem, JsonGenerator gen) {
        Deque<Class<?>> hierarchy = new ArrayDeque<>();
        for (Class<?> type = problem.getClass();
             type != null && type != AbstractThrowableProblem.class && type != Throwable.class;
             type = type.getSuperclass()) {
            hierarchy.push(type);
        }
        for (Class<?> type : hierarchy) {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || RESERVED_FIELDS.contains(field.getName())
                    || problem.getParameters().containsKey(field.getName())) {
                    continue;
                }
                Method getter = findGetter(type, field.getName());
                if (getter != null) {
                    try {
                        gen.writePOJOProperty(field.getName(), getter.invoke(problem));
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("Cannot read problem property " + field.getName(), e);
                    }
                }
            }
        }
    }

    private static Method findGetter(Class<?> type, String name) {
        String suffix = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        for (String prefix : new String[]{"get", "is"}) {
            try {
                Method method = type.getMethod(prefix + suffix);
                return Modifier.isPublic(method.getModifiers()) ? method : null;
            } catch (NoSuchMethodException ignored) {
                // try the next prefix
            }
        }
        return null;
    }

    static class ProblemSerializer extends ValueSerializer<Problem> {

        @Override
        public void serialize(Problem problem, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
            gen.writeStartObject();
            writeSubclassProperties(problem, gen);
            writeProblemFields(problem, gen);
            for (Map.Entry<String, Object> entry : problem.getParameters().entrySet()) {
                if (!RESERVED_FIELDS.contains(entry.getKey())) {
                    gen.writePOJOProperty(entry.getKey(), entry.getValue());
                }
            }
            gen.writeEndObject();
        }
    }

    static class ConstraintViolationProblemSerializer extends ValueSerializer<ConstraintViolationProblem> {

        @Override
        public void serialize(ConstraintViolationProblem problem, JsonGenerator gen, SerializationContext ctxt)
            throws JacksonException {
            gen.writeStartObject();
            writeProblemFields(problem, gen);
            gen.writePOJOProperty("violations", problem.getViolations());
            gen.writeEndObject();
        }
    }

    static class ViolationSerializer extends ValueSerializer<Violation> {

        @Override
        public void serialize(Violation violation, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
            gen.writeStartObject();
            gen.writeStringProperty("field", violation.getField());
            gen.writeStringProperty("message", violation.getMessage());
            gen.writeEndObject();
        }
    }
}

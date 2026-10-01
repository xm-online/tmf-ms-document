package com.icthh.xm.tmf.ms.document.web.rest.errors;

import com.icthh.xm.tmf.ms.document.web.rest.util.HeaderUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.DefaultProblem;
import org.zalando.problem.Problem;
import org.zalando.problem.StatusType;
import org.zalando.problem.ThrowableProblem;

/**
 * Controller advice to translate the server side exceptions to client-friendly json structures.
 * The error response follows RFC7807 - Problem Details for HTTP APIs (https://tools.ietf.org/html/rfc7807).
 *
 * <p>The bodies are the ones the zalando {@code ProblemHandling} advice produced before the Spring Boot 4
 * migration: zalando problem-spring-web only supports Jackson 2, so the body is written as an ordered map
 * instead of serializing {@link Problem} (a {@link Throwable}) with the Jackson 3 mapper. The zalando
 * exception types are kept, LEP scripts can still throw them.
 */
@Slf4j
@ControllerAdvice
public class ExceptionTranslator extends ResponseEntityExceptionHandler {

    private static final String FIELD_ERRORS_KEY = "fieldErrors";
    private static final String MESSAGE_KEY = "message";
    private static final String PATH_KEY = "path";
    private static final String VIOLATIONS_KEY = "violations";

    private static final URI ZALANDO_CONSTRAINT_VIOLATION_TYPE =
        URI.create("https://zalando.github.io/problem/constraint-violation");
    /** Problem fields written by {@link #writeProblemFields}; bean properties must not shadow them. */
    private static final Set<String> PROBLEM_FIELDS = Set.of("type", "title", "status", "detail", "instance",
        "parameters", "cause", "stackTrace", "suppressed", "message", "localizedMessage");

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        BindingResult result = ex.getBindingResult();
        List<FieldErrorVM> fieldErrors = result.getFieldErrors().stream()
            .map(f -> new FieldErrorVM(f.getObjectName(), f.getField(), f.getCode()))
            .toList();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", ErrorConstants.CONSTRAINT_VIOLATION_TYPE.toString());
        body.put("title", "Method argument not valid");
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put(PATH_KEY, path(request));
        body.put(MESSAGE_KEY, ErrorConstants.ERR_VALIDATION);
        body.put(FIELD_ERRORS_KEY, fieldErrors);
        return respond(body, HttpStatus.BAD_REQUEST, headers);
    }

    /**
     * Spring MVC exceptions (405, 415, unreadable body, missing parameter, ...).
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        return respond(withMessage(statusCode.value(), reasonPhrase(statusCode.value()), ex.getMessage(), request),
            statusCode, headers);
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleProblem(ThrowableProblem problem, NativeWebRequest request) {
        HttpHeaders headers = new HttpHeaders();
        if (problem instanceof BadRequestAlertException alert) {
            headers.addAll(HeaderUtil.createFailureAlert(alert.getEntityName(), alert.getErrorKey(), alert.getMessage()));
        }
        int status = problem.getStatus() == null ? HttpStatus.INTERNAL_SERVER_ERROR.value()
            : problem.getStatus().getStatusCode();
        if (problem instanceof DefaultProblem) {
            // a problem built with Problem.builder(): completed with path and message like any other error
            Map<String, Object> body = new LinkedHashMap<>();
            writeProblemFields(problem, body, true);
            body.put(PATH_KEY, path(request));
            body.putAll(problem.getParameters());
            if (!problem.getParameters().containsKey(MESSAGE_KEY) && problem.getStatus() != null) {
                body.put(MESSAGE_KEY, "error.http." + status);
            }
            return respond(body, HttpStatusCode.valueOf(status), headers);
        }
        // own problem types (BadRequestAlertException, CustomParameterizedException, ...) are written as they are
        Map<String, Object> body = new LinkedHashMap<>();
        writeBeanProperties(problem, body);
        writeProblemFields(problem, body, false);
        problem.getParameters().forEach((key, value) -> {
            if (!PROBLEM_FIELDS.contains(key) || MESSAGE_KEY.equals(key)) {
                body.put(key, value);
            }
        });
        return respond(body, HttpStatusCode.valueOf(status), headers);
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, NativeWebRequest request) {
        List<Map<String, Object>> violations = ex.getConstraintViolations().stream()
            .map(ExceptionTranslator::toViolation)
            .toList();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", ZALANDO_CONSTRAINT_VIOLATION_TYPE.toString());
        body.put("title", "Constraint Violation");
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put(PATH_KEY, path(request));
        body.put(VIOLATIONS_KEY, violations);
        body.put(MESSAGE_KEY, ErrorConstants.ERR_VALIDATION);
        return respond(body, HttpStatus.BAD_REQUEST, new HttpHeaders());
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleNoSuchElementException(NoSuchElementException ex, NativeWebRequest request) {
        return respond(withoutTitle(HttpStatus.NOT_FOUND, ErrorConstants.ENTITY_NOT_FOUND_TYPE.toString(), request),
            HttpStatus.NOT_FOUND, new HttpHeaders());
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleConcurrencyFailure(ConcurrencyFailureException ex, NativeWebRequest request) {
        return respond(withoutTitle(HttpStatus.CONFLICT, ErrorConstants.ERR_CONCURRENCY_FAILURE, request),
            HttpStatus.CONFLICT, new HttpHeaders());
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, NativeWebRequest request) {
        return respond(withMessage(HttpStatus.FORBIDDEN.value(), reasonPhrase(HttpStatus.FORBIDDEN.value()),
            ex.getMessage(), request), HttpStatus.FORBIDDEN, new HttpHeaders());
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleThrowable(Exception ex, NativeWebRequest request) {
        log.error("Internal Server Error", ex);
        int status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        return respond(withMessage(status, reasonPhrase(status), ex.getMessage(), request),
            HttpStatus.INTERNAL_SERVER_ERROR, new HttpHeaders());
    }

    private static Map<String, Object> withMessage(int status, String title, @Nullable String detail, Object request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", ErrorConstants.DEFAULT_TYPE.toString());
        body.put("title", title);
        body.put("status", status);
        if (detail != null) {
            body.put("detail", detail);
        }
        body.put(PATH_KEY, path(request));
        body.put(MESSAGE_KEY, "error.http." + status);
        return body;
    }

    private static Map<String, Object> withoutTitle(HttpStatus status, String message, NativeWebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", ErrorConstants.DEFAULT_TYPE.toString());
        body.put("status", status.value());
        body.put(PATH_KEY, path(request));
        body.put(MESSAGE_KEY, message);
        return body;
    }

    private static void writeProblemFields(Problem problem, Map<String, Object> body, boolean defaultType) {
        URI type = problem.getType();
        if (defaultType && (type == null || Problem.DEFAULT_TYPE.equals(type))) {
            type = ErrorConstants.DEFAULT_TYPE;
        }
        if (type != null && !Problem.DEFAULT_TYPE.equals(type)) {
            body.put("type", type.toString());
        }
        if (problem.getTitle() != null) {
            body.put("title", problem.getTitle());
        }
        StatusType status = problem.getStatus();
        if (status != null) {
            body.put("status", status.getStatusCode());
        }
        if (problem.getDetail() != null) {
            body.put("detail", problem.getDetail());
        }
        if (problem.getInstance() != null) {
            body.put("instance", problem.getInstance().toString());
        }
    }

    /**
     * The Jackson 2 zalando module serialized problems as beans, so properties exposed by a problem subclass
     * (e.g. {@code entityName} and {@code errorKey} of {@link BadRequestAlertException}) were part of the body,
     * first and in field declaration order.
     */
    private static void writeBeanProperties(Problem problem, Map<String, Object> body) {
        Deque<Class<?>> hierarchy = new ArrayDeque<>();
        for (Class<?> type = problem.getClass();
             type != null && type != AbstractThrowableProblem.class && type != Throwable.class;
             type = type.getSuperclass()) {
            hierarchy.push(type);
        }
        for (Class<?> type : hierarchy) {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || PROBLEM_FIELDS.contains(field.getName())
                    || problem.getParameters().containsKey(field.getName())) {
                    continue;
                }
                Method getter = findGetter(type, field.getName());
                if (getter != null) {
                    try {
                        body.put(field.getName(), getter.invoke(problem));
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

    private static Map<String, Object> toViolation(ConstraintViolation<?> violation) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("field", violation.getPropertyPath().toString());
        result.put("message", violation.getMessage());
        return result;
    }

    private static String reasonPhrase(int status) {
        HttpStatus resolved = HttpStatus.resolve(status);
        return resolved == null ? null : resolved.getReasonPhrase();
    }

    private static String path(Object request) {
        if (request instanceof NativeWebRequest webRequest) {
            HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
            if (servletRequest != null) {
                return servletRequest.getRequestURI();
            }
        }
        return null;
    }

    private static ResponseEntity<Object> respond(Map<String, Object> body, HttpStatusCode status, HttpHeaders headers) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.addAll(headers);
        responseHeaders.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        return new ResponseEntity<>(body, responseHeaders, status);
    }
}

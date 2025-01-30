package com.icthh.xm.tmf.ms.document.config;

import com.icthh.xm.commons.lep.TenantScriptStorage;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Document.
 * <p>
 * Properties are configured in the application.yml file.
 * See {@link io.github.jhipster.config.JHipsterProperties} for a good example.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private boolean kafkaEnabled;
    private String kafkaSystemTopic;
    private String kafkaSystemQueue;
    private boolean timelinesEnabled;
    private String dbSchemaSuffix;
    private int retryThreadCount;

    private final Lep lep = new Lep();
    private final Retry retry = new Retry();
    private List<String> tenantIgnoredPathList = Collections.emptyList();
    private final DocumentGeneration documentGeneration = new DocumentGeneration();

    private Carbone carbone = new Carbone();

    @Getter
    @Setter
    public static class Lep {
        private TenantScriptStorage tenantScriptStorage;
        private String lepResourcePathPattern;
    }

    @Getter
    @Setter
    private static class Retry {
        private int maxAttempts;
        private long delay;
        private int multiplier;
    }

    @Getter
    @Setter
    public static class DocumentGeneration {
        private String specificationPathPattern;
        private String jasperTemplatesPathPattern;
        private String carboneTemplatesPathPattern;
    }

    @Getter
    @Setter
    public static class Carbone {
        private String apiVersionKey;
        private String apiVersionValue;
    }

}

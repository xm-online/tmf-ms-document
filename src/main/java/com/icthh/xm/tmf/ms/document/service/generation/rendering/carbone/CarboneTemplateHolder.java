package com.icthh.xm.tmf.ms.document.service.generation.rendering.carbone;

import com.icthh.xm.commons.config.client.api.RefreshableConfiguration;
import com.icthh.xm.commons.tenant.TenantContextHolder;
import com.icthh.xm.commons.tenant.TenantContextUtils;
import com.icthh.xm.tmf.ms.document.config.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.icthh.xm.tmf.ms.document.config.Constants.TENANT_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;

@Component
@RequiredArgsConstructor
@Slf4j
public class CarboneTemplateHolder implements RefreshableConfiguration {

    private static final String FILENAME_PATTERN_VAR_NAME = "filename";

    private final TenantContextHolder tenantContextHolder;
    private final ApplicationProperties applicationProperties;

    private final Map<String, Map<String, byte[]>> tenantCarboneTemplateMap = new ConcurrentHashMap<>();
    private final AntPathMatcher matcher = new AntPathMatcher();

    @Value("${application.document-generation.carbone-templates-path-pattern}")
    private String carboneTemplatesPathPattern;

    /**
     * Get JasperReports template bytes by key.
     *
     * @param key the specification key
     * @return bytes of a templates
     * @throws IllegalArgumentException if template found by key
     */
    public byte[] getTemplateByKey(String key) {
        byte[] templateBytes = getTenantTemplates().get(key);
        if (templateBytes == null) {
            throw new IllegalArgumentException("Template not found for key: " + key);
        }
        return templateBytes;
    }

    @Override
    public void onInit(String configKey, String configValue) {
        onRefresh(configKey, configValue);
    }

    @Override
    public void onRefresh(String key, String config) {
        String docKey = extractFromConfigKey(key, FILENAME_PATTERN_VAR_NAME).toUpperCase();

        try {
            doRefresh(key, config);
        } catch (Exception ex) {
            log.warn("Unable to process carbone template: {}, error: {}", docKey, ex.getMessage());
        }
    }

    private void doRefresh(String key, String config) {
        String docKey = extractFromConfigKey(key, FILENAME_PATTERN_VAR_NAME).toUpperCase();
        String tenant = extractFromConfigKey(key, TENANT_NAME);

        if (StringUtils.isBlank(config)) {
            tenantCarboneTemplateMap.computeIfPresent(tenant, (t, templates) -> {
                templates.remove(docKey);
                return templates;
            });
            log.info("Template '{}' for tenant {} was removed", docKey, tenant);
        } else {
            tenantCarboneTemplateMap.compute(tenant, (t, templates) -> {
                templates = templates == null ? new HashMap<>() : templates;
                templates.put(docKey, compileCarboneTemplate(key, config));
                return templates;
            });
            log.info("Template '{}' for tenant {} was updated", docKey, tenant);
        }
    }

    private byte[] compileCarboneTemplate(String key, String config) {
        List<String> binaryFileTypes = applicationProperties.getBinaryFileTypes();

        byte[] compiledTemplate = config.getBytes(UTF_8);

        if (binaryFileTypes.stream().anyMatch(key::endsWith)) {
            compiledTemplate = Base64.getDecoder().decode(config);
        }

        return  compiledTemplate;
    }

    private Map<String, byte[]> getTenantTemplates() {
        String tenantKeyValue = getTenantKeyValue();
        Map<String, byte[]> templates = tenantCarboneTemplateMap.get(tenantKeyValue);
        if (MapUtils.isEmpty(templates)) {
            return Collections.emptyMap();
        }
        return templates;
    }

    private String getTenantKeyValue() {
        return TenantContextUtils.getRequiredTenantKeyValue(tenantContextHolder);
    }

    private String extractFromConfigKey(String key, String varName) {
        return matcher
            .extractUriTemplateVariables(carboneTemplatesPathPattern, key)
            .get(varName);
    }

    @Override
    public boolean isListeningConfiguration(String updatedKey) {
        return matcher.match(carboneTemplatesPathPattern, updatedKey);
    }
}

package com.icthh.xm.tmf.ms.document.service.generation.rendering.jasper;

import static com.icthh.xm.commons.config.client.repository.TenantConfigRepository.TENANT_NAME;

import com.icthh.xm.commons.config.client.api.RefreshableConfiguration;
import com.icthh.xm.commons.tenant.TenantContextHolder;
import com.icthh.xm.commons.tenant.TenantContextUtils;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

@Component
@RequiredArgsConstructor
@Slf4j
public class JasperReportsTemplateHolder implements RefreshableConfiguration {

    private static final String FILENAME_PATTERN_VAR_NAME = "filename";
    private final TenantContextHolder tenantContextHolder;

    @Value("${application.document-generation.jasper-templates-path-pattern}")
    private String jasperTemplatesPathPattern;

    private final Map<String, Map<String, byte[]>> tenantJasperTemplateMap = new ConcurrentHashMap<>();

    private final AntPathMatcher matcher = new AntPathMatcher();

    /**
     * Get JasperReports template bytes by key.
     *
     * @param key the specification key
     * @return bytes of a templates
     * @throws IllegalArgumentException if template found by key
     */
    public byte[] getJasperTemplateByKey(String key) {
        byte[] jasperTemplateBytes = getTenantTemplates().get(key);
        if (jasperTemplateBytes == null) {
            throw new IllegalArgumentException("JasperReports template not found for key: " + key);
        }
        return jasperTemplateBytes;
    }

    @Override
    public void onInit(String configKey, String configValue) {
        onRefresh(configKey, configValue);
    }

    @Override
    public void onRefresh(String key, String config) {
        String tenant = extractFromConfigKey(key, TENANT_NAME);
        String filename = extractFromConfigKey(key, FILENAME_PATTERN_VAR_NAME);
        String docKey = filename.toUpperCase();

        if (StringUtils.isBlank(config)) {
            tenantJasperTemplateMap.computeIfPresent(tenant, (t, templates) -> {
                templates.remove(docKey);
                return templates;
            });
            log.info("JasperReports template '{}' for tenant {} was removed", docKey, tenant);
        } else {
            tenantJasperTemplateMap.compute(tenant, (t, templates) -> {
                templates = templates == null ? new HashMap<>() : templates;
                byte[] compiledTemplate = compileJrxml(docKey, config);
                if (compiledTemplate != null) {
                    templates.put(docKey, compiledTemplate);
                } else {
                    templates.remove(docKey);
                }
                return templates;
            });
            log.info("JasperReports template '{}' for tenant {} was updated", docKey, tenant);
        }
    }

    private byte[] compileJrxml(String docKey, String config) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JasperCompileManager.compileReportToStream(
                IOUtils.toInputStream(config, StandardCharsets.UTF_8), outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Failed to compile JasperReports template of document key={}", docKey, e);
        }
        return null;
    }

    private String extractFromConfigKey(String key, String varName) {
        return matcher
            .extractUriTemplateVariables(jasperTemplatesPathPattern, key)
            .get(varName);
    }

    private Map<String, byte[]> getTenantTemplates() {
        String tenantKeyValue = getTenantKeyValue();
        Map<String, byte[]> templates = tenantJasperTemplateMap.get(tenantKeyValue);
        if (MapUtils.isEmpty(templates)) {
            return Collections.emptyMap();
        }
        return templates;
    }

    private String getTenantKeyValue() {
        return TenantContextUtils.getRequiredTenantKeyValue(tenantContextHolder);
    }

    @Override
    public boolean isListeningConfiguration(String updatedKey) {
        return matcher.match(jasperTemplatesPathPattern, updatedKey);
    }
}

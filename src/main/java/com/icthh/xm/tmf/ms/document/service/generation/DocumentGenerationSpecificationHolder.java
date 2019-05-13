package com.icthh.xm.tmf.ms.document.service.generation;

import static com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance;
import static com.icthh.xm.commons.config.client.repository.TenantConfigRepository.TENANT_NAME;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.icthh.xm.commons.config.client.api.RefreshableConfiguration;
import com.icthh.xm.commons.tenant.TenantContextHolder;
import com.icthh.xm.commons.tenant.TenantContextUtils;
import com.icthh.xm.tmf.ms.document.web.rest.errors.BadRequestAlertException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

/**
 * Holder of specifications for document generation.
 * Implements {@link RefreshableConfiguration} and keeps all specifications up to date.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DocumentGenerationSpecificationHolder implements RefreshableConfiguration {

    private final TenantContextHolder tenantContextHolder;

    @Value("${application.document-generation.specification-path-pattern}")
    private String specificationPathPattern;

    private final Map<String, Map<String, DocumentGenerationSpec>> tenantSpecificationMap = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
    private final AntPathMatcher matcher = new AntPathMatcher();

    /**
     * Get specification by key.
     *
     * @param key the specification key
     * @return found document generation specification by a key
     * @throws IllegalArgumentException if no specification found by a key
     */
    public DocumentGenerationSpec getSpecificationByKey(String key) {
        DocumentGenerationSpec spec = getTenantSpecifications().get(key);
        if (spec == null) {
            throw new IllegalArgumentException("Document generation specification not found for key: " + key);
        }
        return spec;
    }

    @Override
    public void onInit(String configKey, String configValue) {
        onRefresh(configKey, configValue);
    }

    @Override
    public void onRefresh(String key, String config) {
        String tenant = matcher
            .extractUriTemplateVariables(specificationPathPattern, key)
            .get(TENANT_NAME);

        if (StringUtils.isBlank(config)) {
            tenantSpecificationMap.remove(tenant);
            log.info("Document generation specification for tenant {} was removed", tenant);
        } else {
            tenantSpecificationMap.put(tenant, yamlConfigToSpecMap(key, config));
            log.info("Document generation specification for tenant {} was updated", tenant);
        }
    }

    private Map<String, DocumentGenerationSpec> yamlConfigToSpecMap(String key, String config) {
        MapType type = defaultInstance().constructMapType(HashMap.class,
            defaultInstance().constructType(String.class),
            defaultInstance().constructType(DocumentGenerationSpec.class));
        try {
            return objectMapper.readValue(config, type);
        } catch (IOException e) {
            log.error("Failed to read document generation specification from YAML config file: {}",
                key, e);
        }
        return Collections.emptyMap();
    }

    private Map<String, DocumentGenerationSpec> getTenantSpecifications() {
        String tenantKeyValue = getTenantKeyValue();
        Map<String, DocumentGenerationSpec> specMap = tenantSpecificationMap.get(tenantKeyValue);
        if (MapUtils.isEmpty(specMap)) {
            return Collections.emptyMap();
        }
        specMap.forEach((k, v) -> v.setKey(k));
        return specMap;
    }

    private String getTenantKeyValue() {
        return TenantContextUtils.getRequiredTenantKeyValue(tenantContextHolder);
    }

    @Override
    public boolean isListeningConfiguration(String updatedKey) {
        return matcher.match(specificationPathPattern, updatedKey);
    }
}

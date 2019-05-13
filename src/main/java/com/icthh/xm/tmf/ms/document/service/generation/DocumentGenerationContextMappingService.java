package com.icthh.xm.tmf.ms.document.service.generation;

import com.icthh.xm.commons.lep.LogicExtensionPoint;
import com.icthh.xm.commons.lep.spring.LepService;
import com.icthh.xm.tmf.ms.document.service.generation.resolver.DocumentGenerationMappingKeyResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Service for mapping context data of a document to the data representation for document
 * rendering.
 */
@Component
@LepService(group = "mapper")
@Slf4j
public class DocumentGenerationContextMappingService {

    /**
     * Map a document context to data for rendering. Execute LEP by a specific {@code key}. If no
     * LEP found return context without mapping.
     *
     * @param key of a document specification
     * @param context document context
     * @return processed data ready for document rendering
     */
    @LogicExtensionPoint(value = "DocumentContextMapping",
        resolver = DocumentGenerationMappingKeyResolver.class)
    public Object toRenderingData(String key, Object context) {
        log.debug("No document context mapping found for key '{}'. Proceed with original document context", key);
        return context;
    }
}

package com.icthh.xm.tmf.ms.document.service.generation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Factory for {@link DocumentRenderer}.
 */
@Component
public class DocumentRendererFactory {

    private final Map<DocumentRendererType, DocumentRenderer> renderers;

    public DocumentRendererFactory(List<DocumentRenderer> documentRenderers) {
        renderers = buildRendererMap(documentRenderers);
    }

    /**
     * Get renderer of a specific type.
     *
     * @param type type of a renderer
     * @return renderer of a specific type
     */
    public DocumentRenderer getRenderer(DocumentRendererType type) {
        DocumentRenderer renderer = renderers.get(type);
        if (renderer == null) {
            throw new IllegalStateException("Renderer implementation not found for type: " + type);
        }
        return renderer;
    }

    private Map<DocumentRendererType, DocumentRenderer> buildRendererMap(List<DocumentRenderer> documentRenderers) {
        Map<DocumentRendererType, DocumentRenderer> renderersByTypeMap = documentRenderers.stream()
            .collect(Collectors.toMap(DocumentRenderer::getType, Function.identity()));
        return Collections.unmodifiableMap(renderersByTypeMap);
    }
}

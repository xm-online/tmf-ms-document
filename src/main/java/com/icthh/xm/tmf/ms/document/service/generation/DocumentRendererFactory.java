package com.icthh.xm.tmf.ms.document.service.generation;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.List;
import java.util.Map;
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
        Builder<DocumentRendererType, DocumentRenderer> mapBuilder = ImmutableMap.builder();
        DocumentRendererType[] types = DocumentRendererType.values();
        for (DocumentRendererType type : types) {
            for (DocumentRenderer renderer : documentRenderers) {
                if (type.equals(renderer.getType())) {
                    mapBuilder.put(type, renderer);
                }
            }
        }
        return mapBuilder.build();
    }
}

package com.icthh.xm.tmf.ms.document.service.generation;

import com.icthh.xm.commons.logging.aop.IgnoreLogginAspect;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

/**
 * Service for rendering documents.
 */
@Service
@RequiredArgsConstructor
public class DocumentRenderingService {

    private final DocumentRendererFactory rendererFactory;

    /**
     * Render document with a specific renderer implementation.
     *
     * @param rendererType type of renderer to use
     * @param resultMediaType result document media type
     * @param data data for rendering
     * @return rendered document in binary representation
     */
    @IgnoreLogginAspect
    public byte[] render(String key, DocumentRendererType rendererType, MediaType resultMediaType, Object data) {
        DocumentRenderer renderer = rendererFactory.getRenderer(rendererType);
        if (!renderer.supportsMediaType(resultMediaType)) {
            throw new IllegalArgumentException(String.format("Renderer of type %s doesn't support '%s' media type",
                rendererType.name(), resultMediaType.toString()));
        }
        return renderer.render(key, resultMediaType, data);
    }
}

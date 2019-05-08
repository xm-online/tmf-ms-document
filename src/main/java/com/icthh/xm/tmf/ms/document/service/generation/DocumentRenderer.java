package com.icthh.xm.tmf.ms.document.service.generation;

import com.icthh.xm.tmf.ms.document.service.generation.rendering.exception.DocumentRenderingException;
import org.springframework.http.MediaType;

/**
 * Interface for rendering documents.
 */
public interface DocumentRenderer {

    /**
     * Render document of {@code mediaType} by specific {@code key} with {@code data} content.
     *
     * @param key a key of document generation specification
     * @param mediaType mime type of the generated document
     * @param data content of the document
     * @return generated document in bytes representation
     * @throws DocumentRenderingException if error occurs during document rendering
     */
    byte[] render(String key, MediaType mediaType, Object data) throws DocumentRenderingException;

    /**
     * Return whether the renderer able to render to {@code mediaType} format.
     *
     * @param mediaType target media type
     * @return boolean value
     */
    default boolean supportsMediaType(MediaType mediaType) {
        return getType().getSupportedMimeTypes().contains(mediaType);
    }

    /**
     * Get enum type of the renderer.
     *
     * @return enum type of the renderer
     */
    DocumentRendererType getType();
}

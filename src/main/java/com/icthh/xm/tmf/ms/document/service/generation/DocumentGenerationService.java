package com.icthh.xm.tmf.ms.document.service.generation;

import static com.icthh.xm.tmf.ms.document.service.generation.DocumentGenerationUtils.buildDocumentFilename;

import com.icthh.xm.tmf.ms.document.web.rest.errors.InternalServerErrorException;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

/**
 * Service for documents generation by a specific key described in the document generation
 * specifications.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentGenerationService {

    private final DocumentGenerationContextMappingService mappingService;
    private final DocumentRenderingService renderingService;
    private final DocumentGenerationSpecificationHolder specificationHolder;

    /**
     * Generate document of a specific type by a specification key.
     *
     * @param key the specification key
     * @param context context of a document
     * @param documentMimeType mime type of a result document. If not specified, default mime type
     * from the specification is used.
     * @return generated document
     */
    public GeneratedDocument generate(String key, Object context, @Nullable MediaType documentMimeType) {
        DocumentGenerationSpec spec = specificationHolder.getSpecificationByKey(key);
        checkExpectedDocumentTypeSupported(documentMimeType, spec);
        MediaType contentType = documentMimeType == null ? spec.getDefaultDocumentMimeType() : documentMimeType;

        Object renderingData = processDocumentContext(key, context);
        byte[] documentBytes = renderDocument(key, spec, contentType, renderingData);

        return GeneratedDocument.builder()
            .filename(buildDocumentFilename(key))
            .contentType(contentType)
            .documentByteResource(new ByteArrayResource(documentBytes))
            .build();
    }

    private byte[] renderDocument(String key, DocumentGenerationSpec spec, MediaType contentType,
        Object renderingData) {
        byte[] documentBytes;
        try {
            documentBytes = renderingService.render(key, spec, contentType, renderingData);
        } catch (Exception e) {
            String msg = String.format("Failed to render document with key = '%s', mime type = '%s'", key, contentType.toString());
            log.error(msg, e);
            throw new InternalServerErrorException(msg + ". " + e.getMessage());
        }
        return documentBytes;
    }

    private Object processDocumentContext(String key, Object context) {
        try {
            return mappingService.toRenderingData(key, context);
        } catch (Exception e) {
            String msg = "Failed to process document context with key '" + key + "'";
            log.error(msg, e);
            throw new InternalServerErrorException(msg + ". " + e.getMessage());
        }
    }

    private void checkExpectedDocumentTypeSupported(@Nullable MediaType documentMimeType, DocumentGenerationSpec spec) {
        if (documentMimeType != null &&
            !spec.getAllowedDocumentMimeTypes().contains(documentMimeType)) {
            throw new IllegalArgumentException(documentMimeType + " mime type not supported by specification with key " + spec.getKey());
        }
    }
}

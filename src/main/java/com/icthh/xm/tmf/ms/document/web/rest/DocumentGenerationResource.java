package com.icthh.xm.tmf.ms.document.web.rest;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.icthh.xm.tmf.ms.document.service.converter.MediaTypeConverter;
import com.icthh.xm.tmf.ms.document.service.generation.DocumentGenerationService;
import com.icthh.xm.tmf.ms.document.service.generation.GeneratedDocument;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documents/generate")
@RequiredArgsConstructor
public class DocumentGenerationResource {

    private final DocumentGenerationService documentGenerationService;

    @PostMapping
    @PreAuthorize("hasPermission({}, 'DOCUMENT.GENERATE')")
    public ResponseEntity<ByteArrayResource> generateDocument(@RequestBody @Valid GenerateDocumentRequest request) {
        GeneratedDocument document = documentGenerationService
            .generate(request.getKey(), request.getContext(), request.getDocumentMimeType());
        return ResponseEntity.ok()
            .headers(createDocumentHeaders(document))
            .contentLength(document.getDocumentByteResource().contentLength())
            .body(document.getDocumentByteResource());
    }

    private static HttpHeaders createDocumentHeaders(GeneratedDocument document) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, document.getContentType().toString());
        httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + document.getFilename());
        return httpHeaders;
    }

    @Data
    public static class GenerateDocumentRequest {

        @NotBlank
        private String key;

        @NotNull
        private Object context;

        @JsonDeserialize(converter = MediaTypeConverter.class)
        private MediaType documentMimeType;
    }
}

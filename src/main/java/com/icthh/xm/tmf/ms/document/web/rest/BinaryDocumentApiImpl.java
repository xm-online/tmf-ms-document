package com.icthh.xm.tmf.ms.document.web.rest;

import static com.icthh.xm.tmf.ms.document.web.rest.util.HeaderUtil.createAttachmentHeaders;
import static com.icthh.xm.tmf.ms.document.web.rest.util.MediaTypeUtil.parseMediaType;

import com.codahale.metrics.annotation.Timed;
import com.icthh.xm.commons.lep.LogicExtensionPoint;
import com.icthh.xm.commons.lep.spring.LepService;
import com.icthh.xm.commons.permission.annotation.PrivilegeDescription;
import com.icthh.xm.tmf.ms.document.lep.keyresolver.ProfileKeyResolver;
import com.icthh.xm.tmf.ms.document.service.generation.DocumentGenerationService;
import com.icthh.xm.tmf.ms.document.service.generation.GeneratedDocument;
import com.icthh.xm.tmf.ms.document.web.api.BinaryDocumentApiDelegate;
import com.icthh.xm.tmf.ms.document.web.api.model.DocumentGenerate;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
@LepService(group = "service")
@RequiredArgsConstructor
public class BinaryDocumentApiImpl implements BinaryDocumentApiDelegate {

    private final DocumentGenerationService documentGenerationService;

    @PreAuthorize("hasPermission({}, 'DOCUMENT.RETRIEVE.BINARY')")
    @Timed
    @LogicExtensionPoint("RetrieveBinary")
    @Override
    @PrivilegeDescription("Privilege to retrieve binary document")
    public ResponseEntity<Resource> retrieveBinaryDocument(String id) {
        return null;
    }

    @PreAuthorize("hasPermission({'documentGenerate': #documentGenerate}, 'DOCUMENT.GENERATE.BINARY')")
    @Timed
    @Override
    @LogicExtensionPoint(value = "GenerateBinary", resolver = ProfileKeyResolver.class)
    @PrivilegeDescription("Privilege to generate binary document")
    public ResponseEntity<Resource> generateBinaryDocument(DocumentGenerate documentGenerate) {
        MediaType documentMimeType = parseMediaType(documentGenerate.getDocumentMimeType());
        GeneratedDocument document = documentGenerationService
            .generate(documentGenerate.getKey(), documentGenerate.getDocumentContext(), documentMimeType);
        return ResponseEntity.ok()
            .headers(createAttachmentHeaders(document.getFilename(), document.getContentType().toString()))
            .contentLength(document.getDocumentByteResource().contentLength())
            .body(document.getDocumentByteResource());
    }
}

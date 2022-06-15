package com.icthh.xm.tmf.ms.document.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.icthh.xm.commons.lep.LogicExtensionPoint;
import com.icthh.xm.commons.lep.spring.LepService;
import com.icthh.xm.commons.permission.annotation.PrivilegeDescription;
import com.icthh.xm.tmf.ms.document.lep.keresolver.DocumentTypeResolver;
import com.icthh.xm.tmf.ms.document.web.api.DocumentApiDelegate;
import com.icthh.xm.tmf.ms.document.web.api.model.Document;
import com.icthh.xm.tmf.ms.document.web.api.model.DocumentCreate;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
@LepService(group = "service")
@RequiredArgsConstructor
public class DocumentApiImpl implements DocumentApiDelegate {

    @Timed
    @LogicExtensionPoint(value = "CreateDocument", resolver = DocumentTypeResolver.class)
    @PreAuthorize("hasPermission({type: #document.type}, 'DOCUMENT.ACTION.CREATE')")
    @PrivilegeDescription("Create document")
    @Override
    public ResponseEntity<Document> createDocument(DocumentCreate document) {
        return ResponseEntity.ok().build();
    }
}

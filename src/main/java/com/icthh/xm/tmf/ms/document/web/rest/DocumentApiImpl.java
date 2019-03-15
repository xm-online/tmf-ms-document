package com.icthh.xm.tmf.ms.document.web.rest;

import static java.util.Collections.emptyList;

import com.codahale.metrics.annotation.Timed;
import com.icthh.xm.commons.lep.LogicExtensionPoint;
import com.icthh.xm.commons.lep.spring.LepService;
import com.icthh.xm.tmf.ms.document.web.api.DocumentApiDelegate;
import com.icthh.xm.tmf.ms.document.web.api.model.Document;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
@LepService(group = "service")
public class DocumentApiImpl implements DocumentApiDelegate {

    @PreAuthorize("hasPermission({}, 'DOCUMENT.RETRIEVE')")
    @Timed
    @LogicExtensionPoint("Retrieve")
    @Override
    public ResponseEntity<List<Document>> retrieveDocument(String id) {
        return ResponseEntity.ok(emptyList());
    }

    @PreAuthorize("hasPermission({}, 'DOCUMENT.RETRIEVE.PDF')")
    @Timed
    @LogicExtensionPoint("RetrievePdf")
    @Override
    public ResponseEntity<Resource> retrieveDocumentPdf(String id) {
        return null;
    }
}

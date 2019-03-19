package com.icthh.xm.tmf.ms.document.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.icthh.xm.commons.lep.LogicExtensionPoint;
import com.icthh.xm.commons.lep.spring.LepService;
import com.icthh.xm.tmf.ms.document.web.api.BinaryDocumentApiDelegate;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
@LepService(group = "service")
public class DocumentApiImpl implements BinaryDocumentApiDelegate {

    @PreAuthorize("hasPermission({}, 'DOCUMENT.RETRIEVE.BINARY')")
    @Timed
    @LogicExtensionPoint("RetrieveBinary")
    @Override
    public ResponseEntity<Resource> retrieveBinaryDocument(String id) {
        return null;
    }
}

package com.icthh.xm.tmf.ms.document.lep.keresolver;

import com.icthh.xm.lep.api.LepKeyResolver;
import com.icthh.xm.lep.api.LepMethod;
import com.icthh.xm.tmf.ms.document.web.api.model.DocumentCreate;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class DocumentTypeResolver implements LepKeyResolver {

    public static final String DOCUMENT_CREATE = "document";

    @Override
    public List<String> segments(LepMethod method) {
        DocumentCreate documentCreate = method.getParameter(DOCUMENT_CREATE, DocumentCreate.class);
        Objects.requireNonNull(documentCreate, "LEP method required parameter 'document' is null");
        return List.of(documentCreate.getType());
    }

}

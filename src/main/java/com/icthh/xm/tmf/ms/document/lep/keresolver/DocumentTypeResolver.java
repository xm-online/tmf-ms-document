package com.icthh.xm.tmf.ms.document.lep.keresolver;

import com.icthh.xm.commons.lep.AppendLepKeyResolver;
import com.icthh.xm.lep.api.LepManagerService;
import com.icthh.xm.lep.api.LepMethod;
import com.icthh.xm.lep.api.commons.SeparatorSegmentedLepKey;
import com.icthh.xm.tmf.ms.document.web.api.model.DocumentCreate;
import org.springframework.stereotype.Component;

@Component
public class DocumentTypeResolver extends AppendLepKeyResolver {

    public static final String DOCUMENT_CREATE = "document";

    @Override
    protected String[] getAppendSegments(SeparatorSegmentedLepKey baseKey, LepMethod method,
                                         LepManagerService managerService) {
        DocumentCreate documentCreate = getRequiredParam(method, DOCUMENT_CREATE, DocumentCreate.class);
        String translatedLocationTypeKey = translateToLepConvention(documentCreate.getType());
        return new String[]{
            translatedLocationTypeKey
        };
    }
}

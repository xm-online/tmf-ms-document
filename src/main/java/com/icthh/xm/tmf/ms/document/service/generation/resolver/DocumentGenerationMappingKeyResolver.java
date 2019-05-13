package com.icthh.xm.tmf.ms.document.service.generation.resolver;

import com.icthh.xm.commons.lep.AppendLepKeyResolver;
import com.icthh.xm.lep.api.LepManagerService;
import com.icthh.xm.lep.api.LepMethod;
import com.icthh.xm.lep.api.commons.SeparatorSegmentedLepKey;
import org.springframework.stereotype.Component;

@Component
public class DocumentGenerationMappingKeyResolver extends AppendLepKeyResolver {

    @Override
    protected String[] getAppendSegments(SeparatorSegmentedLepKey baseKey, LepMethod method,
        LepManagerService managerService) {
        String key = getRequiredStrParam(method, "key");
        String translatedKey = translateToLepConvention(key);
        return new String[] {
            translatedKey.toUpperCase()
        };
    }
}

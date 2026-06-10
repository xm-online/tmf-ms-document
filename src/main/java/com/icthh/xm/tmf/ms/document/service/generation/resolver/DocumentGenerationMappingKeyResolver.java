package com.icthh.xm.tmf.ms.document.service.generation.resolver;

import com.icthh.xm.lep.api.LepKeyResolver;
import com.icthh.xm.lep.api.LepMethod;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class DocumentGenerationMappingKeyResolver implements LepKeyResolver {

    @Override
    public List<String> segments(LepMethod method) {
        String key = method.getParameter("key", String.class);
        Objects.requireNonNull(key, "LEP method required parameter 'key' is null");
        return List.of(translateToLepConvention(key));
    }

    private static String translateToLepConvention(String key) {
        Objects.requireNonNull(key, "Document type can't be null");
        return key.replace("-", "_").replace(".", "$");
    }
}

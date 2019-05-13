package com.icthh.xm.tmf.ms.document.service.generation;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DocumentGenerationUtils {

    public static String buildDocumentFilename(String key) {
        return key.toLowerCase();
    }
}

package com.icthh.xm.tmf.ms.document.web.rest.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

@UtilityClass
public class MediaTypeUtil {

    /**
     * Parse string to {@link MediaType} or return {@code null} if value is empty.
     *
     * @param value string to parse
     * @return MediaType or null
     */
    public static MediaType parseMediaType(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return MediaType.parseMediaType(value);
    }
}

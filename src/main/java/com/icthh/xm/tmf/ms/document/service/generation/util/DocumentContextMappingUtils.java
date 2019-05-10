package com.icthh.xm.tmf.ms.document.service.generation.util;

import com.google.common.base.Joiner;
import javax.annotation.Nullable;
import lombok.experimental.UtilityClass;

@SuppressWarnings("unused")
@UtilityClass
public class DocumentContextMappingUtils {

    /**
     * Join objects to string with a separator ignoring null values.
     *
     * @return result of joining
     */
    public static String joinNullSafe(String separator, @Nullable Object first,
        @Nullable Object second, @Nullable Object... rest) {
        if (rest == null) {
            return Joiner.on(separator).skipNulls().join(first, second);
        } else {
            return Joiner.on(separator).skipNulls().join(first, second, rest);
        }
    }

}

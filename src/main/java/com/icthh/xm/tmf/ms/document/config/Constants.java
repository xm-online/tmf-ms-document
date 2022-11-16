package com.icthh.xm.tmf.ms.document.config;

import lombok.experimental.UtilityClass;
import org.springframework.http.MediaType;

/**
 * Application constants.
 */
@UtilityClass
public final class Constants {

    // Regex for acceptable logins
    public static final String LOGIN_REGEX = "^[_.@A-Za-z0-9-]*$";

    public static final String SYSTEM_ACCOUNT = "system";
    public static final String ANONYMOUS_USER = "anonymoususer";
    public static final String DEFAULT_LANGUAGE = "en";

    public static final String TENANT_NAME = "tenantName";

    public static final MediaType APPLICATION_DOCX = MediaType.valueOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
}

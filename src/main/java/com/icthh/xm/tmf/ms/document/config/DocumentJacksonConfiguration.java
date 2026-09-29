package com.icthh.xm.tmf.ms.document.config;

import com.icthh.xm.tmf.ms.document.web.rest.errors.ProblemModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Service specific Jackson setup. Named apart from xm-commons {@code JacksonConfiguration}, which already
 * contributes the JavaTime and Hibernate 7 modules and the application {@code JsonMapper}.
 */
@Configuration
public class DocumentJacksonConfiguration {

    /*
     * Module for serialization of RFC7807 Problem (Jackson 3).
     */
    @Bean
    public ProblemModule problemModule() {
        return new ProblemModule();
    }
}

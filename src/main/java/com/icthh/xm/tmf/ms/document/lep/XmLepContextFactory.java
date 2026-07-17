package com.icthh.xm.tmf.ms.document.lep;

import com.icthh.xm.commons.config.client.service.TenantConfigService;
import com.icthh.xm.commons.lep.api.LepContextFactory;
import com.icthh.xm.commons.permission.service.PermissionCheckService;
import com.icthh.xm.lep.api.LepMethod;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class XmLepContextFactory implements LepContextFactory {

    private final TenantConfigService tenantConfigService;
    private final RestTemplate restTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final PermissionCheckService permissionCheckService;
    private final MeterRegistry meterRegistry;

    public XmLepContextFactory(TenantConfigService tenantConfigService,
                               @Qualifier("loadBalancedRestTemplate") RestTemplate restTemplate,
                               JdbcTemplate jdbcTemplate,
                               PermissionCheckService permissionCheckService,
                               MeterRegistry meterRegistry) {
        this.tenantConfigService = tenantConfigService;
        this.restTemplate = restTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.permissionCheckService = permissionCheckService;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public LepContext buildLepContext(LepMethod lepMethod) {
        LepContext lepContext = new LepContext();

        LepContext.Services services = new LepContext.Services();
        services.tenantConfigService = tenantConfigService;
        services.permissionService = permissionCheckService;
        lepContext.services = services;

        LepContext.Templates templates = new LepContext.Templates();
        templates.rest = restTemplate;
        templates.jdbc = jdbcTemplate;
        lepContext.templates = templates;
        lepContext.meterRegistry = meterRegistry;

        return lepContext;
    }
}

package com.icthh.xm.tmf.ms.document.lep;

import com.icthh.xm.commons.config.client.service.TenantConfigService;
import com.icthh.xm.commons.lep.api.BaseLepContext;
import com.icthh.xm.commons.permission.service.PermissionCheckService;
import com.icthh.xm.tmf.ms.document.service.metrics.MetricsAdapter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

public class LepContext extends BaseLepContext {

    public Services services;
    public Templates templates;

    public static class Services {
        public TenantConfigService tenantConfigService;
        public PermissionCheckService permissionService;
        public MetricsAdapter metricsAdapter;
    }

    public static class Templates {
        public RestTemplate rest;
        public JdbcTemplate jdbc;
    }
}

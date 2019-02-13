package com.icthh.xm.tmf.ms.document.web.rest;

import com.icthh.xm.commons.gen.api.TenantsApiDelegate;
import com.icthh.xm.commons.gen.model.Tenant;
import com.icthh.xm.tmf.ms.document.service.tenant.TenantService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantResource implements TenantsApiDelegate {

    private final TenantService tenantService;

    @Override
    @PreAuthorize("hasPermission({'tenant':#tenant}, 'DOCUMENT.TENANT.CREATE')")
    public ResponseEntity<Void> addTenant(Tenant tenant) {
        tenantService.createTenant(tenant.getTenantKey());
        return ResponseEntity.ok().build();
    }

    @Override
    @PreAuthorize("hasPermission({'tenantKey':#tenantKey}, 'DOCUMENT.TENANT.DELETE')")
    public ResponseEntity<Void> deleteTenant(String tenantKey) {
        tenantService.deleteTenant(tenantKey);
        return ResponseEntity.ok().build();
    }

    @Override
    @PostAuthorize("hasPermission(null, 'DOCUMENT.TENANT.GET_LIST')")
    public ResponseEntity<List<Tenant>> getAllTenantInfo() {
        throw new UnsupportedOperationException();
    }

    @Override
    @PostAuthorize("hasPermission({'returnObject': returnObject.body}, 'DOCUMENT.TENANT.GET_LIST.ITEM')")
    public ResponseEntity<Tenant> getTenant(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    @PreAuthorize("hasPermission({'tenant':#tenant, 'state':#state}, 'DOCUMENT.TENANT.UPDATE')")
    public ResponseEntity<Void> manageTenant(String tenant, String state) {
        tenantService.manageTenant(tenant, state);
        return ResponseEntity.ok().build();
    }
}

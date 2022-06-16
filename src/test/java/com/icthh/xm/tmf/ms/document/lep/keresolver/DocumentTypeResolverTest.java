package com.icthh.xm.tmf.ms.document.lep.keresolver;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.icthh.xm.commons.lep.XmLepConstants;
import com.icthh.xm.commons.lep.spring.LepServiceHandler;
import com.icthh.xm.lep.api.LepKey;
import com.icthh.xm.lep.api.LepKeyResolver;
import com.icthh.xm.lep.api.LepManager;
import com.icthh.xm.lep.api.LepMethod;
import com.icthh.xm.lep.api.Version;
import com.icthh.xm.lep.core.CoreLepManager;
import com.icthh.xm.tmf.ms.document.web.api.model.DocumentCreate;
import com.icthh.xm.tmf.ms.document.web.rest.DocumentApiImpl;
import java.lang.reflect.Method;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

@RunWith(MockitoJUnitRunner.class)
public class DocumentTypeResolverTest {

    private static final String TYPE_KEY = "REPORT";

    @InjectMocks
    private LepServiceHandler lepServiceHandler;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private CoreLepManager lepManager;

    @Captor
    private ArgumentCaptor<LepKey> baseLepKey;

    @Captor
    private ArgumentCaptor<LepKeyResolver> keyResolver;

    @Captor
    private ArgumentCaptor<LepMethod> lepMethod;

    @Captor
    private ArgumentCaptor<Version> version;

    @Test
    public void  testResolveLepKeyByType() throws Throwable {
        Method method = DocumentApiImpl.class.getMethod("createDocument", DocumentCreate.class);

        when(applicationContext.getBean(LepManager.class)).thenReturn(lepManager);

        DocumentTypeResolver resolver = new DocumentTypeResolver();
        when(applicationContext.getBean(DocumentTypeResolver.class)).thenReturn(resolver);

        DocumentCreate documentCreate = new DocumentCreate();
        documentCreate.setType(TYPE_KEY);

        lepServiceHandler.onMethodInvoke(DocumentApiImpl.class,
            new DocumentApiImpl(), method, new Object[]{documentCreate});

        verify(lepManager)
            .processLep(baseLepKey.capture(), version.capture(), keyResolver.capture(), lepMethod.capture());

        LepKey resolvedKey = resolver.resolve(baseLepKey.getValue(), lepMethod.getValue(), null);

        assertEquals(
            String.join(XmLepConstants.EXTENSION_KEY_SEPARATOR,
                "service", "CreateDocument", TYPE_KEY), resolvedKey.getId());
    }

}

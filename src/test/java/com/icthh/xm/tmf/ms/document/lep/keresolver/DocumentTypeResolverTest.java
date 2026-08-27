package com.icthh.xm.tmf.ms.document.lep.keresolver;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.icthh.xm.lep.api.LepMethod;
import com.icthh.xm.tmf.ms.document.web.api.model.DocumentCreate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DocumentTypeResolverTest {

    private static final String TYPE_KEY = "REPORT";

    @InjectMocks
    private DocumentTypeResolver resolver;

    @Mock
    private LepMethod method;

    @Test
    public void testResolveLepKeyByType() {
        DocumentCreate documentCreate = new DocumentCreate();
        documentCreate.setType(TYPE_KEY);

        when(method.getParameter(DocumentTypeResolver.DOCUMENT_CREATE, DocumentCreate.class))
            .thenReturn(documentCreate);

        assertEquals(singletonList(TYPE_KEY), resolver.segments(method));

        verify(method).getParameter(DocumentTypeResolver.DOCUMENT_CREATE, DocumentCreate.class);
    }

}

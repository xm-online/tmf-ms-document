package com.icthh.xm.tmf.ms.document;

import com.icthh.xm.tmf.ms.document.config.IntegrationTestConfiguration;
import com.icthh.xm.tmf.ms.document.config.TestLepConfiguration;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Base class for tests that start the whole application context.
 * TestLepConfiguration must stay first: it overrides the LEP engine beans of the main LepConfiguration.
 */
@SpringBootTest(classes = {
    TestLepConfiguration.class,
    DocumentApp.class,
    IntegrationTestConfiguration.class
})
@ExtendWith(SpringExtension.class)
public abstract class AbstractSpringBootTest {
}

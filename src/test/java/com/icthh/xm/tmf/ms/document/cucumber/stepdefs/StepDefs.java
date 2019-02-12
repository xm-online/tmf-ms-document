package com.icthh.xm.tmf.ms.document.cucumber.stepdefs;

import com.icthh.xm.tmf.ms.document.DocumentApp;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

@WebAppConfiguration
@SpringBootTest
@ContextConfiguration(classes = DocumentApp.class)
public abstract class StepDefs {

    protected ResultActions actions;

}

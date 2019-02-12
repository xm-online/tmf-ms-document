package com.icthh.xm.tmf.ms.document.cucumber.stepdefs;

import com.icthh.xm.tmf.ms.document.DocumentApp;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import org.springframework.boot.test.context.SpringBootTest;

@WebAppConfiguration
@SpringBootTest
@ContextConfiguration(classes = DocumentApp.class)
public abstract class StepDefs {

    protected ResultActions actions;

}

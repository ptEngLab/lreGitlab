package com.lre.model.test.testcontent.scheduler.action.stopvusers;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.lre.model.test.testcontent.scheduler.action.Action;
import com.lre.model.test.testcontent.scheduler.action.common.VusersAction;

import static com.lre.common.constants.ConfigConstants.LRE_API_XMLNS;

@JacksonXmlRootElement(localName = "StopVusers", namespace = LRE_API_XMLNS)
public class StopVusers extends VusersAction {

    @Override
    public void applyTo(Action.ActionBuilder builder) {
        builder.stopVusers(this);
    }

}
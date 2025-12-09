package com.lre.model.test.testcontent.scheduler.action.startvusers;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.lre.model.test.testcontent.scheduler.action.Action;
import com.lre.model.test.testcontent.scheduler.action.common.VusersAction;

import static com.lre.common.constants.ConfigConstants.LRE_API_XMLNS;


@JacksonXmlRootElement(localName = "StartVusers", namespace = LRE_API_XMLNS)
public class StartVusers extends VusersAction {

    @Override
    public void applyTo(Action.ActionBuilder builder) {
        builder.startVusers(this);
    }

}
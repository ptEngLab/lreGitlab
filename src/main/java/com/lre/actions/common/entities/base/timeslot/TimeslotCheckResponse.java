package com.lre.actions.common.entities.base.timeslot;

import com.lre.actions.utils.JsonUtils;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class TimeslotCheckResponse extends ArrayList<String> {

    public static TimeslotCheckResponse jsonToObject(String json){
        List<String> errorMessages = JsonUtils.fromJsonArray(json, String.class);
        TimeslotCheckResponse timeslotCheckResponse = new TimeslotCheckResponse();
        timeslotCheckResponse.addAll(errorMessages);
        return  timeslotCheckResponse;
    }

    public boolean hasErrors() { return !isEmpty();    }
}

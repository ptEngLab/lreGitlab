package com.lre.model.timeslot;

import lombok.NoArgsConstructor;

import java.util.ArrayList;

@NoArgsConstructor
public class TimeslotCheckResponse extends ArrayList<String> {
    public boolean hasErrors() {
        return !isEmpty();
    }
}

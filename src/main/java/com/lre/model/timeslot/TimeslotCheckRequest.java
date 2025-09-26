package com.lre.model.timeslot;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TimeslotCheckRequest {

    @JsonProperty("DurationInMinutes")
    private int durationInMinutes;

    @JsonProperty("IsUseVuds")
    private boolean isUseVuds ;

    @JsonProperty("VudsAmount")
    private int vudsAmount;

    @JsonProperty("IsNewReservationMode")
    private boolean isNewReservationMode = true;

    @JsonProperty("PostRunAction")
    private int postRunAction;

    @JsonProperty("TestInstanceId")
    private int testInstanceId;

    @JsonProperty("ReservationId")
    private int reservationId = -1;

    public TimeslotCheckRequest(int durationInMinutes, boolean isUseVuds, int vudsAmount,
                                int postRunAction, int testInstanceId){
        this.durationInMinutes = durationInMinutes;
        this.isUseVuds = isUseVuds;
        this.vudsAmount = vudsAmount;
        this.postRunAction = postRunAction;
        this.testInstanceId = testInstanceId;
    }

}

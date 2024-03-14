package com.marindulja.mentalhealthbackend.integrations.zoom;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZoomMeetingRequest {
    @JsonProperty("topic")
    String topic;
    @JsonProperty("type")
    int type;
    @JsonProperty("start_time")
    String startTime;
    @JsonProperty("duration")
    int duration;
    @JsonProperty("schedule_for")
    String scheduleFor;
    @JsonProperty("timezone")
    String timezone;
    @JsonProperty("password")
    String password;
    @JsonProperty("agenda")
    String agenda;
}

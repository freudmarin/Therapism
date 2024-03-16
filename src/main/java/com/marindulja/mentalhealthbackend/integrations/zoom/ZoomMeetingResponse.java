package com.marindulja.mentalhealthbackend.integrations.zoom;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@JsonPropertyOrder({
        "uuid",
        "id",
        "host_id",
        "host_email",
        "assistant_id",
        "topic",
        "type",
        "status",
        "start_time",
        "duration",
        "timezone",
        "agenda",
        "created_at",
        "start_url",
        "join_url",
        "password",
        "h323_password",
        "pstn_password",
        "encrypted_password",
        "settings",
        "pre_schedule"
})
public class ZoomMeetingResponse implements Serializable {
    @JsonProperty("uuid")
    String uuid;
    @JsonProperty("id")
    long meetingId;
    @JsonProperty("host_id")
    String hostId;
    @JsonProperty("host_email")
    String hostEmail;
    @JsonProperty("assistant_id")
    String assistantId;
    @JsonProperty("topic")
    String topic;
    @JsonProperty("type")
    int type;
    @JsonProperty("status")
    String status;
    @JsonProperty("start_time")
    String startTime;
    @JsonProperty("duration")
    int duration;
    @JsonProperty("timezone")
    String timezone;
    @JsonProperty("agenda")
    private String agenda;
    @JsonProperty("created_at")
    String createdAt;
    @JsonProperty("start_url")
    String startUrl;
    @JsonProperty("join_url")
    String joinUrl;
    @JsonProperty("password")
    private String password;
    @JsonProperty("h323_password")
    private String h323_password;
    @JsonProperty("pstn_password")
    private String pstn_password;
    @JsonProperty("encrypted_password")
    private String encrypted_password;
    @JsonIgnore
    @JsonProperty("settings")
    private Object settings;
    @JsonProperty("pre_schedule")
    private boolean pre_schedule;
}

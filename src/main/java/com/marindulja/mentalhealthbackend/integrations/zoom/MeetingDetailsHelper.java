package com.marindulja.mentalhealthbackend.integrations.zoom;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "meeting_name",
        "agenda",
        "topic",
        "start_date_time",
        "end_date_time",
        "duration",
        "timezone",
        "user_id",
        "password",
        "type"
})
public class MeetingDetailsHelper implements Serializable {
    @JsonProperty("meeting_name")
    private String meetingName;
    @JsonProperty("agenda")
    private String agenda;
    @JsonProperty("topic")
    private String topic;
    @JsonProperty("duration")
    private int duration;
    @JsonProperty("start_date_time")
    private String startDateTime;
    @JsonProperty("end_date_time")
    private String endDateTime;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("password")
    private String password;
    @JsonProperty("timezone")
    private String timezone;
    @JsonProperty("type")
    private String type;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();
    private final static long serialVersionUID = -7358039937780347836L;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MeetingDetailsHelper)) return false;
        MeetingDetailsHelper that = (MeetingDetailsHelper) o;
        return getMeetingName().equals(that.getMeetingName()) &&
                Objects.equals(getTopic(), that.getTopic()) &&
                Objects.equals(getAgenda(), that.getAdditionalProperties()) &&
                getStartDateTime().equals(that.getStartDateTime()) &&
                getEndDateTime().equals(that.getEndDateTime()) &&
                Objects.equals(getUserId(), that.getUserId()) &&
                Objects.equals(getTimezone(), that.getTimezone()) &&
                Objects.equals(getPassword(), that.getPassword());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMeetingName(), getTopic(), getAgenda(), getTimezone(), getStartDateTime(), getEndDateTime(), getUserId(), getPassword());
    }

    @Override
    public String toString() {
        return "MeetingDetailsHelper{" +
                "meetingName='" + meetingName + '\'' +
                ", topic='" + topic + '\'' +
                ", agenda='" + agenda + '\'' +
                ", startDateTime='" + startDateTime + '\'' +
                ", endDateTime='" + endDateTime + '\'' +
                ", timezone='" + timezone + '\'' +
                '}';
    }
}

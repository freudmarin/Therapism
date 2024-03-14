package com.marindulja.mentalhealthbackend.integrations.zoom;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PersonalDetailsResponse implements Serializable {
    @JsonProperty("id")
    String id;
    @JsonProperty("first_name")
    String firstName;
    @JsonProperty("last_name")
    String lastName;
    @JsonProperty("email")
    String email;
    @JsonProperty("type")
    int type;
    @JsonProperty("role_name")
    String roleName;
    @JsonProperty("personal_meeting_url")
    String personalMeetingUrl;
    @JsonProperty("timezone")
    String timezone;
    @JsonProperty("verified")
    boolean verified;
    @JsonProperty("created_at")
    String createdAt;
    @JsonProperty("pic_url")
    String picUrl;
    @JsonProperty("account_id")
    String accountId;
    @JsonProperty("language")
    String language;
    @JsonProperty("phone_country")
    private String phoneCountry;
    @JsonProperty("phone_number")
    private String phoneNumber;
    @JsonProperty("status")
    private String status;
    @JsonProperty("host_key")
    private String hostKey;

}

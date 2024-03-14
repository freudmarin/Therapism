package com.marindulja.mentalhealthbackend.integrations.zoom;

public class ZoomDetails {
    static String zoomHostAPIUrl = "https://api.zoom.us";
    static String zoomUrl = "https://zoom.us";

    static String accessTokenUrlFromOAuth = zoomUrl + "/oauth/token?grant_type=authorization_code&code={oAuthCode}&redirect_uri={redirectUrl}";
    static String getMeetingUrl = zoomHostAPIUrl + "/v2/meetings/{meetingId}";
    static String createMeetingUrl = zoomHostAPIUrl + "/v2/users/{userId}/meetings";
    static String updateMeetingUrl = zoomHostAPIUrl + "/v2/meetings/{meetingId}";
}

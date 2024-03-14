package com.marindulja.mentalhealthbackend.integrations.zoom;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@Slf4j
public class ZoomApiIntegration {

    @Value("${app.zoom.clientId}")
    private String clientId;

    @Value("${app.zoom.clientSecret}")
    private String clientSecret;

    @Value("${app.zoom.redirectUrl}")
    private String redirectUrl;

    private final CloseableHttpClient httpClient;

    private final ObjectMapper objectMapper;

    public ZoomApiIntegration(CloseableHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    // https://marketplace.zoom.us/docs/guides/auth/oauth
    // Exchange oAuth Token for Access Token
    public TokenResponse callTokenApi(String oAuthToken) throws IOException {
        String credentials = clientId + ":" + clientSecret;
        String authorizationHeader = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());

        HttpPost httpPost = new HttpPost("https://zoom.us/oauth/token");
        httpPost.setHeader("Authorization", authorizationHeader);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        String requestBody = "grant_type=authorization_code&code=" + oAuthToken + "&redirect_uri=" + URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8);
        httpPost.setEntity(new StringEntity(requestBody));

        org.apache.http.HttpResponse httpResponse = httpClient.execute(httpPost);
        String responseString = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");

        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new IllegalStateException("Failed to retrieve access token. Status code: " + httpResponse.getStatusLine().getStatusCode());
        }

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(responseString, TokenResponse.class);
    }

    public ZoomMeetingResponse callCreateMeetingApi(MeetingDetailsHelper meetingDetails, ZoomMeetingRequest zoomMeetingRequest, String accessToken) {
        try {
            String requestBody = objectMapper.writeValueAsString(zoomMeetingRequest);
            String zoomCreateMeetingUrl = ZoomDetails.createMeetingUrl.replace("{userId}", meetingDetails.getUserId());

            HttpPost httpPost = new HttpPost(zoomCreateMeetingUrl);
            httpPost.setHeader("Authorization", "Bearer " + accessToken);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(requestBody));

            HttpResponse httpResponse = httpClient.execute(httpPost);
            String responseString = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");

            if (httpResponse.getStatusLine().getStatusCode() == 201) { // Check for HTTP OK status
                return objectMapper.readValue(responseString, ZoomMeetingResponse.class);
            } else {
                // Log error or handle non-OK response
                throw new IllegalStateException("Failed to create Zoom meeting. HTTP error code: " + httpResponse.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            // Log and convert to unchecked exception
            throw new RuntimeException("Failed to call Zoom Create Meeting API", e);
        }
    }

    public ZoomMeetingResponse callUpdateMeetingApi(ZoomMeetingRequest zoomMeetingRequest, Long meetingId, String accessToken) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(zoomMeetingRequest);

            String zoomUrl = ZoomDetails.updateMeetingUrl.replace("{meetingId}", String.valueOf(meetingId));
            HttpPatch httpPatch = new HttpPatch(zoomUrl);
            httpPatch.setHeader("Authorization", "Bearer " + accessToken);
            httpPatch.setHeader("Content-Type", "application/json");
            httpPatch.setEntity(new StringEntity(requestBody, "UTF-8"));

            org.apache.http.HttpResponse httpResponse = httpClient.execute(httpPatch);
            if (httpResponse.getStatusLine().getStatusCode() == 204) {
                ZoomMeetingResponse meetingResponse = getMeetingDetails(meetingId, accessToken);
                return meetingResponse;
            } else {
                throw new IllegalStateException("Failed to update Zoom meeting. Status code: " + httpResponse.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to call Zoom Update Meeting API", e);
        }
    }

    public ZoomMeetingResponse getMeetingDetails(Long meetingId, String accessToken) throws IOException {
        String meetingDetailsUrl = ZoomDetails.getMeetingUrl.replace("{meetingId}", String.valueOf(meetingId));
        HttpGet httpGet = new HttpGet(meetingDetailsUrl);
        httpGet.setHeader("Authorization", "Bearer " + accessToken);
        httpGet.setHeader("Content-Type", "application/json");
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");

            if (response.getStatusLine().getStatusCode() == 200) {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(responseString, ZoomMeetingResponse.class);
            } else {
                throw new IllegalStateException("Failed to retrieve Zoom meeting details. Status code: " + response.getStatusLine().getStatusCode());
            }
        }
    }
}

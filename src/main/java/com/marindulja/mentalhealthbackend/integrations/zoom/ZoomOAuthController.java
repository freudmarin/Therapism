package com.marindulja.mentalhealthbackend.integrations.zoom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("api/v1/zoom")
public class ZoomOAuthController {

    private final ZoomApiIntegration apiIntegration;
    @Value("${app.zoom.clientId}")
    private String clientId;

    public ZoomOAuthController(ZoomApiIntegration apiIntegration) {
        this.apiIntegration = apiIntegration;
    }

    @GetMapping("/authorize")
    public String redirectToZoomAuth() {
        // Construct the authorization URL and redirect the user
        String baseUrl = "https://therapism.com"; // Set your base URL here
        return "https://zoom.us/oauth/authorize?response_type=code&client_id=" + clientId + "&redirect_uri=" + URLEncoder.encode(baseUrl, StandardCharsets.UTF_8);
    }
}

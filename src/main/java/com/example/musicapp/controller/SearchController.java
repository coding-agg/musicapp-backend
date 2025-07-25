package com.example.musicapp.controller;

import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/search")
public class SearchController extends AuthenticatedController {

    private static final String PYTHON_API_URL = "http://localhost:5001/search";

    /**
     * This endpoint requires a valid Firebase token.
     * It simply forwards the search request to a Python server.
     */
    @PostMapping
    public ResponseEntity<String> searchYouTube(
            @RequestBody Map<String, String> payload,
            HttpServletRequest servletRequest) {

        getAuthenticatedUser(servletRequest);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

        return restTemplate.postForEntity(PYTHON_API_URL, request, String.class);
    }
}



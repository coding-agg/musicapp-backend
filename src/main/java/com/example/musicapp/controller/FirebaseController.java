package com.example.musicapp.controller;

import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/profile")
public class FirebaseController {

    /**
     * This endpoint is PUBLIC and does not require Firebase Authentication.
     * However, it assumes that FirebaseToken is attached by the filter — so it may
     * still require the token if your security config is strict.
     *
     * To truly make this public, you may want to adjust how the filter handles it.
     */

    @GetMapping
    public ResponseEntity<Map<String, Object>> profile(HttpServletRequest request) {
        FirebaseToken user = (FirebaseToken) request.getAttribute("firebaseUser");

        if (user == null) {
            // This means no token was attached or this is a real public hit
            Map<String, Object> error = new HashMap<>();
            error.put("error", "User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        Map<String, Object> firebaseClaim = (Map<String, Object>) user.getClaims().get("firebase");
        String provider = firebaseClaim != null ? (String) firebaseClaim.get("sign_in_provider") : "unknown";

        Map<String, Object> response = new HashMap<>();
        response.put("uid", user.getUid());
        response.put("email", user.getEmail());
        response.put("name", user.getName());
        response.put("provider", provider);

        System.out.println("Server Reached here");
        return ResponseEntity.ok(response);
    }
}


package com.example.musicapp.controller;

import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public abstract class AuthenticatedController {

    protected FirebaseToken getAuthenticatedUser(HttpServletRequest request) {
        FirebaseToken user = (FirebaseToken) request.getAttribute("firebaseUser");
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
        }
        return user;
    }
}


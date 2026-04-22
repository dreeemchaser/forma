package com.forma.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/moderator/")
public class ModeratorController {

    @GetMapping
    public ResponseEntity<?> getModerationQueue(@AuthenticationPrincipal UserDetails userDetails) throws
            Exception {
        return null;
    }

}

package com.dobugs.yologaauthenticationapi.config.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api")
@RestController
public class FakeController {

    @GetMapping("/hasNotAnnotation")
    public ResponseEntity<Void> hasNotAnnotation() {
        return ResponseEntity.ok().build();
    }

    @Authorized
    @GetMapping("/hasAuthorizedAnnotation")
    public ResponseEntity<Void> hasAuthorizedAnnotation() {
        return ResponseEntity.ok().build();
    }

    @Authorized
    @ValidatedRefreshToken
    @GetMapping("/hasValidatedRefreshTokenAnnotation")
    public ResponseEntity<Void> hasValidatedRefreshTokenAnnotation() {
        return ResponseEntity.ok().build();
    }
}

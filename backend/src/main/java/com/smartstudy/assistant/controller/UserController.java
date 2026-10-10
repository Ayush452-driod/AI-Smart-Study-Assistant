package com.smartstudy.assistant.controller;

import com.smartstudy.assistant.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> profile(Authentication authentication) {

        return ResponseEntity.ok(
                userService.getProfile(authentication.getName())
        );
    }

    @PutMapping("/me")
    public ResponseEntity<?> update(
            Authentication authentication,
            @RequestBody Map<String, String> body) {

        return ResponseEntity.ok(
                userService.updateProfile(
                        authentication.getName(),
                        body.get("name")
                )
        );
    }

    @PostMapping(
            value = "/profile-picture",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadPicture(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {

        try {
            return ResponseEntity.ok(
                    userService.uploadPicture(
                            authentication.getName(),
                            file
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/profile-picture")
    public ResponseEntity<?> removePicture(
            Authentication authentication) {

        try {
            return ResponseEntity.ok(
                    userService.removePicture(
                            authentication.getName()
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }
}

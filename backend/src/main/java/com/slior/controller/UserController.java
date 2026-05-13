package com.slior.controller;

import com.slior.dto.user.UserDataExportDto;
import com.slior.model.User;
import com.slior.repository.UserRepository;
import com.slior.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/me/data-export")
    public ResponseEntity<UserDataExportDto> exportMyData(@AuthenticationPrincipal UserDetails userDetails) {
        User user = findCurrentUser(userDetails);
        return ResponseEntity.ok(userService.exportUserData(user.getId()));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(@AuthenticationPrincipal UserDetails userDetails) {
        User user = findCurrentUser(userDetails);
        userService.requestDeletion(user.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/limit-processing")
    public ResponseEntity<Void> limitProcessing(@AuthenticationPrincipal UserDetails userDetails, @RequestParam boolean limited) {
        User user = findCurrentUser(userDetails);
        userService.updateLimitProcessing(user.getId(), limited);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/me/notifications")
    public ResponseEntity<Void> updateNotifications(@AuthenticationPrincipal UserDetails userDetails, @RequestParam boolean enabled) {
        User user = findCurrentUser(userDetails);
        userService.updateNotificationConsent(user.getId(), enabled);
        return ResponseEntity.ok().build();
    }

    private User findCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }
}

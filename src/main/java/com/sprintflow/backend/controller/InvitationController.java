package com.sprintflow.backend.controller;

import com.sprintflow.backend.service.InvitationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invitations")
@SecurityRequirement(name = "bearerAuth")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping("/{token}/join")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void joinWorkspace(
            @PathVariable String token,
            Authentication authentication) {

        invitationService.joinWorkspace(
                token,
                authentication
        );
    }

    @PostMapping("/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acceptInvitation(
            @RequestParam String token,
            Authentication authentication) {

        invitationService.acceptInvitation(
                token,
                authentication
        );
    }
}
package com.sprintflow.backend.controller;

import com.sprintflow.backend.service.InvitationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invitations")
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
}
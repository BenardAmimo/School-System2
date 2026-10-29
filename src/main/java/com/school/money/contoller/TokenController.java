package com.school.money.contoller;

import com.school.money.request.ReasonRequest;
import com.school.money.request.TokenRequest;
import com.school.money.response.TokenResponse;
import com.school.money.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/money/tokens")
public class TokenController {

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) { this.tokenService = tokenService; }

    @PostMapping
    public ResponseEntity<TokenResponse> nominate(@RequestBody TokenRequest request, Authentication auth) {
        return ResponseEntity.ok(tokenService.nominate(request, auth));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<TokenResponse> approve(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(tokenService.approve(id, auth));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<TokenResponse> reject(@PathVariable Long id, @RequestBody ReasonRequest request, Authentication auth) {
        return ResponseEntity.ok(tokenService.reject(id, request, auth));
    }

    @GetMapping
    public ResponseEntity<List<TokenResponse>> all(Authentication auth) {
        return ResponseEntity.ok(tokenService.all(auth));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<TokenResponse>> mine(Authentication auth) {
        return ResponseEntity.ok(tokenService.mine(auth));
    }
}
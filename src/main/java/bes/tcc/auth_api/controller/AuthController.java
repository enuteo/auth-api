package bes.tcc.auth_api.auth;

import bes.tcc.auth_api.dto.LoginRequest;
import bes.tcc.auth_api.dto.TokenResponse;
import bes.tcc.auth_api.service.KeycloakAuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
	private final KeycloakAuthService keycloakAuthService;

	public AuthController(KeycloakAuthService keycloakAuthService) {
		this.keycloakAuthService = keycloakAuthService;
	}

	@PostMapping("/login")
	public TokenResponse login(@Valid @RequestBody LoginRequest loginRequest) {
		return keycloakAuthService.authenticate(loginRequest);
	}
}

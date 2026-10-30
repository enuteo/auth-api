package bes.tcc.auth_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {
	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<AuthError> handleInvalidCredentials() {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(new AuthError("invalid_credentials", "Usuário ou senha inválidos."));
	}

	@ExceptionHandler(IdentityProviderException.class)
	public ResponseEntity<AuthError> handleIdentityProviderFailure() {
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
				.body(new AuthError("identity_provider_error", "Não foi possível autenticar no Keycloak."));
	}

	public record AuthError(String code, String message) {
	}
}

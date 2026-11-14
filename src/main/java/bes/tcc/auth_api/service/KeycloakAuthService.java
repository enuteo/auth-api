package bes.tcc.auth_api.service;

import bes.tcc.auth_api.config.KeycloakProperties;
import bes.tcc.auth_api.dto.LoginRequest;
import bes.tcc.auth_api.dto.TokenResponse;
import bes.tcc.auth_api.exception.IdentityProviderException;
import bes.tcc.auth_api.exception.InvalidCredentialsException;
import java.net.URI;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class KeycloakAuthService {
	private final RestClient restClient;
	private final ObjectMapper objectMapper;
	private final URI tokenEndpoint;
	private final String clientId;

	public KeycloakAuthService(
			KeycloakProperties properties,
			RestClient.Builder restClientBuilder,
			ObjectMapper objectMapper) {
		this.restClient = restClientBuilder.build();
		this.objectMapper = objectMapper;
		this.clientId = properties.clientId();
		this.tokenEndpoint = UriComponentsBuilder.fromUriString(properties.baseUrl())
				.path("/realms/{realm}/protocol/openid-connect/token")
				.buildAndExpand(properties.realm())
				.toUri();
	}

	public TokenResponse authenticate(LoginRequest loginRequest) {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("grant_type", "password");
		form.add("client_id", clientId);
		form.add("username", loginRequest.username());
		form.add("password", loginRequest.password());

		try {
			TokenResponse tokenResponse = restClient.post()
					.uri(tokenEndpoint)
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.body(form)
					.retrieve()
					.body(TokenResponse.class);
			if (tokenResponse == null) {
				throw new IdentityProviderException("Keycloak returned an empty token response.", null);
			}
			return tokenResponse;
		} catch (RestClientResponseException exception) {
			if (isInvalidCredentials(exception)) {
				throw new InvalidCredentialsException();
			}
			throw new IdentityProviderException("Keycloak rejected the token request.", exception);
		} catch (RestClientException exception) {
			throw new IdentityProviderException("Could not communicate with Keycloak.", exception);
		}
	}

	private boolean isInvalidCredentials(RestClientResponseException exception) {
		try {
			JsonNode response = objectMapper.readTree(exception.getResponseBodyAsString());
			return "invalid_grant".equals(response.path("error").asText());
		} catch (JacksonException parseException) {
			throw new IdentityProviderException("Keycloak returned an invalid error response.", parseException);
		}
	}
}

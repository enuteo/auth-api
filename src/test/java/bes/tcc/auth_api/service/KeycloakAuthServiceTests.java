package bes.tcc.auth_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import bes.tcc.auth_api.config.KeycloakProperties;
import bes.tcc.auth_api.dto.LoginRequest;
import bes.tcc.auth_api.dto.TokenResponse;
import bes.tcc.auth_api.exception.InvalidCredentialsException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

class KeycloakAuthServiceTests {
	private final RestClient.Builder restClientBuilder = RestClient.builder();
	private final MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
	private final KeycloakAuthService service = new KeycloakAuthService(
			new KeycloakProperties("http://localhost:8080", "authtest", "auth-api-test"),
			restClientBuilder,
			new ObjectMapper());

	@Test
	void authenticatesWithClientIdAndUserCredentials() {
		server.expect(requestTo(
						"http://localhost:8080/realms/authtest/protocol/openid-connect/token"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(content().string(org.hamcrest.Matchers.allOf(
						org.hamcrest.Matchers.containsString("grant_type=password"),
						org.hamcrest.Matchers.containsString("client_id=auth-api-test"),
						org.hamcrest.Matchers.containsString("username=user"),
						org.hamcrest.Matchers.containsString("password=userpass"))))
				.andRespond(withSuccess(
						"{\"access_token\":\"access\",\"refresh_token\":\"refresh\","
								+ "\"token_type\":\"Bearer\",\"expires_in\":300}",
						MediaType.APPLICATION_JSON));

		TokenResponse response = service.authenticate(new LoginRequest("user", "userpass"));

		assertEquals("access", response.accessToken());
		assertEquals("refresh", response.refreshToken());
		assertEquals("Bearer", response.tokenType());
		server.verify();
	}

	@Test
	void rejectsInvalidUserCredentials() {
		server.expect(requestTo(
						"http://localhost:8080/realms/authtest/protocol/openid-connect/token"))
				.andRespond(withBadRequest()
						.body("{\"error\":\"invalid_grant\","
								+ "\"error_description\":\"Invalid user credentials\"}")
						.contentType(MediaType.APPLICATION_JSON));

		assertThrows(
				InvalidCredentialsException.class,
				() -> service.authenticate(new LoginRequest("user", "wrong")));
		server.verify();
	}
}

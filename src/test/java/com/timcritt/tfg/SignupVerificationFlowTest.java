package com.timcritt.tfg;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest(
        classes = TfgApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class SignupVerificationFlowTest {

    private static final String MAILPIT_HOST = "localhost";
    private static final int MAILPIT_SMTP_PORT = 1025;
    private static final int MAILPIT_UI_PORT = 8025;

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @DynamicPropertySource
    static void registerMailProperties(DynamicPropertyRegistry registry) {
        assertMailpitIsReachable();

        registry.add("spring.mail.host", () -> MAILPIT_HOST);
        registry.add("spring.mail.port", () -> MAILPIT_SMTP_PORT);
        registry.add("app.mail.from", () -> "no-reply@tfg.local");

        System.out.println("[Test] Using Mailpit SMTP at " + MAILPIT_HOST + ":" + MAILPIT_SMTP_PORT);
        System.out.println("[Test] Mailpit UI: http://" + MAILPIT_HOST + ":" + MAILPIT_UI_PORT + "/");
    }

    private static void assertMailpitIsReachable() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(MAILPIT_HOST, MAILPIT_SMTP_PORT), 2000);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Expected Mailpit to already be running at "
                            + MAILPIT_HOST + ":" + MAILPIT_SMTP_PORT
                            + " but it was not reachable. Start your Docker dev environment first.",
                    e
            );
        }
    }

    @Test
    void signup_shouldSucceed() {
        TestUser user = newTestUser();

        ResponseEntity<Map> response = signupUser(user);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void signup_shouldCreateVerificationTokenInDatabase() throws Exception {
        TestUser user = newTestUser();

        ResponseEntity<Map> signupResponse = signupUser(user);
        assertThat(signupResponse.getStatusCode().is2xxSuccessful()).isTrue();

        String token = waitForVerificationToken(user.email());

        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
    }

    @Test
    void login_beforeEmailConfirmation_shouldReturn403() throws Exception {
        TestUser user = newTestUser();

        ResponseEntity<Map> signupResponse = signupUser(user);
        assertThat(signupResponse.getStatusCode().is2xxSuccessful()).isTrue();

        try {
            ResponseEntity<Map> loginResponse = loginUser(user);

            assertThat(loginResponse.getStatusCode().value()).isEqualTo(403);
            assertThat(loginResponse.getBody()).containsEntry("error", "Please confirm your email");

        } catch (HttpClientErrorException e) {
            Map body = mapper.readValue(e.getResponseBodyAsString(), Map.class);

            assertThat(e.getStatusCode().value()).isEqualTo(403);
            assertThat(body).containsEntry("error", "Please confirm your email");
        }
    }

    @Test
    void confirmEmail_withValidToken_shouldSucceed() throws Exception {
        TestUser user = newTestUser();

        ResponseEntity<Map> signupResponse = signupUser(user);
        assertThat(signupResponse.getStatusCode().is2xxSuccessful()).isTrue();

        String token = waitForVerificationToken(user.email());
        assertThat(token).isNotNull();

        ResponseEntity<Map> confirmResponse = confirmEmail(token);

        assertThat(confirmResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(confirmResponse.getBody()).containsEntry("message", "Email confirmed");
    }

    @Test
    void login_afterEmailConfirmation_shouldSucceedAndReturnBearerToken() throws Exception {
        TestUser user = newTestUser();

        ResponseEntity<Map> signupResponse = signupUser(user);
        assertThat(signupResponse.getStatusCode().is2xxSuccessful()).isTrue();

        String token = waitForVerificationToken(user.email());
        assertThat(token).isNotNull();

        ResponseEntity<Map> confirmResponse = confirmEmail(token);
        assertThat(confirmResponse.getStatusCode().is2xxSuccessful()).isTrue();

        ResponseEntity<Map> loginResponse = loginUser(user);

        assertThat(loginResponse.getStatusCode().is2xxSuccessful()).isTrue();

        String authHeader = loginResponse.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        assertThat(authHeader).isNotNull();
        assertThat(authHeader).startsWith("Bearer ");
    }

    // ------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------

    private TestUser newTestUser() {
        String suffix = String.valueOf(System.currentTimeMillis());
        String username = "ituser" + suffix;

        return new TestUser(
                username,
                "ITest",
                "User",
                username + "@example.com",
                "Password123!"
        );
    }

    private ResponseEntity<Map> signupUser(TestUser user) {
        Map<String, String> payload = Map.of(
                "username", user.username(),
                "name", user.name(),
                "surname", user.surname(),
                "email", user.email(),
                "password", user.password()
        );

        return rest.postForEntity(
                url("/api/auth/signup"),
                new HttpEntity<>(payload, jsonHeaders()),
                Map.class
        );
    }

    private ResponseEntity<Map> loginUser(TestUser user) {
        Map<String, String> payload = Map.of(
                "username", user.username(),
                "password", user.password()
        );

        return rest.postForEntity(
                url("/api/auth/login"),
                new HttpEntity<>(payload, jsonHeaders()),
                Map.class
        );
    }

    private ResponseEntity<Map> confirmEmail(String token) {
        return rest.getForEntity(
                url("/api/auth/confirm-email?token=" + token),
                Map.class
        );
    }

    private String waitForVerificationToken(String email) throws InterruptedException {
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < 10_000) {
            List<String> tokens = jdbcTemplate.queryForList(
                    """
                    select token
                    from email_verification_token
                    where user_email = ?
                    order by created_at desc
                    limit 1
                    """,
                    new Object[]{email},
                    String.class
            );

            if (!tokens.isEmpty() && tokens.get(0) != null && !tokens.get(0).isBlank()) {
                return tokens.get(0);
            }

            Thread.sleep(250);
        }

        fail("Timed out waiting for verification token for email: " + email);
        return null;
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private record TestUser(
            String username,
            String name,
            String surname,
            String email,
            String password
    ) {}
}
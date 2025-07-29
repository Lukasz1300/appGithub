package com.example.appApi;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 9561)

public class GitHubApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private final String username = "testuser";

    @BeforeEach
    void setupWireMock() {
        WireMock.reset();

        // Mock dla pobierania repozytoriów użytkownika
        stubFor(get(urlEqualTo("/users/" + username + "/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {
                                    "name": "repo1",
                                    "fork": false,
                                    "owner": { "login": "testuser" }
                                  },
                                  {
                                    "name": "repo2",
                                    "fork": true,
                                    "owner": { "login": "testuser" }
                                  }
                                ]
                                """)));

        // Mock dla pobierania branchy repozytorium "repo1"
        stubFor(get(urlEqualTo("/repos/testuser/repo1/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {
                                    "name": "main",
                                    "commit": { "sha": "abc123" }
                                  },
                                  {
                                    "name": "dev",
                                    "commit": { "sha": "def456" }
                                  }
                                ]
                                """)));
    }

    @Test
    void shouldReturnRepositoriesWithBranches_whenUserExists() {
        // given
        String url = "http://localhost:" + port + "/api/github/users/" + username + "/repositories";

        // when
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("repo1");
        assertThat(response.getBody()).contains("main", "abc123");
        assertThat(response.getBody()).contains("dev", "def456");
        assertThat(response.getBody()).doesNotContain("repo2"); // bo to fork
    }
}

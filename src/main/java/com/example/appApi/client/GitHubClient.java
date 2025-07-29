package com.example.appApi.client;

import com.example.appApi.dto.BranchDto;
import com.example.appApi.dto.RepositoryDto;
import com.example.appApi.exception.GitHubApiException;
import com.example.appApi.exception.GitHubUserNotFoundException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubClient {

    @Value("${github.api.url}")
    private String githubApiUrl; // np. http://localhost:9561/users/{username}/repos

    @Value("${github.api.branches-url}")
    private String githubBranchesUrl; // np. http://localhost:9561/repos/{owner}/{repo}/branches

    private final RestTemplate restTemplate;

    public List<RepositoryDto> getUserRepositories(String username) {
        try {
            ResponseEntity<List<RepositoryResponse>> response = restTemplate.exchange(
                    githubApiUrl,
                    HttpMethod.GET,
                    createHeaders(),
                    new ParameterizedTypeReference<>() {},
                    username
            );

            List<RepositoryResponse> repositories = response.getBody();

            if (repositories == null) {
                return Collections.emptyList();
            }

            return repositories.stream()
                    .filter(repo -> !repo.isFork())
                    .map(repo -> {
                        List<BranchDto> branches = fetchBranches(repo.getOwner().getLogin(), repo.getName());
                        return new RepositoryDto(repo.getName(), repo.getOwner().getLogin(), branches);
                    })
                    .collect(Collectors.toList());

        } catch (HttpClientErrorException.NotFound e) {
            throw new GitHubUserNotFoundException("GitHub user not found: " + username);
        } catch (RestClientException e) {
            log.error("Error while fetching GitHub data for user {}: {}", username, e.getMessage());
            throw new GitHubApiException("Error communicating with GitHub API");
        }
    }

    private List<BranchDto> fetchBranches(String owner, String repo) {
        try {
            ResponseEntity<List<BranchResponse>> response = restTemplate.exchange(
                    githubBranchesUrl,
                    HttpMethod.GET,
                    createHeaders(),
                    new ParameterizedTypeReference<>() {},
                    owner, repo
            );

            List<BranchResponse> branches = response.getBody();

            if (branches == null) {
                return Collections.emptyList();
            }

            return branches.stream()
                    .map(branch -> new BranchDto(branch.getName(), branch.getCommit().getSha()))
                    .collect(Collectors.toList());

        } catch (RestClientException e) {
            log.error("Error while fetching branches for {}/{}: {}", owner, repo, e.getMessage());
            throw new GitHubApiException("Error fetching branches for repository");
        }
    }

    private HttpEntity<?> createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "GitHub-Repository-Fetcher");
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return new HttpEntity<>(headers);
    }

    // --- Wewnętrzne klasy pomocnicze do mapowania odpowiedzi z GitHuba ---

    @Data
    private static class RepositoryResponse {
        private String name;
        private boolean fork;
        private OwnerResponse owner;
    }

    @Data
    private static class OwnerResponse {
        private String login;
    }

    @Data
    private static class BranchResponse {
        private String name;
        private CommitResponse commit;
    }

    @Data
    private static class CommitResponse {
        private String sha;
    }
}

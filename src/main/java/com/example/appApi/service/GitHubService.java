package com.example.appApi.service;


import com.example.appApi.client.GitHubClient;
import com.example.appApi.dto.RepositoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GitHubService {
    private final GitHubClient gitHubClient;

    public List<RepositoryDto> getUserRepositories(String username) {
        return gitHubClient.getUserRepositories(username);
    }
}
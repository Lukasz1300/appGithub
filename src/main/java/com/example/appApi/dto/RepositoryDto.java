package com.example.appApi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class RepositoryDto {
    private String repositoryName;
    private String ownerLogin;
    private List<BranchDto> branches;
}

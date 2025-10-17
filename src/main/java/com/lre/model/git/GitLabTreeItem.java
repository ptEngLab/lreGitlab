package com.lre.model.git;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GitLabTreeItem {
    private String id;
    private String name;
    private String type; // "tree" for folders, "blob" for files
    private String path;
    private String mode;
    
    @JsonProperty("ref")
    private String branch;
}
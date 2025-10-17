package com.lre.model.git;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GitLabCommit {
    private String id;
    
    @JsonProperty("short_id")
    private String shortId;
    
    private String title;
    private String message;
    
    @JsonProperty("author_name")
    private String authorName;
    
    @JsonProperty("author_email")
    private String authorEmail;
    
    @JsonProperty("committed_date")
    private String committedDate; 
    
    @JsonProperty("web_url")
    private String webUrl;
    
    public String getSha() {
        return id;
    }
}
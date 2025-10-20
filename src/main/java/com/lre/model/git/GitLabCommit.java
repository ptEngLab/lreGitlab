package com.lre.model.git;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GitLabCommit {

    @JsonProperty("id")
    private String sha = "";
    
    @JsonProperty("committed_date")
    private String committedDate = "";

    @JsonProperty("path")
    private String path = "";

    @JsonIgnore
    public boolean isEmpty() {
        return sha.isBlank() && committedDate.isBlank();
    }
}
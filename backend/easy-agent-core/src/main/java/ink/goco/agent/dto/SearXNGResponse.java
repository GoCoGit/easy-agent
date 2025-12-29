package ink.goco.agent.dto;

import lombok.Data;

import java.util.List;

@Data
public class SearXNGResponse {
    private List<SearchResult> results;
    private String query;
}

package ink.goco.agent.entity;

import lombok.Data;

import java.util.List;

@Data
public class SearXNGResponse {
    private List<SearchResult> results;
    private String query;
}

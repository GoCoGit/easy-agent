package ink.goco.agent.entity;

import lombok.Data;

@Data
public class SearchResult {
    private String title;
    private String url;
    private String content;
    private Double score;
}

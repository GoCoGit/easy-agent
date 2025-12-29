package ink.goco.agent.dto;

import lombok.Data;

@Data
public class SearchResult {
    private String title;
    private String url;
    private String content;
    private Double score;
}

package ink.goco.agent.constant;

public class MyPrompt {

    public static final String RAG_PROMPT = """
            基于上下文的知识库回答问题：
            【上下文】
            {context}
            
            【问题】
            {question}
            
            【输出】
            如果没有查到，请回复：不知道。
            如果查到，请回复具体的内容。不相关的近似内容不必提到。
            """;

    public static final String SEARXNG_PROMPT = """
            你是一个互联网搜索大师，请基于以下互联网返回的结果作为上下文，根据你的理解结合用户的提问综合后，生成并且输出专业的回答：
            【上下文】
            {context}
            
            【问题】
            {question}
            
            【输出】
            如果没有查到，请回复：不知道。
            如果查到，请回复具体的内容。
            """;


    public static final String SYSTEM_PROMPT = """
            Your name is WangHuaHua, The King of Code!
            """;
}

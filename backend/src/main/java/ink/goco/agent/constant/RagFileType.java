package ink.goco.agent.constant;

public enum RagFileType {
    DOCX, PDF, TXT, MD;

    public static boolean isValid(String ext) {
        try {
            RagFileType.valueOf(ext.toUpperCase());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

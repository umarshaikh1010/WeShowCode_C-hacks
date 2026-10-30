public class Lesson {
    private final String id;
    private final String subjectId;
    private final String title;
    private final String readTime;
    private final String contentChunk;

    public Lesson(String id, String subjectId, String title, String readTime, String contentChunk) {
        this.id = id;
        this.subjectId = subjectId;
        this.title = title;
        this.readTime = readTime;
        this.contentChunk = contentChunk;
    }

    public String getId() {
        return id;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public String getTitle() {
        return title;
    }

    public String getReadTime() {
        return readTime;
    }

    public String getContentChunk() {
        return contentChunk;
    }
}

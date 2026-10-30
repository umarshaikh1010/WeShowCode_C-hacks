import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChapterNote {
    private final String id;
    private final int grade;
    private final String subject;
    private final String chapter;
    private final String title;
    private final List<String> pages = new ArrayList<>();

    public ChapterNote(String id, int grade, String subject, String chapter, String title) {
        this.id = id;
        this.grade = grade;
        this.subject = subject;
        this.chapter = chapter;
        this.title = title;
    }

    public void addPage(String pageContent) {
        this.pages.add(pageContent);
    }

    public String getId() {
        return id;
    }

    public int getGrade() {
        return grade;
    }

    public String getSubject() {
        return subject;
    }

    public String getChapter() {
        return chapter;
    }

    public String getTitle() {
        return title;
    }

    public int getTotalPages() {
        return pages.isEmpty() ? 1 : pages.size();
    }

    public String getPageContent(int pageNumber) {
        if (pages.isEmpty()) return "<p>Notes currently being compiled for this chapter.</p>";
        int idx = Math.max(0, Math.min(pageNumber - 1, pages.size() - 1));
        return pages.get(idx);
    }

    public List<String> getPages() {
        return Collections.unmodifiableList(pages);
    }
}


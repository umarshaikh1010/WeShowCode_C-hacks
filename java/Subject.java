import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Subject {
    private final String id;
    private final int grade;
    private final String name;
    private final String description;
    private final String iconSvgPath;
    private final String color;
    private final List<Lesson> lessons = new ArrayList<>();

    public Subject(String id, int grade, String name, String description, String iconSvgPath, String color) {
        this.id = id;
        this.grade = grade;
        this.name = name;
        this.description = description;
        this.iconSvgPath = iconSvgPath;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public int getGrade() {
        return grade;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIconSvgPath() {
        return iconSvgPath;
    }

    public String getColor() {
        return color;
    }

    public List<Lesson> getLessons() {
        return Collections.unmodifiableList(lessons);
    }

    public void addLesson(Lesson lesson) {
        this.lessons.add(lesson);
    }
}

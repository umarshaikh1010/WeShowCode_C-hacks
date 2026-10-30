public class Formula {
    private final String id;
    private final int grade;
    private final String subject;
    private final String chapter;
    private final String title;
    private final String formula;
    private final String description;
    private final String examTip;

    public Formula(String id, int grade, String subject, String chapter, String title,
                   String formula, String description, String examTip) {
        this.id = id;
        this.grade = grade;
        this.subject = subject;
        this.chapter = chapter;
        this.title = title;
        this.formula = formula;
        this.description = description;
        this.examTip = examTip;
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

    public String getFormula() {
        return formula;
    }

    public String getDescription() {
        return description;
    }

    public String getExamTip() {
        return examTip;
    }
}


public class Question {
    private final int id;
    private final String subjectId;
    private final int grade;
    private final String chapter;
    private final String yearOrSource;
    private final String difficulty;
    private final String questionText;
    private final String[] options;
    private final int correctAnswerIndex;
    private final String explanation;

    public Question(int id, String subjectId, int grade, String chapter, String yearOrSource, String difficulty,
                    String questionText, String[] options, int correctAnswerIndex, String explanation) {
        this.id = id;
        this.subjectId = subjectId;
        this.grade = grade;
        this.chapter = chapter;
        this.yearOrSource = yearOrSource;
        this.difficulty = difficulty;
        this.questionText = questionText;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
        this.explanation = explanation;
    }

    // Convenience constructor for legacy check-in questions
    public Question(int id, String subjectId, String questionText, String[] options, int correctAnswerIndex, String explanation) {
        this(id, subjectId, subjectId.startsWith("12") ? 12 : 11, "Core Chapter", "CBSE PYQ", "Standard",
             questionText, options, correctAnswerIndex, explanation);
    }

    public int getId() {
        return id;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public int getGrade() {
        return grade;
    }

    public String getChapter() {
        return chapter;
    }

    public String getYearOrSource() {
        return yearOrSource;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String[] getOptions() {
        return options;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public String getExplanation() {
        return explanation;
    }
}

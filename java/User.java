import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class User {
    private final String username;
    private final String password;
    private int gradeLevel;
    private int quizzesTaken;
    private int totalScore;
    private int testsTaken;
    private int testQuestionsAttempted;
    private int testQuestionsCorrect;
    private final Set<String> completedLessons = ConcurrentHashMap.newKeySet();
    private final Set<Integer> bookmarkedQuestions = ConcurrentHashMap.newKeySet();

    public User(String username, String password, int gradeLevel) {
        this.username = username;
        this.password = password;
        this.gradeLevel = gradeLevel;
        this.quizzesTaken = 0;
        this.totalScore = 0;
        this.testsTaken = 0;
        this.testQuestionsAttempted = 0;
        this.testQuestionsCorrect = 0;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(int gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public int getQuizzesTaken() {
        return quizzesTaken;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getTestsTaken() {
        return testsTaken;
    }

    public int getTestQuestionsAttempted() {
        return testQuestionsAttempted;
    }

    public int getTestQuestionsCorrect() {
        return testQuestionsCorrect;
    }

    public Set<String> getCompletedLessons() {
        return Collections.unmodifiableSet(completedLessons);
    }

    public void markLessonCompleted(String lessonId) {
        if (lessonId != null && !lessonId.isEmpty()) {
            completedLessons.add(lessonId);
        }
    }

    public synchronized void recordQuizResult(int score) {
        this.quizzesTaken++;
        this.totalScore += score;
    }

    public synchronized void recordTestResult(int attempted, int correct) {
        this.testsTaken++;
        this.testQuestionsAttempted += attempted;
        this.testQuestionsCorrect += correct;
        this.totalScore += correct;
    }

    // Bookmark management for Notebook
    public boolean toggleBookmark(int questionId) {
        if (bookmarkedQuestions.contains(questionId)) {
            bookmarkedQuestions.remove(questionId);
            return false;
        } else {
            bookmarkedQuestions.add(questionId);
            return true;
        }
    }

    public boolean isBookmarked(int questionId) {
        return bookmarkedQuestions.contains(questionId);
    }

    public Set<Integer> getBookmarkedQuestions() {
        return Collections.unmodifiableSet(bookmarkedQuestions);
    }

    public int getAccuracyPercent() {
        int totalQuestions = quizzesTaken + testQuestionsAttempted;
        int totalCorrect = totalScore;
        if (totalQuestions == 0) return 0;
        return (int) Math.round(((double) totalCorrect / totalQuestions) * 100);
    }

    public int getOverallReadiness() {
        // Factors: lesson completion (up to 40%), quiz & test performance (up to 40%), notebook engagement (up to 20%)
        int lessonFactor = Math.min(40, completedLessons.size() * 5);
        int testFactor = Math.min(40, totalScore * 5);
        int bookmarkFactor = Math.min(20, bookmarkedQuestions.size() * 4);
        int total = lessonFactor + testFactor + bookmarkFactor;
        return Math.min(100, Math.max(15, total)); // Baseline 15% on enrollment
    }
}

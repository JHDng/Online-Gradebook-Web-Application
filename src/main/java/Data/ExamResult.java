package Data;

public class ExamResult {
	private Student student;
	private GradeStatus gradeStatus;
	private Course course;
	private ExamDate examDate;
	public Student getStudent() {
		return student;
	}
	public void setStudent(Student student) {
		this.student = student;
	}
	public GradeStatus getGradeStatus() {
		return gradeStatus;
	}
	public void setGradeStatus(GradeStatus gradeStatus) {
		this.gradeStatus = gradeStatus;
	}
	public Course getCourse() {
		return course;
	}
	public void setCourse(Course course) {
		this.course = course;
	}
	public ExamDate getExamDate() {
		return examDate;
	}
	public void setExamDate(ExamDate examDate) {
		this.examDate = examDate;
	}
}
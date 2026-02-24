package Data;

import java.time.LocalDate;

public class ExamDate {
	private int examDateID;
	private int courseID;
	private LocalDate date;
	
	public int getExamDateID() {
		return examDateID;
	}
	public void setExamDateID(int examDateID) {
		this.examDateID = examDateID;
	}
	public int getCourseID() {
		return courseID;
	}
	public void setCourseID(int courseID) {
		this.courseID = courseID;
	}
	public LocalDate getDate() {
		return date;
	}
	public void setDate(LocalDate date) {
		this.date = date;
	}
}
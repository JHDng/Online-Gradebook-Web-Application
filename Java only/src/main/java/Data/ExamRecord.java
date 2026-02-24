package Data;

import java.time.LocalDate;
import java.time.LocalTime;

public class ExamRecord {
	private int recordID;
	private LocalDate creationDate;
	private LocalTime creationTime;
	private ExamDate examDate;
	private Course course;
	public int getRecordID() {
		return recordID;
	}
	public void setRecordID(int recordID) {
		this.recordID = recordID;
	}
	public LocalDate getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(LocalDate creationDate) {
		this.creationDate = creationDate;
	}
	public LocalTime getCreationTime() {
		return creationTime;
	}
	public void setCreationTime(LocalTime creationTime) {
		this.creationTime = creationTime;
	}
	public ExamDate getExamDate() {
		return examDate;
	}
	public void setExamDate(ExamDate examDate) {
		this.examDate = examDate;
	}
	public Course getCourse() {
		return course;
	}
	public void setCourse(Course course) {
		this.course = course;
	}
	
	
}
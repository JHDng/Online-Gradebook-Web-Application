package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import Data.Course;
import Data.ExamDate;
import Data.ExamRecord;
import Data.GradeStatus;
import Data.Student;
import Data.StudentRegistration;

public class ExamRecordDAO {
	private Connection connection;

	public ExamRecordDAO(Connection connection) {
		this.connection = connection;
	}
	
	public int produceRecord(String professorUUID, int examDateID, List<Integer> idList) throws SQLException {
		String query = "INSERT into records VALUES(default, ?, ?, ?, ?)";
		PreparedStatement pstatement = null;
		int code = 0;
		int res = 0;
		int recordID = -1;
		try {
			pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			pstatement.setString(1, LocalDate.now().toString());
			pstatement.setString(2, LocalTime.now().toString());
			pstatement.setString(3, professorUUID);
			pstatement.setInt(4, examDateID);
			res = pstatement.executeUpdate();
			
			try (ResultSet generatedKeys = pstatement.getGeneratedKeys()) {
	            if (generatedKeys.next()) {
	                recordID = generatedKeys.getInt(1);
	            } else {
	                throw new SQLException("Creating record failed, no ID obtained.");
	            }
	        }
			
			for(int id : idList) {
				code += updateStudentsRecords(id, recordID);
			}
			
			if(code == idList.size() && res == 1) {
				return recordID;
			} else {
				return -1;
			}
		} catch (SQLException e0) {
			throw new SQLException(e0);
		} finally {
			try {
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
		}
	}
	
	private int updateStudentsRecords(int studentID, int recordID) throws SQLException {
		String query = "INSERT into students_records VALUES(?, ?)";
		PreparedStatement pstatement = null;
		int code = 0;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, studentID);
			pstatement.setInt(2, recordID);
			code = pstatement.executeUpdate();
			
		} catch (SQLException e0) {
			throw new SQLException(e0);
		} finally {
			try {
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
		}
		return code;
	}
	
	public ExamRecord findRecordbyRecordID(int recordID, String professorUUID) throws SQLException {
		ExamRecord record = null;
		String query =  "SELECT " +
					    "r.RecordID AS RecordID, " +
					    "r.CreationDate AS RecordCreationDate, " +
					    "r.CreationTime AS RecordCreationTime, " +
			
					    "ed.ExamDateID AS ExamDateID, " +
					    "ed.Date AS ExamDate, " +
			
					    "c.CourseID AS CourseID, " +
					    "c.Name AS CourseName " +
			
					    "FROM records r " +
					    "JOIN examdates ed ON r.ExamDateID = ed.ExamDateID " +
					    "JOIN courses c ON ed.CourseID = c.CourseID " +
					    "WHERE r.RecordID = ? AND r.ProfessorUUID = ?";

		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, recordID);
			pstatement.setString(2, professorUUID);
			result = pstatement.executeQuery();
			while (result.next()) {
		        record = new ExamRecord();
		        record.setRecordID(result.getInt("RecordID"));
		        record.setCreationDate(((java.sql.Date) result.getDate("RecordCreationDate")).toLocalDate());
		        record.setCreationTime(((java.sql.Time) result.getTime("RecordCreationTime")).toLocalTime());
		        
		        ExamDate ed = new ExamDate();
		        ed.setCourseID(result.getInt("CourseID"));
		        ed.setExamDateID(result.getInt("ExamDateID"));
		        ed.setDate(((java.sql.Date) result.getDate("ExamDate")).toLocalDate());
		        record.setExamDate(ed);
		        
		        Course c = new Course();
		        c.setCourseID(result.getInt("CourseID"));
		        c.setName(result.getString("CourseName"));
		        record.setCourse(c);
		        
		        return record;
			}
		} catch (SQLException e0) {
			throw new SQLException(e0);
		} finally {
			try {
				if (result != null) {
					result.close();
				}
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}
		
		return record;
	}
	
	public List<StudentRegistration> findStudentsByRecordID(int recordID) throws SQLException {
		List<StudentRegistration> srList = new ArrayList<>();
		String query =  "SELECT " +
		
						"s.StudentID AS StudentID, " +
						"s.Major AS StudentMajor, " +
						 
						"u.Name AS UserName, " +
						"u.Surname AS UserSurname, " +
						
						"se.Grade AS Grade " +
		
					    "FROM records r " +
					    "JOIN students_records sr ON sr.RecordID = r.RecordID " +
					    "JOIN students s ON s.StudentID = sr.StudentID " +
					    "JOIN users u ON u.UserUUID = s.UserUUID " +
					    "JOIN students_examdates se ON se.StudentID = s.StudentID AND se.ExamDateID = r.ExamDateID " +
					    "WHERE r.RecordID = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, recordID);
			result = pstatement.executeQuery();
			while (result.next()) {
		        StudentRegistration sr = new StudentRegistration();
		        
		        Student s = new Student();
		        s.setName(result.getString("UserName"));
		        s.setSurname(result.getString("UserSurname"));
		        s.setStudentID(result.getInt("StudentID"));
		        s.setMajor(result.getString("StudentMajor"));
		        sr.setStudent(s);
		        
		        GradeStatus gs = new GradeStatus();
		        gs.setGrade(result.getInt("Grade"));
		        sr.setGradeStatus(gs);
		        
		        srList.add(sr);
			}
		} catch (SQLException e0) {
			throw new SQLException(e0);
		} finally {
			try {
				if (result != null) {
					result.close();
				}
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}
		return srList;
	}
	
	public List<ExamRecord> findExamRecordsByProfessorUUID(String professorUUID) throws SQLException {
		List<ExamRecord> erList = new ArrayList<>();
		String query =  "SELECT " +
						"r.RecordID AS RecordID, " +
					    "r.CreationDate AS RecordCreationDate, " +
					    "r.CreationTime AS RecordCreationTime, " +
					    
					    "ed.Date AS Date, " +
					    "c.CourseID AS CourseID, " +
					    "c.Name AS CourseName " +
					    
					    "FROM records r " +
					    "JOIN examdates ed ON ed.ExamDateID = r.ExamDateID " +
					    "JOIN courses c ON c.CourseID = ed.CourseID " +
					    "WHERE r.ProfessorUUID = ?";

		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, professorUUID);
			result = pstatement.executeQuery();
			while (result.next()) {
		        ExamRecord er = new ExamRecord();
		        er.setRecordID(result.getInt("RecordID"));
		        er.setCreationDate(((java.sql.Date) result.getDate("RecordCreationDate")).toLocalDate());
		        er.setCreationTime(((java.sql.Time) result.getTime("RecordCreationTime")).toLocalTime());
		        
		        ExamDate ed = new ExamDate();
		        ed.setDate(((java.sql.Date) result.getDate("Date")).toLocalDate());
		        er.setExamDate(ed);
		        
		        Course c = new Course();
		        c.setName(result.getString("CourseName"));
		        c.setCourseID(result.getInt("CourseID"));
		        er.setCourse(c);
		        
		        erList.add(er);
			}
		} catch (SQLException e0) {
			throw new SQLException(e0);
		} finally {
			try {
				if (result != null) {
					result.close();
				}
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}
		
		return erList;
	}
}
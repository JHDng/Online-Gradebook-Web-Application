package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import Data.Course;
import Data.ExamDate;
import Data.ExamDateRegistration;
import Data.ExamResult;
import Data.GradeStatus;
import Data.Student;

public class ExamDateDAO {
	private Connection connection;

	public ExamDateDAO(Connection connection) {
		this.connection = connection;
	}
	
	public int publishGrades(int examDateID) throws SQLException {
		String query = 	"UPDATE students_examdates " +
						"SET Status = 'published' " + 
						"WHERE Status = 'inserted' and ExamDateID = ?";
		PreparedStatement pstatement = null;
		int code = 0;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, examDateID);
			code = pstatement.executeUpdate();
		} catch (SQLException e0) {
			throw new SQLException(e0);
		} finally {
			try {
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}
		return code;
	}
	
	public int modifyGrade(int studentID, int examDateID, int grade) throws SQLException {
		String query = "UPDATE students_examdates " +
	               "SET Grade = ?, Status = ? " +
	               "WHERE StudentID = ? AND ExamDateID = ? AND Status NOT IN ('published', 'refused', 'recorded')";
		PreparedStatement pstatement = null;
		int code = 0;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, grade);
			pstatement.setString(2, "inserted");
			pstatement.setInt(3, studentID);
			pstatement.setInt(4, examDateID);
			code = pstatement.executeUpdate();
		} catch (SQLException e0) {
			throw new SQLException(e0);
		} finally {
			try {
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}
		return code;
	}
	
	public int modifyMultipleGrades(Map<String, String[]> studentToGrades, int examDateID) throws SQLException {
		String query = "UPDATE students_examdates " +
                 "SET Grade = ?, Status = ? " +
                 "WHERE StudentID = ? AND ExamDateID = ? AND Status = 'not inserted'";
		PreparedStatement pstatement = null;
		int[] res = {};
		try {
			connection.setAutoCommit(false);
			pstatement = connection.prepareStatement(query);
			
			for (String studentID : studentToGrades.keySet()) {
		        String grade = studentToGrades.get(studentID)[0];
		        
		        pstatement.setString(1, grade);
		        pstatement.setString(2, "inserted");
		        pstatement.setString(3, studentID);
		        pstatement.setInt(4, examDateID);
		        pstatement.addBatch();
		    }
			// returns array: example [1, 1, 0, ...]
			res = pstatement.executeBatch();
			connection.commit();
			
			// if one of the update goes wrong rollback
			if(Arrays.stream(res).anyMatch(n -> n == 0)) {
				connection.rollback();
				return 0;
			} else {
				return res.length;
			}
			
		} catch (SQLException e0) {
			if (connection != null) {
		        connection.rollback();
		    }
			throw new SQLException(e0);
		} finally {
			try {
				if (connection != null) {
			        connection.setAutoCommit(true);
			    }
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}
	}
	
	public List<ExamDateRegistration> findRegisteredDatesByStudentID(int studentID, int courseID) throws SQLException {
		List<ExamDateRegistration> dates = new ArrayList<>();
		String query = 	"SELECT e.ExamDateID, e.CourseID, e.Date, se.StudentID " + 
						"FROM examdates e " + 
						"LEFT JOIN students_examdates se on e.ExamDateID=se.ExamDateID and se.StudentID = ? " +
						"WHERE e.CourseID = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, studentID);
			pstatement.setInt(2, courseID);
			result = pstatement.executeQuery();
			while (result.next()) {
                ExamDateRegistration examDateReg = new ExamDateRegistration();
                ExamDate examDate = new ExamDate();
                examDate.setExamDateID(result.getInt("ExamDateID"));
                examDate.setCourseID(result.getInt("CourseID"));
                examDate.setDate(((java.sql.Date) result.getDate("Date")).toLocalDate());
                examDateReg.setExamDate(examDate);
                if(result.getString("StudentID") == null) {
                	examDateReg.setRegistered(false);
                } else {
                	examDateReg.setRegistered(true);
                }
                dates.add(examDateReg);
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
		return dates;
	}
	
	public int subscribeToExam(int studentID, int examDateID) throws SQLException {
		String query = "INSERT INTO students_examdates VALUES (?, ?, default, default)";
		PreparedStatement pstatement = null;
		int code = 0;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, studentID);
			pstatement.setInt(2, examDateID);
			code = pstatement.executeUpdate();
		} catch (SQLException e0) {
			throw new SQLException(e0);
		} finally {
			try {
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}
		return code;
	}
	
	public List<ExamDate> findDatesByCourseID(int courseID, String professorUUID) throws SQLException {
		List<ExamDate> dates = new ArrayList<>();
		String query = "SELECT e.ExamDateID, e.CourseID, e.Date " + 
					   "FROM examdates e " +
					   "JOIN courses c on e.CourseID=c.CourseID " +
					   "WHERE c.CourseID = ? and ProfessorUUID = ? ";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, courseID);
			pstatement.setString(2, professorUUID);
			result = pstatement.executeQuery();
			while (result.next()) {
                ExamDate examDate = new ExamDate();
                examDate.setExamDateID(result.getInt("ExamDateID"));
                examDate.setCourseID(result.getInt("CourseID"));
                examDate.setDate(((java.sql.Date) result.getDate("Date")).toLocalDate());
                dates.add(examDate);
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
		return dates;
	}
	
	public int refuseGrade(int studentID, int examDateID) throws SQLException {
		String query = "UPDATE students_examdates " +
	               "SET Status = 'refused' " +
	               "WHERE StudentID = ? AND ExamDateID = ? AND Status = 'published' " +
	               "AND Grade NOT IN (-1, -2, -3, -4)";
		PreparedStatement pstatement = null;
		int code = 0;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, studentID);
			pstatement.setInt(2, examDateID);
			code = pstatement.executeUpdate();
		} catch (SQLException e0) {
			throw new SQLException(e0);
		} finally {
			try {
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}
		return code;
	}
	
	public int recordGradesStart(String professorUUID, int examDateID) throws SQLException {
		// Step 1: finds the studentIDs to associate with the record
		List<Integer> idList = new ArrayList<>();
		String query = 	"SELECT StudentID " +
						"FROM students_examdates " +
						"WHERE ExamDateID = ? AND (Status = 'published' OR Status = 'refused')";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		int recordID = -1;
		try {
			connection.setAutoCommit(false);
			
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, examDateID);
			result = pstatement.executeQuery();
			while (result.next()) {
				idList.add(result.getInt("StudentID"));
			}
			if(idList.isEmpty()) {
				return -1;
			} else {
				recordID = recordGradesUpdate(professorUUID, examDateID, idList);
				connection.commit();				
			}
		} catch (SQLException e0) {
			try {
				if(connection != null) {
					connection.rollback();
				}
			} catch (SQLException rollbackEx) {
				System.out.println("Error in connectionRollback");
	            rollbackEx.printStackTrace();
			}
			throw new SQLException(e0);
		} finally {
			try {				
				if(connection != null) {
					connection.setAutoCommit(true);
				}
			} catch (Exception e0) {
				throw new SQLException(e0);
			}
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
		
		return recordID;
	}
	
	private int recordGradesUpdate(String professorUUID, int examDateID, List<Integer> idList) throws SQLException {
		String query = 	"UPDATE students_examdates " +
						"SET Grade = CASE WHEN Status = 'refused' THEN -2 ELSE Grade END, " +
						"Status = 'recorded' " +
						"WHERE ExamDateID = ? AND (Status = 'published' OR Status = 'refused')";
		PreparedStatement pstatement = null;
		int recordID  = -1;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, examDateID);
			pstatement.executeUpdate();
			
			recordID = new ExamRecordDAO(connection).produceRecord(professorUUID, examDateID, idList);
		} catch (SQLException e0) {
			throw new SQLException(e0);
		} finally {
			try {
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}
		return recordID;
	}
	
	public ExamResult findExamResultByStudIDExamDateID(int studentID, int examDateID) throws SQLException {
		ExamResult er = null;
		
		String query = "SELECT s.StudentID AS StudentID, " +
	               "u.Name AS StudentName, " +
	               "u.Surname AS StudentSurname, " +
	               "e.Date AS ExamDate, " +
	               "c.CourseID AS CourseID, " +
	               "c.Name AS CourseName, " +
	               "se.Grade AS Grade, " +
	               "se.Status AS Status, " +
	               "s.Major AS Major " +
	               "FROM students_examdates se " +
	               "JOIN students s ON s.StudentID = se.StudentID " +
	               "JOIN users u ON u.UserUUID = s.UserUUID " +
	               "JOIN examdates e ON se.ExamDateID = e.ExamDateID " +
	               "JOIN courses c ON e.CourseID = c.CourseID " +
	               "WHERE s.StudentID = ? AND se.ExamDateID = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, studentID);
			pstatement.setInt(2, examDateID);
			result = pstatement.executeQuery();
			while (result.next()) {
				er = new ExamResult();
				Course c = new Course();
				c.setCourseID(result.getInt("CourseID"));
				c.setName(result.getString("CourseName"));
				ExamDate e = new ExamDate();
				e.setCourseID(result.getInt("CourseID"));
				e.setDate(((java.sql.Date) result.getDate("ExamDate")).toLocalDate());
				e.setExamDateID(0);
				GradeStatus gs = new GradeStatus();
				gs.setStatus(result.getString("Status"));
				int grade = result.getInt("Grade");
				// -1 Absent
				// -2 Failed
				// -3 Failed(Deferred)
				// -4 Undefined
				if(result.wasNull())	gs.setGrade(-4);
				else					gs.setGrade(grade);
				Student s = new Student();
				s.setName(result.getString("StudentName"));
				s.setSurname(result.getString("StudentSurname"));
				s.setMajor(result.getString("Major"));
				s.setStudentID(result.getInt("StudentID"));
				
				er.setStudent(s);
				er.setGradeStatus(gs);
				er.setExamDate(e);
				er.setCourse(c);
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
		return er;
	}
}
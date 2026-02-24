package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Data.GradeStatus;
import Data.Student;
import Data.StudentRegistration;

public class StudentDAO {
	private Connection connection;

	public StudentDAO(Connection connection) {
		this.connection = connection;
	}
	
	
	
	public Student findStudentByStudIDExamDateID(int studentID, int examDateID) throws SQLException {
		Student s = null;
		String query = 	"SELECT u.Username, u.Email, u.Name, u.Surname, u.UserType, s.StudentID, s.Major " +
						"FROM users u " +
						"JOIN students s ON u.UserUUID = s.UserUUID " +
						"JOIN students_examdates se ON s.StudentID = se.StudentID " +
						"WHERE s.StudentID = ? and se.ExamDateID = ? and (se.Status = 'inserted' or se.Status = 'not inserted')";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, studentID);
			pstatement.setInt(2, examDateID);
			result = pstatement.executeQuery();
			while (result.next()) {
				s = new Student();
				s.setStudentID(result.getInt("StudentID"));
				s.setUsername(result.getString("Username"));
				s.setEmail(result.getString("Email"));
				s.setMajor(result.getString("Major"));
				s.setName(result.getString("Name"));
				s.setSurname(result.getString("Surname"));
				s.setUserType(result.getString("UserType"));
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
		return s;
	}
	
	public List<StudentRegistration> findStudentsGradesByExamDateID(int examDateID) throws SQLException {
		List<StudentRegistration> studReg = new ArrayList<>();
		String query = 	"SELECT u.Username, u.Email, u.Name, u.Surname, u.UserType, s.StudentID, s.Major, se.Grade, se.Status " +
						"FROM users u " +
						"JOIN students s ON u.UserUUID = s.UserUUID " +
						"JOIN students_examdates se ON s.StudentID = se.StudentID " +
						"WHERE se.ExamDateID = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, examDateID);
			result = pstatement.executeQuery();
			while (result.next()) {
				StudentRegistration sr = new StudentRegistration();
				Student s = new Student();
				s.setStudentID(result.getInt("StudentID"));
				s.setUsername(result.getString("Username"));
				s.setEmail(result.getString("Email"));
				s.setMajor(result.getString("Major"));
				s.setName(result.getString("Name"));
				s.setSurname(result.getString("Surname"));
				s.setUserType(result.getString("UserType"));
				sr.setStudent(s);
				GradeStatus gs = new GradeStatus();
				int grade = result.getInt("Grade");
				// -1 Absent
				// -2 Failed
				// -3 Failed(Deferred)
				// -4 Undefined
				if(result.wasNull())	gs.setGrade(-4);
				else					gs.setGrade(grade);
				gs.setStatus(result.getString("Status"));
				sr.setGradeStatus(gs);
				studReg.add(sr);
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
		return studReg;
	}
}
package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Data.Course;

public class CourseDAO {
	private Connection connection;

	public CourseDAO(Connection connection) {
		this.connection = connection;
	}
	
	public List<Course> findCoursesByProfessorUUID(String userUUID) throws SQLException {
		List<Course> courses = new ArrayList<>();
		String query = "SELECT CourseID, Name from courses WHERE ProfessorUUID = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, userUUID);
			result = pstatement.executeQuery();
			while (result.next()) {
				Course course = new Course();
				course.setCourseID(result.getInt("CourseID"));
				course.setName(result.getString("Name"));
				courses.add(course);
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
		return courses;
	}
	
	public List<Course> findCoursesByStudentID(int studentID) throws SQLException {
		List<Course> courses = new ArrayList<>();
		String query = 	"SELECT c.CourseID, c.Name " + 
						"FROM students_courses sc " +
						"JOIN courses c on sc.CourseID=c.CourseID " +
						"WHERE StudentID = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, studentID);
			result = pstatement.executeQuery();
			while (result.next()) {
				Course course = new Course();
				course.setCourseID(result.getInt("CourseID"));
				course.setName(result.getString("Name"));
				courses.add(course);
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
		return courses;
	}
}
package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import Data.Student;
import Data.User;

public class UserDAO {
	private Connection connection;

	public UserDAO(Connection connection) {
		this.connection = connection;
	}

	public User checkCredentials(String username, String password) throws SQLException {
		PasswordToolkit pt = new PasswordToolkit();
		String passFromDB = null;
		String query = 	"SELECT u.UserUUID, u.Username, u.UserType, u.Email, u.Name, u.Surname, u.Password, s.Major, s.StudentID " + 
						"FROM users u " +
						"LEFT JOIN students s on u.UserUUID=s.UserUUID " +
						"WHERE Username = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, username);
			result = pstatement.executeQuery();
			while (result.next()) {
				passFromDB = result.getString("Password");
				if(pt.hashPassword(password, Base64.getDecoder().decode(passFromDB.split("\\$")[0]))
						.equals(passFromDB.split("\\$")[1])) { 
					if(result.getString("UserType").equals("professor")) {						
						User user = new User();
						user.setUserUUID(result.getString("UserUUID"));
						user.setUsername(result.getString("Username"));
						user.setUserType(result.getString("UserType"));
						user.setEmail(result.getString("Email"));
						user.setName(result.getString("Name"));
						user.setSurname(result.getString("Surname"));
						return user;
					} else if (result.getString("UserType").equals("student")) {
						Student stud = new Student();
						stud.setUserUUID(result.getString("UserUUID"));
						stud.setUsername(result.getString("Username"));
						stud.setUserType(result.getString("UserType"));
						stud.setEmail(result.getString("Email"));
						stud.setName(result.getString("Name"));
						stud.setSurname(result.getString("Surname"));
						stud.setMajor(result.getString("Major"));
						stud.setStudentID(result.getInt("StudentID"));
						return stud;
					}
				}
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
		return null;
	}

	public List<User> findUsersByUUID(String UUID) throws SQLException {
		List<User> users = new ArrayList<>();
		String query = "SELECT Username, UserType, Email, Name, Surname" + 
					   "FROM users WHERE UserUUID = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, UUID);
			result = pstatement.executeQuery();
			while (result.next()) {
				User user = new User();
				user.setUsername(result.getString("Username"));
				user.setUserType(result.getString("UserType"));
				user.setEmail(result.getString("Email"));
				user.setName(result.getString("Name"));
				user.setSurname(result.getString("Surname"));
				users.add(user);
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
		return users;
	}

	public List<User> findUsersByEmail(String email) throws SQLException {
		List<User> users = new ArrayList<>();
		String query = "SELECT Username, UserType, Email, Name, Surname " + 
					   "FROM users WHERE Email = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, email);
			result = pstatement.executeQuery();
			while (result.next()) {
				User user = new User();
				user.setUsername(result.getString("Username"));
				user.setUserType(result.getString("UserType"));
				user.setEmail(result.getString("Email"));
				user.setName(result.getString("Name"));
				user.setSurname(result.getString("Surname"));
				users.add(user);
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
		return users;
	}

	public List<User> findUsersByUsername(String username) throws SQLException {
		List<User> users = new ArrayList<>();
		String query = "SELECT Username, UserType, Email, Name, Surname " + 
		               "FROM users WHERE Username = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, username);
			result = pstatement.executeQuery();
			while (result.next()) {
				User user = new User();
				user.setUsername(result.getString("Username"));
				user.setUserType(result.getString("UserType"));
				user.setEmail(result.getString("Email"));
				user.setName(result.getString("Name"));
				user.setSurname(result.getString("Surname"));
				users.add(user);
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
		return users;
	}

	public int registerUser(String username, String password, String name, String surname, String email, String userType, int studentID, String major) throws SQLException {
		String uuid = UUID.randomUUID().toString();

		try {
			while(!findUsersByUUID(uuid).isEmpty()) { // there is a copy of the generated UUID
				uuid = UUID.randomUUID().toString();
			}
		} catch (SQLException e) {
			System.out.println("Error in findUsersByUUID in registerUser");
			e.printStackTrace();
		}

		String query = "INSERT into users (UserUUID, Username, Password, Name, Surname, Email, UserType)   VALUES(?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement pstatement = null;
		PasswordToolkit pt = new PasswordToolkit();
		byte[] salt = pt.generateSalt();
		int code = 0;

		try {
			if(userType.equals("student")) {
				connection.setAutoCommit(false);
			}
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, uuid);
			pstatement.setString(2, username);
			pstatement.setString(3, Base64.getEncoder().encodeToString(salt) + "$" + pt.hashPassword(password, salt));
			pstatement.setString(4, name);
			pstatement.setString(5, surname);
			pstatement.setString(6, email);
			pstatement.setString(7, userType);
			code = pstatement.executeUpdate();
			if(userType.equals("student")) {
				try {
					insertStudent(uuid, studentID, major);
				} catch (SQLException e) {
					System.out.println("Error in insertStudent");
					e.printStackTrace();
					throw new SQLException(e);
				}
				connection.commit();
			}
		} catch (SQLException e0) {
			try {
				if(connection != null && userType.equals("student")) {
					connection.rollback();
				}
			} catch (SQLException rollbackEx) {
				System.out.println("Error in connectionRollback");
	            rollbackEx.printStackTrace();
			}
			throw new SQLException(e0);
		} finally {
			try {
				if(connection != null && userType.equals("student")) {
					connection.setAutoCommit(true);
				}
				if(pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
		}
		return code;
	}

	private int insertStudent(String userUUID, int studentID, String major) throws SQLException {
		String query = "INSERT into students (UserUUID, StudentID, Major)   VALUES(?, ?, ?)";
		PreparedStatement pstatement = null;
		int code = 0;

		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, userUUID);
			pstatement.setInt(2, studentID);
			pstatement.setString(3, major);
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
}
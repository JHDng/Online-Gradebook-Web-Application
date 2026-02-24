package Servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import DAO.UserDAO;
import Data.SafeChecker;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class RegisterStudent
 */
@WebServlet("/RegisterUser")
public class RegisterUser extends HttpServlet {
	private static final long serialVersionUID = 1L;

	
	// in a realist scenario this could need a table in the database
	private static final List<String> majorsList = List.of("computer", "mechanical", "civil", "electrical");

	private Connection connection;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterUser() {
        super();
        // TODO Auto-generated constructor stub
    }

    @Override
	public void init() throws ServletException {
		try {
			ServletContext context = getServletContext();
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String password = context.getInitParameter("dbPassword");
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, password);
		} catch (ClassNotFoundException e) {
			throw new UnavailableException("Can't load database driver");
		} catch (SQLException e) {
			throw new UnavailableException("Couldn't get db connection");
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String name = request.getParameter("name");
		String surname = request.getParameter("surname");
		String email = request.getParameter("email");
		String userType= request.getParameter("userType");
		String major = request.getParameter("major");
		String studentID_str = request.getParameter("studentID");
		int studentID = 0;
		int ret = 0;

		if(!SafeChecker.isStringSafe(username) || !SafeChecker.isStringSafe(password) || !SafeChecker.isStringSafe(name)
				|| !SafeChecker.isStringSafe(surname) || !SafeChecker.isStringSafe(email) || !SafeChecker.isStringSafe(userType)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters or max length reached (127)");
			return;
		}

		if(!SafeChecker.isEmailSafe(email)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid email");
			return;
		}

		if(!userType.equals("student") && !userType.equals("professor")) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user type");
			return;
		}

		if(userType.equals("student")) {
			if(!SafeChecker.isStringSafe(major) || !SafeChecker.isStringSafe(studentID_str)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters or max length reached (127)");
				return;
			}
			if(!majorsList.contains(major)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid major");
				return;
			}
			if(!SafeChecker.isStringSafe(studentID_str) || studentID_str.length() != 6) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid studentID: must be a number with 6 digits");
				return;
			} else {
				try {					
					studentID = Integer.parseInt(studentID_str);
				} catch (NumberFormatException e) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid student id format");
					return;
				}
			}
		} 

		UserDAO uDAO = new UserDAO(connection);
		try {
			if(uDAO.findUsersByEmail(email).size() != 0 || uDAO.findUsersByUsername(username).size() != 0) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid email or username: already registered");
				return;
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error in finding users");
			return;
		}

		try {
			ret = uDAO.registerUser(username, password, name, surname, email, userType, studentID, major);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error in register user");
			return;
		}

		if(ret == 1) {
			response.sendRedirect("login.html");
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "return value not 1");
		}
	}

	@Override
	public void destroy() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}
}

package Servlets;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import com.google.gson.Gson;

import DAO.CourseDAO;
import Data.Course;
import Data.Student;

/**
 * Servlet implementation class GetCoursesStudent
 */
@WebServlet("/GetCoursesStudent")
public class GetCoursesStudent extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetCoursesStudent() {
        super();
        // TODO Auto-generated constructor stub
    }
    
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
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CourseDAO cDAO = new CourseDAO(connection);
		List<Course> courses = null;
		
		try {
			// Finds courses with provided studentID
			courses = cDAO.findCoursesByStudentID(((Student) request.getSession().getAttribute("user")).getStudentID());
			if (courses == null || courses.isEmpty()) { // Courses not found
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				response.getWriter().println("No courses found");
	    		return;
	        } else {
	        	// Rearrange courses list
	        	courses.sort((c1, c2) -> c2.getName().compareToIgnoreCase(c1.getName()));
	        	String json = new Gson().toJson(courses);
	        	response.setStatus(HttpServletResponse.SC_OK);
	    		response.setContentType("application/json");
	    		response.setCharacterEncoding("UTF-8");
	    		response.getWriter().write(json);
	    		return;
	        }
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Error in finding courses");
			e.printStackTrace();
			return;
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
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

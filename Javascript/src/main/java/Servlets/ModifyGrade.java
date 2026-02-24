package Servlets;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import DAO.ExamDateDAO;
import DAO.StudentDAO;
import Data.SafeChecker;
import Data.Student;

/**
 * Servlet implementation class UpdateGrade
 */
@WebServlet("/ModifyGrade")
@MultipartConfig
public class ModifyGrade extends HttpServlet {
	private static final long serialVersionUID = 1L;
	List<Integer> validGradesList = List.of(-1,-2,-3,18,19,20,21,22,23,24,25,26,27,28,29,30,31);
	private Connection connection = null;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ModifyGrade() {
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
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String examDateID_str = request.getParameter("examDateID");
		String studentID_str = request.getParameter("studentID");
		String grade_str = request.getParameter("grade");
		int studentID = 0;
		int examDateID = 0;
		int grade = 0;
		
		// Checks for string safety and parses them
		if(!SafeChecker.isStringSafe(studentID_str) || studentID_str.length() != 6) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Invalid student id");
			return;
		} else {
			try {
				studentID = Integer.parseInt(studentID_str);				
			} catch (NumberFormatException e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Invalid student id format");
				return;
			}
		}
		
		if(!SafeChecker.isStringSafe(examDateID_str)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Invalid exam date id");
			return;
		} else {
			try {				
				examDateID = Integer.parseInt(examDateID_str);
			} catch (NumberFormatException e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Invalid exam date id format");
				return;
			}
		}
		
		if(!SafeChecker.isStringSafe(grade_str)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Invalid grade");
			return;
		} else {
			try {				
				grade = Integer.parseInt(grade_str);
			} catch (NumberFormatException e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Invalid grade format");
				return;
			}
			if(!validGradesList.contains(grade)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Invalid grade value");
				return;
			}
		}
		
		StudentDAO sDAO = new StudentDAO(connection);
		ExamDateDAO eDAO = new ExamDateDAO(connection);
		try {
			// Checks if there is a student with provided studentID and examDateID
			Student s = sDAO.findStudentByStudIDExamDateID(studentID, examDateID);
			if(s == null) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				response.getWriter().println("No student found in such exam date");
				return;
			} else {
				try {					
					// Modify grade
					int ret = eDAO.modifyGrade(studentID, examDateID, grade);
					// If ret == 0 something went wrong
					if(ret == 0) {
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						response.getWriter().println("Couldn't modify grade");
						return;
					}
				} catch(SQLException e) {
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					response.getWriter().println("Error in modifying grade");
					e.printStackTrace();
					return;
				}
			}
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Error in finding student by id");
			e.printStackTrace();
			return;
		}
	}
	
	@Override
	public void destroy() {
	      try {
	        if (connection != null){
	            connection.close();
	        }
	      } catch (SQLException sqle) {
	    	  sqle.printStackTrace();
	      }
    }

}

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import DAO.ExamDateDAO;
import DAO.StudentDAO;
import Data.SafeChecker;
import Data.Student;

/**
 * Servlet implementation class ModifyMultipleGrades
 */
@WebServlet("/ModifyMultipleGrades")
@MultipartConfig
public class ModifyMultipleGrades extends HttpServlet {
	private static final long serialVersionUID = 1L;
	List<Integer> validGradesList = List.of(-1,-2,-3,18,19,20,21,22,23,24,25,26,27,28,29,30,31);
	private Connection connection = null;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ModifyMultipleGrades() {
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
			// Gets the parameters map from request (studentID -> grade)
			List<String> keysToRemove = new ArrayList<>();
			Map<String, String[]> parametersMap = new HashMap<>(request.getParameterMap());
			// Gets the examDateID from the map and removes it 
			String examDateID_str = parametersMap.remove("examDateID")[0];
			int examDateID = -1;
			
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
			
			StudentDAO sDAO = new StudentDAO(connection);
			ExamDateDAO eDAO = new ExamDateDAO(connection);
			try {
				for(Entry<String, String[]> stud_grade : parametersMap.entrySet()) {
					String studentID_str = stud_grade.getKey();
					String grade_str = stud_grade.getValue()[0];
					int studentID = -1;
					int grade = -99;
					// Checks strings safety and parses them
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
					
					if(!SafeChecker.isStringSafe(grade_str)) {
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						response.getWriter().println("Invalid grade string format");
						return;
					} else {
						try {
							grade = Integer.parseInt(grade_str);				
						} catch (NumberFormatException e) {
							response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
							response.getWriter().println("Invalid grade format");
							return;
						}
					}
					
					// -4 corresponds to "not inserted"
					// If no modification was made filter it out
					if(grade == -4) {
						keysToRemove.add(studentID_str);
					} else if(!validGradesList.contains(grade)) {
						// Check grade validity
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						response.getWriter().println("Invalid grade present");
						return;
					} else {					
						// Checks for student with provided examDateID
						Student s = sDAO.findStudentByStudIDExamDateID(studentID, examDateID);
						if(s == null) {
							response.setStatus(HttpServletResponse.SC_NOT_FOUND);
							response.getWriter().println("No student found in such exam date");
							return;
						}
					}
					
				}
				
				// Removes the "not inserted" students from the map
				for (String key : keysToRemove) {
				    parametersMap.remove(key);
				}
				
				try {	
					// Modify all grades in one shot
					int ret = eDAO.modifyMultipleGrades(parametersMap, examDateID);
					// If one of the update goes wrong returns 0
					if(ret == 0) {
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						response.getWriter().println("Couldn't modify grades");
						return;
					}
				} catch(SQLException e) {
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					response.getWriter().println("Error in modifying grade");
					e.printStackTrace();
					return;
				}
				
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Error in finding student by id");
				e.printStackTrace();
				return;
			}
	}

}

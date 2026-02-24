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
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import DAO.StudentDAO;
import Data.SafeChecker;
import Data.StudentRegistration;

/**
 * Servlet implementation class GetRegisteredStudents
 */
@WebServlet("/GetRegisteredStudents")
public class GetRegisteredStudents extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetRegisteredStudents() {
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
		String examDateID_str = request.getParameter("examDateID");
		int examDateID = 0;
		
		// Checks for strings safety and parses them
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
		List<StudentRegistration> sr = new ArrayList<>();
		
		// Finds registered students with provided examDateID
		try {
		    sr = sDAO.findStudentsGradesByExamDateID(examDateID);
		    if (sr == null || sr.isEmpty()) {
		    	response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				response.getWriter().println("No registered students found");
				return;
		    }
		} catch (SQLException e) {
		    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Error in finding registered students");
		    e.printStackTrace();
		    return;
		}
		
		String json = new Gson().toJson(sr);
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(json);
		return;
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
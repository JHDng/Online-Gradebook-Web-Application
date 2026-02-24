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
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import Adapters.LocalDateAdapter;
import DAO.ExamDateDAO;
import Data.ExamDateRegistration;
import Data.SafeChecker;
import Data.Student;

/**
 * Servlet implementation class GetDatesStudent
 */
@WebServlet("/GetDatesStudent")
public class GetDatesStudent extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetDatesStudent() {
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
		ExamDateDAO eDAO = new ExamDateDAO(connection);
		String courseID_str = request.getParameter("courseID");
		int courseID = 0;
		
		// Checks for strings safety then parses it
		if(SafeChecker.isStringSafe(courseID_str)) {
			try {	    				
				courseID = Integer.parseInt(courseID_str);
			} catch (NumberFormatException e){
				courseID = -1;
			}
		} else {
			// default value so no dates are returned in case of wrong courseID
			courseID = -1;
		}
		try {
			// Gets the dates with provided studentID and courseID
			int studentID = ((Student) request.getSession().getAttribute("user")).getStudentID();
			// Contains true if student is registered or else false
			List<ExamDateRegistration> dates = eDAO.findRegisteredDatesByStudentID(studentID, courseID);
    		if(dates == null || dates.isEmpty()) {
    			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				response.getWriter().println("No dates found");
	    		return;
    		} else {	    
    			// Rearrange dates list
    			dates.sort(Comparator.comparing((ExamDateRegistration r) -> r.getExamDate().getDate()).reversed());
				
				Gson gson = new GsonBuilder()
					    .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
					    .create();
				String json = gson.toJson(dates);
				
				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().println(json);
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

}

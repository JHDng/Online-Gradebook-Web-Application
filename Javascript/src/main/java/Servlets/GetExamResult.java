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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import Adapters.LocalDateAdapter;
import DAO.ExamDateDAO;
import Data.ExamResult;
import Data.SafeChecker;
import Data.Student;

/**
 * Servlet implementation class GetExamResult
 */
@WebServlet("/GetExamResult")
public class GetExamResult extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetExamResult() {
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
		
		int studentID = ((Student) request.getSession().getAttribute("user")).getStudentID();
		
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
		
		ExamDateDAO eDAO = new ExamDateDAO(connection);
		try {
			// Checks if there is an exam result with provided studentID and examDateID
			ExamResult e = eDAO.findExamResultByStudIDExamDateID(studentID, examDateID);
			if (e == null) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				response.getWriter().println("Couldn't find exam date");
				return;
		    } else {
		    	// Student can't see result if it is just inserted or not inserted
		    	if(e.getGradeStatus().getStatus().equals("inserted") ||
		    			e.getGradeStatus().getStatus().equals("not inserted")) {
		    		response.setStatus(HttpServletResponse.SC_OK);
		    		response.setContentType("application/json");
		    		response.setCharacterEncoding("UTF-8");
		    		response.getWriter().write("not published");
		    		return;
		    	} else {	 
		    		// Student can see result if published, recorded, refused
		    		Gson gson = new GsonBuilder()
		    				.registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
		    				.create();
		    		String json = gson.toJson(e);
		    		response.setStatus(HttpServletResponse.SC_OK);
		    		response.setContentType("application/json");
		    		response.setCharacterEncoding("UTF-8");
		    		response.getWriter().write(json);
		    		return;
		    	}
		    }
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Error in finding student grades");
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

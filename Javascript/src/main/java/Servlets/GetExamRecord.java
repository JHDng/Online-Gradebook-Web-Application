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
import java.time.LocalTime;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import Adapters.LocalDateAdapter;
import Adapters.LocalTimeAdapter;
import DAO.ExamRecordDAO;
import Data.ExamRecord;
import Data.SafeChecker;
import Data.StudentRegistration;
import Data.User;

/**
 * Servlet implementation class GetExamRecord
 */
@WebServlet("/GetExamRecord")
public class GetExamRecord extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetExamRecord() {
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
		String recordID_str = request.getParameter("recordID");
		int recordID = -1;
		
		// Checks for strings safety and parses them
		if(!SafeChecker.isStringSafe(recordID_str)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Invalid record id");
			return;
		} else {
			try {				
				recordID = Integer.parseInt(recordID_str);
			} catch (NumberFormatException e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Invalid record id format");
				return;
			}
		}
		
		ExamRecordDAO erDAO = new ExamRecordDAO(connection);
		try {
			// Checks if there are records with provided recordID
			ExamRecord er = erDAO.findRecordbyRecordID(recordID, ((User) request.getSession().getAttribute("user")).getUserUUID());
			if(er == null) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				response.getWriter().println("No exam record with that id");
				return;
			} else {
				// Checks if there are registrations with that recordID
				List<StudentRegistration> srList = erDAO.findStudentsByRecordID(er.getRecordID());
				if(srList == null || srList.isEmpty()) {
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
					response.getWriter().println("No students found in this record (impossible)");
					return;
				} else {	
					// Adds registration list to the exam record
					er.setSrList(srList);
					Gson gson = new GsonBuilder()
						    .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
						    .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
						    .create();
					String json = gson.toJson(er);
		        	response.setStatus(HttpServletResponse.SC_OK);
		    		response.setContentType("application/json");
		    		response.setCharacterEncoding("UTF-8");
		    		response.getWriter().write(json);
					return;
				}
			}
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Error in finding record by id");
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

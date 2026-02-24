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
import Data.User;

/**
 * Servlet implementation class GetRecords
 */
@WebServlet("/GetRecords")
public class GetRecords extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetRecords() {
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
		ExamRecordDAO erDAO = new ExamRecordDAO(connection);
		
		// Finds the records with provided professorUUID
		try {
			List<ExamRecord> erList = erDAO.findExamRecordsByProfessorUUID(((User) request.getSession().getAttribute("user")).getUserUUID());
			if(erList == null || erList.isEmpty()) {// not found case
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				response.getWriter().println("No records found");
				return;
			} else {
				// Sends the list to the client
				Gson gson = new GsonBuilder()
							    .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
							    .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
							    .create();
				String json = gson.toJson(erList);
	        	response.setStatus(HttpServletResponse.SC_OK);
	    		response.setContentType("application/json");
	    		response.setCharacterEncoding("UTF-8");
	    		response.getWriter().write(json);
	    		return;
			}
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Error in finding records");
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

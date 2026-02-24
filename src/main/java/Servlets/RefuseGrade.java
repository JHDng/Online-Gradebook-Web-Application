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

import DAO.ExamDateDAO;
import Data.SafeChecker;
import Data.Student;

/**
 * Servlet implementation class RefuseGrade
 */
@WebServlet("/RefuseGrade")
public class RefuseGrade extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RefuseGrade() {
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
		int studentID = ((Student) request.getSession().getAttribute("user")).getStudentID();
		
		String examDateID_str = request.getParameter("examDateID");
		int examDateID = 0;
		
		// Checks for strings safety and them parses them
		if(!SafeChecker.isStringSafe(examDateID_str)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid exam date id");
			return;
		} else {
			try {				
				examDateID = Integer.parseInt(examDateID_str);
			} catch (NumberFormatException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid exam date id format");
				return;
			}
		}
		
		// Refuse grade 
		ExamDateDAO eDAO = new ExamDateDAO(connection);
		try {
			eDAO.refuseGrade(studentID, examDateID);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error in refusing grade");
			e.printStackTrace();
			return;
		}
		
		String path = getServletContext().getContextPath() + "/GoToExamResult?examDateID=" + examDateID;
		response.sendRedirect(path);
		return;
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

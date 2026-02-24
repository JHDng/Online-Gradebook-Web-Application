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

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import DAO.ExamRecordDAO;
import Data.ExamRecord;
import Data.SafeChecker;
import Data.StudentRegistration;
import Data.User;

/**
 * Servlet implementation class GoToExamRecord
 */
@WebServlet("/GoToExamRecord")
public class GoToExamRecord extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GoToExamRecord() {
        super();
        // TODO Auto-generated constructor stub
    }

    public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(servletContext);    
		WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(webApplication);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");	

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
		String path = "/WEB-INF/Professor/examRecord.html";
		JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
		WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
		
		String recordID_str = request.getParameter("recordID");
		int recordID = -1;
		// Checks for strings safety and parses them
		if(!SafeChecker.isStringSafe(recordID_str)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid record id");
			return;
		} else {
			try {				
				recordID = Integer.parseInt(recordID_str);
			} catch (NumberFormatException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid record id format");
				return;
			}
		}
		
		ExamRecordDAO erDAO = new ExamRecordDAO(connection);
		try {
			// Ensures there is a record with that recordID
			ExamRecord er = erDAO.findRecordbyRecordID(recordID, ((User) request.getSession().getAttribute("user")).getUserUUID());
			if(er == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "No exam record with that id");
				return;
			} else { // Record present case
				// Gets the students 
				List<StudentRegistration> srList = erDAO.findStudentsByRecordID(er.getRecordID());
				if(srList == null || srList.isEmpty()) {
					response.sendError(HttpServletResponse.SC_NOT_FOUND, "No students found in this record (impossible)");
					return;
				} else {
					// Sets the variables
					ctx.setVariable("record", er);
					ctx.setVariable("studentRegistrations", srList);
					templateEngine.process(path, ctx, response.getWriter());
					return;
				}
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error in finding record by id");
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

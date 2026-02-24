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

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import DAO.StudentDAO;
import Data.SafeChecker;
import Data.Student;

/**
 * Servlet implementation class GoToGradeForm
 */
@WebServlet("/GoToGradeForm")
public class GoToGradeForm extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GoToGradeForm() {
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
		String path = "/WEB-INF/Professor/gradeForm.html";
		JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
		WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
		
		String examDateID_str = request.getParameter("examDateID");
		String studentID_str = request.getParameter("studentID");
		int studentID = 0;
		int examDateID = 0;
		
		// Checks for strings safety and parses them
		if(!SafeChecker.isStringSafe(studentID_str)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid student id");
			return;
		} else {
			try {				
				studentID = Integer.parseInt(studentID_str);
			} catch (NumberFormatException e){
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid student id format");
				return;
			}
		}
		
		if(!SafeChecker.isStringSafe(examDateID_str)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid exam date id");
			return;
		} else {
			try {
				examDateID = Integer.parseInt(examDateID_str);				
			} catch (NumberFormatException e){
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid exam date id format");
				return;
			}
		}
		
		StudentDAO sDAO = new StudentDAO(connection);
		try {
			// Checks if there is a student with that studentID and examDateID
			Student s = sDAO.findStudentByStudIDExamDateID(studentID, examDateID);
			if(s == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "No student found in such exam date");
				return;
			} else {				
				ctx.setVariable("student", s);
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error in finding student by id");
			e.printStackTrace();
			return;
		}
		
		ctx.setVariable("examDateID", examDateID);
		templateEngine.process(path, ctx, response.getWriter());
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

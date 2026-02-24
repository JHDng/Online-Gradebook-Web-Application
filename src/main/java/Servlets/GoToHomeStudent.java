package Servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import DAO.CourseDAO;
import DAO.ExamDateDAO;
import Data.Course;
import Data.ExamDateRegistration;
import Data.SafeChecker;
import Data.Student;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class GoToHomeStudent
 */
@WebServlet("/GoToHomeStudent")
public class GoToHomeStudent extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public GoToHomeStudent() {
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
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = "/WEB-INF/Student/homeStudent.html";
		JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
		WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
		
		CourseDAO cDAO = new CourseDAO(connection);
		ExamDateDAO eDAO = new ExamDateDAO(connection);
		List<Course> courses = null;
		String courseID_str = request.getParameter("courseID");
		int courseID = 0;
		
		try {
			// Checks if there are courses with provided studentID
			courses = cDAO.findCoursesByStudentID(((Student) request.getSession().getAttribute("user")).getStudentID());
			if (courses == null || courses.isEmpty()) { // Courses not found
	    		ctx.setVariable("hiddenTextCourse", "color:black;"); // no courses text on
	    		ctx.setVariable("hiddenTextDate", "display:none"); // no dates text off
	    		ctx.setVariable("hiddenTextName", "display:none");
	    		ctx.setVariable("Identifier", courseID);
	    		templateEngine.process(path, ctx, response.getWriter());
	    		return;
	        } else {
	        	// Checks for string safety then parses it
	    		if(SafeChecker.isStringSafe(courseID_str)) {
	    			try {	    				
	    				courseID = Integer.parseInt(courseID_str);
	    			} catch (NumberFormatException e){
	    				courseID = courses.getFirst().getCourseID();
	    			}
	    		} else {
	    			// Gets first course as default
	    			courseID = courses.getFirst().getCourseID();
	    		}
	    		// Gets dates with provided studentID and courseID
	    		int studentID = ((Student) request.getSession().getAttribute("user")).getStudentID();
	    		List<ExamDateRegistration> dates = eDAO.findRegisteredDatesByStudentID(studentID, courseID);
	    		if(dates == null || dates.isEmpty()) { // no dates found case
	    			ctx.setVariable("hiddenTextDate", "color:black;"); // no dates text on
	    			ctx.setVariable("hiddenTextName", "display:none");
	    			ctx.setVariable("Identifier", courseID);
	    		} else {	    
	    			// Rearrange dates list
	    			dates.sort(Comparator.comparing((ExamDateRegistration reg) -> reg.getExamDate().getDate()).reversed());
	    			ctx.setVariable("dates", dates); // dates found case
	    			ctx.setVariable("hiddenTextDate", "display:none"); // no dates text off
	    			ctx.setVariable("hiddenTextName", "color:black;");
	    			ctx.setVariable("Identifier", courseID);
	    		}
	    		// Rearrange the courses list
	    		courses.sort((c1, c2) -> c2.getName().compareToIgnoreCase(c1.getName()));
	    		ctx.setVariable("courses", courses);
	    		ctx.setVariable("hiddenTextCourse", "display:none"); // no courses text off
	    		templateEngine.process(path, ctx, response.getWriter());
	    		return;
	        }
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error in finding users");
			e.printStackTrace();
			return;
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
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

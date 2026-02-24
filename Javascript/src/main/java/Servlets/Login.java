package Servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.google.gson.Gson;

import DAO.UserDAO;
import Data.SafeChecker;
import Data.User;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
/**
 * Servlet implementation class LoginServlet
 */
@WebServlet("/Login")
@MultipartConfig
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection = null;
    /**
     * @see HttpServlet#HttpServlet()
     */
    @Override
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
    		throw new UnavailableException("Can't load db driver");
    	} catch (SQLException e) {
    		throw new UnavailableException("Couldn't connect");
    	}
    }


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		
		if(!SafeChecker.isStringSafe(username) || !SafeChecker.isStringSafe(password)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect fields or maximum length reached(127)");
			return;
		}

		UserDAO uDAO = new UserDAO(connection);

		try {
	        User user = uDAO.checkCredentials(username, password);
	        if (user == null) { // User not found
	        	response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().println("Incorrect credentials");
	        } else {
	        	request.getSession().setAttribute("user", user);
	        	User safeUser = user.safeCopy();
	        	String json = new Gson().toJson(safeUser);
				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().println(json);
			}
	    } catch (SQLException e) {
	        e.printStackTrace();
	        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        response.getWriter().println("Error in credentials checking");
	        return;
	    }
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

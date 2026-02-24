package Filters;

import java.io.IOException;

import Data.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class StudentFilter implements Filter {

	public StudentFilter() {

	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		System.out.print("Student filter executing ..\n");
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String loginpath = req.getServletContext().getContextPath() + "/login.html";
		HttpSession s = req.getSession();
		User u = null;
		u = (User) s.getAttribute("user");
		if (!u.getUserType().equals("student")) {
			res.setStatus(403);
			res.setHeader("Location", loginpath);
			return;
		}
		// pass the request along the filter chain
		chain.doFilter(request, response);
	}

}
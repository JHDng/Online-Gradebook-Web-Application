package Data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SafeChecker {
	
	private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
	private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
	
	public static boolean isStringSafe(String string) {
		if(string == null || string.isEmpty() || string.length() > 127) {
			return false;
		}
		return true;
	}
	
	public static boolean isEmailSafe(String email) {
        if (email == null) {
			return false;
		}
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
	}
}
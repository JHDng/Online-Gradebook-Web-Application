package DAO;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordToolkit {

	// more iterations = more secure
    private static final int ITERATIONS = 128;
    // 16 characters
    private static final int KEY_LENGTH = 100;

    public String hashPassword(String password, byte[] salt){
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = null;
        byte[] hashed = null;
		try {
			factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Error in secretFactoryKey");
			e.printStackTrace();
		}
		try {
			hashed = factory.generateSecret(spec).getEncoded();
		} catch (InvalidKeySpecException e) {
			System.out.println("Error in generateSecret");
			e.printStackTrace();
		}
        return Base64.getEncoder().encodeToString(hashed);
    }

    public byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }
}

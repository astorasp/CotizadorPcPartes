package mx.com.qtx.seguridad.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class to generate BCrypt hashes for testing purposes.
 * This class is used to generate password hashes for test data.
 */
public class BCryptPasswordGenerator {
    
    public static void main(String[] args) {
        // Create BCrypt encoder with strength 12
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        
        // Password to hash
        String plainPassword = "admin123";
        if (args.length > 0) {
            plainPassword = args[0];
        }
        
        // Generate hash
        String hashedPassword = encoder.encode(plainPassword);
        
        // Print results
        System.out.println("=== BCrypt Password Hash Generator ===");
        System.out.println("Plain Password: " + plainPassword);
        System.out.println("BCrypt Strength: 12");
        System.out.println("Generated Hash: " + hashedPassword);
        System.out.println("=====================================");
        
        // Verify the hash works
        boolean matches = encoder.matches(plainPassword, hashedPassword);
        System.out.println("Verification Test: " + (matches ? "PASSED" : "FAILED"));
        
        // Generate a few more hashes to show they're different each time
        System.out.println("\nAdditional hashes (showing salt randomness):");
        for (int i = 1; i <= 3; i++) {
            String anotherHash = encoder.encode(plainPassword);
            System.out.println("Hash " + i + ": " + anotherHash);
        }
    }
}
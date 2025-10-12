// Test file to demonstrate the new password generation
// This is just for testing - not part of the main application

public class PasswordGenerationTest {
    
    public static void main(String[] args) {
        System.out.println("=== Testing New Password Generation ===");
        
        // Test the new password generation
        for (int i = 0; i < 10; i++) {
            String password = generateDynamicPassword("VOTER_123456");
            System.out.println("Generated Password " + (i + 1) + ": " + password);
        }
    }
    
    private static String generateDynamicPassword(String voterId) {
        // Define character sets for password generation
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialChars = "@#$%&*!?";
        
        // Combine all character sets
        String allChars = uppercase + lowercase + numbers + specialChars;
        
        // Generate a random password of length 12
        StringBuilder password = new StringBuilder();
        java.util.Random random = new java.util.Random();
        
        // Ensure at least one character from each set
        password.append(uppercase.charAt(random.nextInt(uppercase.length())));
        password.append(lowercase.charAt(random.nextInt(lowercase.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));
        
        // Fill the remaining 8 characters randomly
        for (int i = 4; i < 12; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        
        // Shuffle the password to randomize positions
        String passwordStr = password.toString();
        char[] passwordArray = passwordStr.toCharArray();
        
        // Fisher-Yates shuffle algorithm
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }
        
        return new String(passwordArray);
    }
}

import java.io.*;
import java.util.*;

/**
 * Helpers.java - Shared Utility File
 * Contains static methods for ID generation, validation, and file operations
 * Used by all members to maintain data consistency
 */
public class Helpers {

    // ==========================================
    // 1. GENERATE UNIQUE ID
    // ==========================================
    /**
     * Generates a unique ID with the given prefix by scanning the file
     * and finding the highest existing number for that prefix.
     * 
     * @param prefix The ID prefix (e.g., "STU", "LEC", "AL", "ADM", "MOD", "AS", "CLS")
     * @param fileName The file to scan (e.g., "users.txt", "modules.txt")
     * @return The next available unique ID (e.g., "STU06" if "STU05" was highest)
     */
    public static String generateUniqueID(String prefix, String fileName) {
        int maxNum = 0;
        List<String> lines = readFromFile(fileName);
        
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            
            String[] parts = line.split("\\|");
            if (parts.length > 0) {
                String currentID = parts[0];
                
                // Check if this ID starts with our prefix
                if (currentID.startsWith(prefix)) {
                    try {
                        // Extract the numeric part after the prefix
                        String numPart = currentID.substring(prefix.length());
                        int num = Integer.parseInt(numPart);
                        if (num > maxNum) {
                            maxNum = num;
                        }
                    } catch (NumberFormatException e) {
                        // Skip if the ID doesn't follow expected format
                        continue;
                    }
                }
            }
        }
        
        // Generate next ID with proper padding
        int nextNum = maxNum + 1;
        
        // Format based on prefix type
        if (prefix.equals("MOD")) {
            // Modules use 3 digits: MOD101, MOD102, etc.
            return prefix + String.format("%03d", nextNum);
        } else {
            // Users and other entities use 2 digits: STU01, LEC01, etc.
            return prefix + String.format("%02d", nextNum);
        }
    }

    // ==========================================
    // 2. VALIDATE EMAIL
    // ==========================================
    /**
     * Validates email format by checking for @ and . symbols
     * 
     * @param email The email string to validate
     * @return true if email contains both @ and ., false otherwise
     */
    public static boolean validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.contains("@") && email.contains(".");
    }

    // ==========================================
    // 3. WRITE TO FILE (Universal Saver)
    // ==========================================
    /**
     * Appends a line of data to the specified file
     * Creates the file if it doesn't exist
     * 
     * @param fileName The target file name
     * @param data The data string to append (should be pipe-delimited)
     */
    public static void writeToFile(String fileName, String data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(data);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing to file " + fileName + ": " + e.getMessage());
        }
    }

    // ==========================================
    // 4. READ FROM FILE (Universal Loader)
    // ==========================================
    /**
     * Reads all lines from a file and returns them as a List
     * Returns empty list if file doesn't exist or is empty
     * 
     * @param fileName The file to read
     * @return List of all lines in the file
     */
    public static List<String> readFromFile(String fileName) {
        List<String> lines = new ArrayList<>();
        File file = new File(fileName);
        
        // Return empty list if file doesn't exist
        if (!file.exists()) {
            return lines;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading from file " + fileName + ": " + e.getMessage());
        }
        
        return lines;
    }

    // ==========================================
    // 5. OVERWRITE FILE (For Updates)
    // ==========================================
    /**
     * Completely overwrites a file with new data
     * Used for update operations where entire file needs to be rewritten
     * 
     * @param fileName The target file name
     * @param lines List of lines to write
     */
    public static void overwriteFile(String fileName, List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error overwriting file " + fileName + ": " + e.getMessage());
        }
    }

    // ==========================================
    // 6. CHECK IF FILE EXISTS AND IS NOT EMPTY
    // ==========================================
    /**
     * Checks if a file exists and contains data
     * 
     * @param fileName The file to check
     * @return true if file exists and has content, false otherwise
     */
    public static boolean fileExistsAndNotEmpty(String fileName) {
        File file = new File(fileName);
        return file.exists() && file.length() > 0;
    }

    // ==========================================
    // 7. DELETE RECORD FROM FILE
    // ==========================================
    /**
     * Deletes a record from a file based on ID (first field)
     * 
     * @param fileName The file to modify
     * @param id The ID of the record to delete
     * @return true if deletion successful, false otherwise
     */
    public static boolean deleteRecord(String fileName, String id) {
        List<String> lines = readFromFile(fileName);
        List<String> updatedLines = new ArrayList<>();
        boolean found = false;
        
        for (String line : lines) {
            String[] parts = line.split("\\|");
            if (parts.length > 0 && parts[0].equals(id)) {
                found = true;
                continue; // Skip this line (delete it)
            }
            updatedLines.add(line);
        }
        
        if (found) {
            overwriteFile(fileName, updatedLines);
        }
        
        return found;
    }

    // ==========================================
    // 8. UPDATE RECORD IN FILE
    // ==========================================
    /**
     * Updates a specific record in a file
     * 
     * @param fileName The file to modify
     * @param id The ID of the record to update
     * @param newRecord The complete new record string (pipe-delimited)
     * @return true if update successful, false otherwise
     */
    public static boolean updateRecord(String fileName, String id, String newRecord) {
        List<String> lines = readFromFile(fileName);
        List<String> updatedLines = new ArrayList<>();
        boolean found = false;
        
        for (String line : lines) {
            String[] parts = line.split("\\|");
            if (parts.length > 0 && parts[0].equals(id)) {
                updatedLines.add(newRecord);
                found = true;
            } else {
                updatedLines.add(line);
            }
        }
        
        if (found) {
            overwriteFile(fileName, updatedLines);
        }
        
        return found;
    }
}
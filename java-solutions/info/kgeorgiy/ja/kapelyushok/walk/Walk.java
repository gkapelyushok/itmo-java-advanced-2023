package info.kgeorgiy.ja.kapelyushok.walk;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Walk {
    private final Path inputPath;
    private final Path outputPath;
    private static final String ZEROS = "0".repeat(64);
    private static final int BUFFER_SIZE = 1 << 13;
    private static final byte[] buffer= new byte[BUFFER_SIZE];
    private static MessageDigest digest;

    private Walk(String inputFileName, String outputFileName) throws WalkException {
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new WalkException("Failed to hash: " + e.getMessage());
        }
        inputPath = getPath(inputFileName, "Invalid path input file: ");
        outputPath = getPath(outputFileName, "Invalid path output file: ");

        if (Files.notExists(outputPath)) {
            Path outputParentPath = outputPath.getParent();
            if (outputParentPath != null) {
                try {
                    Files.createDirectories(outputParentPath);
                } catch (IOException e) {
                    throw new WalkException("Failed to create directories: " + e.getMessage());
                }
            }
        }
    }

    private Path getPath(String fileName, String message) throws WalkException {
        try {
            return Paths.get(fileName);
        } catch (InvalidPathException e) {
            throw new WalkException(message + e.getMessage());
        }
    }
    private void walk() throws WalkException {
        try (BufferedReader reader = Files.newBufferedReader(inputPath)) {
            try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
                String fileName;
                while ((fileName = reader.readLine()) != null) {
                    String hash = getHash(fileName);
                    writer.write(hash + " " + fileName);
                    writer.newLine();
                }
            } catch (IllegalArgumentException e) {
                throw new WalkException("Illegal argument: " + e.getMessage());
            } catch (IOException e) {
                throw new WalkException("Failed to write: " + e.getMessage());
            } catch (UnsupportedOperationException e) {
                throw new WalkException("Unsupported operation: " + e.getMessage());
            } catch (SecurityException e) {
                throw new WalkException("Security exception: " + e.getMessage());
            }
        } catch (IOException e) {
            throw new WalkException("Failed to read: " + e.getMessage());
        } catch (SecurityException e) {
            throw new WalkException("Security exception: " + e.getMessage());
        }
    }
    private static String getHash(String fileName) {
        StringBuilder hexString;
        try {
            digest.reset();
            try (BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(Paths.get(fileName)))) {
                int count;
                while ((count = bis.read(buffer)) > 0) {
                    digest.update(buffer, 0, count);
                }
            }
            byte[] hash = digest.digest();
            hexString = new StringBuilder();
            for (byte aDigest : hash) {
                hexString.append(Integer.toHexString(0x100 | 0xFF & aDigest).substring(1));
            }
        } catch (final IllegalArgumentException | UnsupportedOperationException | IOException | SecurityException e) {
            return ZEROS;
        }
        return hexString.toString();
    }
    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Wrong arguments. Expected: <inputFileName> <outputFileName>");
            return;
        }
        try {
            new Walk(args[0], args[1]).walk();
        } catch (WalkException e) {
            System.err.println(e.getMessage());
        }
    }
}

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.Console;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class GenerateAdminHash {
    public static void main(String[] args) {
        Console console = System.console();
        if (console == null) {
            throw new IllegalStateException("Run this tool in an interactive terminal.");
        }
        char[] password = console.readPassword("New administrator password: ");
        if (password == null) {
            return;
        }
        try {
            CharBuffer input = CharBuffer.wrap(password);
            if (password.length < 8 || input.chars().allMatch(Character::isWhitespace)
                    || StandardCharsets.UTF_8.encode(input.duplicate()).remaining() > 72) {
                throw new IllegalArgumentException("Use at least 8 characters and at most 72 UTF-8 bytes.");
            }
            System.out.println(new BCryptPasswordEncoder().encode(input));
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}

package pro.sky.telegrambot.utils;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class FileReaderUtil {

    public String readFile(String filePath) throws IOException {
        try (InputStream is = ClassLoader.getSystemResourceAsStream(filePath)) {
            if (is == null) {
                throw new IOException("File not found: " + filePath);
            }
            return new String(is.readAllBytes());
        }
    }
}

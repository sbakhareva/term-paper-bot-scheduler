package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import pro.sky.telegrambot.utils.FileReaderUtil;

import java.io.IOException;

import static org.mockito.Mockito.*;

@SpringBootTest
class MessagingServiceTest {

    @Mock
    private NotificationTaskService notificationTaskService;
    @Mock
    private TelegramBot telegramBot;
    @Mock
    private FileReaderUtil fileReaderUtil;
    @InjectMocks
    private MessagingService messagingService;

    @Test
    void sendWelcomeMessageText() throws IOException {
        String welcomeMessage = "Hello!";
        long chatId = 123L;

        when(fileReaderUtil.readFile("messages/welcome_message.txt")).thenReturn(welcomeMessage);

        messagingService.sendWelcomeMessage(chatId);

        SendMessage expectedRequest = new SendMessage(chatId, welcomeMessage);
        verify(telegramBot).execute(expectedRequest);
    }

    @Test
    void addTaskMessageText() {
    }

    void sendWelcomePhotoTest() {

    }

    void processMessageTest() {

    }
}
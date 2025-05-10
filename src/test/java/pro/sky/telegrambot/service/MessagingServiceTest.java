package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.utils.FileReaderUtil;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(captor.capture());

        SendMessage sendMessage = captor.getValue();

        assertThat(sendMessage.getParameters().containsValue(chatId));
        assertThat(sendMessage.getParameters().containsValue(welcomeMessage));
    }

    @Test
    void addTaskMessageText() {
        String addTaskMessage = "Добавьте напоминание!";
        long chatId = 123L;

        messagingService.sendAddTaskMessage(chatId);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(captor.capture());

        SendMessage sendMessage = captor.getValue();

        assertThat(sendMessage.getParameters().containsValue(chatId));
        assertThat(sendMessage.getParameters().containsValue(addTaskMessage));
    }

    @Test
    void sendWelcomePhotoTest() {
        String imagePath = "src/main/resources/images/welcome_image.jpeg";
        long chatId = 123L;

        messagingService.sendWelcomePhoto(chatId);

        ArgumentCaptor<SendPhoto> captor = ArgumentCaptor.forClass(SendPhoto.class);
        verify(telegramBot).execute(captor.capture());

        SendPhoto sendPhoto = captor.getValue();


        assertThat(sendPhoto.getParameters().containsValue(chatId));
        assertThat(sendPhoto.getParameters().containsValue(new File(imagePath)));
    }

    @Test
    void processMessage_WhenFormatIsCorrectTest() {
        String message = "01.01.2022 20:00 Сделать домашнюю работу";
        long chatId = 123L;

        messagingService.processMessage(chatId, message);

        verify(notificationTaskService).addTask(any(NotificationTask.class));

        ArgumentCaptor<SendMessage> responseCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(responseCaptor.capture());

        SendMessage capturedResponse = responseCaptor.getValue();
        assertThat(capturedResponse.getParameters().values().contains(chatId));
        assertThat(capturedResponse.getParameters().containsValue("Напоминание успешно добавлено!"));
    }

    @Test
    void processMessage_WhenFormatIsNotCorrectTest() {
        String message = "oiia";
        long chatId = 123L;

        messagingService.processMessage(chatId, message);

        ArgumentCaptor<SendMessage> responseCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(responseCaptor.capture());

        SendMessage capturedResponse = responseCaptor.getValue();
        assertThat(capturedResponse.getParameters().containsValue(chatId));
        assertThat(capturedResponse.getParameters().containsValue("Неверный формат сообщения! Используйте сообщение вида: 01.01.2022 20:00 Сделать домашнюю работу"));
    }
}
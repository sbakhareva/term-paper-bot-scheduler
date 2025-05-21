package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.utils.FileReaderUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.LocalDateTime.parse;
import static java.util.regex.Pattern.compile;

@Service
public class MessagingService {

    private final NotificationTaskService notificationTaskService;
    private final TelegramBot telegramBot;
    private final FileReaderUtil fileReaderUtil;

    private final Set<Long> chatIds = new HashSet<>();

    public MessagingService(NotificationTaskService notificationTaskService,
                            TelegramBot telegramBot,
                            FileReaderUtil fileReaderUtil) {
        this.notificationTaskService = notificationTaskService;
        this.telegramBot = telegramBot;
        this.fileReaderUtil = fileReaderUtil;
    }

    public void sendWelcomeMessage(long chatId) {
        try {
            var is = fileReaderUtil.readFile("messages/welcome_message.txt");
            SendMessage request = new SendMessage(chatId, is);
            telegramBot.execute(request);
        } catch (IOException e) {
            System.out.println("Файла по такому пути нет!");
        }
    }

    public void sendAddTaskMessage(long chatId) {
        String addTaskMessage = "Напишите, какое напоминание вы хотите добавить, в формате '03.05.2025 17:28 Заняться делами!'";
        SendMessage sendMessage = new SendMessage(chatId, addTaskMessage);
        telegramBot.execute(sendMessage);
    }

    public void sendWelcomePhoto(long chatId) {
        String imagePath = "src/main/resources/images/welcome_image.jpeg";
        File photo = new File(imagePath);
        SendPhoto sendPhoto = new SendPhoto(chatId, photo);
        telegramBot.execute(sendPhoto);
    }

    public void processMessage(long chatId, String message) {
        Pattern pattern = compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)");
        Matcher matcher = pattern.matcher(message);
        if (matcher.matches()) {
            NotificationTask task = new NotificationTask();
            String dateTime = matcher.group(1);
            LocalDateTime localDateTime = parse(dateTime, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            task.setTimestamp(localDateTime);
            task.setText(matcher.group(3));
            task.setChatId(chatId);
            notificationTaskService.addTask(task);
            sendResponse(chatId, "Напоминание успешно добавлено!");
        } else {
            sendResponse(chatId, "Неверный формат сообщения! Используйте сообщение вида: 01.01.2022 20:00 Сделать домашнюю работу");
        }
    }

    public void sendResponse(long chatId, String message) {
        SendMessage response = new SendMessage(chatId, message);
        telegramBot.execute(response);
    }

}

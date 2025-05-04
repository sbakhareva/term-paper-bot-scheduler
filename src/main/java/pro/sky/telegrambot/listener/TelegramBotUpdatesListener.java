package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.service.NotificationTaskService;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.LocalDateTime.parse;
import static java.util.regex.Pattern.compile;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private NotificationTaskService notificationTaskService;

    private final Set<Long> chatIds = new HashSet<>();

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            if (update.message() != null && update.message().text() != null) {
                String text = update.message().text();
                long chatId = update.message().chat().id();
                chatIds.add(chatId);

                if ("/start".equals(text)) {
                    sendWelcomeMessage(chatId);
                    sendWelcomePhoto(chatId);
                } else if ("/add_task".equals(text)) {
                    addTaskMessage(chatId);
                } else processMessage(chatId, text);
            }
        });
        sendRemind();
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }


    private void sendWelcomeMessage(long chatId) {
        try {
            var is = ClassLoader.getSystemResourceAsStream("messages/welcome_message.txt");
            String welcomeMessage = new String(Objects.requireNonNull(is).readAllBytes());
            SendMessage request = new SendMessage(chatId, welcomeMessage);
            telegramBot.execute(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addTaskMessage(long chatId) {
        String addTaskMessage = "Напишите, какое напоминание вы хотите добавить, в формате '03.05.2025 17:28 Заняться делами!'";
        SendMessage sendMessage = new SendMessage(chatId, addTaskMessage);
        telegramBot.execute(sendMessage);
    }

    private void sendWelcomePhoto(long chatId) {
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

    @Scheduled(cron = "0 0/1 * * * *")
    public void sendRemind() {
        List<NotificationTask> tasks = notificationTaskService.getTaskAtTime();

        for (Long id : chatIds) {
            for (NotificationTask task : tasks) {
                String messageText = "Есть запланированные дела: \n " + task.getText();
                sendResponse(id, messageText);
                notificationTaskService.deleteTask(task);
            }
        }
    }
}

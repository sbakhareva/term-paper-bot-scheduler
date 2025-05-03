package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.service.NotificationTaskService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
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

    private void sendWelcomeMessage(long chatId) {
        String welcomeText = "Привет!";
        SendMessage request = new SendMessage(chatId, welcomeText);
        telegramBot.execute(request);
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
                }
                if (!text.isBlank()) {
                    processMessage(chatId, text);
                }
            }
        });
        sendRemind();
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
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

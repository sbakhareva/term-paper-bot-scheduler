package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.service.MessagingService;
import pro.sky.telegrambot.service.NotificationTaskService;
import pro.sky.telegrambot.service.Scheduler;

import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final TelegramBot telegramBot;
    private final NotificationTaskService notificationTaskService;
    private final MessagingService messagingService;
    private final Scheduler scheduler;

    public TelegramBotUpdatesListener(TelegramBot telegramBot,
                                      NotificationTaskService notificationTaskService,
                                      MessagingService messagingService,
                                      Scheduler scheduler) {
        this.telegramBot = telegramBot;
        this.notificationTaskService = notificationTaskService;
        this.messagingService = messagingService;
        this.scheduler = scheduler;
    }

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

                if ("/start".equals(text)) {
                    messagingService.sendWelcomeMessage(chatId);
                    messagingService.sendWelcomePhoto(chatId);
                } else if ("/add_task".equals(text)) {
                    messagingService.sendAddTaskMessage(chatId);
                } else messagingService.processMessage(chatId, text);
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}

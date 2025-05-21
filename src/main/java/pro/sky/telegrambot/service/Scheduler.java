package pro.sky.telegrambot.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.sky.telegrambot.model.NotificationTask;

import java.util.List;

@Component
public class Scheduler {

    private final NotificationTaskService notificationTaskService;
    private final MessagingService messagingService;

    public Scheduler(NotificationTaskService notificationTaskService, MessagingService messagingService) {
        this.notificationTaskService = notificationTaskService;
        this.messagingService = messagingService;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void sendRemind() {
        List<NotificationTask> tasks = notificationTaskService.getTaskAtTime();

        for (NotificationTask task : tasks) {
            String messageText = "Есть запланированные дела: \n " + task.getText();
            messagingService.sendResponse(task.getChatId(), messageText);
            notificationTaskService.deleteTask(task);
        }
    }
}

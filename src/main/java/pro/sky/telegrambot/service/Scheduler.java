package pro.sky.telegrambot.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;

import java.util.List;
import java.util.Set;

@Service
public class Scheduler {

    private final NotificationTaskService notificationTaskService;
    private final MessagingService messagingService;

    public Scheduler(NotificationTaskService notificationTaskService, MessagingService messagingService) {
        this.notificationTaskService = notificationTaskService;
        this.messagingService = messagingService;
    }

    public void sendRemind(Set<Long> chatIds) {
        List<NotificationTask> tasks = notificationTaskService.getTaskAtTime();

        for (Long id : chatIds) {
            for (NotificationTask task : tasks) {
                if (id.equals(task.getChatId())) {
                    String messageText = "Есть запланированные дела: \n " + task.getText();
                    messagingService.sendResponse(id, messageText);
                    notificationTaskService.deleteTask(task);
                }
            }
        }
    }
}

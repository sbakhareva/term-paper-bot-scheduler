package pro.sky.telegrambot.service;

import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NotificationTaskService {

    private final NotificationTaskRepository notificationTaskRepository;

    public NotificationTaskService(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
    }

    public void addTask(NotificationTask notificationTask) {
            notificationTaskRepository.save(notificationTask);
    }

    public List<NotificationTask> getTaskAtTime() {
        LocalDateTime currentTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        return notificationTaskRepository.findNotificationTaskByTimestamp(currentTime);
    }

    public void deleteTask(NotificationTask task) {
        notificationTaskRepository.delete(task);
    }
}

package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendPhoto;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

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

    public String loadMessage(String name) {
        try {
            var is = ClassLoader.getSystemResourceAsStream("messages/" + name + ".txt");
            return new String(Objects.requireNonNull(is).readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Can't load message!");
        }
    }
}

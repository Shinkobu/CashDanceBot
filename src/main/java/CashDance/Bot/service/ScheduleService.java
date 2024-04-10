package CashDance.Bot.service;

import CashDance.Bot.config.BotConfig;
import CashDance.Bot.model.CbMonitor;
import CashDance.Bot.model.Feedback;
import com.google.common.collect.Iterables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.beans.FeatureDescriptor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ScheduleService {

    @Autowired
    TelegramBot telegramBot;
    @Autowired
    BotConfig botConfig;

    @Autowired
    MonitorService monitorService;

    @Autowired
    Repository repository;

    //            @Scheduled(cron = "* * * ? * *") // every second
//    @Scheduled(cron = "0/5 * * ? * *") // every 5 seconds
    @Scheduled(cron = "59 59 23 ? * *") // every day at 23:59:59
    public void run() {

        // saving today statistics to db
        repository.saveCbMonitorToDb(new CbMonitor(LocalDate.now(), monitorService.getDailyUpdateCounter()));
        // message to admin about number of interactions for today
        telegramBot.prepareAndSendMessage(botConfig.getOwnerId(), "Количество взаимодействий за " +
                LocalDate.now() + " составляет " + monitorService.getDailyUpdateCounter().toString());
        // message to admin about number of users for today
        telegramBot.prepareAndSendMessage(botConfig.getOwnerId(), "Количество пользователей: "
                + Iterables.size(repository.getAllUsers()));
        // feedback from users for today
        if (!getTodayFeedback().isEmpty()) {
            telegramBot.prepareAndSendMessage(botConfig.getOwnerId(), "Получена обратная связь: " + "\n"
                    + getTodayFeedback());
        }
        // resets interactions counter
        monitorService.setDailyUpdateCounter(0);
    }

    private List<String> getTodayFeedback() {
        List<Feedback> temp = new ArrayList<>();
        List<String> result = new ArrayList<>();
        temp = repository.getFeedbackList(LocalDateTime.now(), LocalDateTime.now());

        for (Feedback feedback : temp) {
            result.add(feedback.toString());
        }
        return result;
    }
}

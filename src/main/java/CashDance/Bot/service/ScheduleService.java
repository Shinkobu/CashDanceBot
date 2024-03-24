package CashDance.Bot.service;

import CashDance.Bot.config.BotConfig;
import CashDance.Bot.model.CbMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

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

//    @Scheduled(cron = "* * * ? * *") // every second
    @Scheduled(cron = "59 59 23 ? * *") // every day at 23:59:59
    public void run() {

        repository.saveCbMonitorToDb(new CbMonitor(LocalDate.now(), monitorService.getDailyUpdateCounter()));
        telegramBot.prepareAndSendMessage(botConfig.getOwnerId(), "Количество взаимодействий за " +
                LocalDate.now() + " составляет " + monitorService.getDailyUpdateCounter().toString());

        monitorService.setDailyUpdateCounter(0);


    }
}

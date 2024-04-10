package CashDance.Bot.service;

import org.springframework.stereotype.Service;

@Service
public class MonitorService {
    Integer dailyUpdateCounter = 0;

    public void incrementUpdateCounter() {
        dailyUpdateCounter++;
    }
    public Integer getDailyUpdateCounter() {
        return dailyUpdateCounter;
    }
    public void setDailyUpdateCounter(Integer dailyUpdateCounter) {
        this.dailyUpdateCounter = dailyUpdateCounter;
    }
}

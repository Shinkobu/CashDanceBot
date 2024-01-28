package CashDance.Bot.model;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public abstract class CashbackEntity {
    private String name;
    private User user;

    public User getUser() {
        return user;
    }

    public String getName() {
        return name;
    }
}

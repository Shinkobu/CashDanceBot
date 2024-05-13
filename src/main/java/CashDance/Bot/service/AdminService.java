package CashDance.Bot.service;

import CashDance.Bot.config.BotConfig;
import CashDance.Bot.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.List;

@Component
public class AdminService {
    @Autowired
    BotConfig config;
    @Autowired
    Repository repository;


    public boolean isAdmin (long chatId){
        return chatId == config.getOwnerId();

    }
    public String showAllUsers(long chatId) {
        String message = "";
        if (isAdmin(chatId)) {
            List<User> users = (List<User>) repository.getAllUsers();
            for (User user : users) {
                message = message.concat(user.getUserName() + "\n");
            }
        } else
            message = "Недостаточно полномочий";
        return message;
    }

    public void messageToAllUsers(long chatId){
        String message = "123";
        if (chatId == config.getOwnerId()) {
            List<User> users = (List<User>) repository.getAllUsers();
            for (User user : users) {
                message = message.concat(user.getUserName() + "\n");

            }
        } else
            message = "Недостаточно полномочий";
    }


}

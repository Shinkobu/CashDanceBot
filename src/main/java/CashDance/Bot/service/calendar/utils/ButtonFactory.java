package CashDance.Bot.service.calendar.utils;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
@Component
public class ButtonFactory {

    public static InlineKeyboardButton createButton(String name, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton(name);
        button.setCallbackData(callbackData);
        return button;
    }
}

package CashDance.Bot.service;

import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;


public class Methods {

//    private void showMenu(long chatId, String menuName, ArrayList<MenuOption> menuOptionArrayList) {
//
//        SendMessage message = new SendMessage();
//        message.setChatId(String.valueOf(chatId));
//        message.setText(menuName);
//
//        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
//        List<List<InlineKeyboardButton>> inlineRowsList = new ArrayList<>();
//
//        for (MenuOption menuOption : menuOptionArrayList) {
//            List<InlineKeyboardButton> inlineRowButtonList = new ArrayList<>();
//            var tempButton = new InlineKeyboardButton();
//
//            tempButton.setText(menuOption.getOptionName());
//            tempButton.setCallbackData(menuOption.menuButton.name());
//            inlineRowButtonList.add(tempButton);
//            inlineRowsList.add(inlineRowButtonList);
//        }
//
//        inlineKeyboardMarkup.setKeyboard(inlineRowsList);
//
//        message.setReplyMarkup(inlineKeyboardMarkup);
//        ;
//    }


}

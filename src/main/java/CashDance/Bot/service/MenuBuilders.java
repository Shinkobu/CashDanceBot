package CashDance.Bot.service;

import CashDance.Bot.model.BankCard;
import CashDance.Bot.model.CbCategory;
import CashDance.Bot.model.CbChance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static CashDance.Bot.service.Constants.*;

@Slf4j
@Component
public class MenuBuilders {

    void delay(int delayTime) {
        try {
            Thread.sleep(delayTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Builds list of MenuOptions which is required to show inline menu
     *
     * @param bankCardList - list of bank cards
     * @param menuButtons  - special reply signal for each button in menu. Used to determine that button of this menu
     *                     is pressed
     * @return list of MenuOptions which is required to show inline menu
     */
    List<MenuOption> cardMenuBuilder(List<BankCard> bankCardList, MenuButtons menuButtons) {
        Collections.sort(bankCardList);
        List<MenuOption> resultList = new ArrayList<>();
        for (BankCard bankCard : bankCardList) {
            MenuOption menuOption = new MenuOption(bankCard.getName(), menuButtons);
            resultList.add(menuOption);
        }
        resultList.add(new MenuOption("<<Отмена", MenuButtons.ALLMENU_TOMAINMENU));
        return resultList;
    }

    List<MenuOption> categoryMenuBuilder(List<CbCategory> cbCategoryList, MenuButtons menuButtons) {
        Collections.sort(cbCategoryList);
        List<MenuOption> resultList = new ArrayList<>();
        for (CbCategory cbCategory : cbCategoryList) {
            MenuOption menuOption = new MenuOption(cbCategory.getName(), menuButtons);
            resultList.add(menuOption);
        }
        resultList.add(new MenuOption("<<Отмена", MenuButtons.ALLMENU_TOMAINMENU));
        return resultList;
    }

    private List<MenuOption> cbChanceMenuBuilder(List<CbChance> cbChanceList, MenuButtons menuButtons) {
        List<MenuOption> resultList = new ArrayList<>();
        for (CbChance cbChance : cbChanceList) {
            MenuOption menuOption = new MenuOption(cbChance.shortToString(), menuButtons);
            resultList.add(menuOption);
        }
        resultList.add(new MenuOption("<<Отмена", MenuButtons.ALLMENU_TOMAINMENU));
        return resultList;
    }

    SendMessage inlineMenuBuilder(long chatId, String menuName, List<MenuOption> menuOptionArrayList) {

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(menuName + ":");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineRowsList = new ArrayList<>();

        for (MenuOption menuOption : menuOptionArrayList) {
            List<InlineKeyboardButton> inlineRowButtonList = new ArrayList<>();
            var tempButton = new InlineKeyboardButton();

            tempButton.setText(menuOption.getOptionName());
            // TODO Problem is here. callback data is limited to 64 characters
            /* callback consists of
            1) .getOptionName() - usually a name which is used as an identifier to understand which object is chosen
            2) .menuButton.name() - a constant which is used to understand in which menu button is pressed
             */
            tempButton.setCallbackData(menuOption.getOptionName() + DELIMETER + menuOption.menuButton.name());
            inlineRowButtonList.add(tempButton);
            inlineRowsList.add(inlineRowButtonList);
        }

        inlineKeyboardMarkup.setKeyboard(inlineRowsList);

        message.setReplyMarkup(inlineKeyboardMarkup);
        return message;
    }
}

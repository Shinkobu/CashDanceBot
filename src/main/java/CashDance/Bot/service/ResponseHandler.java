package CashDance.Bot.service;

import java.util.List;

import static CashDance.Bot.service.Constants.*;

public class ResponseHandler {

    static MenuButtons menuButtons;

    protected static List<MenuOption> InlineButtonsHandler(long chatId, String callbackData) {

//      String to enum
        menuButtons = MenuButtons.valueOf(callbackData);

        switch (menuButtons) {
            case MAINMENU_MYCASHBACK:
                return mainMenuList;
            default:
                break;
        }
        return null;
    }
}

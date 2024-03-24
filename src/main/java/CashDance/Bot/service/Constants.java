package CashDance.Bot.service;

import java.util.ArrayList;
import java.util.List;

public class Constants {

    static final String DELIMETER = "///";

// Кнопки отмены в меню для прерывания диалогов ввода данных

    static final MenuOption cancel_toMainMenu = new MenuOption("<<Отмена", MenuButtons.ALLMENU_TOMAINMENU);
//    static final MenuOption cancel_toCardsMenu = new MenuOption("<<Отмена", MenuButtons.MAINMENU_MYCARDS);
//    static final MenuOption cancel_toCatsMenu = new MenuOption("<<Отмена", MenuButtons.MAINMENU_MYCATEGORIES);
//    static final MenuOption cancel_toChancesMenu = new MenuOption("<<Отмена", MenuButtons.MAINMENU_MYCASHBACK);

    static List<MenuOption> cancel_toMainMenuList = new ArrayList<>() {{
        add(cancel_toMainMenu);
    }};
//    static List<MenuOption> cancel_toCardsMenuList = new ArrayList<>() {{
//        add(cancel_toCardsMenu);
//    }};
//    static List<MenuOption> cancel_toCatsMenuList = new ArrayList<>() {{
//        add(cancel_toCatsMenu);
//    }};
//    static List<MenuOption> cancel_toChancesMenuList = new ArrayList<>() {{
//        add(cancel_toChancesMenu);
//    }};

    static final MenuOption yesNoMenu_Yes = new MenuOption("Да", MenuButtons.CHOICEMENU_YES);
    static final MenuOption yesNoMenu_No = new MenuOption("Нет", MenuButtons.CHOICEMENU_NO);

    static List<MenuOption> choiceMenuList = new ArrayList<>() {{
        add(yesNoMenu_Yes);
        add(yesNoMenu_No);
    }};

    static final MenuOption mainMenu_CashbackOfCategory = new MenuOption("Мой кэшбэк", MenuButtons.MAINMENU_MYCASHBACK);
    static final MenuOption mainMenu_MyCards = new MenuOption("Мои банковские карты", MenuButtons.MAINMENU_MYCARDS);
    static final MenuOption mainMenu_MyCategories = new MenuOption("Мои категории кэшбэка", MenuButtons.MAINMENU_MYCATEGORIES);
    static final MenuOption allMenu_ToMainMenu = new MenuOption("В главное меню", MenuButtons.ALLMENU_TOMAINMENU);


    static List<MenuOption> mainMenuList = new ArrayList<>() {{
        add(mainMenu_CashbackOfCategory);
        add(mainMenu_MyCards);
        add(mainMenu_MyCategories);
    }};

    static final MenuOption chancesMenu_ActualChancesOfCategory = new MenuOption("Кэшбэк по категории", MenuButtons.CHANCES_ACTCHANCES_CAT);
    static final MenuOption chancesMenu_ActualChancesOfCard = new MenuOption("Кэшбэк по карте", MenuButtons.CHANCES_ACTCHANCES_CARD);
    static final MenuOption chancesMenu_NewChance = new MenuOption("Новый кэшбэк", MenuButtons.CHANCESMENU_NEWCHANCE);
    static final MenuOption chancesMenu_EditChance = new MenuOption("Изменить кэшбэк", MenuButtons.CHANCESMENU_EDITCHANCE);
    static final MenuOption chancesMenu_DeleteChance = new MenuOption("Удалить кэшбэк", MenuButtons.CHANCESMENU_DELETECHANCE);

    static List<MenuOption> chancesMenuList = new ArrayList<>() {{
        add(chancesMenu_ActualChancesOfCategory);
        add(chancesMenu_ActualChancesOfCard);
        add(chancesMenu_NewChance);
//        add(chancesMenu_EditChance);
        add(chancesMenu_DeleteChance);
        add(allMenu_ToMainMenu);
    }};


    static final MenuOption cardsMenu_MyCards = new MenuOption("Мои карты", MenuButtons.CARDSMENU_ALLMYCARDS);
    static final MenuOption cardsMenu_NewCard = new MenuOption("Новая карта", MenuButtons.CARDSMENU_NEWCARD);
    static final MenuOption cardsMenu_EditCard = new MenuOption("Изменить карту", MenuButtons.CARDSMENU_EDITCARD);
    static final MenuOption cardsMenu_DeleteCard = new MenuOption("Удалить карту", MenuButtons.CARDSMENU_DELETECARD);


    static List<MenuOption> cardMenuList = new ArrayList<>() {{
        add(cardsMenu_MyCards);
        add(cardsMenu_NewCard);
        add(cardsMenu_EditCard);
        add(cardsMenu_DeleteCard);
        add(allMenu_ToMainMenu);
    }};



    static final MenuOption categoriesMenu_MyCategories = new MenuOption("Мои категории", MenuButtons.CATMENU_MYCATS);
    static final MenuOption categoriesMenu_NewCategory = new MenuOption("Новая категория", MenuButtons.CATMENU_NEWCAT);
    static final MenuOption categoriesMenu_EditCategory = new MenuOption("Изменить категорию", MenuButtons.CATMENU_EDITCAT);
    static final MenuOption categoriesMenu_DeleteCategory = new MenuOption("Удалить категорию", MenuButtons.CATMENU_DELETECAT);


    static List<MenuOption> categoryMenuList = new ArrayList<>() {{
        add(categoriesMenu_MyCategories);
        add(categoriesMenu_NewCategory);
        add(categoriesMenu_EditCategory);
        add(categoriesMenu_DeleteCategory);
        add(allMenu_ToMainMenu);
    }};

}

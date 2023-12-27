package CashDance.Bot.service;

import java.util.ArrayList;
import java.util.List;

public class Constants {

    static final MenuOption mainMenu_CashbackOfCategory = new MenuOption("Кешбек по категории", MenuButtons.MAINMENU_MYCASHBACK);
    static final MenuOption mainMenu_MyCards = new MenuOption("Мои банковские карты", MenuButtons.MAINMENU_MYCARDS);
    static final MenuOption mainMenu_MyCategories = new MenuOption("Мои категории кешбека", MenuButtons.MAINMENU_MYCATEGORIES);
    static final MenuOption allMenu_ToMainMenu = new MenuOption("В главное меню", MenuButtons.ALLMENU_TOMAINMENU);


    static List<MenuOption> mainMenuList = new ArrayList<>() {{
        add(mainMenu_CashbackOfCategory);
        add(mainMenu_MyCards);
        add(mainMenu_MyCategories);
    }};

    static final MenuOption cardsMenu_MyCards = new MenuOption("Мои карты", MenuButtons.CARDSNMENU_MYCARDS);
    static final MenuOption cardsMenu_NewCard = new MenuOption("Новая карта", MenuButtons.CARDSMENU_NEWCARD);
    static final MenuOption cardsMenu_EditCard = new MenuOption("Редактировать карту", MenuButtons.CARDSMENU_EDITCARD);
    static final MenuOption cardsMenu_DeleteCard = new MenuOption("Удалить карту", MenuButtons.CARDSMENU_DELETECARD);


    static List<MenuOption> cardMenuList = new ArrayList<>() {{
        add(cardsMenu_MyCards);
        add(cardsMenu_NewCard);
        add(cardsMenu_EditCard);
        add(cardsMenu_DeleteCard);
        add(allMenu_ToMainMenu);
    }};
    
    static final MenuOption categoriesMenu_MyCategories = new MenuOption("Мои категории", MenuButtons.CATEGORIESMENU_MYCATEGORIES);
    static final MenuOption categoriesMenu_NewCategory = new MenuOption("Новая категория", MenuButtons.CATEGORIESMENU_NEWCARD);
    static final MenuOption categoriesMenu_EditCategory = new MenuOption("Редактировать категорию", MenuButtons.CATEGORIESMENU_EDITCATEGORY);
    static final MenuOption categoriesMenu_DeleteCategory = new MenuOption("Удалить категорию", MenuButtons.CATEGORIESMENU_DELETECATEGORY);


    static List<MenuOption> categoryMenuList = new ArrayList<>() {{
        add(categoriesMenu_MyCategories);
        add(categoriesMenu_NewCategory);
        add(categoriesMenu_EditCategory);
        add(categoriesMenu_DeleteCategory);
        add(allMenu_ToMainMenu);
    }};

}

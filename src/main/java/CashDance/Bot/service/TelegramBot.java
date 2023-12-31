package CashDance.Bot.service;


import CashDance.Bot.config.BotConfig;
import CashDance.Bot.model.BankCard;
import CashDance.Bot.model.CbCategory;
import CashDance.Bot.model.CbChance;
import CashDance.Bot.model.interfaces.*;
import CashDance.Bot.model.User;
import com.sun.xml.bind.v2.TODO;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static CashDance.Bot.service.ChoiceFor.*;
import static CashDance.Bot.service.Constants.*;


@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    static final String YES_BUTTON = "YES_BUTTON";
    static final String NO_BUTTON = "NO_BUTTON";
    static final String ERROR_TEXT = "Error occurred: ";
    private static final String HELP_TEXT = "Это программа-помощник по кешбеку с ваших банковских карт.\n\n" +
            "Пример проблемы: у Василия есть карта альфа-банка, карта тинькофф и карта озон банка. Василий знает, что " +
            "по всем картам каждый месяц обновляются категории кешбека. В один месяц высокий кешбек за заправки по " +
            "тинькофф и за супермаркеты у озона, в следующий - всё меняется. И вот Василий приезжает на заправку и пытается вспомнить " +
            "по какой карте сегодня он получит максимальный кешбек? Нужно открыть банковское приложение и посмотреть!\n\n Хорошо, но " +
            "для этого придётся открывать 3-4 приложения, а это долго, копаться неохота, семья ждёт в машине и Василий оплачивает топливо картой наугад.\n\n" +
            "CashDanceBot решает эту проблему. \n\n Василий может раз в месяц занести информацию по какой карте в каких категориях у него " +
            "кешбек и на заправке просто спросить у бота, какие у него есть действующие кешбеки по категории Заправки. Бот сделает выборку " +
            "и подскажет, что по он получит альфе 1%, по озону 3%, по тинькофф 5%.\n\n" +
            "Для просмотра доступных команд используйте /commands";

    private static final String COMMANDS_TEXT = "Раздел в разработке";

    //            "/start to see a welcome message \n\n" +
//            "Type /register to register \n\n" +
//            "Type /mydata to see data stored about yourself \n\n" +
//            "Type /help to see this message again";
    final BotConfig config;
    private ServiceState serviceState;
    private ServiceState chatState;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BankCardRepository bankCardRepository;
    @Autowired
    private AdsRepository adsRepository;
    @Autowired
    private CbCategoryRepository cbCategoryRepository;
    @Autowired
    private CbChanceRepository cbChanceRepository;
    private String newBankName;
    private String newCardName;
    private String newCategoryName;
    private BankCard bankCardForNewChance;
    private CbCategory categoryForNewChance;
    private Double rateForNewChance;
    private LocalDate startDateOfNewChance;
    private LocalDate endDateOfNewChance;
    private Long BankCardId;
    private ChoiceFor choiceForEnum;


    public TelegramBot(BotConfig config) {

        chatState = ServiceState.DEFAULT_STATE;
//        chatState.put(0L,ServiceState.DEFAULT_STATE);
//      configuring bot Menu in constructor
        this.config = config;
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand("/start", "get a welcome message"));
        listofCommands.add(new BotCommand("/mainmenu", "show main menu"));
        listofCommands.add(new BotCommand("/mycards", "manage your bank cards"));
        listofCommands.add(new BotCommand("/register", "register yourself"));
        listofCommands.add(new BotCommand("/mydata", "get your data stored"));
//        listofCommands.add(new BotCommand("/deletedata", "delete your data"));
//        listofCommands.add(new BotCommand("/help", "info how to use this bot"));
//        listofCommands.add(new BotCommand("/settings", "set your preferences"));

        listofCommands.add(new BotCommand("/allcards", "see all your cards"));
        listofCommands.add(new BotCommand("/newcard", "create a new card"));
//        listofCommands.add(new BotCommand("/changecard", "modify your card"));
//        listofCommands.add(new BotCommand("/deletecard", "delete your card"));
        try {
            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));

        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
//            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    //    incoming message handling
    @Override
    public void onUpdateReceived(Update update) {


        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

//          message to all users. Only bot owner can do it
            if (messageText.contains("/send") && config.getOwnerId() == chatId) {
                var textToSend = EmojiParser.parseToUnicode(messageText.substring(messageText.indexOf(" ")));
                var users = userRepository.findAll();
                for (User user : users) {
                    prepareAndSendMessage(user.getChatId(), textToSend);
                }
            } else {

//          other commands handling
                switch (messageText) {
                    case "/start":
                        registerUser(update.getMessage());
                        startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                        showMenu(chatId, "Главное меню", Constants.mainMenuList);
                        chatState = ServiceState.DEFAULT_STATE;
                        break;
                    case "/help":
                        prepareAndSendMessage(chatId, HELP_TEXT);
                        chatState = ServiceState.DEFAULT_STATE;
                        break;
                    case "/commands":
                        prepareAndSendMessage(chatId, COMMANDS_TEXT);
                        chatState = ServiceState.DEFAULT_STATE;
                        break;
                    case "/register":
                        register(chatId);
                        chatState = ServiceState.DEFAULT_STATE;
                        break;
                    case "/newcard":
                        log.info("Building new bank card...");
                        prepareAndSendMessage(chatId, "Введите название банка");
                        chatState = ServiceState.NEW_BANK_CARD__AWAITING_BANK_NAME;
                        log.info("Waiting for bank name...");
                        break;
                    case "/newcategory":
                        log.info("Building new category...");
                        prepareAndSendMessage(chatId, "Введите название категории");
                        chatState = ServiceState.NEW_CATEGORY__AWAITING_CATEGORY_NAME;
                        log.info("Waiting for category name...");
                        break;
                    case "/mycards":
                        showMyCards(chatId);
                        chatState = ServiceState.DEFAULT_STATE;
                        break;
                    case "/mycategories":
                        showMyCategories(chatId);
                        chatState = ServiceState.DEFAULT_STATE;
                        break;
                    case "/mycategoriesbuttons":
                        showMyCategoriesButtons(chatId);
                        chatState = ServiceState.DEFAULT_STATE;
                        break;
                    case "/newchance":
                        log.info("Building new cashback chance");
                        prepareAndSendMessage(chatId, "Выберите карту");
                        showMyCards(chatId);
                        chatState = ServiceState.NEW_CHANCE__AWAITING_BANK_CARD_ID;
                        log.info("Waiting for bank card id...");
//                        prepareAndSendMessage(chatId, "Введите дату начала действия кешбека");
//                        prepareAndSendMessage(chatId, "Введите дату окончания действия кешбека");

                        break;
                    case "/mychances":
                        showMyChances(chatId);
                        chatState = ServiceState.DEFAULT_STATE;
                        break;
                    case "/mychancesofcategory":
                        prepareAndSendMessage(chatId, "Выберите категорию");
                        showMyCategories(chatId);
                        chatState = ServiceState.ALL_CHANCES__AWAITING_CATEGORY_ID;
                        break;
                    case "/mychancesofcategoryactive":
                        prepareAndSendMessage(chatId, "Выберите категорию");
                        showMyCategories(chatId);
                        chatState = ServiceState.ALL_CHANCES_ACTIVE__AWAITING_CATEGORY_ID;
                        break;
                    case "/mainmenu":
                        showMenu(chatId, "Главное меню", Constants.mainMenuList);
                        chatState = ServiceState.DEFAULT_STATE;
                        break;
                    case "/cardmenu":
                        showMenu(chatId, "Мои банковские карты", Constants.cardMenuList);
                        chatState = ServiceState.DEFAULT_STATE;
                        break;
                    case "/categorymenu":
                        showMenu(chatId, "Мои категории кешбека", Constants.categoryMenuList);
                        chatState = ServiceState.DEFAULT_STATE;
                        break;
                    default:

                        switch (chatState) {
                            case NEW_BANK_CARD__AWAITING_BANK_NAME:
                                newBankName = update.getMessage().getText();
                                chatState = ServiceState.NEW_BANK_CARD__AWAITING_BANK_CARD_NAME;
                                prepareAndSendMessage(chatId, "Введите название карты");
                                break;
                            case EDIT_BANK_CARD__AWAITING_BANK_NAME:
                                newBankName = update.getMessage().getText();
                                chatState = ServiceState.EDIT_BANK_CARD__AWAITING_BANK_CARD_NAME;
                                prepareAndSendMessage(chatId, "Введите новое название карты");
                                break;
                            case NEW_BANK_CARD__AWAITING_BANK_CARD_NAME:
                                newCardName = update.getMessage().getText();
                                chatState = ServiceState.DEFAULT_STATE;
                                saveCardToDb(chatId, true, 0L);

                                showMenu(chatId, "Главное меню", Constants.mainMenuList);
                                chatState = ServiceState.DEFAULT_STATE;

                                break;
                            case EDIT_BANK_CARD__AWAITING_BANK_CARD_NAME:
                                newCardName = update.getMessage().getText();
                                chatState = ServiceState.DEFAULT_STATE;
                                saveCardToDb(chatId, false, BankCardId);

                                showMenu(chatId, "Главное меню", Constants.mainMenuList);
                                chatState = ServiceState.DEFAULT_STATE;

                                break;
                            case NEW_CATEGORY__AWAITING_CATEGORY_NAME:
                                newCategoryName = update.getMessage().getText();
                                chatState = ServiceState.DEFAULT_STATE;
                                saveNewCategoryToDb(chatId);
                                break;
                            case NEW_CHANCE__AWAITING_BANK_CARD_ID:
                                String bankCardId = update.getMessage().getText();
                                Optional<BankCard> bankCard = bankCardRepository.findById(Long.valueOf(bankCardId));
                                bankCardForNewChance = bankCard.get();
                                chatState = ServiceState.NEW_CHANCE__AWAITING_CATEGORY_ID;
                                prepareAndSendMessage(chatId, "Введите id категории");
                                showMyCategories(chatId);
                                break;
                            case NEW_CHANCE__AWAITING_CATEGORY_ID:
                                String categoryId = update.getMessage().getText();
                                Optional<CbCategory> cbCategory = cbCategoryRepository.findById(Long.valueOf(categoryId));
                                categoryForNewChance = cbCategory.get();
                                chatState = ServiceState.NEW_CHANCE__AWAITING_RATE;
                                prepareAndSendMessage(chatId, "Введите % кешбека");
                                break;
                            case NEW_CHANCE__AWAITING_RATE:
                                rateForNewChance = Double.parseDouble(update.getMessage().getText()) / 100;
                                chatState = ServiceState.NEW_CHANCE__AWAITING_START_DATE;
                                prepareAndSendMessage(chatId, "Введите дату начала действия кэшбека в формате dd-mm-yyyy");
                                break;
                            case NEW_CHANCE__AWAITING_START_DATE:
//                                public static DateTimeFormatter oldDateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                                startDateOfNewChance = LocalDate.parse(update.getMessage().getText(),
                                        DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                                chatState = ServiceState.NEW_CHANCE__AWAITING_END_DATE;
                                prepareAndSendMessage(chatId, "Введите дату окончания действия кэшбека в формате dd-mm-yyyy");
                                break;
                            case NEW_CHANCE__AWAITING_END_DATE:

                                endDateOfNewChance = LocalDate.parse(update.getMessage().getText(),
                                        DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                                if (endDateOfNewChance.isAfter(startDateOfNewChance) || endDateOfNewChance.isEqual(startDateOfNewChance)) {
                                    chatState = ServiceState.DEFAULT_STATE;
                                    saveNewChancetoDb(chatId);
                                } else {
                                    prepareAndSendMessage(chatId, "Введённая дата не может быть раньше даты начала");
                                }

                                break;
                            case ALL_CHANCES__AWAITING_CATEGORY_ID:
                                String categoryId2 = update.getMessage().getText();
                                Optional<CbCategory> cbCategory1 = cbCategoryRepository.findById(Long.valueOf(categoryId2));
                                CbCategory cbCategory2 = cbCategory1.get();
                                showMyChancesOfCategory(chatId, cbCategory2, false);
                                chatState = ServiceState.DEFAULT_STATE;
                                break;
                            case ALL_CHANCES_ACTIVE__AWAITING_CATEGORY_ID:
                                String categoryId3 = update.getMessage().getText();
                                Optional<CbCategory> cbCategory3 = cbCategoryRepository.findById(Long.valueOf(categoryId3));
                                CbCategory cbCategory4 = cbCategory3.get();
                                showMyChancesOfCategory(chatId, cbCategory4, true);
                                chatState = ServiceState.DEFAULT_STATE;
                                break;


                            default:
                                prepareAndSendMessage(chatId, "Sorry, command was not recognized");
                                chatState = ServiceState.DEFAULT_STATE;
                        }
                }
            }
// If inlinekeyboard button is pressed
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
// Edition of sent message after pressing button
            EditMessageText message = new EditMessageText();
            String text = "Вы выбрали: " + callbackData;
            executeEditMessageText(text, chatId, messageId);

            //      String to enum
//            MenuButtons menuButtons = MenuButtons.valueOf(callbackData);
            MenuOption menuOption = callbackParser(callbackData);

            switch (menuOption.getMenuButton()) {
                case MAINMENU_MYCASHBACK:
                    showMyChances(chatId);
                    break;
                case MAINMENU_MYCARDS:
                    showMenu(chatId, "Мои карты", Constants.cardMenuList);
                    break;
                case MAINMENU_MYCATEGORIES:
                    showMenu(chatId, "Мои категории", Constants.categoryMenuList);
                    break;
                case ALLMENU_TOMAINMENU:
                    showMenu(chatId, "Главное меню", Constants.mainMenuList);
                    break;
                case CARDSMENU_MYCARDS:
                    showMyCards(chatId);
                    showMenu(chatId, "Мои банковские карты", Constants.cardMenuList);
                    break;
                case CARDSMENU_NEWCARD:
//                  TODO refactor. DRY
                    log.info("Building new bank card...");
                    prepareAndSendMessage(chatId, "Введите название банка");
                    chatState = ServiceState.NEW_BANK_CARD__AWAITING_BANK_NAME;
                    log.info("Waiting for bank name...");
                    break;
                case CARDSMENU_EDITCARD:
                    log.info("Editing bank card...");
                    List cardList = getMyCardsList(chatId);
                    List menuOptionList = menuBuilder(cardList, MenuButtons.CARDSMENU_EDITCARD_ANYCARD);
                    showMenu(chatId, "Выберите карту", menuOptionList);
                    break;
                case CARDSMENU_EDITCARD_ANYCARD:
                    prepareAndSendMessage(chatId, "Выбрана карта " + menuOption.getOptionName());
//                    showMyCard(chatId, menuOption.getOptionName());
                    BankCardId = getMyCardId(chatId, menuOption.getOptionName());
                    log.info("Editing bank card...");
                    prepareAndSendMessage(chatId, "Введите новое название банка");
                    chatState = ServiceState.EDIT_BANK_CARD__AWAITING_BANK_NAME;
                    break;
                case CARDSMENU_DELETECARD:
                    List cardList1 = getMyCardsList(chatId);
                    List menuOptionList1 = menuBuilder(cardList1, MenuButtons.CARDSMENU_DELETECARD_ANYCARD);
                    showMenu(chatId, "Выберите карту", menuOptionList1);
                    break;
                case CARDSMENU_DELETECARD_ANYCARD:
                    prepareAndSendMessage(chatId, "Выбрана карта " + menuOption.getOptionName());
                    BankCardId = getMyCardId(chatId, menuOption.getOptionName());


                    showMenu(chatId, "Будет удалена карта " + menuOption.getOptionName() + " со всеми кешбеками. \n" +
                            "Эту операцию нельзя отменить. Вы уверены?", choiceMenuList);
                    choiceForEnum = DELETE_BANK_CARD;
                    break;

                case CHOICEMENU_NO:
                    prepareAndSendMessage(chatId, "Операция отменена");
                    showMenu(chatId, "Главное меню", Constants.mainMenuList);
                    break;
                case CHOICEMENU_YES:

                    switch (choiceForEnum) {
                        case DELETE_BANK_CARD:
                            log.info("Deleting bank card...");
                            deleteBankCard(BankCardId);
                            prepareAndSendMessage(chatId, "Карта удалена из базы данных");
                            break;
                        case DELETE_CATEGORY:
                            //TODO
                            break;
                        case DELETE_CHANCE:
                            //TODO
                            break;
                    }
                    showMenu(chatId, "Главное меню", Constants.mainMenuList);
                    break;
                default:
                    break;
            }

            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error("Error occured: " + e.getMessage());
            }
        }
    }

    private void deleteBankCard(Long bankCardId) {
        bankCardRepository.deleteById(bankCardId);
        log.info("BankCard with ID " + bankCardId + " deleted from db");
    }

    private List<MenuOption> menuBuilder(List<BankCard> bankCardList, MenuButtons menuButtons) {
        List<MenuOption> resultList = new ArrayList<>();
        for (BankCard bankCard : bankCardList) {
            MenuOption menuOption = new MenuOption(bankCard.getCardName(), menuButtons);
            resultList.add(menuOption);
        }
        return resultList;
    }

    private void saveNewCategoryToDb(long chatId) {
        Optional<User> optionalUser = userRepository.findById(chatId);
        // todo add presence check
        User user = optionalUser.get();
        // todo check if card already registered
        CbCategory newCbCategory = new CbCategory();
        newCbCategory.setUser(user);
        newCbCategory.setName(newCategoryName);

        cbCategoryRepository.save(newCbCategory);
        log.info("Category saved to db " + newCbCategory);
        prepareAndSendMessage(chatId, "Категория сохранена: " + newCbCategory);
    }

    private void showMyCards(long chatId) {
        Iterable<BankCard> bankCardList = bankCardRepository.findAll();
        String bankCardString = "";
        for (BankCard bankCard : bankCardList) {
            if (bankCard.getUser().getChatId().equals(chatId)) {
                bankCardString += bankCard.toString() + "\n";
            }
        }
        prepareAndSendMessage(chatId, bankCardString);
    }

    private void showMyCard(long chatId, String cardName) {
        Iterable<BankCard> bankCardList = bankCardRepository.findAll();
        String bankCardString = "";
        for (BankCard bankCard : bankCardList) {
            if (bankCard.getUser().getChatId().equals(chatId) && bankCard.getCardName().equals(cardName)) {
                bankCardString += bankCard.toString() + "\n";
            }
        }
        prepareAndSendMessage(chatId, bankCardString);
    }

    private Long getMyCardId(long chatId, String cardName) {
        Iterable<BankCard> bankCardList = bankCardRepository.findAll();
        Long bankCardId = null;
        for (BankCard bankCard : bankCardList) {
            if (bankCard.getUser().getChatId().equals(chatId) && bankCard.getCardName().equals(cardName)) {
                bankCardId = bankCard.getCardId();
            }
        }
        return bankCardId;
    }

    private List getMyCardsList(long chatId) {
        Iterable<BankCard> bankCardList = bankCardRepository.findAll();
        List<BankCard> resultList = new ArrayList<>();
        for (BankCard bankCard : bankCardList) {
            if (bankCard.getUser().getChatId().equals(chatId)) {
                resultList.add(bankCard);
            }
        }
        return resultList;
    }

    private void showMyCategories(long chatId) {
        Iterable<CbCategory> cbCategories = cbCategoryRepository.findAll();
        String s = "";
        for (CbCategory cbCategory : cbCategories) {
            if (cbCategory.getUser().getChatId().equals(chatId)) {
                s += cbCategory.toString() + "\n";
            }
        }
        prepareAndSendMessage(chatId, s);
    }

    private void showMyCategoriesButtons(long chatId) {
        Iterable<CbCategory> cbCategories = cbCategoryRepository.findAll();

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите категорию:");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineRowsList = new ArrayList<>();

        for (CbCategory cbCategory : cbCategories) {

            List<InlineKeyboardButton> inlineRowButtonList = new ArrayList<>();
            var tempButton = new InlineKeyboardButton();

            tempButton.setText(cbCategory.getName());
            tempButton.setCallbackData(cbCategory.getName());
            inlineRowButtonList.add(tempButton);
            inlineRowsList.add(inlineRowButtonList);
        }

        inlineKeyboardMarkup.setKeyboard(inlineRowsList);

        message.setReplyMarkup(inlineKeyboardMarkup);
        executeMessage(message);
    }

    private void showMenu(long chatId, String menuName, List<MenuOption> menuOptionArrayList) {

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(menuName + ":");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineRowsList = new ArrayList<>();

        for (MenuOption menuOption : menuOptionArrayList) {
            List<InlineKeyboardButton> inlineRowButtonList = new ArrayList<>();
            var tempButton = new InlineKeyboardButton();

            tempButton.setText(menuOption.getOptionName());
            tempButton.setCallbackData(menuOption.getOptionName() + DELIMETER + menuOption.menuButton.name());
            inlineRowButtonList.add(tempButton);
            inlineRowsList.add(inlineRowButtonList);
        }

        inlineKeyboardMarkup.setKeyboard(inlineRowsList);

        message.setReplyMarkup(inlineKeyboardMarkup);
        executeMessage(message);
    }

    private MenuOption callbackParser(String callback) {
        List<String> resultList = List.of(callback.split(DELIMETER));
        MenuOption menuOption = new MenuOption(
                resultList.get(0),
                MenuButtons.valueOf(resultList.get(1)));
        return menuOption;
    }

//    private void showGeneratedMenu(long chatId, String menuName, List<MenuOption> menuOptionArrayList) {
//
//        SendMessage message = new SendMessage();
//        message.setChatId(String.valueOf(chatId));
//        message.setText(menuName + ":");
//
//        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
//        List<List<InlineKeyboardButton>> inlineRowsList = new ArrayList<>();
//
//        for (MenuOption menuOption : menuOptionArrayList) {
//            List<InlineKeyboardButton> inlineRowButtonList = new ArrayList<>();
//            var tempButton = new InlineKeyboardButton();
//
//            tempButton.setText(menuOption.getOptionName());
//            tempButton.setCallbackData(menuOption.getOptionName() + DELIMETER + menuOption.menuButton.name());
//            inlineRowButtonList.add(tempButton);
//            inlineRowsList.add(inlineRowButtonList);
//        }
//
//        inlineKeyboardMarkup.setKeyboard(inlineRowsList);
//
//        message.setReplyMarkup(inlineKeyboardMarkup);
//        executeMessage(message);
//    }

    private void showMyChances(long chatId) {
        Iterable<CbChance> cbChances = cbChanceRepository.findAll();

        String s = "";
        for (CbChance cbChance : cbChances) {
            if (cbChance.getUser().getChatId().equals(chatId)) {
                s += cbChance.toString() + "\n";
            }
        }
        prepareAndSendMessage(chatId, s);
    }

    private void showMyChancesOfCategory(long chatId, CbCategory cbCategory, boolean isActive) {
        Iterable<CbChance> cbChances = cbChanceRepository.findAll();
        String s = "";
        for (CbChance cbChance : cbChances) {
            if (cbChance.getUser().getChatId().equals(chatId)) {

                if (isActive) {
                    LocalDate todayDate = LocalDate.now();
                    if (cbChance.getEndDate().isAfter(todayDate.minusDays(1)) &&
                            cbChance.getStartDate().isBefore(todayDate.plusDays(1))) {
                        if (cbChance.getCbCategory().getName().equals(cbCategory.getName())) {
                            s += cbChance.toString() + "\n";
                        }
                    }
                } else {
                    if (cbChance.getCbCategory().getName().equals(cbCategory.getName())) {
                        s += cbChance.toString() + "\n";
                    }
                }
            }
        }
        if (s.equals("")) {
            s = "Записей не найдено!";
        }
        prepareAndSendMessage(chatId, s);
    }

    private void saveCardToDb(long chatId, boolean isNew, long bankCardId) {
        Optional<User> optionalUser = userRepository.findById(chatId);
        // todo add presence check
        User user = optionalUser.get();
        // todo check if card already registered
        BankCard newBankCard = new BankCard();
        newBankCard.setBankName(newBankName);
        newBankCard.setCardName(newCardName);
        newBankCard.setUser(user);
        if (!isNew) {
            newBankCard.setCardId(bankCardId);
        }
        bankCardRepository.save(newBankCard);
        log.info("Bank card saved to db " + newBankCard);
        prepareAndSendMessage(chatId, "Карта сохранена: " + newBankCard);
    }

    private void saveNewChancetoDb(long chatId) {
        Optional<User> optionalUser = userRepository.findById(chatId);
        // todo add presence check
        User user = optionalUser.get();
        // todo check if chance already registered

        CbChance newCbChance = new CbChance();
        newCbChance.setUser(user);
        newCbChance.setBankCard(bankCardForNewChance);
        newCbChance.setCbCategory(categoryForNewChance);

        newCbChance.setRate(rateForNewChance);
        newCbChance.setStartDate(startDateOfNewChance);
        newCbChance.setEndDate(endDateOfNewChance);


        cbChanceRepository.save(newCbChance);
        log.info("Chance saved to db " + newCbChance);
        prepareAndSendMessage(chatId, "Кешбек сохранен: " + newCbChance);
    }

    private void myCardsOptions() {
    }

    //    stub demo method without real registration
    private void register(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Do you really want to register?");
        // buttons in the message
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineRowsList = new ArrayList<>();
        List<InlineKeyboardButton> inlineRowButtonList = new ArrayList<>();

        var yesButton = new InlineKeyboardButton();
        var noButton = new InlineKeyboardButton();
        yesButton.setText("Yes");
        yesButton.setCallbackData(YES_BUTTON);
        noButton.setText("No");
        noButton.setCallbackData(YES_BUTTON);
        inlineRowButtonList.add(yesButton);
        inlineRowButtonList.add(noButton);
        inlineRowsList.add(inlineRowButtonList);
        inlineKeyboardMarkup.setKeyboard(inlineRowsList);
        message.setReplyMarkup(inlineKeyboardMarkup);
        executeMessage(message);
    }

    //  Registration in DB
    private void registerUser(Message message) {
        // check if user already registered
        if (userRepository.findById(message.getChatId()).isEmpty()) {
            var chatId = message.getChatId();
            var chat = message.getChat();
            User user = new User();
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
            userRepository.save(user);
            log.info("User saved to db " + user);

            List<String> cbCategoryNames = new ArrayList<>();
            cbCategoryNames.addAll(Arrays.asList("Авто", "Заправки", "Дом и ремонт", "Кафе и рестораны",
                    "Супермаркеты", "Развлечения", "Такси"));
            for (String s : cbCategoryNames) {
                CbCategory cbCategory = new CbCategory();
                cbCategory.setName(s);
                cbCategory.setUser(user);
                cbCategoryRepository.save(cbCategory);
            }
            log.info("Basic Cb categories for user " + user + " created!");

//            BankCard bankCard = new BankCard();
//            bankCard.setCardName("Кредитка сбера");
//            bankCard.setBankName("Сбербанк");
//            bankCard.setUser(user);
//            bankCardRepository.save(bankCard);
//
//            BankCard bankCard1 = new BankCard();
//            bankCard1.setCardName("Карта Тинькофф");
//            bankCard1.setBankName("Тинькофф");
//            bankCard1.setUser(user);
//            bankCardRepository.save(bankCard1);
//
//            BankCard bankCard2 = new BankCard();
//            bankCard2.setCardName("Кредитка Альфы");
//            bankCard2.setBankName("Альфа-банк");
//            bankCard2.setUser(user);
//            bankCardRepository.save(bankCard2);

        }
        log.info("User with id " + message.getChatId() + " is already registered");
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = EmojiParser.parseToUnicode("Привет, " + name + ", с вами CashDanceBot" + " :blush: \n\n" +
                "Для справки используйте команду /help \n\n" +
                "Для перехода в главное меню используйте команду /mainmenu \n\n" +
                "Для просмотра доступных команд используйте /commands");
        log.info("Replied to user " + name);
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
//      ReplyKeyboard example
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("/mychancesofcategory");
        row.add("/mychancesofcategoryactive");
        row.add("/newcard");
        row.add("/newcategory");
        row.add("/newchance");
        keyboardRows.add(row);
        row = new KeyboardRow();
        row.add("/start");
        row.add("/start");
        row.add("/mycards");
        row.add("/mycategories");
        row.add("/mychances");
        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);
        executeMessage(message);
    }

    private void executeEditMessageText(String text, long chatId, long messageId) {
        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setMessageId((int) messageId);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }


    void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }

    void prepareAndSendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);
    }

    //    @Scheduled(cron = "${cron.scheduler}") // parameters to tune the sending time
    //    private void sendAds() {
    //        var ads = adsRepository.findAll();
    //        var users = userRepository.findAll();
    //
    //        for (Ads ad : ads) {
    //            //TODO make a separate method
    //            for (User user : users) {
    //                prepareAndSendMessage(user.getChatId(), ad.getAd());
    //            }
    //
    //        }
    //    }

//    private String receiveValueFromUser(Message message, Long chatId, ServiceState serviceState) {
//        chatState = serviceState;
//        return message.getText();
//    }
}

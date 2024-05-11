package CashDance.Bot.service;


import CashDance.Bot.config.BotConfig;
import CashDance.Bot.model.*;
import CashDance.Bot.service.calendar.CalendarController;
import CashDance.Bot.service.calendar.InlineCalendar;
import CashDance.Bot.service.calendar.utils.Locale;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
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

    final BotConfig config;
    //        Stores temporary data for runtime purposes.
    //        Solved the problem of multithreading.
    private final Map<Long, ChatDataHolder> chatDataHolderMap = new HashMap<>();
    @Autowired
    private EntityBuilders entityBuilders;
    @Autowired
    private MenuBuilders menuBuilders;
    @Autowired
    private AdminService adminService;
    @Autowired
    private Repository repository;
    @Autowired
    private MonitorService monitorService;
    private Integer number;
    private SendMessage lastSendMessage;
    private Update lastUpdate;
    @Autowired
    private CalendarController calendarController;

    public TelegramBot(BotConfig config) {
//      configuring bot Menu in constructor
        this.config = config;
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand("/start", "get a welcome message"));
        listofCommands.add(new BotCommand("/mainmenu", "show main menu"));
        listofCommands.add(new BotCommand("/cardsmenu", "show cards menu"));
        listofCommands.add(new BotCommand("/categoriesmenu", "show categories menu"));
        try {
            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));

        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void setLastUpdate(Update lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    public String getBotVersion() {
        return config.getVersionText();
    }

    //    incoming message handling
    @Override
    public void onUpdateReceived(Update update) {
        monitorService.incrementUpdateCounter();

        setLastUpdate(update);
//      1) Handling incoming message

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            chatDataHolderMap.putIfAbsent(chatId, new ChatDataHolder());

            switch (messageText) {
                case "/calendar":
                    executeMessage(calendarController.startCalendar(String.valueOf(chatId),
                            "Chose ur bitrh date"));
                    break;
                case "/start":
                    registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    showMainMenu(chatId);
                    ChatDataHolder chatDataHolder = chatDataHolderMap.get(chatId);
                    chatDataHolder.setChatState(ServiceState.DEFAULT_STATE);
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                    break;
                case "/help":
                    prepareAndSendMessage(chatId, config.getHelpText());
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                    break;
                case "/initnumber":
                    number = 1;
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                    prepareAndSendMessage(chatId, number.toString());
                    break;
                case "/setnumber":
                    number = 2;
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                    break;
                case "/allusers":
                    prepareAndSendMessage(chatId, adminService.showAllUsers(chatId));
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                    break;
                case "/getnumber":
                    prepareAndSendMessage(chatId, number.toString());
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                    break;
//                case "/register":
//                    register(chatId);
//                    // chatState = ServiceState.DEFAULT_STATE;
//                    chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
//                    break;
//                case "/newcard":
//                    log.info("Building new bank card...");
//                    prepareAndSendMessage(chatId, "Введите название банка");
//                    chatDataHolderMap.get(chatId).setChatState(ServiceState.NEW_BANK_CARD__AWAITING_BANK_NAME);
//                    log.info("Waiting for bank name...");
//                    break;
//                case "/newcategory":
//                    log.info("Building new category...");
//                    prepareAndSendMessage(chatId, "Введите название категории");
//                    chatDataHolderMap.get(chatId).setChatState(ServiceState.NEW_CATEGORY__AWAITING_CATEGORY_NAME);
//                    log.info("Waiting for category name...");
//                    break;
                case "/mycards":
                    showMyCards(chatId, false);
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                    break;
                case "/mycategories":
                    showMyCategories(chatId);
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                    break;
                case "/feedback":
                    showMenu(chatId, "Введите сообщение ниже, максимум 500 символов", cancel_toMainMenuList);
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.NEW_FEEDBACK__AWATING_MESSAGE);
                    break;
//                case "/newchance":
//                    log.info("Building new cashback chance");
//                    prepareAndSendMessage(chatId, "Выберите карту");
//                    showMyCards(chatId, true);
//                    chatDataHolderMap.get(chatId).setChatState(ServiceState.NEW_CHANCE__AWAITING_BANK_CARD_ID);
//                    log.info("Waiting for bank card id...");
//                    break;
                case "/mychances":
                    showMyChances(chatId);
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                    break;
                case "/mychancesofcategory":
                    prepareAndSendMessage(chatId, "Выберите категорию");
                    showMyCategories(chatId);
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.ALL_CHANCES__AWAITING_CATEGORY_ID);
                    break;
                case "/mychancesofcategoryactive":
                    prepareAndSendMessage(chatId, "Выберите категорию");
                    showMyCategories(chatId);
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.ALL_CHANCES_ACTIVE__AWAITING_CATEGORY_ID);
                    break;
                case "/mainmenu":
                    showMenu(chatId, "Главное меню", mainMenuList);
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                    break;
                case "/cardsmenu":
                    showMenu(chatId, "Мои банковские карты", cardMenuList);
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                    break;
                case "/categoriesmenu":
                    showMenu(chatId, "Мои категории кешбека", categoryMenuList);
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                    break;
                case "/version":
                    prepareAndSendMessage(chatId, getBotVersion());
                    showMainMenu(chatId);
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                    break;
                default:

                    switch (chatDataHolderMap.get(chatId).getChatState()) {
                        case NEW_BANK_CARD__AWAITING_BANK_NAME:
                            log.info(chatId + " Received bank name: " + update.getMessage().getText());
                            showMenu(chatId, "Введите название карты", cancel_toMainMenuList);
                            log.info(chatId + " Waiting for card name...");
                            chatDataHolderMap.get(chatId).setChatState(ServiceState.NEW_BANK_CARD__AWAITING_BANK_CARD_NAME);
                            chatDataHolderMap.get(chatId).setNewBankName(update.getMessage().getText());

                            break;
                        case EDIT_BANK_CARD__AWAITING_BANK_NAME:
                            chatDataHolderMap.get(chatId).setNewBankName(update.getMessage().getText());
                            chatDataHolderMap.get(chatId).setChatState(ServiceState.EDIT_BANK_CARD__AWAITING_BANK_CARD_NAME);
                            showMenu(chatId, "Введите новое название карты", cancel_toMainMenuList);

                            break;
                        case NEW_BANK_CARD__AWAITING_BANK_CARD_NAME:
                            chatDataHolderMap.get(chatId).setNewCardName(update.getMessage().getText());
                            log.info(chatId + "Received bank card name: " + update.getMessage().getText());
                            BankCard bankCard = entityBuilders.bankCardBuilder(repository.getUserByChatId(chatId),
                                    chatDataHolderMap.get(chatId).getNewBankName(),
                                    chatDataHolderMap.get(chatId).getNewCardName(),
                                    true, 0L);
                            if (!repository.hasCardDuplicatesInDb(chatId, bankCard)) {
                                repository.saveBankCardToDb(bankCard);
                                prepareAndSendMessage(bankCard.getUser().getChatId(), "Карта сохранена: " + bankCard);
                                log.info(chatId + " Bank card saved to db - " + bankCard);
                            } else {
                                prepareAndSendMessage(chatId, "Ошибка. Карта с таким именем уже существует!");
                                log.info(chatId + " Bank card with the same name exists! - " + update.getMessage().getText());
                            }
                            showCardsMenu(chatId);
                            chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                            break;
                        case EDIT_BANK_CARD__AWAITING_BANK_CARD_NAME:
                            chatDataHolderMap.get(chatId).setNewCardName(update.getMessage().getText());
                            BankCard bankCard1 = entityBuilders.bankCardBuilder(repository.getUserByChatId(chatId),
                                    chatDataHolderMap.get(chatId).getNewBankName(),
                                    chatDataHolderMap.get(chatId).getNewCardName(),
                                    false,
                                    chatDataHolderMap.get(chatId).getBankCardId());
                            repository.saveBankCardToDb(bankCard1);
                            prepareAndSendMessage(chatId, "Карта изменена!");
                            showCardsMenu(chatId);
                            chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                            break;

                        case EDIT_CAT__AWAITING_CAT_NAME:
                            chatDataHolderMap.get(chatId).setNewCategoryName(update.getMessage().getText());
                            CbCategory cbCategory111 = entityBuilders.cbCategoryBuilder(repository.getUserByChatId(chatId),
                                    chatDataHolderMap.get(chatId).getNewCategoryName(), false,
                                    chatDataHolderMap.get(chatId).getCategoryId());
                            repository.saveCbCategoryToDb(cbCategory111);
                            prepareAndSendMessage(chatId, "Категория изменена!");
                            showCategoriesMenu(chatId);
                            chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                            break;
                        case NEW_CATEGORY__AWAITING_CATEGORY_NAME:
                            chatDataHolderMap.get(chatId).setNewCategoryName(update.getMessage().getText());
                            CbCategory cbCategory = entityBuilders.cbCategoryBuilder(repository.getUserByChatId(chatId),
                                    chatDataHolderMap.get(chatId).getNewCategoryName(), true, 0L);
                            if (!repository.hasCatDuplicatesInDb(chatId, cbCategory)) {
                                repository.saveCbCategoryToDb(cbCategory);
                                prepareAndSendMessage(cbCategory.getUser().getChatId(), "Категория сохранена: " + cbCategory);
                            } else {
                                prepareAndSendMessage(chatId, "Ошибка. Категория с таким именем уже существует!");
                            }
                            showCategoriesMenu(chatId);

                            chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                            break;
                        case NEW_FEEDBACK__AWATING_MESSAGE:
                            if (update.getMessage().getText().length() >= 500) {
                                prepareAndSendMessage(chatId, "Слишком длинное сообщение, максимум - 500 символов");
                            } else {
                                repository.saveFeedbackToDb(entityBuilders.feedbackBuilder(
                                        repository.getUserByChatId(chatId),
                                        update.getMessage().getText()));

                                prepareAndSendMessage(chatId, "Обратная связь получена");
                                log.info(chatId + "- feedback received");
                            }
                            showMainMenu(chatId);
                            chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                            break;
                        case NEW_CHANCE__AWAITING_BANK_CARD_ID:
                            String bankCardId = update.getMessage().getText();
                            chatDataHolderMap.get(chatId).setBankCardForNewChance(repository.findBankCardById(Long.valueOf(bankCardId)));
                            chatDataHolderMap.get(chatId).setChatState(ServiceState.NEW_CHANCE__AWAITING_CATEGORY_ID);
                            showMenu(chatId, "Введите id категории", cancel_toMainMenuList);

                            showMyCategories(chatId);
                            break;
                        case NEW_CHANCE__AWAITING_CATEGORY_ID:
                            String categoryId = update.getMessage().getText();
                            chatDataHolderMap.get(chatId).setCategoryForNewChance(repository.findCatById(Long.valueOf(categoryId)));
                            chatDataHolderMap.get(chatId).setChatState(ServiceState.NEW_CHANCE__AWAITING_RATE);
                            showMenu(chatId, "Введите % кешбека", cancel_toMainMenuList);
                            break;
                        case NEW_CHANCE__AWAITING_RATE:
                            chatDataHolderMap.get(chatId).setRateForNewChance(Double.parseDouble(update.getMessage().getText()) / 100);
                            chatDataHolderMap.get(chatId).setChatState(ServiceState.NEW_CHANCE__AWAITING_START_DATE);
//                            showMenu(chatId, "Введите дату начала действия кэшбека в формате dd-mm-yyyy", cancel_toMainMenuList);

                            executeMessage(calendarController.startCalendar(String.valueOf(chatId),
                                    "Выберите дату начала кэшбека"));

                            break;
//                        case NEW_CHANCE__AWAITING_START_DATE:
////                            перенести ниже
//
//                            chatDataHolderMap.get(chatId).setStartDateOfNewChance(
//                                    LocalDate.parse(update.getMessage().getText(),
//                                            DateTimeFormatter.ofPattern("dd-MM-yyyy")));
//                            chatDataHolderMap.get(chatId).setChatState(ServiceState.NEW_CHANCE__AWAITING_END_DATE);
//                            showMenu(chatId, "Введите дату окончания действия кэшбека в формате dd-mm-yyyy", cancel_toMainMenuList);
//                            break;
//                        case NEW_CHANCE__AWAITING_END_DATE:
////                            перенести ниже
//                            chatDataHolderMap.get(chatId).setEndDateOfNewChance(LocalDate.parse(update.getMessage().getText(),
//                                    DateTimeFormatter.ofPattern("dd-MM-yyyy")));
//
//                            if (chatDataHolderMap.get(chatId).getEndDateOfNewChance()
//                                    .isAfter(chatDataHolderMap.get(chatId).getStartDateOfNewChance())
//                                    || chatDataHolderMap.get(chatId).getEndDateOfNewChance()
//                                    .isEqual(chatDataHolderMap.get(chatId).getStartDateOfNewChance())) {
//
//                                chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
//                                repository.saveCbChanceToDb(
//                                        entityBuilders.cbChanceBuilder(repository.getUserByChatId(chatId), chatDataHolderMap, chatId));
//                                prepareAndSendMessage(chatId, "Кэшбек сохранён!");
//                                showChancesMenu(chatId);
//                            } else {
//                                prepareAndSendMessage(chatId, "Введённая дата не может быть раньше даты начала");
//                            }
//                            break;
                        case ALL_CHANCES__AWAITING_CATEGORY_ID:
                            String categoryId2 = update.getMessage().getText();
                            CbCategory cbCategory2 = repository.findCatById(Long.valueOf(categoryId2));
                            showMyChancesOfCategory(chatId, cbCategory2.getName(), false);

                            chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                            break;
                        case ALL_CHANCES_ACTIVE__AWAITING_CATEGORY_ID:
                            String categoryId3 = update.getMessage().getText();
                            CbCategory cbCategory3 = repository.findCatById(Long.valueOf(categoryId3));
                            showMyChancesOfCategory(chatId, cbCategory3.getName(), true);
                            chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                            break;
                        case DELETE_CHANCE__AWAITING_CHANCE_ID:
                            log.info("Deleting Chance...");
                            repository.deleteCbChance(Long.valueOf(update.getMessage().getText()), this);
                            prepareAndSendMessage(chatId, "Кэшбек удален из базы данных");

                            chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                            showChancesMenu(chatId);
                            break;
                        default:
                            prepareAndSendMessage(chatId, "Sorry, command was not recognized");
                            chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                    }
            }
//      2) Handling - inlinekeyboard button is pressed
            //          a) Calendar unit
        } else if (update.hasCallbackQuery() &
                update.getCallbackQuery().getData().contains(InlineCalendar.CONTROL_ALIAS) |
                update.getCallbackQuery().getData().contains(InlineCalendar.DATE_ALIAS)) {

            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (update.getCallbackQuery().getData().contains(InlineCalendar.CONTROL_ALIAS)) {
                try {
                    execute(calendarController.control(update.getCallbackQuery()));
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else if ((update.getCallbackQuery().getData().contains(InlineCalendar.DATE_ALIAS))) {
                String result = calendarController.resolve(update.getCallbackQuery());
                System.out.println("User date is: " + result);


                switch (chatDataHolderMap.get(chatId).getChatState()) {
                    case NEW_CHANCE__AWAITING_START_DATE:
                        chatDataHolderMap.get(chatId).setStartDateOfNewChance(
                                LocalDate.parse(result,
                                        DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                        chatDataHolderMap.get(chatId).setChatState(ServiceState.NEW_CHANCE__AWAITING_END_DATE);
                        executeMessage(calendarController.startCalendar(String.valueOf(chatId),
                                "Выберите дату окончания действия кэшбека"));
                        break;
                    case NEW_CHANCE__AWAITING_END_DATE:
                        chatDataHolderMap.get(chatId).setEndDateOfNewChance(LocalDate.parse(result,
                                DateTimeFormatter.ofPattern("yyyy-MM-dd")));

                        if (chatDataHolderMap.get(chatId).getEndDateOfNewChance()
                                .isAfter(chatDataHolderMap.get(chatId).getStartDateOfNewChance())
                                || chatDataHolderMap.get(chatId).getEndDateOfNewChance()
                                .isEqual(chatDataHolderMap.get(chatId).getStartDateOfNewChance())) {

                            chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                            repository.saveCbChanceToDb(
                                    entityBuilders.cbChanceBuilder(repository.getUserByChatId(chatId), chatDataHolderMap, chatId));
                            prepareAndSendMessage(chatId, "Кэшбек сохранён!");
                            showChancesMenu(chatId);
                        } else {
                            prepareAndSendMessage(chatId, "Введённая дата не может быть раньше даты начала");
                        }
                        break;
                }
            }


//          b) General unit
        } else if (update.hasCallbackQuery()) {

            String callbackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            chatDataHolderMap.putIfAbsent(chatId, new ChatDataHolder());
//          Edition of sent message after pressing button
            String text = "Вы выбрали: " + callbackData;
            executeEditMessageText(text, chatId, messageId);
            MenuOption menuOption = callbackParser(callbackData);

            switch (menuOption.getMenuButton()) {
                case MAINMENU_MYCASHBACK:
                    showChancesMenu(chatId);
                    break;
                case MAINMENU_MYCARDS:
                    showCardsMenu(chatId);
                    break;
                case MAINMENU_MYCATEGORIES:
                    showCategoriesMenu(chatId);
                    break;
                case ALLMENU_TOMAINMENU:
                    showMainMenu(chatId);
                    break;
                case CARDSMENU_ALLMYCARDS:
                    showMyCards(chatId, false);
                    showCardsMenu(chatId);
                    break;
                case CARDSMENU_NEWCARD:
//                  TODO refactor. DRY
                    log.info(chatId + "Building new bank card...");
                    showMenu(chatId, "Введите название банка", cancel_toMainMenuList);
                    log.info(chatId + "Waiting for bank name...");
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.NEW_BANK_CARD__AWAITING_BANK_NAME);
                    break;
                case CARDSMENU_EDITCARD:
                    showMenu(chatId, "Выберите карту",
                            menuBuilders.cardMenuBuilder(repository.getMyCardsList(chatId),
                                    MenuButtons.CARDSMENU_EDITCARD_ANYCARD));
                    break;
                case CARDSMENU_EDITCARD_ANYCARD:
                    log.info(chatId + "Editing bank card...");
                    prepareAndSendMessage(chatId, "Выбрана карта " + menuOption.getOptionName());
                    chatDataHolderMap.get(chatId).setBankCardId(repository.getMyCardId(chatId, menuOption.getOptionName()));
                    showMenu(chatId, "Введите новое название банка", cancel_toMainMenuList);
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.EDIT_BANK_CARD__AWAITING_BANK_NAME);
                    break;
                case CARDSMENU_DELETECARD:
                    showMenu(chatId,
                            "Выберите карту",
                            menuBuilders.cardMenuBuilder(repository.getMyCardsList(chatId), MenuButtons.CARDSMENU_DELETECARD_ANYCARD));
                    break;
                case CARDSMENU_DELETECARD_ANYCARD:
                    prepareAndSendMessage(chatId, "Выбрана карта " + menuOption.getOptionName());
                    chatDataHolderMap.get(chatId).setBankCardId(repository.getMyCardId(chatId, menuOption.getOptionName()));
                    showMenu(chatId, "Будет удалена карта " + menuOption.getOptionName() + " со всеми кешбеками. \n" +
                            "Эту операцию нельзя отменить. Вы уверены?", choiceMenuList);
                    chatDataHolderMap.get(chatId).setChoice(DELETE_BANK_CARD);
                    break;

                case CHOICEMENU_NO:
                    prepareAndSendMessage(chatId, "Операция отменена");
                    showMenu(chatId, "Главное меню", mainMenuList);
                    break;
                case CHOICEMENU_YES:

                    switch (chatDataHolderMap.get(chatId).getChoice()) {
                        case DELETE_BANK_CARD:
                            log.info(chatId + "Deleting bank card...");
                            repository.deleteBankCard(chatDataHolderMap.get(chatId).getBankCardId());
                            prepareAndSendMessage(chatId, "Карта удалена из базы данных");
                            showCardsMenu(chatId);
                            break;
                        case DELETE_CATEGORY:
                            log.info(chatId + "Deleting category...");
                            repository.deleteCategory(chatDataHolderMap.get(chatId).getCategoryId());
                            prepareAndSendMessage(chatId, "Категория удалена из базы данных");
                            showCategoriesMenu(chatId);
                            break;
                    }
                    break;

                case CATMENU_MYCATS:
                    showMyCategories(chatId);
                    showCategoriesMenu(chatId);
                    break;

                case CATMENU_NEWCAT:
                    log.info(chatId + "Building new category...");
                    showMenu(chatId, "Введите название категории", cancel_toMainMenuList);
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.NEW_CATEGORY__AWAITING_CATEGORY_NAME);
                    log.info(chatId + "Waiting for category name...");
                    break;

                case CATMENU_EDITCAT:
                    log.info(chatId + "Editing category...");
                    showMenu(chatId, "Выберите категорию",
                            menuBuilders.categoryMenuBuilder(repository.getMyCategoriesList(chatId),
                                    MenuButtons.CATMENU_EDITCAT_ANYCAT));
                    break;
                case CATMENU_EDITCAT_ANYCAT:
                    prepareAndSendMessage(chatId, "Выбрана категория " + menuOption.getOptionName());
                    chatDataHolderMap.get(chatId).setCategoryId(
                            repository.getMyCategoryId(chatId, menuOption.getOptionName()));
                    log.info(chatId + "Editing category card...");
                    showMenu(chatId, "Введите новое название категории", cancel_toMainMenuList);
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.EDIT_CAT__AWAITING_CAT_NAME);
                    break;
                case CATMENU_DELETECAT:
                    showMenu(chatId,
                            "Выберите категорию",
                            menuBuilders.categoryMenuBuilder(repository.getMyCategoriesList(chatId), MenuButtons.CATMENU_DELETECAT_ANYCAT));
                    break;
                case CATMENU_DELETECAT_ANYCAT:
                    prepareAndSendMessage(chatId, "Выбрана категория " + menuOption.getOptionName());
                    chatDataHolderMap.get(chatId).setCategoryId(
                            repository.getMyCategoryId(chatId, menuOption.getOptionName()));

                    showMenu(chatId, "Будет удалена категория " + menuOption.getOptionName() + " со всеми кешбеками. \n" +
                            "Эту операцию нельзя отменить. Вы уверены?", choiceMenuList);
                    chatDataHolderMap.get(chatId).setChoice(DELETE_CATEGORY);
                    break;
                case CHANCES_ACTCHANCES_CAT:
                    showMenu(chatId, "Выберите категорию",
                            menuBuilders.categoryMenuBuilder(repository.getMyCategoriesList(chatId), MenuButtons.ACTCHANCES_CAT_CHOSEN));
                    break;
                case ACTCHANCES_CAT_CHOSEN:
                    chatDataHolderMap.get(chatId).setCategoryId(
                            repository.getMyCategoryId(chatId, menuOption.getOptionName()));
                    showMyChancesOfCategory(chatId, menuOption.getOptionName(), true);
                    showChancesMenu(chatId);
                    break;
                case CHANCES_ACTCHANCES_CARD:
                    showMenu(chatId, "Выберите карту",
                            menuBuilders.cardMenuBuilder(repository.getMyCardsList(chatId), MenuButtons.ACTCHANCES_CARD_CHOSEN));
                    break;
                case ACTCHANCES_CARD_CHOSEN:
                    chatDataHolderMap.get(chatId).setBankCardId(repository.getMyCardId(chatId, menuOption.getOptionName()));
                    showMyChancesOfCard(chatId, menuOption.getOptionName(), true, false);
                    showChancesMenu(chatId);
                    break;

                case CHANCESMENU_NEWCHANCE:
                    showMenu(chatId, "Выберите карту",
                            menuBuilders.cardMenuBuilder(repository.getMyCardsList(chatId),
                                    MenuButtons.NEWCHANCE_ANYCARD));
                    break;
                case NEWCHANCE_ANYCARD:
                    prepareAndSendMessage(chatId, "Выбрана карта " + menuOption.getOptionName());

                    chatDataHolderMap.get(chatId).setBankCardForNewChance(
                            repository.findBankCardById(repository.getMyCardId(chatId, menuOption.getOptionName())));

                    showMenu(chatId, "Выберите категорию",
                            menuBuilders.categoryMenuBuilder(repository.getMyCategoriesList(chatId),
                                    MenuButtons.NEWCHANCE_ANYCAT));
                    break;
                case NEWCHANCE_ANYCAT:
                    prepareAndSendMessage(chatId, "Выбрана категория " + menuOption.getOptionName());

                    chatDataHolderMap.get(chatId).setCategoryForNewChance(repository.findCatById(
                            repository.getMyCategoryId(chatId, menuOption.getOptionName())));
                    showMenu(chatId, "Введите % кэшбека", cancel_toMainMenuList);
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.NEW_CHANCE__AWAITING_RATE);
                    break;
                case CHANCESMENU_DELETECHANCE:
                    prepareAndSendMessage(chatId, "Выберите карту");
                    showMenu(chatId, "Выберите карту, по которой нужно удалить кэшбек",
                            menuBuilders.cardMenuBuilder(repository.getMyCardsList(chatId),
                                    MenuButtons.DELETECHANCE_ANYCARD));
                    break;
                case DELETECHANCE_ANYCARD:
                    prepareAndSendMessage(chatId, "Выбрана карта " + menuOption.getOptionName());
                    chatDataHolderMap.get(chatId).setBankCardId(repository.getMyCardId(chatId, menuOption.getOptionName()));
                    prepareAndSendMessage(chatId, "Весь ваш действующий кэшбек по карте.\n" +
                            "Введите Id номер кэшбека, который хотите удалить:");
                    showMyChancesOfCard(chatId, menuOption.getOptionName(), true, true);
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.DELETE_CHANCE__AWAITING_CHANCE_ID);
                    break;
                default:
                    break;
            }
        }
    }

    void showMainMenu(long chatId) {
        menuBuilders.delay(config.getMenuDelayTime());
        showMenu(chatId, "Главное меню", mainMenuList);
    }

    void showCategoriesMenu(long chatId) {
        menuBuilders.delay(config.getMenuDelayTime());
        showMenu(chatId, "Категории кэшбека", categoryMenuList);
    }

    void showCardsMenu(long chatId) {
        menuBuilders.delay(config.getMenuDelayTime());
        showMenu(chatId, "Банковские карты", Constants.cardMenuList);
    }

    /**
     * @param chatId              - telegram user chatID
     * @param menuName            - name of the menu, that is printed above menu options
     * @param menuOptionArrayList - menu options that are displayed in message zone below menu name
     */
    public void showMenu(long chatId, String menuName, List<MenuOption> menuOptionArrayList) {
        SendMessage sendMessage = menuBuilders.inlineMenuBuilder(chatId, menuName, menuOptionArrayList);
        executeMessage(sendMessage);
        lastSendMessage = sendMessage;
    }

    private void showMyCards(long chatId, boolean showWithId) {
        List<BankCard> bankCardList = (List<BankCard>) repository.getAllBankCards();
        Collections.sort(bankCardList);
        StringBuilder bankCardString = new StringBuilder();
        for (BankCard bankCard : bankCardList) {
            if (bankCard.getUser().getChatId().equals(chatId)) {
                if (!showWithId) bankCardString.append(bankCard).append("\n");
                if (showWithId) bankCardString.append(bankCard.getCardId().toString()).append(bankCard).append("\n");
            }
        }
        prepareAndSendMessage(chatId, bankCardString.toString());
    }

    private void showMyCategories(long chatId) {
        List<CbCategory> cbCategories = (List<CbCategory>) repository.getAllCbCategories();
        Collections.sort(cbCategories);
        StringBuilder s = new StringBuilder();
        for (CbCategory cbCategory : cbCategories) {
            if (cbCategory.getUser().getChatId().equals(chatId)) {
                s.append(cbCategory).append("\n");
            }
        }
        prepareAndSendMessage(chatId, s.toString());
    }

    private MenuOption callbackParser(String callback) {
        List<String> resultList = List.of(callback.split(DELIMETER));
        return new MenuOption(
                resultList.get(0),
                MenuButtons.valueOf(resultList.get(1)));
    }

    private void showMyChances(long chatId) {
        Iterable<CbChance> cbChances = repository.getAllCbChances();

        StringBuilder s = new StringBuilder();
        for (CbChance cbChance : cbChances) {
            if (cbChance.getUser().getChatId().equals(chatId)) {
                s.append(cbChance).append("\n");
            }
        }
        prepareAndSendMessage(chatId, s.toString());
    }

    private void showMyChancesOfCategory(long chatId, String categoryName, boolean isActive) {
        Iterable<CbChance> cbChances = repository.getAllCbChances();
        StringBuilder s = new StringBuilder();
        for (CbChance cbChance : cbChances) {
            if (cbChance.getUser().getChatId().equals(chatId)) {

                if (isActive) {
                    LocalDate todayDate = LocalDate.now();
                    if (cbChance.getEndDate().isAfter(todayDate.minusDays(1)) &&
                            cbChance.getStartDate().isBefore(todayDate.plusDays(1))) {
                        if (cbChance.getCbCategory().getName().equals(categoryName)) {
                            s.append(cbChance).append("\n");
                        }
                    }
                } else {
                    if (cbChance.getCbCategory().getName().equals(categoryName)) {
                        s.append(cbChance).append("\n");
                    }
                }
            }
        }
        if (s.toString().equals("")) {
            s = new StringBuilder("Записей не найдено!");
        }
        prepareAndSendMessage(chatId, s.toString());
    }

    private void showMyCard(long chatId, String cardName) {
        Iterable<BankCard> bankCardList = repository.getAllBankCards();
        StringBuilder bankCardString = new StringBuilder();
        for (BankCard bankCard : bankCardList) {
            if (bankCard.getUser().getChatId().equals(chatId) && bankCard.getName().equals(cardName)) {
                bankCardString.append(bankCard).append("\n");
            }
        }
        prepareAndSendMessage(chatId, bankCardString.toString());
    }

    private void showMyChancesOfCard(long chatId, String cardName, boolean isActive, boolean showWithId) {
        Iterable<CbChance> cbChances = repository.getAllCbChances();
        StringBuilder s = new StringBuilder();
        for (CbChance cbChance : cbChances) {
            if (cbChance.getUser().getChatId().equals(chatId)) {


                if (isActive) {
                    LocalDate todayDate = LocalDate.now();
                    if (cbChance.getEndDate().isAfter(todayDate.minusDays(1)) &&
                            cbChance.getStartDate().isBefore(todayDate.plusDays(1))) {
                        if (cbChance.getBankCard().getName().equals(cardName)) {
                            if (showWithId) s.append(cbChance.getCbChanceId()).append(cbChance).append("\n");
                            if (!showWithId) s.append(cbChance).append("\n");
                        }
                    } else {
                        if (cbChance.getBankCard().getName().equals(cardName)) {
                            if (showWithId) s.append(cbChance.getCbChanceId()).append(cbChance).append("\n");
                            if (!showWithId) s.append(cbChance).append("\n");
                        }
                    }
                }
            }
        }
        if (s.toString().equals("")) {
            s = new StringBuilder("Записей не найдено!");
        }
        prepareAndSendMessage(chatId, s.toString());

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
        if (repository.getUserById(message).isEmpty()) {
            var chatId = message.getChatId();
            var chat = message.getChat();
            User user = new User();
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
            repository.saveUserToDb(user);
            log.info("User saved to db " + user);

            List<String> cbCategoryNames = new ArrayList<>(Arrays.asList("Авто", "Заправки", "Дом и ремонт", "Кафе и рестораны",
                    "Супермаркеты", "Развлечения", "Такси"));
            for (String s : cbCategoryNames) {
                CbCategory cbCategory = new CbCategory();
                cbCategory.setName(s);
                cbCategory.setUser(user);
                repository.saveCbCategoryToDb(cbCategory);
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
                "Для перехода в главное меню используйте команду /mainmenu \n\n " +
                "Информация о текущей версии /version\n" +
                "Cообщить о проблеме, предложить улучшение /feedback");

        log.info("Replied to user - " + chatId);
        prepareAndSendMessage(chatId, answer);
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
        lastSendMessage = message;
    }

    void showChancesMenu(long chatId) {
        menuBuilders.delay(config.getMenuDelayTime());
        showMenu(chatId, "Мой кэшбэк", chancesMenuList);
    }


    public SendMessage getLastSendMessage() {
        return lastSendMessage;
    }
}

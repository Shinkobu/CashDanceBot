package CashDance.Bot.service;


import CashDance.Bot.config.BotConfig;
import CashDance.Bot.model.*;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.GetUpdates;
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
    private static final String HELP_TEXT = "Это программа-помощник по кешбеку с ваших банковских карт.\n\n" +
            "Пример проблемы: у Василия есть карта альфа-банка, карта тинькофф и карта озон банка. Василий знает, что " +
            "по всем картам каждый месяц обновляются категории кешбека. В один месяц высокий кешбек за заправки по " +
            "тинькофф и за супермаркеты у озона, в следующий - всё меняется. И вот Василий приезжает на заправку и пытается вспомнить " +
            "по какой карте сегодня он получит максимальный кешбек? Нужно открыть банковское приложение и посмотреть!\n\n Хорошо, но " +
            "для этого придётся открывать 3-4 приложения, а это долго, копаться неохота, семья ждёт в машине и Василий оплачивает топливо картой наугад.\n\n" +
            "CashDanceBot решает эту проблему. \n\n Василий может раз в месяц занести информацию по какой карте в каких категориях у него " +
            "кешбек и на заправке просто спросить у бота, какие у него есть действующие кешбеки по категории Заправки. Бот сделает выборку " +
            "и подскажет, что по он получит альфе 1%, по озону 3%, по тинькофф 5%.\n\n" +
            "Навигация осуществляется через меню /mainmenu \n\n";

    private static final String COMMANDS_TEXT = "Раздел в разработке";

    final BotConfig config;
    //    private ServiceState chatState;
    @Autowired
    private EntityBuilders entityBuilders;
    @Autowired
    private MenuBuilders menuBuilders;
    @Autowired
    private Repository repository;
    private String newBankName;
    //    private String newCardName;
//    private String newCategoryName;
//    private BankCard bankCardForNewChance;
//    private CbCategory categoryForNewChance;
//    private Double rateForNewChance;
//    private LocalDate startDateOfNewChance;
//    private LocalDate endDateOfNewChance;
//    private Long bankCardId;
//    private Long cbChanceId;
//    private Long categoryId;
//    private ChoiceFor choiceForEnum;
    private Integer number;

    //        Stores temporary data for runtime purposes.
    //        Solved the problem of multithreading.
    private Map<Long, ChatDataHolder> chatDataHolderMap = new HashMap<>();

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

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    public String getBotVersion() {
        return config.getVersion();
    }

    //    incoming message handling
    @Override
    public void onUpdateReceived(Update update) {


//      1) Handling incoming message

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            chatDataHolderMap.putIfAbsent(chatId, new ChatDataHolder());

            switch (messageText) {
                case "/start":
                    registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    showMainMenu(chatId, 0);
                    ChatDataHolder chatDataHolder = chatDataHolderMap.get(chatId);
                    chatDataHolder.setChatState(ServiceState.DEFAULT_STATE);
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                    break;
                case "/help":
                    prepareAndSendMessage(chatId, HELP_TEXT);
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                    break;
                case "/initnumber":
                    number = 1;
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                    prepareAndSendMessage(chatId, update.getUpdateId().toString());
                    break;
                case "/setnumber":
                    number = 2;
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                    break;
                case "/getnumber":
                    prepareAndSendMessage(chatId, number.toString());
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                    break;
                case "/register":
                    register(chatId);
                    // chatState = ServiceState.DEFAULT_STATE;
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                    break;
                case "/newcard":
                    log.info("Building new bank card...");
                    prepareAndSendMessage(chatId, "Введите название банка");
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.NEW_BANK_CARD__AWAITING_BANK_NAME);
                    log.info("Waiting for bank name...");
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.NEW_BANK_CARD__AWAITING_BANK_NAME);
                    break;
                case "/newcategory":
                    log.info("Building new category...");
                    prepareAndSendMessage(chatId, "Введите название категории");
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.NEW_CATEGORY__AWAITING_CATEGORY_NAME);
                    log.info("Waiting for category name...");
                    break;
                case "/mycards":
                    showMyCards(chatId, false);
                    // chatState = ServiceState.DEFAULT_STATE;
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                    break;
                case "/mycategories":
                    showMyCategories(chatId);
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                    break;
                case "/newchance":
                    log.info("Building new cashback chance");
                    prepareAndSendMessage(chatId, "Выберите карту");
                    showMyCards(chatId, true);
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.NEW_CHANCE__AWAITING_BANK_CARD_ID);
                    log.info("Waiting for bank card id...");
                    break;
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
                    showMenu(chatId, "Главное меню", Constants.mainMenuList);
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                    break;
                case "/cardsmenu":
                    showMenu(chatId, "Мои банковские карты", Constants.cardMenuList);
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                    break;
                case "/categoriesmenu":
                    showMenu(chatId, "Мои категории кешбека", Constants.categoryMenuList);
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                    break;
                case "/version":
                    prepareAndSendMessage(chatId, getBotVersion());
                    showMainMenu(chatId, 2000);
                    chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                    break;
                default:

                    switch (chatDataHolderMap.get(chatId).getChatState()) {
                        case NEW_BANK_CARD__AWAITING_BANK_NAME:
                            log.info(chatId + " Received bank name: " + update.getMessage().getText());
                            prepareAndSendMessage(chatId, "Введите название карты");
                            log.info(chatId + " Waiting for card name...");
                            chatDataHolderMap.get(chatId).setChatState(ServiceState.NEW_BANK_CARD__AWAITING_BANK_CARD_NAME);
                            chatDataHolderMap.get(chatId).setNewBankName(update.getMessage().getText());
//                            prepareAndSendMessage(chatId, chatDataHolderMap.get(chatId).toString());

                            break;
                        case EDIT_BANK_CARD__AWAITING_BANK_NAME:
                            chatDataHolderMap.get(chatId).setNewBankName(update.getMessage().getText());
                            chatDataHolderMap.get(chatId).setChatState(ServiceState.EDIT_BANK_CARD__AWAITING_BANK_CARD_NAME);
                            prepareAndSendMessage(chatId, "Введите новое название карты");
                            break;
                        case NEW_BANK_CARD__AWAITING_BANK_CARD_NAME:
                            chatDataHolderMap.get(chatId).setNewCardName(update.getMessage().getText());
                            log.info(chatId + "Received bank card name: " + update.getMessage().getText());
//                            TODO refactor builder
                            BankCard bankCard = entityBuilders.bankCardBuilder(repository.getUserByChatId(chatId),
                                    chatDataHolderMap.get(chatId).getNewBankName(),
                                    chatDataHolderMap.get(chatId).getNewCardName(),
                                    true, 0L, this);
                            if (!repository.hasCardDuplicatesInDb(chatId, bankCard)) {
                                repository.saveBankCardToDb(bankCard);
                                prepareAndSendMessage(bankCard.getUser().getChatId(), "Карта сохранена: " + bankCard);
                                log.info(chatId + " Bank card saved to db - " + bankCard);
                            } else {
                                prepareAndSendMessage(chatId, "Ошибка. Карта с таким именем уже существует!");
                                log.info(chatId + " Bank card with the same name exists! - " + update.getMessage().getText());
                            }
                            showCardsMenu(chatId, 2000);
                            chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                            break;
                        case EDIT_BANK_CARD__AWAITING_BANK_CARD_NAME:
                            chatDataHolderMap.get(chatId).setNewCardName(update.getMessage().getText());
                            BankCard bankCard1 = entityBuilders.bankCardBuilder(repository.getUserByChatId(chatId),
                                    chatDataHolderMap.get(chatId).getNewBankName(),
                                    chatDataHolderMap.get(chatId).getNewCardName(),
                                    false,
                                    chatDataHolderMap.get(chatId).getBankCardId(),
                                    this);
                            repository.saveBankCardToDb(bankCard1);
                            prepareAndSendMessage(chatId, "Карта изменена!");
                            showCardsMenu(chatId, 2000);
                            chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                            break;

                        case EDIT_CAT__AWAITING_CAT_NAME:
                            chatDataHolderMap.get(chatId).setNewCategoryName(update.getMessage().getText());
                            CbCategory cbCategory111 = entityBuilders.cbCategoryBuilder(repository.getUserByChatId(chatId),
                                    chatDataHolderMap.get(chatId).getNewCategoryName(), false,
                                    chatDataHolderMap.get(chatId).getCategoryId());
                            repository.saveCbCategoryToDb(cbCategory111);
                            prepareAndSendMessage(chatId, "Категория изменена!");
                            showCategoriesMenu(chatId, 2000);
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
                            showCategoriesMenu(chatId, 2000);

                            chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                            break;
                        case NEW_CHANCE__AWAITING_BANK_CARD_ID:
                            String bankCardId = update.getMessage().getText();
                            chatDataHolderMap.get(chatId).setBankCardForNewChance(repository.findBankCardById(Long.valueOf(bankCardId)));
                            chatDataHolderMap.get(chatId).setChatState(ServiceState.NEW_CHANCE__AWAITING_CATEGORY_ID);
                            prepareAndSendMessage(chatId, "Введите id категории");
                            showMyCategories(chatId);
                            break;
                        case NEW_CHANCE__AWAITING_CATEGORY_ID:
                            String categoryId = update.getMessage().getText();
                            chatDataHolderMap.get(chatId).setCategoryForNewChance(repository.findCatById(Long.valueOf(categoryId)));
                            chatDataHolderMap.get(chatId).setChatState(ServiceState.NEW_CHANCE__AWAITING_RATE);
                            prepareAndSendMessage(chatId, "Введите % кешбека");
                            break;
                        case NEW_CHANCE__AWAITING_RATE:
                            chatDataHolderMap.get(chatId).setRateForNewChance(Double.parseDouble(update.getMessage().getText()) / 100);
                            chatDataHolderMap.get(chatId).setChatState(ServiceState.NEW_CHANCE__AWAITING_START_DATE);
                            prepareAndSendMessage(chatId, "Введите дату начала действия кэшбека в формате dd-mm-yyyy");
                            break;
                        case NEW_CHANCE__AWAITING_START_DATE:
                            chatDataHolderMap.get(chatId).setStartDateOfNewChance(
                                    LocalDate.parse(update.getMessage().getText(),
                                            DateTimeFormatter.ofPattern("dd-MM-yyyy")));
                            chatDataHolderMap.get(chatId).setChatState(ServiceState.NEW_CHANCE__AWAITING_END_DATE);
                            prepareAndSendMessage(chatId, "Введите дату окончания действия кэшбека в формате dd-mm-yyyy");
                            break;
                        case NEW_CHANCE__AWAITING_END_DATE:

                            chatDataHolderMap.get(chatId).setEndDateOfNewChance(LocalDate.parse(update.getMessage().getText(),
                                    DateTimeFormatter.ofPattern("dd-MM-yyyy")));

                            if (chatDataHolderMap.get(chatId).getEndDateOfNewChance()
                                    .isAfter(chatDataHolderMap.get(chatId).getStartDateOfNewChance())
                                    || chatDataHolderMap.get(chatId).getEndDateOfNewChance()
                                    .isEqual(chatDataHolderMap.get(chatId).getStartDateOfNewChance())) {

                                chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                                saveNewChancetoDb(chatId);
                                showChancesMenu(chatId, 2000);
                            } else {
                                prepareAndSendMessage(chatId, "Введённая дата не может быть раньше даты начала");
                            }
                            break;
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
                            showChancesMenu(chatId, 2000);
                            break;
                        default:
                            prepareAndSendMessage(chatId, "Sorry, command was not recognized");
                            chatDataHolderMap.get(chatId).setChatState(ServiceState.DEFAULT_STATE);
                    }
            }
//      2) Handling - inlinekeyboard button is pressed
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            chatDataHolderMap.putIfAbsent(chatId, new ChatDataHolder());
//          Edition of sent message after pressing button
            EditMessageText message = new EditMessageText();
            String text = "Вы выбрали: " + callbackData;
            executeEditMessageText(text, chatId, messageId);
            MenuOption menuOption = callbackParser(callbackData);

            switch (menuOption.getMenuButton()) {
                case MAINMENU_MYCASHBACK:
                    showChancesMenu(chatId, 0);
                    break;
                case MAINMENU_MYCARDS:
                    showCardsMenu(chatId, 0);
                    break;
                case MAINMENU_MYCATEGORIES:
                    showCategoriesMenu(chatId, 0);
                    break;
                case ALLMENU_TOMAINMENU:
                    showMainMenu(chatId, 0);
                    break;
                case CARDSMENU_ALLMYCARDS:
                    showMyCards(chatId, false);
                    showCardsMenu(chatId, 2000);
                    break;
                case CARDSMENU_NEWCARD:
//                  TODO refactor. DRY
                    log.info(chatId + "Building new bank card...");
                    prepareAndSendMessage(chatId, "Введите название банка");
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
                    prepareAndSendMessage(chatId, "Введите новое название банка");
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
                    showMenu(chatId, "Главное меню", Constants.mainMenuList);
                    break;
                case CHOICEMENU_YES:

                    switch (chatDataHolderMap.get(chatId).getChoice()) {
                        case DELETE_BANK_CARD:
                            log.info(chatId + "Deleting bank card...");
                            repository.deleteBankCard(chatDataHolderMap.get(chatId).getBankCardId());
                            prepareAndSendMessage(chatId, "Карта удалена из базы данных");
                            showCardsMenu(chatId, 2000);
                            break;
                        case DELETE_CATEGORY:
                            log.info(chatId + "Deleting category...");
                            repository.deleteCategory(chatDataHolderMap.get(chatId).getCategoryId());
                            prepareAndSendMessage(chatId, "Категория удалена из базы данных");
                            showCategoriesMenu(chatId, 2000);
                            break;
                        case DELETE_CHANCE:
                            //TODO
                            break;
                    }
                    break;

                case CATMENU_MYCATS:
                    showMyCategories(chatId);
                    showCategoriesMenu(chatId, 2000);
                    break;

                case CATMENU_NEWCAT:
                    log.info(chatId + "Building new category...");
                    prepareAndSendMessage(chatId, "Введите название категории");
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
                    prepareAndSendMessage(chatId, "Введите новое название категории");
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
                    showChancesMenu(chatId, 2000);
                    break;
                case CHANCES_ACTCHANCES_CARD:
                    showMenu(chatId, "Выберите карту",
                            menuBuilders.cardMenuBuilder(repository.getMyCardsList(chatId), MenuButtons.ACTCHANCES_CARD_CHOSEN));
                    break;
                case ACTCHANCES_CARD_CHOSEN:
                    chatDataHolderMap.get(chatId).setBankCardId(repository.getMyCardId(chatId, menuOption.getOptionName()));
                    showMyChancesOfCard(chatId, menuOption.getOptionName(), true, false);
                    showChancesMenu(chatId, 2000);
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
                    prepareAndSendMessage(chatId, "Введите % кэшбека");
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

    void showMainMenu(long chatId, int delayTime) {
        menuBuilders.delay(delayTime);
        showMenu(chatId, "Главное меню", mainMenuList);
    }

    void showCategoriesMenu(long chatId, int delayTime) {
        menuBuilders.delay(delayTime);
        showMenu(chatId, "Категории кэшбека", categoryMenuList);
    }

    void showCardsMenu(long chatId, int delayTime) {
        menuBuilders.delay(delayTime);
        showMenu(chatId, "Банковские карты", Constants.cardMenuList);
    }

    public void showMenu(long chatId, String menuName, List<MenuOption> menuOptionArrayList) {
        SendMessage sendMessage = menuBuilders.inlineMenuBuilder(chatId, menuName, menuOptionArrayList);
        executeMessage(sendMessage);
    }

    ///// TODO
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
                s.append(cbCategory.toString()).append("\n");
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
                s.append(cbChance.toString()).append("\n");
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
                            s.append(cbChance.toString()).append("\n");
                        }
                    }
                } else {
                    if (cbChance.getCbCategory().getName().equals(categoryName)) {
                        s.append(cbChance.toString()).append("\n");
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
        String bankCardString = "";
        for (BankCard bankCard : bankCardList) {
            if (bankCard.getUser().getChatId().equals(chatId) && bankCard.getName().equals(cardName)) {
                bankCardString += bankCard.toString() + "\n";
            }
        }
        prepareAndSendMessage(chatId, bankCardString);
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
                            if (showWithId) s.append(cbChance.getCbChanceId()).append(cbChance.toString()).append("\n");
                            if (!showWithId) s.append(cbChance.toString()).append("\n");
                        }
                    } else {
                        if (cbChance.getBankCard().getName().equals(cardName)) {
                            if (showWithId) s.append(cbChance.getCbChanceId()).append(cbChance.toString()).append("\n");
                            if (!showWithId) s.append(cbChance.toString()).append("\n");
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

    // TODO refactor
    private void saveNewChancetoDb(long chatId) {
        User user = repository.getUserByChatId(chatId);

        CbChance newCbChance = new CbChance();
        newCbChance.setUser(user);
        newCbChance.setBankCard(chatDataHolderMap.get(chatId).getBankCardForNewChance());
        newCbChance.setCbCategory(chatDataHolderMap.get(chatId).getCategoryForNewChance());
        newCbChance.setRate(chatDataHolderMap.get(chatId).getRateForNewChance());
        newCbChance.setStartDate(chatDataHolderMap.get(chatId).getStartDateOfNewChance());
        newCbChance.setEndDate(chatDataHolderMap.get(chatId).getEndDateOfNewChance());

        repository.saveCbChanceToDb(newCbChance);
        log.info("Chance saved to db " + newCbChance);
        prepareAndSendMessage(chatId, "Кешбек сохранен: " + newCbChance);
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

            List<String> cbCategoryNames = new ArrayList<>();
            cbCategoryNames.addAll(Arrays.asList("Авто", "Заправки", "Дом и ремонт", "Кафе и рестораны",
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
                "Информация о текущей версии /version");
        log.info("Replied to user " + name + " - " + chatId);
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
    }

    void showChancesMenu(long chatId, int delayTime) {
        menuBuilders.delay(delayTime);
        showMenu(chatId, "Мой кэшбэк", chancesMenuList);
    }
}

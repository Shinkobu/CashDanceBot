package CashDance.Bot.service;


import CashDance.Bot.config.BotConfig;
import CashDance.Bot.model.*;
import CashDance.Bot.model.interfaces.*;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
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
            "Навигация осуществляется через меню /mainmenu \n\n";

    private static final String COMMANDS_TEXT = "Раздел в разработке";

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
    private Long bankCardId;
    private Long cbChanceId;
    private Long categoryId;
    private ChoiceFor choiceForEnum;


    public TelegramBot(BotConfig config) {

        chatState = ServiceState.DEFAULT_STATE;
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
                    showMyCards(chatId, false);
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
                    showMyCards(chatId, true);
                    chatState = ServiceState.NEW_CHANCE__AWAITING_BANK_CARD_ID;
                    log.info("Waiting for bank card id...");
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
                case "/cardsmenu":
                    showMenu(chatId, "Мои банковские карты", Constants.cardMenuList);
                    chatState = ServiceState.DEFAULT_STATE;
                    break;
                case "/categoriesmenu":
                    showMenu(chatId, "Мои категории кешбека", Constants.categoryMenuList);
                    chatState = ServiceState.DEFAULT_STATE;
                    break;
                case "/version":
                    prepareAndSendMessage(chatId, getBotVersion());
                    showMainMenu(chatId, 2000);
                    chatState = ServiceState.DEFAULT_STATE;
                    break;
                default:

                    switch (chatState) {
                        case NEW_BANK_CARD__AWAITING_BANK_NAME:
                            newBankName = update.getMessage().getText();
                            log.info(chatId + "Received bank name: " + newBankName);
                            chatState = ServiceState.NEW_BANK_CARD__AWAITING_BANK_CARD_NAME;
                            prepareAndSendMessage(chatId, "Введите название карты");
                            log.info(chatId + "Waiting for card name...");
                            break;
                        case EDIT_BANK_CARD__AWAITING_BANK_NAME:
                            newBankName = update.getMessage().getText();
                            chatState = ServiceState.EDIT_BANK_CARD__AWAITING_BANK_CARD_NAME;
                            prepareAndSendMessage(chatId, "Введите новое название карты");
                            break;
                        case NEW_BANK_CARD__AWAITING_BANK_CARD_NAME:
                            newCardName = update.getMessage().getText();
                            log.info(chatId + "Received bank card name: " + newCardName);
                            BankCard bankCard = bankCardBuilder(chatId, newBankName, newCardName, true, 0L);
                            if (!hasDuplicatesInDb(chatId, bankCard, bankCardRepository)) {
                                saveBankCardToDb(bankCard);
                                prepareAndSendMessage(bankCard.getUser().getChatId(), "Карта сохранена: " + bankCard);
                                log.info(chatId + "Bank card saved to db - " + bankCard);
                            } else {
                                prepareAndSendMessage(chatId, "Ошибка. Карта с таким именем уже существует!");
                                log.info(chatId + "Bank card with the same name exists! - " + newCardName);
                            }
                            showCardsMenu(chatId, 2000);
                            chatState = ServiceState.DEFAULT_STATE;
                            break;
                        case EDIT_BANK_CARD__AWAITING_BANK_CARD_NAME:
                            newCardName = update.getMessage().getText();
                            BankCard bankCard1 = bankCardBuilder(chatId, newBankName, newCardName, false, bankCardId);
                            saveBankCardToDb(bankCard1);
                            showMainMenu(chatId, 2000);
                            chatState = ServiceState.DEFAULT_STATE;
                            break;

                        case EDIT_CAT__AWAITING_CAT_NAME:
                            newCategoryName = update.getMessage().getText();

                            CbCategory cbCategory111 = cbCategoryBuilder(chatId, newCategoryName, true, 0L);
                            saveCbCategoryToDb(cbCategory111);

                            showCategoriesMenu(chatId,2000);
                            chatState = ServiceState.DEFAULT_STATE;
                            break;
                        case NEW_CATEGORY__AWAITING_CATEGORY_NAME:
                            newCategoryName = update.getMessage().getText();
                            CbCategory cbCategory = cbCategoryBuilder(chatId, newCategoryName, true, 0L);

                            if (!hasDuplicatesInDb(chatId, cbCategory, cbCategoryRepository)) {
                                saveCbCategoryToDb(cbCategory);
                                prepareAndSendMessage(cbCategory.getUser().getChatId(), "Категория сохранена: " + cbCategory);
                            } else {
                                prepareAndSendMessage(chatId, "Ошибка. Категория с таким именем уже существует!");
                            }
                            showCategoriesMenu(chatId, 2000);
                            chatState = ServiceState.DEFAULT_STATE;
                            break;
                        case NEW_CHANCE__AWAITING_BANK_CARD_ID:
                            String bankCardId = update.getMessage().getText();
                            Optional<BankCard> bankCard11 = bankCardRepository.findById(Long.valueOf(bankCardId));
                            bankCardForNewChance = bankCard11.get();
                            chatState = ServiceState.NEW_CHANCE__AWAITING_CATEGORY_ID;
                            prepareAndSendMessage(chatId, "Введите id категории");
                            showMyCategories(chatId);
                            break;
                        case NEW_CHANCE__AWAITING_CATEGORY_ID:
                            String categoryId = update.getMessage().getText();
                            Optional<CbCategory> cbCategory1 = cbCategoryRepository.findById(Long.valueOf(categoryId));
                            categoryForNewChance = cbCategory1.get();
                            chatState = ServiceState.NEW_CHANCE__AWAITING_RATE;
                            prepareAndSendMessage(chatId, "Введите % кешбека");
                            break;
                        case NEW_CHANCE__AWAITING_RATE:
                            rateForNewChance = Double.parseDouble(update.getMessage().getText()) / 100;
                            chatState = ServiceState.NEW_CHANCE__AWAITING_START_DATE;
                            prepareAndSendMessage(chatId, "Введите дату начала действия кэшбека в формате dd-mm-yyyy");
                            break;
                        case NEW_CHANCE__AWAITING_START_DATE:
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
                            Optional<CbCategory> cbCategory11 = cbCategoryRepository.findById(Long.valueOf(categoryId2));
                            CbCategory cbCategory2 = cbCategory11.get();
                            showMyChancesOfCategory(chatId, cbCategory2.getName(), false);
                            chatState = ServiceState.DEFAULT_STATE;
                            break;
                        case ALL_CHANCES_ACTIVE__AWAITING_CATEGORY_ID:
                            String categoryId3 = update.getMessage().getText();
                            Optional<CbCategory> cbCategory3 = cbCategoryRepository.findById(Long.valueOf(categoryId3));
                            CbCategory cbCategory4 = cbCategory3.get();
                            showMyChancesOfCategory(chatId, cbCategory4.getName(), true);
                            chatState = ServiceState.DEFAULT_STATE;
                            break;
                        case DELETE_CHANCE__AWAITING_CHANCE_ID:
                            cbChanceId = Long.valueOf(update.getMessage().getText());
                            log.info("Deleting Chance...");
                            deleteCbChance(cbChanceId);
                            prepareAndSendMessage(chatId, "Кэшбек удален из базы данных");
                            chatState = ServiceState.DEFAULT_STATE;
                            showChancesMenu(chatId, 2000);
                            break;
                        default:
                            prepareAndSendMessage(chatId, "Sorry, command was not recognized");
                            chatState = ServiceState.DEFAULT_STATE;
                    }
            }
//      2) Handling - inlinekeyboard button is pressed
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
//          Edition of sent message after pressing button
            EditMessageText message = new EditMessageText();
            String text = "Вы выбрали: " + callbackData;
            executeEditMessageText(text, chatId, messageId);

            MenuOption menuOption = callbackParser(callbackData);

            switch (menuOption.getMenuButton()) {
                case MAINMENU_MYCASHBACK:
                    showChancesMenu(chatId,0);
                    break;
                case MAINMENU_MYCARDS:
                    showCardsMenu(chatId, 0);
                    break;
                case MAINMENU_MYCATEGORIES:
                    showCategoriesMenu(chatId,0);
                    break;
                case ALLMENU_TOMAINMENU:
                    showMainMenu(chatId,0);
                    break;
                case CARDSMENU_ALLMYCARDS:
                    showMyCards(chatId, false);
                    showCardsMenu(chatId, 2000);
                    break;
                case CARDSMENU_NEWCARD:
//                  TODO refactor. DRY
                    log.info(chatId + "Building new bank card...");
                    prepareAndSendMessage(chatId, "Введите название банка");
                    chatState = ServiceState.NEW_BANK_CARD__AWAITING_BANK_NAME;
                    log.info(chatId + "Waiting for bank name...");
                    break;
                case CARDSMENU_EDITCARD:
                    showMenu(chatId, "Выберите карту",
                            cardMenuBuilder(getMyCardsList(chatId),
                                    MenuButtons.CARDSMENU_EDITCARD_ANYCARD));
                    break;
                case CARDSMENU_EDITCARD_ANYCARD:
                    log.info(chatId + "Editing bank card...");
                    prepareAndSendMessage(chatId, "Выбрана карта " + menuOption.getOptionName());
                    bankCardId = getMyCardId(chatId, menuOption.getOptionName());
                    prepareAndSendMessage(chatId, "Введите новое название банка");
                    chatState = ServiceState.EDIT_BANK_CARD__AWAITING_BANK_NAME;
                    break;
                case CARDSMENU_DELETECARD:
                    showMenu(chatId,
                            "Выберите карту",
                            cardMenuBuilder(getMyCardsList(chatId), MenuButtons.CARDSMENU_DELETECARD_ANYCARD));
                    break;
                case CARDSMENU_DELETECARD_ANYCARD:
                    prepareAndSendMessage(chatId, "Выбрана карта " + menuOption.getOptionName());
                    bankCardId = getMyCardId(chatId, menuOption.getOptionName());
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
                            log.info(chatId + "Deleting bank card...");
                            deleteBankCard(bankCardId);
                            prepareAndSendMessage(chatId, "Карта удалена из базы данных");
                            break;
                        case DELETE_CATEGORY:
                            log.info(chatId + "Deleting category...");
                            deleteCategory(categoryId);
                            prepareAndSendMessage(chatId, "Категория удалена из базы данных");
                            break;
                        case DELETE_CHANCE:
                            //TODO
                            break;
                    }
                    showMainMenu(chatId, 2000);
                    break;

                case CATMENU_MYCATS:
                    showMyCategories(chatId);
                    showCategoriesMenu(chatId, 2000);
                    break;

                case CATMENU_NEWCAT:
                    log.info(chatId + "Building new category...");
                    prepareAndSendMessage(chatId, "Введите название категории");
                    chatState = ServiceState.NEW_CATEGORY__AWAITING_CATEGORY_NAME;
                    log.info(chatId +"Waiting for category name...");
                    break;

                case CATMENU_EDITCAT:
                    log.info(chatId +"Editing category...");
                    showMenu(chatId, "Выберите категорию",
                            categoryMenuBuilder(getMyCategoriesList(chatId),
                                    MenuButtons.CATMENU_EDITCAT_ANYCAT));
                    break;
                case CATMENU_EDITCAT_ANYCAT:
                    prepareAndSendMessage(chatId, "Выбрана категория " + menuOption.getOptionName());
                    categoryId = getMyCategoryId(chatId, menuOption.getOptionName());
                    log.info(chatId +"Editing category card...");
                    prepareAndSendMessage(chatId, "Введите новое название категории");
                    chatState = ServiceState.EDIT_CAT__AWAITING_CAT_NAME;
                    break;

                case CATMENU_DELETECAT:
                    showMenu(chatId,
                            "Выберите категорию",
                            categoryMenuBuilder(getMyCategoriesList(chatId), MenuButtons.CATMENU_DELETECAT_ANYCAT));
                    break;
                case CATMENU_DELETECAT_ANYCAT:
                    prepareAndSendMessage(chatId, "Выбрана категория " + menuOption.getOptionName());
                    categoryId = getMyCategoryId(chatId, menuOption.getOptionName());

                    showMenu(chatId, "Будет удалена категория " + menuOption.getOptionName() + " со всеми кешбеками. \n" +
                            "Эту операцию нельзя отменить. Вы уверены?", choiceMenuList);
                    choiceForEnum = DELETE_CATEGORY;
                    break;

                case CHANCES_ACTCHANCES_CAT:
                    showMenu(chatId, "Выберите категорию",
                            categoryMenuBuilder(getMyCategoriesList(chatId), MenuButtons.ACTCHANCES_CAT_CHOSEN));
                    break;

                case ACTCHANCES_CAT_CHOSEN:
                    categoryId = getMyCategoryId(chatId, menuOption.getOptionName());
                    showMyChancesOfCategory(chatId, menuOption.getOptionName(), true);
                    showChancesMenu(chatId, 2000);
                    break;


                case CHANCES_ACTCHANCES_CARD:
                    showMenu(chatId, "Выберите карту",
                            cardMenuBuilder(getMyCardsList(chatId), MenuButtons.ACTCHANCES_CARD_CHOSEN));
                    break;

                case ACTCHANCES_CARD_CHOSEN:
                    bankCardId = getMyCardId(chatId, menuOption.getOptionName());
                    showMyChancesOfCard(chatId, menuOption.getOptionName(), true, false);
                    showChancesMenu(chatId, 2000);
                    break;

                case CHANCESMENU_NEWCHANCE:
                    showMenu(chatId, "Выберите карту",
                            cardMenuBuilder(getMyCardsList(chatId),
                                    MenuButtons.NEWCHANCE_ANYCARD));
                    break;

                case NEWCHANCE_ANYCARD:
                    prepareAndSendMessage(chatId, "Выбрана карта " + menuOption.getOptionName());
                    Optional<BankCard> tempBankCard = bankCardRepository.findById(getMyCardId(chatId, menuOption.getOptionName()));
                    bankCardForNewChance = tempBankCard.get();

                    showMenu(chatId, "Выберите категорию",
                            categoryMenuBuilder(getMyCategoriesList(chatId),
                                    MenuButtons.NEWCHANCE_ANYCAT));
                    break;

                case NEWCHANCE_ANYCAT:
                    prepareAndSendMessage(chatId, "Выбрана категория " + menuOption.getOptionName());
                    Optional<CbCategory> tempCbCategory = cbCategoryRepository.findById(getMyCategoryId(chatId, menuOption.getOptionName()));
                    categoryForNewChance = tempCbCategory.get();

                    prepareAndSendMessage(chatId, "Введите % кэшбека");
                    chatState = ServiceState.NEW_CHANCE__AWAITING_RATE;
                    break;

//                To develop edit CbChance later. For this time only delete is available

//                case CHANCESMENU_EDITCHANCE:
//                    prepareAndSendMessage(chatId, "Выберите карту");
//                    showMenu(chatId, "Выберите карту, по которой нужно изменить кэшбек",
//                            cardMenuBuilder(getMyCardsList(chatId),
//                                    MenuButtons.EDITCHANCE_ANYCARD));
//                    break;
//
//                case EDITCHANCE_ANYCARD:
//                    prepareAndSendMessage(chatId, "Выбрана карта " + menuOption.getOptionName());
//                    bankCardForNewChance = bankCardRepository.findById(getMyCardId(chatId, menuOption.getOptionName())).get();
//
//                    showMenu(chatId, "Выберите кэшбек, который нужно изменить",
//                            cbChanceMenuBuilder(getMyCbChancesList(chatId),
//                                    MenuButtons.EDITCHANCE_ANYCHANCE));
//                    break;
                case CHANCESMENU_DELETECHANCE:
                    prepareAndSendMessage(chatId, "Выберите карту");
                    showMenu(chatId, "Выберите карту, по которой нужно удалить кэшбек",
                            cardMenuBuilder(getMyCardsList(chatId),
                                    MenuButtons.DELETECHANCE_ANYCARD));
                    break;

                case DELETECHANCE_ANYCARD:
                    prepareAndSendMessage(chatId, "Выбрана карта " + menuOption.getOptionName());
                    bankCardId = getMyCardId(chatId, menuOption.getOptionName());
                    prepareAndSendMessage(chatId, "Весь ваш действующий кэшбек по карте.\n" +
                            "Введите Id номер кэшбека, который хотите удалить:");
                    showMyChancesOfCard(chatId, menuOption.getOptionName(), true, true);
                    chatState = ServiceState.DELETE_CHANCE__AWAITING_CHANCE_ID;
                    break;
                default:
                    break;
            }
        }
    }

    private void showChancesMenu(long chatId, int delayTime) {
        delay(delayTime);
        showMenu(chatId, "Мой кэшбэк", Constants.chancesMenuList);
    }

    private void delay(int delayTime) {
        try {
            Thread.sleep(delayTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void showCardsMenu(long chatId, int delayTime) {
        delay(delayTime);
        showMenu(chatId, "Банковские карты", Constants.cardMenuList);
    }

    private void showCategoriesMenu(long chatId, int delayTime) {
        delay(delayTime);
        showMenu(chatId, "Категории кэшбека", categoryMenuList);
    }

    private void showMainMenu(long chatId, int delayTime) {
        delay(delayTime);
        showMenu(chatId, "Главное меню", mainMenuList);
    }

    private void deleteBankCard(Long bankCardId) {
        bankCardRepository.deleteById(bankCardId);
        log.info("BankCard with ID " + bankCardId + " deleted from db");
    }

    private void deleteCategory(Long categoryId) {
        cbCategoryRepository.deleteById(categoryId);
        log.info("Category with ID " + categoryId + " deleted from db");
    }

    private void deleteCbChance(Long cbChanceId) {
        cbChanceRepository.deleteById(cbChanceId);
        log.info("Chance with ID " + cbChanceId + " deleted from db");
    }

    /**
     * Builds list of MenuOptions which is required to show inline menu
     *
     * @param bankCardList - list of bank cards
     * @param menuButtons  - special reply signal for each button in menu. Used to determine that button of this menu
     *                     is pressed
     * @return list of MenuOptions which is required to show inline menu
     */
    private List<MenuOption> cardMenuBuilder(List<BankCard> bankCardList, MenuButtons menuButtons) {
        List<MenuOption> resultList = new ArrayList<>();
        for (BankCard bankCard : bankCardList) {
            MenuOption menuOption = new MenuOption(bankCard.getName(), menuButtons);
            resultList.add(menuOption);
        }
        return resultList;
    }

    private List<MenuOption> categoryMenuBuilder(List<CbCategory> cbCategoryList, MenuButtons menuButtons) {
        List<MenuOption> resultList = new ArrayList<>();
        for (CbCategory cbCategory : cbCategoryList) {
            MenuOption menuOption = new MenuOption(cbCategory.getName(), menuButtons);
            resultList.add(menuOption);
        }
        return resultList;
    }

    private List<MenuOption> cbChanceMenuBuilder(List<CbChance> cbChanceList, MenuButtons menuButtons) {
        List<MenuOption> resultList = new ArrayList<>();
        for (CbChance cbChance : cbChanceList) {
            MenuOption menuOption = new MenuOption(cbChance.shortToString(), menuButtons);
            resultList.add(menuOption);
        }
        return resultList;
    }

    private void showMyCards(long chatId, boolean showWithId) {
        Iterable<BankCard> bankCardList = bankCardRepository.findAll();
        StringBuilder bankCardString = new StringBuilder();
        for (BankCard bankCard : bankCardList) {
            if (bankCard.getUser().getChatId().equals(chatId)) {
                if (!showWithId) bankCardString.append(bankCard).append("\n");
                if (showWithId) bankCardString.append(bankCard.getCardId().toString()).append(bankCard).append("\n");
            }
        }
        prepareAndSendMessage(chatId, bankCardString.toString());
    }

    private void showMyCard(long chatId, String cardName) {
        Iterable<BankCard> bankCardList = bankCardRepository.findAll();
        String bankCardString = "";
        for (BankCard bankCard : bankCardList) {
            if (bankCard.getUser().getChatId().equals(chatId) && bankCard.getName().equals(cardName)) {
                bankCardString += bankCard.toString() + "\n";
            }
        }
        prepareAndSendMessage(chatId, bankCardString);
    }

    private Long getMyCardId(long chatId, String cardName) {
        Iterable<BankCard> bankCardList = bankCardRepository.findAll();
        Long bankCardId = null;
        for (BankCard bankCard : bankCardList) {
            if (bankCard.getUser().getChatId().equals(chatId) && bankCard.getName().equals(cardName)) {
                bankCardId = bankCard.getCardId();
            }
        }
        return bankCardId;
    }

    private Long getMyCategoryId(long chatId, String categoryName) {
        Iterable<CbCategory> categoryList = cbCategoryRepository.findAll();
        Long categoryId = null;
        for (CbCategory cbCategory : categoryList) {
            if (cbCategory.getUser().getChatId().equals(chatId) && cbCategory.getName().equals(categoryName)) {
                categoryId = cbCategory.getCbCategoryId();
            }
        }
        return categoryId;
    }

    private List<BankCard> getMyCardsList(long chatId) {
        Iterable<BankCard> bankCardList = bankCardRepository.findAll();
        List<BankCard> resultList = new ArrayList<>();
        for (BankCard bankCard : bankCardList) {
            if (bankCard.getUser().getChatId().equals(chatId)) {
                resultList.add(bankCard);
            }
        }
        return resultList;
    }

    private List<CbCategory> getMyCategoriesList(long chatId) {
        Iterable<CbCategory> cbCategoriesList = cbCategoryRepository.findAll();
        List<CbCategory> resultList = new ArrayList<>();
        for (CbCategory cbCategory : cbCategoriesList) {
            if (cbCategory.getUser().getChatId().equals(chatId)) {
                resultList.add(cbCategory);
            }
        }
        return resultList;
    }

    private List<CbChance> getMyCbChancesList(long chatId) {
        Iterable<CbChance> cbChancesList = cbChanceRepository.findAll();
        List<CbChance> resultList = new ArrayList<>();
        for (CbChance cbChance : cbChancesList) {
            if (cbChance.getUser().getChatId().equals(chatId)) {
                resultList.add(cbChance);
            }
        }
        return resultList;
    }

    private void showMyCategories(long chatId) {
        Iterable<CbCategory> cbCategories = cbCategoryRepository.findAll();
        StringBuilder s = new StringBuilder();
        for (CbCategory cbCategory : cbCategories) {
            if (cbCategory.getUser().getChatId().equals(chatId)) {
                s.append(cbCategory.toString()).append("\n");
            }
        }
        prepareAndSendMessage(chatId, s.toString());
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
        executeMessage(message);
    }

    private MenuOption callbackParser(String callback) {
        List<String> resultList = List.of(callback.split(DELIMETER));
        return new MenuOption(
                resultList.get(0),
                MenuButtons.valueOf(resultList.get(1)));
    }

    private void showMyChances(long chatId) {
        Iterable<CbChance> cbChances = cbChanceRepository.findAll();

        StringBuilder s = new StringBuilder();
        for (CbChance cbChance : cbChances) {
            if (cbChance.getUser().getChatId().equals(chatId)) {
                s.append(cbChance.toString()).append("\n");
            }
        }
        prepareAndSendMessage(chatId, s.toString());
    }

    private void showMyChancesOfCategory(long chatId, String categoryName, boolean isActive) {
        Iterable<CbChance> cbChances = cbChanceRepository.findAll();
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

    private void showMyChancesOfCard(long chatId, String cardName, boolean isActive, boolean showWithId) {
        Iterable<CbChance> cbChances = cbChanceRepository.findAll();
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

    /**
     * Builds a BankCard.
     *
     * @param chatId     current user
     * @param isNew      if new bank card is created, then false;
     *                   if an existing bank card is edited, then true;
     * @param bankCardId if new bank card is created, then ignored;
     *                   *              if an existing bank card is edited, then id of edited card;
     * @return
     */

    private BankCard bankCardBuilder(long chatId, String newBankName, String newCardName, boolean isNew, long bankCardId) {
        log.info(chatId + "Building bank card..." + newCardName);
        User user = getUserByChatId(chatId);
        BankCard newBankCard = new BankCard();
        newBankCard.setBankName(newBankName);
        newBankCard.setName(newCardName);
        newBankCard.setUser(user);
        if (!isNew) {
            newBankCard.setCardId(bankCardId);
        }
        return newBankCard;
    }

    private CbCategory cbCategoryBuilder(long chatId, String newCategoryName, boolean isNew, long categoryId) {
        User user = getUserByChatId(chatId);
        CbCategory newCbCategory = new CbCategory();
        newCbCategory.setName(newCategoryName);
        newCbCategory.setUser(user);
        if (!isNew) {
            newCbCategory.setCbCategoryId(categoryId);
        }
        return newCbCategory;
    }

    private User getUserByChatId(long chatId) {
        Optional<User> optionalUser = userRepository.findById(chatId);
        User user = optionalUser.get();
        return user;
    }

    private boolean hasDuplicatesInDb(long chatId, CashbackEntity cashbackEntity, CrudRepository crudRepository) {

        List<CashbackEntity> cashbackEntities = (List<CashbackEntity>) crudRepository.findAll();
        for (CashbackEntity entity : cashbackEntities) {
            if (entity.getUser().getChatId().equals(chatId) &&
                    entity.getName().equals(cashbackEntity.getName())) {
                return true;
            }
        }
        return false;
    }

    private void saveBankCardToDb(BankCard bankCard) {
        bankCardRepository.save(bankCard);
        log.info("Bank card saved to db " + bankCard);
    }

    private void saveCbCategoryToDb(CbCategory cbCategory) {
        cbCategoryRepository.save(cbCategory);
        log.info("Category saved to db " + cbCategory);
    }

    private void saveNewChancetoDb(long chatId) {
        User user = getUserByChatId(chatId);

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
}

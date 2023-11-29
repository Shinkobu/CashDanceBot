package CashDance.Bot.service;


import CashDance.Bot.config.BotConfig;
import CashDance.Bot.model.BankCard;
import CashDance.Bot.model.interfaces.AdsRepository;
import CashDance.Bot.model.User;
import CashDance.Bot.model.interfaces.BankCardRepository;
import CashDance.Bot.model.interfaces.UserRepository;
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

import java.util.*;


@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    static final String YES_BUTTON = "YES_BUTTON";
    static final String NO_BUTTON = "NO_BUTTON";
    static final String ERROR_TEXT = "Error occurred: ";
    private static final String HELP_TEXT = "This bot is created to demonstrate Spring capabilities. \n\n " +
            "You can execute commands from the main menu on the left or by typing command\n\n" +
            "Type /start to see a welcome message \n\n" +
            "Type /register to register \n\n" +
            "Type /mydata to see data stored about yourself \n\n" +
            "Type /help to see this message again";
    final BotConfig config;
    //    private Map<Long, ServiceState> chatState = new HashMap<>();
    private ServiceState serviceState;
    private ServiceState chatState;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankCardRepository bankCardRepository;
    @Autowired
    private AdsRepository adsRepository;

    public TelegramBot(BotConfig config) {

        chatState = ServiceState.DEFAULT_STATE;
//        chatState.put(0L,ServiceState.DEFAULT_STATE);
//      configuring bot Menu in constructor
        this.config = config;
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand("/start", "get a welcome message"));
        listofCommands.add(new BotCommand("/mycards", "manage your bank cards"));
        listofCommands.add(new BotCommand("/register", "register yourself"));
        listofCommands.add(new BotCommand("/mydata", "get your data stored"));
        listofCommands.add(new BotCommand("/deletedata", "delete your data"));
        listofCommands.add(new BotCommand("/help", "info how to use this bot"));
        listofCommands.add(new BotCommand("/settings", "set your preferences"));

        listofCommands.add(new BotCommand("/allcards", "see all your cards"));
        listofCommands.add(new BotCommand("/newcard", "create a new card"));
        listofCommands.add(new BotCommand("/changecard", "modify your card"));
        listofCommands.add(new BotCommand("/deletecard", "delete your card"));
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


    private String newBankName;
    private String newCardName;
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
                        break;
                    case "/help":
                        prepareAndSendMessage(chatId, HELP_TEXT);
                        break;
                    case "/register":
                        register(chatId);
                        break;
                    case "/newcard":
                        log.info("Building new bank card...");
                        prepareAndSendMessage(chatId, "Введите название банка");
                        chatState = ServiceState.AWAITING_BANK_NAME;
                        log.info("Waiting for bank name...");
                        break;
                    case "/mycards":
                        showMyCards(chatId);
                        break;
                    default:

                        switch (chatState) {
                            case AWAITING_BANK_NAME:
                                newBankName = update.getMessage().getText();
                                chatState = ServiceState.AWAITING_BANK_CARD_NAME;
                                prepareAndSendMessage(chatId, "Введите название карты");
                                break;
                            case AWAITING_BANK_CARD_NAME:
                                newCardName = update.getMessage().getText();
                                chatState = ServiceState.DEFAULT_STATE;
                                saveNewCardToDb(chatId);
                                break;
                            default:
                                prepareAndSendMessage(chatId, "Sorry, command was not recognized");
                        }
                }
            }
//           if inlinekeyboard button is pressed
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
// Edition of sent message after pressing yes/no button
            EditMessageText message = new EditMessageText();
            if (callbackData.equals(YES_BUTTON)) {
                String text = "You pressed YES button";
                executeEditMessageText(text, chatId, messageId);

            } else if (callbackData.equals(NO_BUTTON)) {
                String text = "You pressed NO button";
                executeEditMessageText(text, chatId, messageId);
            }
            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error("Error occured: " + e.getMessage());
            }
        }
    }

    private void showMyCards(long chatId) {
        Iterable<BankCard> bankCardList = bankCardRepository.findAll();
        String bankCardString = "";
        for (BankCard bankCard : bankCardList) {
            bankCardString += bankCard.toString() + "\n";
        }
        prepareAndSendMessage(chatId,bankCardString);
    }

    private void saveNewCardToDb(long chatId) {
        Optional<User> optionalUser = userRepository.findById(chatId);
        // todo add presence check
        User user = optionalUser.get();
        // todo check if card already registered
        BankCard newBankCard = new BankCard();
        newBankCard.setBankName(newBankName);
        newBankCard.setCardName(newCardName);
        newBankCard.setUser(user);
        bankCardRepository.save(newBankCard);
        log.info("Bank card saved to db " + newBankCard);
        prepareAndSendMessage(chatId, "Карта сохранена: " + newBankCard);
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
        }
        log.info("User with id " + message.getChatId() + " is already registered");
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = EmojiParser.parseToUnicode("Hi, " + name + ", nice to meet you!" + " :blush:");
//      String answer = "Hi, " + name + ", nice to meet you!";
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
        row.add("/newcard");
        row.add("/mycards");
        keyboardRows.add(row);
        row = new KeyboardRow();
        row.add("/start");
        row.add("check my data");
        row.add("delete my data");
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

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }

    private void prepareAndSendMessage(long chatId, String textToSend) {
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

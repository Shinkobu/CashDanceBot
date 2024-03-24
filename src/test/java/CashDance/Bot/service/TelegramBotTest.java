package CashDance.Bot.service;

import CashDance.Bot.config.BotConfig;
import CashDance.Bot.model.CbCategory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class TelegramBotTest {

    @Autowired
    private TelegramBot telegramBot;
    @Autowired
    private BotConfig botConfig;
    @Autowired
    private MenuBuilders menuBuilders;

    @Autowired
    private Repository repository;
//    @Autowired
//    private Constants constants;


    // example simple test
    @Test
    public void initNumberTest() {
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(botConfig.getOwnerId());
        message.setText("/initnumber");
        message.setChat(chat);
        update.setMessage(message);
        telegramBot.onUpdateReceived(update);
        assertEquals("1", telegramBot.getLastSendMessage().getText());
    }

    @Test
    public void showMainMenuTest() {
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(botConfig.getOwnerId());
        message.setText("/mainmenu");
        message.setChat(chat);
        update.setMessage(message);
        telegramBot.onUpdateReceived(update);
        assertEquals(menuBuilders.inlineMenuBuilder(chat.getId(), "Главное меню", Constants.mainMenuList),
                telegramBot.getLastSendMessage());
    }


    @Test
    public void createNewCategory() {
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(botConfig.getOwnerId());
        CallbackQuery callbackQuery = new CallbackQuery();
        message.setChat(chat);
        int i = 0;

        // initial message is required. It will be edited by bot
        message.setText("/mainmenu");
        message.setMessageId(i++);
        callbackQuery.setMessage(message);
        update.setMessage(message);
        update.setCallbackQuery(callbackQuery);

        telegramBot.onUpdateReceived(update);

        // entering categories menu
        message.setText(null);
        message.setMessageId(i++);
        callbackQuery.setData(
                Constants.mainMenu_MyCategories.getOptionName()
                        + Constants.DELIMETER
                        + MenuButtons.MAINMENU_MYCATEGORIES);
//        callbackQuery.setData("Мои банковские карты///MAINMENU_MYCARDS");
        callbackQuery.setMessage(message);
        update.setMessage(message);
        update.setCallbackQuery(callbackQuery);
        telegramBot.onUpdateReceived(update);

        // new category

        message.setText(null);
        message.setMessageId(i++);
        callbackQuery.setData(
                Constants.categoriesMenu_NewCategory.getOptionName()
                        + Constants.DELIMETER
                        + MenuButtons.CATMENU_NEWCAT);
        callbackQuery.setMessage(message);
        update.setMessage(message);
        update.setCallbackQuery(callbackQuery);

        telegramBot.onUpdateReceived(update);

        // entering catname
        String testCatName = "TestCatName";
        message.setText(testCatName);
        message.setMessageId(i++);
        callbackQuery.setData(null);
        callbackQuery.setMessage(null);
        update.setMessage(message);
        update.setCallbackQuery(null);

        telegramBot.onUpdateReceived(update);
        int isFound = 0;
        for (CbCategory cat : repository.findAllUserCats(botConfig.getOwnerId())) {
            if (cat.getName().equals(testCatName)) {
                isFound = 1;
                assertEquals(1,isFound);
                repository.deleteCategory(cat.getCbCategoryId());
            }
        }





//        assertEquals(menuBuilders.inlineMenuBuilder(chat.getId(), "Главное меню", Constants.mainMenuList),
//                telegramBot.getLastMessage());
    }

    @Test
    public void createNewBankCardTest() {
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(botConfig.getOwnerId());
        CallbackQuery callbackQuery = new CallbackQuery();
        message.setChat(chat);
        int i = 0;

        // initial message is required. It will be edited by bot
        message.setText("/mainmenu");
        message.setMessageId(i++);
        callbackQuery.setMessage(message);
        update.setMessage(message);
        update.setCallbackQuery(callbackQuery);

        telegramBot.onUpdateReceived(update);

        // entering cards menu
        message.setText(null);
        message.setMessageId(i++);
        callbackQuery.setData("Мои банковские карты///MAINMENU_MYCARDS");
        callbackQuery.setMessage(message);
        update.setMessage(message);
        update.setCallbackQuery(callbackQuery);

        telegramBot.onUpdateReceived(update);

        // new card choice

        message.setText(null);
        message.setMessageId(i++);
        callbackQuery.setData("Новая карта///CARDSMENU_NEWCARD");
        callbackQuery.setMessage(message);
        update.setMessage(message);
        update.setCallbackQuery(callbackQuery);

        telegramBot.onUpdateReceived(update);

        // entering bankName
        message.setText("TestBankName");
        message.setMessageId(i++);
        callbackQuery.setData(null);
        callbackQuery.setMessage(null);
        update.setMessage(message);
        update.setCallbackQuery(null);

        telegramBot.onUpdateReceived(update);

        // entering cardName
        message.setText("TestCardName");
        message.setMessageId(i++);
        callbackQuery.setData(null);
        callbackQuery.setMessage(null);
        update.setMessage(message);
        update.setCallbackQuery(null);

        telegramBot.onUpdateReceived(update);


//        assertEquals(menuBuilders.inlineMenuBuilder(chat.getId(), "Главное меню", Constants.mainMenuList),
//                telegramBot.getLastMessage());
    }

//
//    @Test
//    public void successSendMessage(){
//        RestAssured.baseURI = "https://api.telegram.org/bot6453381808:AAGOFykQvobHjylfKR4idNxV_uGP7wKavSY";
//        given()
////                .param("text","some text")
//                .param("chat_id", 410071983)
//                .when()
//                .get("/initnumber")
//                .then()
//                .statusCode(200);
//    }

}
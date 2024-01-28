package CashDance.Bot.service.old;

import CashDance.Bot.model.CbCategory;
import CashDance.Bot.model.User;
import com.vdurmont.emoji.EmojiParser;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class OldMethods {

    /**
     * An example of ReplyKeyboardMarkup (buttons below the reply message field)
     * @param chatId
     * @param textToSend
     */
//    private void sendMessage(long chatId, String textToSend) {
//        SendMessage message = new SendMessage();
//        message.setChatId(String.valueOf(chatId));
//        message.setText(textToSend);
//        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
//        List<KeyboardRow> keyboardRows = new ArrayList<>();
//        KeyboardRow row = new KeyboardRow();
//        row.add("/mychancesofcategory");
//        row.add("/mychancesofcategoryactive");
//        row.add("/newcard");
//        row.add("/newcategory");
//        row.add("/newchance");
//        keyboardRows.add(row);
//        row = new KeyboardRow();
//        row.add("/start");
//        row.add("/start");
//        row.add("/mycards");
//        row.add("/mycategories");
//        row.add("/mychances");
//        keyboardRows.add(row);
//        keyboardMarkup.setKeyboard(keyboardRows);
//        message.setReplyMarkup(keyboardMarkup);
//        executeMessage(message);
//    }

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

//          message to all users. Only bot owner can do it
//    public void onUpdateReceived(Update update) {

//            if (messageText.contains("/send") && config.getOwnerId() == chatId) {
//                var textToSend = EmojiParser.parseToUnicode(messageText.substring(messageText.indexOf(" ")));
//                var users = userRepository.findAll();
//                for (User user : users) {
//                    prepareAndSendMessage(user.getChatId(), textToSend);
//                }
//            } else {


//    private void showMyCategoriesButtons(long chatId) {
//        Iterable<CbCategory> cbCategories = cbCategoryRepository.findAll();
//
//        SendMessage message = new SendMessage();
//        message.setChatId(String.valueOf(chatId));
//        message.setText("Выберите категорию:");
//
//        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
//        List<List<InlineKeyboardButton>> inlineRowsList = new ArrayList<>();
//
//        for (CbCategory cbCategory : cbCategories) {
//
//            List<InlineKeyboardButton> inlineRowButtonList = new ArrayList<>();
//            var tempButton = new InlineKeyboardButton();
//
//            tempButton.setText(cbCategory.getName());
//            tempButton.setCallbackData(cbCategory.getName());
//            inlineRowButtonList.add(tempButton);
//            inlineRowsList.add(inlineRowButtonList);
//        }
//
//        inlineKeyboardMarkup.setKeyboard(inlineRowsList);
//
//        message.setReplyMarkup(inlineKeyboardMarkup);
//        executeMessage(message);
//    }
}

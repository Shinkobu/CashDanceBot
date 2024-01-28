package CashDance.Bot.service;

import CashDance.Bot.model.BankCard;
import CashDance.Bot.model.CbCategory;
import CashDance.Bot.model.User;
import CashDance.Bot.model.interfaces.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EntityBuilders {
    CbCategory cbCategoryBuilder(User user, String newCategoryName, boolean isNew, long categoryId) {
        CbCategory newCbCategory = new CbCategory();
        newCbCategory.setName(newCategoryName);
        newCbCategory.setUser(user);
        if (!isNew) {
            newCbCategory.setCbCategoryId(categoryId);
        }
        return newCbCategory;
    }

    /**
     * Builds a BankCard.
     *
     * @param user      current user
     * @param newBankName
     * @param newCardName
     * @param isNew       if new bank card is created, then false;
     *                    if an existing bank card is edited, then true;
     * @param bankCardId  if new bank card is created, then ignored;
     *                    *              if an existing bank card is edited, then id of edited card;
     * @param telegramBot
     * @return
     */

    BankCard bankCardBuilder(User user, String newBankName, String newCardName, boolean isNew, long bankCardId, TelegramBot telegramBot) {
        log.info(user.getChatId() + "Building bank card..." + newCardName);
        BankCard newBankCard = new BankCard();
        newBankCard.setBankName(newBankName);
        newBankCard.setName(newCardName);
        newBankCard.setUser(user);
        if (!isNew) {
            newBankCard.setCardId(bankCardId);
        }
        return newBankCard;
    }
}

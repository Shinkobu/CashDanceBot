package CashDance.Bot.service;

import CashDance.Bot.model.*;
import CashDance.Bot.model.interfaces.BankCardRepository;
import CashDance.Bot.model.interfaces.CbCategoryRepository;
import CashDance.Bot.model.interfaces.CbChanceRepository;
import CashDance.Bot.model.interfaces.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class Repository {
    @Autowired
    private BankCardRepository bankCardRepository;
    @Autowired
    private CbCategoryRepository cbCategoryRepository;
    @Autowired
    private CbChanceRepository cbChanceRepository;
    @Autowired
    private UserRepository userRepository;


    public BankCard findBankCardById(Long id) {
        Optional<BankCard> bankCard = bankCardRepository.findById(id);
        return bankCard.get();
    }

    public CbCategory findCatById(Long id) {
        Optional<CbCategory> cbCategory = cbCategoryRepository.findById(id);
        return cbCategory.get();
    }

    public void saveBankCardToDb(BankCard bankCard) {
        bankCardRepository.save(bankCard);
        log.info("Bank card saved to db " + bankCard);
    }

    public void saveCbCategoryToDb(CbCategory cbCategory) {
        cbCategoryRepository.save(cbCategory);
        log.info("Category saved to db " + cbCategory);
    }

    public void saveCbChanceToDb(CbChance cbChance) {
        cbChanceRepository.save(cbChance);
        log.info("Chance saved to db " + cbChance);
    }

    void deleteBankCard(Long bankCardId) {
        bankCardRepository.deleteById(bankCardId);
        log.info("BankCard with ID " + bankCardId + " deleted from db");
    }

    void deleteCategory(Long categoryId) {
        cbCategoryRepository.deleteById(categoryId);
        log.info("Category with ID " + categoryId + " deleted from db");
    }

    void deleteCbChance(Long cbChanceId, TelegramBot telegramBot) {
        cbChanceRepository.deleteById(cbChanceId);
        log.info("Chance with ID " + cbChanceId + " deleted from db");
    }

    Long getMyCategoryId(long chatId, String categoryName) {
        Iterable<CbCategory> categoryList = cbCategoryRepository.findAll();
        Long categoryId = null;
        for (CbCategory cbCategory : categoryList) {
            if (cbCategory.getUser().getChatId().equals(chatId) && cbCategory.getName().equals(categoryName)) {
                categoryId = cbCategory.getCbCategoryId();
            }
        }
        return categoryId;
    }

    List<CbCategory> getMyCategoriesList(long chatId) {
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

    boolean hasCardDuplicatesInDb(long chatId, CashbackEntity cashbackEntity) {

        List<BankCard> cashbackEntities = (List<BankCard>) bankCardRepository.findAll();
        for (CashbackEntity entity : cashbackEntities) {
            if (entity.getUser().getChatId().equals(chatId) &&
                    entity.getName().equals(cashbackEntity.getName())) {
                return true;
            }
        }
        return false;
    }

    boolean hasCatDuplicatesInDb(long chatId, CashbackEntity cashbackEntity) {

        List<CbCategory> cashbackEntities = (List<CbCategory>) cbCategoryRepository.findAll();
        for (CashbackEntity entity : cashbackEntities) {
            if (entity.getUser().getChatId().equals(chatId) &&
                    entity.getName().equals(cashbackEntity.getName())) {
                return true;
            }
        }
        return false;
    }

    User getUserByChatId(long chatId) {
        Optional<User> optionalUser = userRepository.findById(chatId);
        User user = optionalUser.get();
        return user;
    }

    Long getMyCardId(long chatId, String cardName) {
        Iterable<BankCard> bankCardList = bankCardRepository.findAll();
        Long bankCardId = null;
        for (BankCard bankCard : bankCardList) {
            if (bankCard.getUser().getChatId().equals(chatId) && bankCard.getName().equals(cardName)) {
                bankCardId = bankCard.getCardId();
            }
        }
        return bankCardId;
    }

    List<BankCard> getMyCardsList(long chatId) {
        Iterable<BankCard> bankCardList = bankCardRepository.findAll();
        List<BankCard> resultList = new ArrayList<>();
        for (BankCard bankCard : bankCardList) {
            if (bankCard.getUser().getChatId().equals(chatId)) {
                resultList.add(bankCard);
            }
        }
        return resultList;
    }

    Iterable<BankCard> getAllBankCards() {
        Iterable<BankCard> bankCardList = bankCardRepository.findAll();
        return bankCardList;
    }

    Iterable<CbChance> getAllCbChances() {
        Iterable<CbChance> cbChances = cbChanceRepository.findAll();
        return cbChances;
    }

    Iterable<CbCategory> getAllCbCategories() {
        Iterable<CbCategory> cbCategories = cbCategoryRepository.findAll();
        return cbCategories;
    }

    User saveUserToDb(User user) {
        return userRepository.save(user);
    }

    Optional<User> getUserById(Message message) {
        return userRepository.findById(message.getChatId());
    }
}

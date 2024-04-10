package CashDance.Bot.service;

import CashDance.Bot.model.*;
import CashDance.Bot.model.interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

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
    @Autowired
    private CbMonitorRepository cbMonitorRepository;
    @Autowired
    private FeedbackRepository feedbackRepository;


    public BankCard findBankCardById(Long id) {
        Optional<BankCard> bankCard = bankCardRepository.findById(id);
        if (bankCard.isPresent()) {
            return bankCard.get();
        } else throw new NoSuchElementException("BankCard is not found");
    }

    public CbCategory findCatById(Long id) {
        Optional<CbCategory> cbCategory = cbCategoryRepository.findById(id);
        if (cbCategory.isPresent()) {
            return cbCategory.get();
        } else throw new NoSuchElementException("Category is not found");
    }

    public List<CbCategory> findAllUserCats(Long userChatId) {
        List<CbCategory> catList = (List<CbCategory>) cbCategoryRepository.findAll();
        return catList.stream()
                .filter(s -> s.getUser().getChatId().equals(userChatId))
                .collect(Collectors.toList());
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

    public void saveCbMonitorToDb(CbMonitor cbMonitor) {
        cbMonitorRepository.save(cbMonitor);
        log.info("CbMonitor saved to db " + cbMonitor);
    }

    public void saveFeedbackToDb(Feedback feedback) {
        feedbackRepository.save(feedback);
        log.info("Feedback saved to db " + feedback);
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

    public List<Feedback> getFeedbackList(LocalDateTime start, LocalDateTime end) {
        Iterable<Feedback> feedbacks = feedbackRepository.findAll();
        List<Feedback> resultList = new ArrayList<>();
        for (Feedback feedback : feedbacks) {
//            LocalDateTime.parse(feedback.getFeedbackDateTime(),DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            if (feedback.getFeedbackDateTime().isAfter(start.minusDays(1)) &&
                    feedback.getFeedbackDateTime().isBefore(end.plusDays(1))) {
                resultList.add(feedback);
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
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        } else throw new NoSuchElementException("User is not found");
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
        return bankCardRepository.findAll();
    }

    Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }

    Iterable<CbChance> getAllCbChances() {
        return cbChanceRepository.findAll();
    }

    Iterable<CbCategory> getAllCbCategories() {
        return cbCategoryRepository.findAll();
    }

    void saveUserToDb(User user) {
        userRepository.save(user);
    }

    Optional<User> getUserById(Message message) {
        return userRepository.findById(message.getChatId());
    }


}

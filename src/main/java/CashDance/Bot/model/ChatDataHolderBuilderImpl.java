package CashDance.Bot.model;

import CashDance.Bot.service.ServiceState;

import java.time.LocalDate;

public class ChatDataHolderBuilderImpl implements ChatDataHolderBuilder {

    ChatDataHolder chatDataHolder = new ChatDataHolder();

    @Override
    public ChatDataHolderBuilderImpl setChatState(ServiceState chatState) {
        chatDataHolder.chatState = chatState;
        return this;
    }

    @Override
    public ChatDataHolderBuilder setNewBankName(String newBankName) {
        chatDataHolder.newBankName = newBankName;
        return this;
    }

    @Override
    public ChatDataHolderBuilder setNewCardName(String newCardName) {
        chatDataHolder.newCardName = newCardName;
        return this;
    }

    @Override
    public ChatDataHolderBuilder setNewCategoryName(String newCategoryName) {
        chatDataHolder.newCategoryName = newCategoryName;
        return this;
    }

    @Override
    public ChatDataHolderBuilder setBankCardForNewChance(BankCard bankCardForNewChance) {
        chatDataHolder.bankCardForNewChance = bankCardForNewChance;
        return this;
    }

    @Override
    public ChatDataHolderBuilder setCategoryForNewChance(CbCategory categoryForNewChance) {
        chatDataHolder.categoryForNewChance = categoryForNewChance;
        return this;
    }

    @Override
    public ChatDataHolderBuilder setRateForNewChance(Double rateForNewChance) {
        chatDataHolder.rateForNewChance = rateForNewChance;
        return this;
    }

    @Override
    public ChatDataHolderBuilder setStartDateOfNewChance(LocalDate startDateOfNewChance) {
        chatDataHolder.startDateOfNewChance = startDateOfNewChance;
        return this;
    }

    @Override
    public ChatDataHolderBuilder setEndDateOfNewChance(LocalDate endDateOfNewChance) {
        chatDataHolder.endDateOfNewChance = endDateOfNewChance;
        return this;
    }

    @Override
    public ChatDataHolderBuilder setBankCardId(Long bankCardId) {
        chatDataHolder.bankCardId = bankCardId;
        return this;
    }

    @Override
    public ChatDataHolderBuilder setCbChanceId(Long cbChanceId) {
        chatDataHolder.cbChanceId = cbChanceId;
        return this;
    }

    @Override
    public ChatDataHolderBuilder setCategoryId(Long categoryId) {
        chatDataHolder.categoryId = categoryId;
        return this;
    }

    @Override
    public ChatDataHolder build() {
        return chatDataHolder;
    }
}

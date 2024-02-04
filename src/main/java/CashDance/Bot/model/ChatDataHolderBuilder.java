package CashDance.Bot.model;

import CashDance.Bot.service.ServiceState;

import java.time.LocalDate;

public interface ChatDataHolderBuilder {

    ChatDataHolderBuilderImpl setChatState(ServiceState chatState);

    ChatDataHolderBuilder setNewBankName(String newBankName);

    ChatDataHolderBuilder setNewCardName(String newCardName);

    ChatDataHolderBuilder setNewCategoryName(String newCategoryName);

    ChatDataHolderBuilder setBankCardForNewChance(BankCard bankCardForNewChance);

    ChatDataHolderBuilder setCategoryForNewChance(CbCategory categoryForNewChance);

    ChatDataHolderBuilder setRateForNewChance(Double rateForNewChance);

    ChatDataHolderBuilder setStartDateOfNewChance(LocalDate startDateOfNewChance);

    ChatDataHolderBuilder setEndDateOfNewChance(LocalDate endDateOfNewChance);

    ChatDataHolderBuilder setBankCardId(Long bankCardId);

    ChatDataHolderBuilder setCbChanceId(Long cbChanceId);

    ChatDataHolderBuilder setCategoryId(Long categoryId);

    ChatDataHolder build ();
}

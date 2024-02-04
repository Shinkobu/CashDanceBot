package CashDance.Bot.model;

import CashDance.Bot.service.*;

import java.time.LocalDate;


public class ChatDataHolder {

    protected ServiceState chatState;

    public ChoiceFor getChoice() {
        return choice;
    }

    public void setChoice(ChoiceFor choice) {
        this.choice = choice;
    }

    protected ChoiceFor choice;
    protected String newBankName;
    protected String newCardName;

    protected String newCategoryName;

    @Override
    public String toString() {
        return "ChatDataHolder{" +
                "chatState=" + chatState +
                ", newBankName='" + newBankName + '\'' +
                ", newCardName='" + newCardName + '\'' +
                ", newCategoryName='" + newCategoryName + '\'' +
                ", bankCardForNewChance=" + bankCardForNewChance +
                ", categoryForNewChance=" + categoryForNewChance +
                ", rateForNewChance=" + rateForNewChance +
                ", startDateOfNewChance=" + startDateOfNewChance +
                ", endDateOfNewChance=" + endDateOfNewChance +
                ", bankCardId=" + bankCardId +
                ", cbChanceId=" + cbChanceId +
                ", categoryId=" + categoryId +
                '}';
    }

    protected BankCard bankCardForNewChance;
    protected CbCategory categoryForNewChance;
    protected Double rateForNewChance;
    protected LocalDate startDateOfNewChance;
    protected LocalDate endDateOfNewChance;

    protected Long bankCardId;
    protected Long cbChanceId;
    protected Long categoryId;

    public ServiceState getChatState() {
        return chatState;
    }

    public void setChatState(ServiceState chatState) {
        this.chatState = chatState;
    }

    public String getNewBankName() {
        return newBankName;
    }

    public void setNewBankName(String newBankName) {
        this.newBankName = newBankName;
    }

    public String getNewCardName() {
        return newCardName;
    }

    public void setNewCardName(String newCardName) {
        this.newCardName = newCardName;
    }

    public String getNewCategoryName() {
        return newCategoryName;
    }

    public void setNewCategoryName(String newCategoryName) {
        this.newCategoryName = newCategoryName;
    }

    public BankCard getBankCardForNewChance() {
        return bankCardForNewChance;
    }

    public void setBankCardForNewChance(BankCard bankCardForNewChance) {
        this.bankCardForNewChance = bankCardForNewChance;
    }

    public CbCategory getCategoryForNewChance() {
        return categoryForNewChance;
    }

    public void setCategoryForNewChance(CbCategory categoryForNewChance) {
        this.categoryForNewChance = categoryForNewChance;
    }

    public Double getRateForNewChance() {
        return rateForNewChance;
    }

    public void setRateForNewChance(Double rateForNewChance) {
        this.rateForNewChance = rateForNewChance;
    }

    public LocalDate getStartDateOfNewChance() {
        return startDateOfNewChance;
    }

    public void setStartDateOfNewChance(LocalDate startDateOfNewChance) {
        this.startDateOfNewChance = startDateOfNewChance;
    }

    public LocalDate getEndDateOfNewChance() {
        return endDateOfNewChance;
    }

    public void setEndDateOfNewChance(LocalDate endDateOfNewChance) {
        this.endDateOfNewChance = endDateOfNewChance;
    }

    public Long getBankCardId() {
        return bankCardId;
    }

    public void setBankCardId(Long bankCardId) {
        this.bankCardId = bankCardId;
    }

    public Long getCbChanceId() {
        return cbChanceId;
    }

    public void setCbChanceId(Long cbChanceId) {
        this.cbChanceId = cbChanceId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}

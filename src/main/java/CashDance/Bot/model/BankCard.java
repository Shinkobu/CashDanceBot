package CashDance.Bot.model;

import javax.persistence.*;


@Entity
@Table(name = "bank_cards_table")
public class BankCard {

    private String cardName;
    private String bankName;
    //    private Date expireDate;

    @Override
    public String toString() {
        return cardName +" "+ bankName +" "+ cardId;
    }

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int cardId;

    public BankCard(String cardName, String bankName, User user, int cardId) {
        this.cardName = cardName;
        this.bankName = bankName;
        this.user = user;
        this.cardId = cardId;
    }

    public BankCard(String name) {
    }

    public BankCard() {

    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public int getCardId() {
        return cardId;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

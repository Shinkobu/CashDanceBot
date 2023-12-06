package CashDance.Bot.model;

import org.hibernate.annotations.OnDelete;

import javax.persistence.*;
import java.util.List;


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

    @OneToMany (mappedBy = "bankCard", cascade = CascadeType.ALL)
    @OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    private List<CbChance> cbChanceList;

    public List<CbChance> getCbChanceList() {
        return cbChanceList;
    }

    public void setCbChanceList(List<CbChance> cbChanceList) {
        this.cbChanceList = cbChanceList;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long cardId;

    public BankCard(String cardName, String bankName, User user, Long cardId) {
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

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

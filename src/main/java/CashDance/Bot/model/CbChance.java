package CashDance.Bot.model;

import javax.persistence.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

@Entity
@Table(name = "cashback_chances_table")
public class CbChance {

    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long cbChanceId;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private CbCategory cbCategory;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "bankCard_id")
    private BankCard bankCard;

    public CbChance() {
    }

    public CbChance(SimpleDateFormat dateFormat, Long cbChanceId, CbCategory cbCategory, User user, BankCard bankCard, LocalDate startDate, LocalDate endDate, String userComment, CbCategory category, Double rate) {
        this.dateFormat = dateFormat;
        this.cbChanceId = cbChanceId;
        this.cbCategory = cbCategory;
        this.user = user;
        this.bankCard = bankCard;
        this.startDate = startDate;
        this.endDate = endDate;
        this.userComment = userComment;
        this.rate = rate;
    }

    private LocalDate startDate;
    private LocalDate endDate;
    private String userComment;

    private Double rate;


    @Override
    public String toString() {
        return " Категория " + cbCategory.getName() +
                ", ставка " + rate * 100 + " %" +
                ", действует с " + startDate +
                ", до " + endDate +
                " по карте " + bankCard.getCardName();
    }

    public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    @Transient
    public void setDateFormat(SimpleDateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    public Long getCbChanceId() {
        return cbChanceId;
    }

    public void setCbChanceId(Long cbChanceId) {
        this.cbChanceId = cbChanceId;
    }

    public CbCategory getCbCategory() {
        return cbCategory;
    }

    public void setCbCategory(CbCategory cbCategory) {
        this.cbCategory = cbCategory;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BankCard getBankCard() {
        return bankCard;
    }

    public void setBankCard(BankCard bankCard) {
        this.bankCard = bankCard;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getUserComment() {
        return userComment;
    }

    public void setUserComment(String userComment) {
        this.userComment = userComment;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }
}

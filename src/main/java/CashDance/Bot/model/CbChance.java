package CashDance.Bot.model;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

@Entity
@Table(name = "cashback_chances_table")
public class CbChance extends CashbackEntity{

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
    private LocalDate startDate;
    private LocalDate endDate;
    private String userComment;
    private Double rate;

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

    public CbChance(CbChanceBuilder cbChanceBuilder) {
        this.cbChanceId = cbChanceBuilder.cbChanceId;
        this.cbCategory = cbChanceBuilder.cbCategory;
        this.user = cbChanceBuilder.user;
        this.bankCard = cbChanceBuilder.bankCard;
        this.startDate = cbChanceBuilder.startDate;
        this.endDate = cbChanceBuilder.endDate;
        this.rate = cbChanceBuilder.rate;
    }

    @Override
    public String toString() {
        return " Категория " + cbCategory.getName() +
                ", ставка " + rate * 100 + " %" +
                ", действует с " + startDate +
                ", до " + endDate +
                " по карте " + bankCard.getName();
    }

    public String shortToString() {
        return cbCategory.getName() +
                " - " + rate * 100 + " %" +
                " по " + bankCard.getName() +
                " с " + startDate +
                " до " + endDate;
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

    public static class CbChanceBuilder{

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        private Long cbChanceId;
        private CbCategory cbCategory;
        private User user;
        private BankCard bankCard;
        private LocalDate startDate;
        private LocalDate endDate;
        private String userComment;
        private Double rate;

        public CbChanceBuilder(Long cbChanceId, CbCategory cbCategory, User user, BankCard bankCard, LocalDate startDate, LocalDate endDate, Double rate) {
            this.cbChanceId = cbChanceId;
            this.cbCategory = cbCategory;
            this.user = user;
            this.bankCard = bankCard;
            this.startDate = startDate;
            this.endDate = endDate;
            this.rate = rate;
        }

        public void setUserComment(String userComment) {
            this.userComment = userComment;
        }

        public CbChance build(){
            return new CbChance(this);
        }
    }
}

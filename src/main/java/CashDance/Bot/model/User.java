package CashDance.Bot.model;

import org.hibernate.annotations.OnDelete;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

// class will be connected to table (name of table)
@Entity //(name = "users_table")
@Table(name = "users_table")
public class User {

    @Id
    @Column(name = "chat_id")
    private Long chatId;
    private String firstName;
    private String lastName;
    private String userName;
    private Timestamp registeredAt;

    /**
     * @OneToMany ("user" is the name of corresponding field in BankCard)
     * cascade = CascadeType.ALL - if user is deleted, all bankcards will be deleted
     * orphanRemoval=true means child entity should be removed automatically by the ORM if it's no longer referenced by a parent entity
     */
    @OneToMany (mappedBy = "user", cascade = CascadeType.ALL)
    @OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    private List<BankCard> bankCardsList;

    public User() {
    }

    public List<BankCard> getBankCardsList() {
        return bankCardsList;
    }

    public void setBankCardsList(List<BankCard> bankCardsList) {
        this.bankCardsList = bankCardsList;
    }

    @Override
    public String toString() {
        return "User{" +
                "chatId=" + chatId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", userName='" + userName + '\'' +
                ", registeredAt=" + registeredAt +
                '}';
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Timestamp getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Timestamp registeredAt) {
        this.registeredAt = registeredAt;
    }
}

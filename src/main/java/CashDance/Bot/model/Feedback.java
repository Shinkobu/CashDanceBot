package CashDance.Bot.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cashback_feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long feedbackId;

    @Column(columnDefinition = "VARCHAR (500)")
    private String feedbackMessage;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    private LocalDateTime feedbackDateTime;

    public Feedback() {
    }

    public Feedback(String feedbackMessage, User user, LocalDateTime feedbackDateTime) {
        this.feedbackMessage = feedbackMessage;
        this.user = user;
        this.feedbackDateTime = feedbackDateTime;
    }

    public long getFeedbackId() {
        return feedbackId;
    }

    @Override
    public String toString() {
        return  "FeedbackId = " + feedbackId + "\n" +
                ", chatId = " + user.getChatId() + "\n" +
                ", feedbackDateTime = " + feedbackDateTime + "\n" +
                ", feedbackMessage = " + feedbackMessage + "\n\n" ;
    }

    public void setFeedbackId(long feedbackId) {
        this.feedbackId = feedbackId;
    }

    public String getFeedbackMessage() {
        return feedbackMessage;
    }

    public void setFeedbackMessage(String feedbackMessage) {
        this.feedbackMessage = feedbackMessage;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getFeedbackDateTime() {
        return feedbackDateTime;
    }

    public void setFeedbackDateTime(LocalDateTime feedbackDateTime) {
        this.feedbackDateTime = feedbackDateTime;
    }
}

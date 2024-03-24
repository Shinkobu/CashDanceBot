package CashDance.Bot.model;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "monitor_table")
public class CbMonitor {
    private LocalDate localDate;
    private Integer numberOfInteractions;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long entryId;

    public CbMonitor() {
    }

    public CbMonitor(LocalDate localDate, Integer numberOfInteractions) {
        this.localDate = localDate;
        this.numberOfInteractions = numberOfInteractions;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    public Integer getNumberOfInteractions() {
        return numberOfInteractions;
    }

    public void setNumberOfInteractions(Integer numberOfInteractions) {
        this.numberOfInteractions = numberOfInteractions;
    }

    public long getEntryId() {
        return entryId;
    }

    public void setEntryId(long entryId) {
        this.entryId = entryId;
    }
}

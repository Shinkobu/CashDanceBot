package CashDance.Bot.model;

import org.hibernate.annotations.OnDelete;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cashback_categories_table")
public class CbCategory extends CashbackEntity implements Comparable<CbCategory>{
    private String name;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long cbCategoryId;

    public CbCategory(String name, User user, int cbCategoryId, List<CbChance> cbChanceList) {
        this.name = name;
        this.user = user;
        this.cbCategoryId = cbCategoryId;
        this.cbChanceList = cbChanceList;
    }

    @Override
    public String toString() {
        return name;
    }

    public void setCbCategoryId(long cbCategoryId) {
        this.cbCategoryId = cbCategoryId;
    }

    public long getCbCategoryId() {
        return cbCategoryId;
    }

    public void setCbCategoryId(int cbCategoryId) {
        this.cbCategoryId = cbCategoryId;
    }

    public List<CbChance> getCbChanceList() {
        return cbChanceList;
    }

    public void setCbChanceList(List<CbChance> cbChanceList) {
        this.cbChanceList = cbChanceList;
    }

    @OneToMany (mappedBy = "cbCategory", cascade = CascadeType.ALL)
    @OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    private List<CbChance> cbChanceList;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    public CbCategory() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(CbCategory o) {
        return this.getName().compareTo(o.getName());
    }
}

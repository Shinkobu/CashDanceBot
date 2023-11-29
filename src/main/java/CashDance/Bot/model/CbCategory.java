package CashDance.Bot.model;

public class CbCategory {
    private String name;

    public CbCategory(String name) {
        this.name = name;
    }

    public CbCategory() {

    }

    @Override
    public String toString() {
        return "CashDance.domain.CbCategory{" +
                "name='" + name + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

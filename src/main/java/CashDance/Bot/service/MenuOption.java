package CashDance.Bot.service;

public class MenuOption {
    String optionName;
    MenuButtons menuButton;

    public MenuOption(String optionName, MenuButtons menuButton) {
        this.optionName = optionName;
        this.menuButton = menuButton;
    }

    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    public MenuButtons getMenuButton() {
        return menuButton;
    }

    public void setMenuButton(MenuButtons menuButton) {
        this.menuButton = menuButton;
    }
}

package CashDance.Bot.service;

public enum ServiceState {
    DEFAULT_STATE,
    NEW_BANK_CARD__AWAITING_BANK_NAME,
    NEW_BANK_CARD__AWAITING_BANK_CARD_NAME,
    NEW_CHANCE__AWAITING_BANK_CARD_ID,
    NEW_CHANCE__AWAITING_CATEGORY_ID,
    ALL_CHANCES__AWAITING_CATEGORY_ID,
    NEW_CHANCE__AWAITING_START_DATE,
    NEW_CHANCE__AWAITING_END_DATE,
    NEW_CATEGORY__AWAITING_CATEGORY_NAME,
    ALL_CHANCES_ACTIVE__AWAITING_CATEGORY_ID,
    EDIT_BANK_CARD__AWAITING_BANK_NAME, EDIT_BANK_CARD__AWAITING_BANK_CARD_NAME, NEW_CHANCE__AWAITING_RATE



}

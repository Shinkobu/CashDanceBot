package CashDance.Bot.service.calendar.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Getter
public enum DaysAbbreviations {

    MONDAY("MON", "ПН"),
    TUESDAY("TUE", "ВТ"),
    WEDNESDAY("WED", "СР"),
    THURSDAY("THU", "ЧТ"),
    FRIDAY("FRI", "ПТ"),
    SATURDAY("SAT", "СБ"),
    SUNDAY("SUN", "ВСКР");

    private final String englishValue;
    private final String russianValue;
}

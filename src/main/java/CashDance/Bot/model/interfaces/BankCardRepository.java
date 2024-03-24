package CashDance.Bot.model.interfaces;

import CashDance.Bot.model.BankCard;
import org.springframework.data.repository.CrudRepository;

public interface BankCardRepository extends CrudRepository<BankCard,Long> {
}

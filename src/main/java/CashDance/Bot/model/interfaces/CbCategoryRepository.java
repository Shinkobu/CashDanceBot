package CashDance.Bot.model.interfaces;

import CashDance.Bot.model.BankCard;
import CashDance.Bot.model.CbCategory;
import org.springframework.data.repository.CrudRepository;

public interface CbCategoryRepository extends CrudRepository<CbCategory,Long> {
}

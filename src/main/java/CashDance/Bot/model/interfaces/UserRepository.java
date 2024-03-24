package CashDance.Bot.model.interfaces;

import CashDance.Bot.model.User;
import org.springframework.data.repository.CrudRepository;

// User - describes a table, Long - data type of a primary key
public interface UserRepository extends CrudRepository<User, Long> {
}

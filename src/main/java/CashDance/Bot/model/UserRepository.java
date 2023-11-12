package CashDance.Bot.model;

import org.springframework.data.repository.CrudRepository;

// User - describes a table, Long - data type of a primary key
public interface UserRepository extends CrudRepository<User, Long> {
}

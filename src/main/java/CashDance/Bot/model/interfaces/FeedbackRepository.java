package CashDance.Bot.model.interfaces;

import CashDance.Bot.model.Feedback;
import org.springframework.data.repository.CrudRepository;

public interface FeedbackRepository extends CrudRepository<Feedback,Long> {
}

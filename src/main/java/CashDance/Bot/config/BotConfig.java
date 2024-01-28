package CashDance.Bot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling // enables methods of message scheduled mailing
@PropertySource("application.properties")
@PropertySource("token.properties")
public class BotConfig {

    public BotConfig() {
    }

    @Value("${bot.name}")
    String botName;

    @Value("${bot.token}")
    String token;

    @Value("${bot.owner}")
    Long ownerId;

    @Value("${bot.version}")
    String version;

    public String getBotName() {
        return botName;
    }
    public String getToken() {
        return token;
    }
    public Long getOwnerId() {
        return ownerId;
    }
    public String getVersion() {return version;}

}

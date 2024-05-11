package CashDance.Bot.config;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableScheduling // enables methods of message scheduled mailing
@PropertySource("application.properties")
@PropertySource("token.properties")
public class BotConfig {


    @Value("${show.menu.delay.time}")
    Integer menuDelayTime;
    @Value("${bot.name}")
    String botName;
    @Value("${bot.token}")
    String token;
    @Value("${bot.owner}")
    Long ownerId;

    public Integer getMenuDelayTime() {
        return menuDelayTime;
    }

    public BotConfig() throws IOException, URISyntaxException {
    }

    public String getBotName() {
        return botName;
    }

    public String getToken() {
        return token;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    @SneakyThrows
    public String getHelpText() {
        try (InputStream inputStream = getClass().getResourceAsStream("/Help.md");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String contents = reader.lines()
                    .collect(Collectors.joining(System.lineSeparator()));
            return contents;
        }
    }

    @SneakyThrows
    public String getVersionText() {
        try (InputStream inputStream = getClass().getResourceAsStream("/Version.md");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String contents = reader.lines()
                    .collect(Collectors.joining(System.lineSeparator()));
            return contents;
        }
    }

}

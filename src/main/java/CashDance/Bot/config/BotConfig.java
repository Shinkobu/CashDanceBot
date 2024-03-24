package CashDance.Bot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
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

    public BotConfig() throws IOException, URISyntaxException {
    }

    @Value("${bot.name}")
    String botName;

    @Value("${bot.token}")
    String token;

    @Value("${bot.owner}")
    Long ownerId;


    URL url = BotConfig.class.getClassLoader()
            .getResource("Version.md");
    List<String> stringList = Files.readAllLines(Paths.get(url.toURI()));
    String versionText = stringList.stream()
            .map(s -> s.concat("\n"))
            .collect(Collectors.joining());

    URL url1 = BotConfig.class.getClassLoader()
            .getResource("Help.md");
    List<String> stringList1 = Files.readAllLines(Paths.get(url1.toURI()));
    String helpText = stringList1.stream()
            .map(s -> s.concat("\n"))
            .collect(Collectors.joining());

    public String getBotName() {
        return botName;
    }
    public String getToken() {
        return token;
    }
    public Long getOwnerId() {
        return ownerId;
    }

    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    public String getVersionText() {return versionText;}

}

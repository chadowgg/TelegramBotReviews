package chdtu.com.handler;

import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TrelloCardHandler {
    @Value("${trello.api.key}")
    private String key;

    @Value("${trello.token}")
    private String token;

    @Value("${trello.id.list}")
    private String list;

    public void createCard(String feedback, String recommendation) {
        JSONObject response = Unirest.post("https://api.trello.com/1/cards")
                .queryString("key", key)
                .queryString("token", token)
                .queryString("idList", list)
                .queryString("name", feedback)
                .queryString("desc", recommendation)
                .asJson()
                .getBody()
                .getObject();

        log.info("Response from Trello API: " + response.toString());
    }
}

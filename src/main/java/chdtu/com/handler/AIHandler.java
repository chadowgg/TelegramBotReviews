package chdtu.com.handler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class AIHandler {
    @Value("${openrouter.api.key}")
    private String API_KEY;

    @Value("${openrouter.api.url}")
    private String API_URL;

    @Value("${openrouter.api.model.url}")
    private String MODEL_URL;

    private final RestTemplate restTemplate = new RestTemplate();

    private final String AI_REQUEST = "Ти асистент для аналізу настрою повідомлень. " +
            "Класифікуй повідомлення користувача як Позитивне, Нейтральне або Негативне, " +
            "та присвой рівень критичності від 1 (низька) до 5 (висока). " +
            "Поверни результат у форматі: Настрій відгуку: <Позитивний/Нейтральний/Негативний>\n" +
            "Критичність: <1-5>\n" +
            "Рекомендація: <як можна вирішити дане питання>";

    public String analysisFeedback(String feedback) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + API_KEY);
        headers.set("HTTP-Referer", "https://telegram-feedback-bot");
        headers.set("Title", "Feedback Bot");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "model", MODEL_URL,
                "messages", List.of(
                        Map.of("role", "system", "content", AI_REQUEST),
                        Map.of("role", "user", "content", feedback)
                ),
                "max_tokens", 1000
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, request, Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        if (choices != null && !choices.isEmpty()) {
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        }

        return "Не вдалося отримати відповідь.";
    }
}

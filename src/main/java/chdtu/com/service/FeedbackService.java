package chdtu.com.service;

import chdtu.com.entity.FeedbackUsers;
import chdtu.com.entity.Users;
import chdtu.com.handler.*;
import chdtu.com.repository.FeedbackUsersRepository;
import chdtu.com.repository.UsersRepository;
import jakarta.persistence.Column;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final AIHandler aiHandler;
    private final GoogleCloudHandler googleCloudHandler;
    private final DataBaseHandler dataBaseHandler;
    private final TrelloCardHandler trelloCardHandler;
    private final UsersRepository usersRepository;
    private final UserHandler userHandler;

    public String handlerFeedback(Users user, String feedback) {
        try {
            String message = aiHandler.analysisFeedback(feedback);

            String[] lines = message.split("\n");

            String mood = "";
            int criticality = 0;
            String recommendation = "";

            for (String line : lines) {
                if (line.startsWith("Настрій відгуку:")) {
                    mood = line.split(":")[1].trim();
                } else if (line.startsWith("Критичність:")) {
                    criticality = Integer.parseInt(line.split(":")[1].trim());
                } else if (line.startsWith("Рекомендація:")) {
                    recommendation = line.split(":")[1].trim();
                }
            }

            if (mood.isEmpty() || criticality == 0 || recommendation.isEmpty()) {
                log.info("A processing error occurred");
                return "Виникла помилка";
            }

            dataBaseHandler.saveMessageToDb(user, feedback, mood, criticality, recommendation);

            googleCloudHandler.saveToGoogleDocs(feedback, message);

            if (2 < criticality) trelloCardHandler.createCard(feedback, recommendation);

            return "Рекомендація: " + recommendation;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            log.error("Error processing review text", e);
            return "Помилка при обробці тексту відгуку";
        } catch (RestClientException e) {
            log.error("HTTP error when accessing AI or Trello", e);
            return "Помилка при зв'язку із сервісом AI/Trello";

        } catch (DataAccessException e) {
            log.error("Database error", e);
            return "Помилка при збереженні у БД";

        } catch (Exception e) {
            log.error("Unknown error", e);
            return "Сталася невідома помилка";
        }
    }

    public SendDocument generateFilterFile(Users user) {
        List<Users> listUsersDB = usersRepository.findAllWithFeedbacks();

        StringBuilder sb = new StringBuilder("\n");

        for (Users userBuffer : listUsersDB) {
            List<FeedbackUsers> feedbackUsers = userBuffer.getFeedbackUsersDB();
            if (feedbackUsers.isEmpty()) {continue;}

            sb.append("Id: ").append(userBuffer.getId()).append("\n");
            sb.append("Surname: ").append(userBuffer.getSurname()).append("\n");
            sb.append("FirstName: ").append(userBuffer.getFirstName()).append("\n");
            sb.append("Patronymic: ").append(userBuffer.getPatronymic()).append("\n");
            sb.append("Position: ").append(userBuffer.getPosition()).append("\n");

            sb.append("Feedbacks:\n");
            for (FeedbackUsers feedbackUserBuffer : feedbackUsers) {
                sb.append("     Feedback: ").append(feedbackUserBuffer.getFeedback()).append("\n");
                sb.append("     Mood feedback: ").append(feedbackUserBuffer.getMoodFeedback()).append("\n");
                sb.append("     Recommendation: ").append(feedbackUserBuffer.getRecommendation()).append("\n");
                sb.append("\n");
            }

            sb.append("\n\n");
        }

        String content = sb.toString();

        InputFile inputFile = new InputFile(
                new ByteArrayInputStream(content.getBytes()),"filtered_feedback_users.txt"
        );

        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(user.getChatId());
        sendDocument.setDocument(inputFile);

        return sendDocument;
    }
}

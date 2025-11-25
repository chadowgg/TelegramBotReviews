package chdtu.com.handler;

import chdtu.com.entity.FeedbackUsers;
import chdtu.com.entity.Users;
import chdtu.com.repository.FeedbackUsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataBaseHandler {
    private final FeedbackUsersRepository feedbackUsersRepository;

    public void saveMessageToDb(Users user,
                                 String feedback,
                                 String mood,
                                 Integer criticality,
                                 String recommendation) {
        FeedbackUsers feedbackUsers = new FeedbackUsers();
        feedbackUsers.setUser(user);
        feedbackUsers.setFeedback(feedback);
        feedbackUsers.setMoodFeedback(mood);
        feedbackUsers.setCritical(criticality);
        feedbackUsers.setRecommendation(recommendation);

        feedbackUsersRepository.save(feedbackUsers);

        log.info("The feedback was saved in DB");
    }
}

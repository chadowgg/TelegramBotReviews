package chdtu.com.handler;

import chdtu.com.entity.Users;
import chdtu.com.enums.Role;
import chdtu.com.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserHandler {

    private final UsersRepository usersRepository;

    public String handleRegistration(Users user) {
        Users userDB = new Users();

        userDB.setChatId(user.getChatId());
        userDB.setSurname(user.getSurname());
        userDB.setFirstName(user.getFirstName());
        userDB.setPatronymic(user.getPatronymic());
        userDB.setPosition(user.getPosition());
        userDB.setRole(Role.USER);
        userDB.setFaculty(user.getFaculty());

        usersRepository.save(userDB);

        return "Успішно зареєстровані";
    }

    public String getUserInformation(Long chatId) {
        Users userDB = usersRepository.getFirstByChatId(chatId).orElse(null);

        if (userDB == null) {
            return "Користувача не знайдено";
        }

        return userDB.getPosition() + "\n"
                + userDB.getFirstName() + " "
                + userDB.getSurname() + "\n"
                + userDB.getFaculty();
    }

    public String saveParametersUsers(Users user) {
        Users userDB = usersRepository.getFirstByChatId(user.getChatId()).orElse(null);

        if (userDB == null) {
            return "Користувача не знайдено";
        }

        try {
            switch (user.getStatus()) {
                case "/edit_full_name":
                    userDB.setSurname(user.getSurname());
                    userDB.setFirstName(user.getFirstName());
                    userDB.setPatronymic(user.getPatronymic());
                    break;
                case "/edit_position":
                    userDB.setPosition(user.getPosition());
                    break;
                case "/edit_faculty":
                    userDB.setFaculty(user.getFaculty());
                    break;
                default:
                    return "Виникла помилка";
            }

            usersRepository.save(userDB);
            return "Збережено";
        } catch (Exception ex) {
            return "Error: " + ex.getClass().getSimpleName() + " - " + ex.getMessage();
        }
    }

    public Long setRoleAdmin(Long userChatId) {
        Users userDB = usersRepository.getFirstById(userChatId).orElse(null);

        if (userDB == null) return 0L;

        userDB.setRole(Role.ADMIN);
        usersRepository.save(userDB);

        return userDB.getId();
    }

    public Long deleteRoleAdmin(Long userChatId) {
        Users userDB = usersRepository.getFirstById(userChatId).orElse(null);

        if (userDB == null) return 0L;

        userDB.setRole(Role.USER);
        usersRepository.save(userDB);

        return userDB.getChatId();
    }

    public void setLastLoginDate(Users user) {
        Users userDB = usersRepository.getFirstByChatId(user.getChatId()).orElse(null);

        if (userDB == null) {
            return;
        }

        userDB.setLastLogin(LocalDateTime.now());

        usersRepository.save(userDB);
    }

    public SendDocument generateFilterFile(Users user) {
        List<Users> listUsersDB = usersRepository.getAllByFaculty(user.getFaculty());

        StringBuilder sb = new StringBuilder();

        for (Users userBuffer : listUsersDB) {
            sb.append("Id: ").append(userBuffer.getId()).append("\n");
            sb.append("Surname: ").append(userBuffer.getSurname()).append("\n");
            sb.append("FirstName: ").append(userBuffer.getFirstName()).append("\n");
            sb.append("Patronymic: ").append(userBuffer.getPatronymic()).append("\n");
            sb.append("Faculty: ").append(userBuffer.getFaculty()).append("\n");
            sb.append("Position: ").append(userBuffer.getPosition()).append("\n");
            sb.append("Last login: ").append(userBuffer.getLastLogin()).append("\n\n");
        }

        String content = sb.toString();
        InputFile inputFile = new InputFile(
                new ByteArrayInputStream(content.getBytes()),"filtered_users.txt"
        );

        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(user.getChatId());
        sendDocument.setDocument(inputFile);
        sendDocument.setCaption("Згенерований файл");

        return sendDocument;
    }
}

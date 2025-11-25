package chdtu.com.service;

import chdtu.com.entity.Users;
import chdtu.com.enums.Faculty;
import chdtu.com.enums.Role;
import chdtu.com.handler.UserHandler;
import chdtu.com.repository.UsersRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MessageService {

    private static final Pattern FULL_NAME_PATTERN =
            Pattern.compile("^[А-ЯІЇЄҐ][а-яіїєґ]+ [А-ЯІЇЄҐ][а-яіїєґ]+ [А-ЯІЇЄҐ][а-яіїєґ]+$");

    private static final Pattern POSITION_PATTERN =
            Pattern.compile("^[А-ЯІЇЄҐ][а-яіїєґ].*");
    private static final Pattern ID_USER_PATTERN =
            Pattern.compile("[0-9]+");

    private static final String STATUS_WAITING_NAME = "/waiting_for_name";
    private static final String STATUS_WAITING_POSITION = "/waiting_for_position";
    private static final String STATUS_WAITING_FACULTY = "/waiting_for_faculty";
    private static final String STATUS_EDIT_FULL_NAME = "/edit_full_name";
    private static final String STATUS_EDIT_POSITION = "/edit_position";
    private static final String STATUS_EDIT_FACULTY = "/edit_faculty";
    private static final String STATUS_REGISTERED = "/registered";
    private static final String STATUS_CREATION_FEEDBACK = "/user_feedback";
    private static final String STATUS_ADMIN_PANEL = "/admin_panel";
    private static final String STATUS_FILTER = "/admin_filter";
    private static final String STATUS_ADMIN_FILTRATION_FEEDBACK = "/admin_filtration_feedback";
    private static final String STATUS_ADMIN_FILTRATION_USERS = "/admin_filtration_users";
    private static final String STATUS_ADD_ADMIN = "/add_admin";
    private static final String STATUS_DELETE_ADMIN = "/delete_admin";

    private final UsersRepository usersRepository;
    private final ButtonsService buttonsService;
    private final UserHandler userHandler;
    private final FeedbackService feedbackService;

    private final Cache<Long, Users> userCache = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .maximumSize(10_000)
            .build();

    public Object messageHandler(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            return handleTextMessage(update);
        }
        else if (update.hasCallbackQuery()) {
            return handleCallbackQuery(update);
        }
        else {
            return createMessage(update.getMessage().getChatId(), "Я не розумію це повідомлення");
        }
    }

    private Object handleTextMessage(Update update) {
        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        if ("/start".equals(text)) {
            return handleStartCommand(chatId);
        }

        Users user = getUser(chatId);


        userHandler.setLastLoginDate(user);

        return switch (user.getStatus()) {
            case STATUS_WAITING_NAME -> handleWaitingForName(chatId, text, user);
            case STATUS_WAITING_POSITION -> handleWaitingForPosition(chatId, text, user);
            case STATUS_REGISTERED -> createMessageWithKeyboard(chatId, "Виберіть дію", buttonsService.showUserActionOptions(chatId));
            case STATUS_EDIT_FULL_NAME -> handleEditFullName(chatId, text, user);
            case STATUS_EDIT_POSITION -> handleEditPosition(chatId,  text, user);
            case STATUS_EDIT_FACULTY -> handleEditFaculty(chatId, text, user);
            case STATUS_CREATION_FEEDBACK -> handlerFeedback(chatId, text, user);
            case STATUS_ADMIN_PANEL -> handleAdminPanel(chatId, text, user);
            case STATUS_FILTER -> createMessageWithKeyboard(chatId,
                    "Оберіть за чим фільтрувати",
                    buttonsService.showFilterParameters(chatId)
            );
            case STATUS_ADMIN_FILTRATION_USERS -> handleFiltrationUsers(chatId, text, user);
            case STATUS_ADMIN_FILTRATION_FEEDBACK -> handleFiltrationFeedbackUsers(chatId, text, user);
            case STATUS_ADD_ADMIN -> verificationAndStorageAdmin(chatId, text, user);
            case STATUS_DELETE_ADMIN -> verificationAndDeletionAdmin(chatId, text, user);
            default -> createMessage(chatId, "Невідомий стан. Натисніть /start");
        };
    }

    private SendMessage handleStartCommand(Long chatId) {
        Users user = getUser(chatId);

        if (user == null) {
            return handleUnregistered(chatId);
        }

        if (user.getRole() == Role.ADMIN) {
            return createMessageWithKeyboard(chatId,
                    "Admin\n" +
                            userHandler.getUserInformation(chatId) +
                            "\n\nВиберіть дію",
                    buttonsService.showAdminActionOptions(chatId)
            );
        }

        user.setStatus(STATUS_REGISTERED);
        updateUserState(user);

        return createMessageWithKeyboard(chatId,
                userHandler.getUserInformation(chatId) +
                    "\n\nВиберіть дію",
                buttonsService.showUserActionOptions(chatId)
        );
    }

    private SendMessage handleUnregistered(Long chatId) {
        Users user = new Users();
        user.setChatId(chatId);
        user.setStatus(STATUS_WAITING_NAME);
        updateUserState(user);

        return createMessage(chatId, "Ви ще не зареєстровані. Введіть ПІБ");
    }

    private SendMessage handleWaitingForName(Long chatId, String text, Users user) {
        if (!isValidFullName(text)) {
            return createMessage(chatId, "Неправильне ПІБ. Введіть у форматі: Прізвище Ім'я По-батькові");
        }

        String[] fullName = text.split(" ");

        user.setSurname(fullName[0]);
        user.setFirstName(fullName[1]);
        user.setPatronymic(fullName[2]);

        updateUserStatus(user, STATUS_WAITING_POSITION);

        updateUserState(user);

        return createMessage(chatId, "Введіть посаду");
    }

    private SendMessage handleWaitingForPosition(Long chatId, String text, Users user) {
        if (!isValidPosition(text)) {
            return createMessage(chatId, "Неправильно написана посада. Має починатися з великої літери");
        }

        user.setPosition(text);
        updateUserStatus(user, STATUS_WAITING_FACULTY);

        return createMessageWithKeyboard(chatId,
                "Оберіть факультет",
                buttonsService.showFacultyOptions(chatId)
        );
    }

    private SendMessage handleWaitingForFaculty(Long chatId, String callbackData, Users user) {
        getFormattedFaculty(user, callbackData);
        updateUserStatus(user, STATUS_REGISTERED);

        String responseText = userHandler.handleRegistration(user);

        updateUserFull(user);

        return createMessageWithKeyboard(chatId,
                responseText + ". Виберіть дію",
                buttonsService.showUserActionOptions(chatId)
        );
    }

    private Object handleCallbackQuery(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String callbackData = update.getCallbackQuery().getData();
        Users user = getUser(chatId);
        userHandler.setLastLoginDate(user);

        if (user.getRole() == Role.ADMIN) {
            return handleAdminCallbackQuery(chatId, callbackData, user);
        }

        return handleUserCallbackQuery(chatId, callbackData, user);
    }

    private Object handleUserCallbackQuery(Long chatId, String callbackData, Users user) {
        if (callbackData.startsWith("/faculty_")) {
            if (STATUS_EDIT_FACULTY.equals(user.getStatus())) {
                return handleEditFaculty(chatId, callbackData, user);
            }
            return handleWaitingForFaculty(chatId, callbackData, user);
        }

        return switch (callbackData) {
            case "/user_edit" -> handleEdit(chatId);
            case "/edit_full_name" -> {
                user.setStatus(STATUS_EDIT_FULL_NAME);
                updateUserState(user);
                yield createMessage(chatId, "Введіть ПІБ");
            }
            case "/edit_position" -> {
                user.setStatus(STATUS_EDIT_POSITION);
                updateUserState(user);
                yield createMessage(chatId, "Введіть посаду");
            }
            case "/edit_faculty" -> {
                user.setStatus(STATUS_EDIT_FACULTY);
                updateUserState(user);
                yield createMessageWithKeyboard(chatId, "Оберіть факультет",
                        buttonsService.showFacultyOptions(chatId));
            }
            case "/user_feedback" -> {
                user.setStatus(STATUS_CREATION_FEEDBACK);
                updateUserState(user);
                yield createMessage(chatId, "Ведіть змістовий відгук");
            }
            default -> createMessage(chatId, "Невідома команда. Натисніть /start");
        };
    }

    private Object handleAdminCallbackQuery(Long chatId, String callbackData, Users user) {
        if (STATUS_ADMIN_FILTRATION_USERS.equals(user.getStatus())) {
            return handleFiltrationUsers(chatId, callbackData, user);
        } else if (STATUS_ADMIN_FILTRATION_FEEDBACK.equals(user.getStatus())) {
            return handleFiltrationFeedbackUsers(chatId, callbackData, user);
        }

        return switch (callbackData) {
            case "/add_admin" -> {
                user.setStatus(STATUS_ADD_ADMIN);
                yield createMessage(chatId, "Введіть ід нового адміна\n(він повинен бути зареєстрованим вже)");
            }
            case "/delete_admin" -> {
                user.setStatus(STATUS_DELETE_ADMIN);
                yield createMessage(chatId, "Введіть ід адміна\n(він повинен бути зареєстрованим вже)");
            }
            case "/admin_filter" -> {
                user.setStatus(STATUS_FILTER);
                updateUserState(user);
                yield createMessageWithKeyboard(chatId,
                        "Оберіть за чим фільтрувати",
                        buttonsService.showFilterParameters(chatId)
                );
            }
            case "/admin_filter_users" -> {
                user.setStatus(STATUS_ADMIN_FILTRATION_USERS);
                updateUserState(user);
                yield createMessageWithKeyboard(chatId,
                        "Оберіть факультет",
                        buttonsService.showFacultyOptions(chatId)
                );
            }
            case "/admin_filter_feedback_users" -> {
                user.setStatus(STATUS_ADMIN_FILTRATION_FEEDBACK);
                updateUserState(user);
                yield createMessageWithKeyboard(chatId,
                        "Оберіть факультет",
                        buttonsService.showFacultyOptions(chatId)
                );
            }
            default -> createMessage(chatId, "Невідома команда. Натисніть /start");
        };
    }

    private SendMessage handleEdit(Long chatId) {
        return createMessageWithKeyboard(chatId, "Редагувати", buttonsService.showEditOptions(chatId));
    }

    private SendMessage handleEditFullName(Long chatId, String text, Users user) {
        if (!isValidFullName(text)) {
            return createMessage(chatId, "Неправильне ПІБ. Введіть у форматі: Прізвище Ім'я По-батькові");
        }

        String[] fullName = text.split(" ");
        user.setSurname(fullName[0]);
        user.setFirstName(fullName[1]);
        user.setPatronymic(fullName[2]);

        String status = userHandler.saveParametersUsers(user);
        updateUserStatus(user, STATUS_REGISTERED);

        updateUserFull(user);

        return createMessageWithKeyboard(chatId,
                status + ".\nВиберіть дію",
                buttonsService.showUserActionOptions(chatId)
        );
    }

    private SendMessage handleEditPosition(Long chatId, String text, Users user) {
        if (!isValidPosition(text)) {
            return createMessage(chatId, "Неправильно написана посада. Має починатися з великої літери");
        }

        user.setPosition(text);
        String status = userHandler.saveParametersUsers(user);
        updateUserStatus(user, STATUS_REGISTERED);

        updateUserFull(user);

        return createMessageWithKeyboard(chatId,
                status + ".\nВиберіть дію",
                buttonsService.showUserActionOptions(chatId)
        );
    }

    private SendMessage handleEditFaculty(Long chatId, String text, Users user) {
        getFormattedFaculty(user, text);
        String status = userHandler.saveParametersUsers(user);
        updateUserStatus(user, STATUS_REGISTERED);

        updateUserFull(user);

        return createMessageWithKeyboard(chatId,
                status + ".\nВиберіть дію",
                buttonsService.showUserActionOptions(chatId)
        );
    }

    private Users getUser(Long chatId) {
        return userCache.get(chatId, key ->
                usersRepository.findByChatId(key).orElse(null)
        );
    }

    private void getFormattedFaculty(Users user, String callbackData) {
        String[] callbackDataArray = callbackData.split("_");

        for (Faculty faculty : Faculty.values()) {
            if (callbackDataArray[1].equals(String.valueOf(faculty))) {
                user.setFaculty(faculty.getDisplayName());
            }
        }
    }

    private SendMessage handlerFeedback(Long chatId, String feedback, Users user) {
        user.setStatus(STATUS_REGISTERED);
        updateUserState(user);

        return createMessageWithKeyboard(chatId,
                feedbackService.handlerFeedback(user, feedback),
                buttonsService.showUserActionOptions(chatId));
    }

    private List<Object> handleFiltrationUsers(Long chatId, String callbackData, Users user) {
        getFormattedFaculty(user, callbackData);
        SendDocument document = userHandler.generateFilterFile(user);
        SendMessage message = createMessageWithKeyboard(
                chatId,
                "Виберіть дію",
                buttonsService.showAdminActionOptions(chatId));
        updateUserStatus(user, STATUS_ADMIN_PANEL);

        return List.of(document, message);
    }

    private List<Object> handleFiltrationFeedbackUsers(Long chatId, String callbackData, Users user) {
        getFormattedFaculty(user, callbackData);
        SendDocument document = feedbackService.generateFilterFile(user);
        SendMessage message = createMessageWithKeyboard(
                chatId,
                "Виберіть дію",
                buttonsService.showAdminActionOptions(chatId));
        updateUserStatus(user, STATUS_ADMIN_PANEL);

        return List.of(document, message);
    }

    private SendMessage verificationAndStorageAdmin(Long chatId, String idUserUpdate, Users user) {
        if (!ID_USER_PATTERN.matcher(idUserUpdate.trim()).matches()) {
            return createMessage(chatId, "Не правильне ід, ведіть знову");
        }

        Long idUser = Long.parseLong(idUserUpdate);
        Long newAdminChatId = userHandler.setRoleAdmin(idUser);

        String result;

        if (!newAdminChatId.equals(0L)) {
              result = "Користувача " + idUserUpdate + " призначено адміном. ";
        } else {
            result = "Виникла помилка. ";
        }

        updateUserStatus(user, STATUS_ADMIN_PANEL);

        return createMessageWithKeyboard(
                chatId,
                result + "Виберыть дію",
                buttonsService.showAdminActionOptions(chatId)
        );
    }

    private SendMessage verificationAndDeletionAdmin(Long chatId, String idUserUpdate, Users user) {
        if (!ID_USER_PATTERN.matcher(idUserUpdate.trim()).matches()) {
            return createMessage(chatId, "Не правильне ід, ведіть знову");
        }

        Long deleteAdminId = Long.valueOf(idUserUpdate);

        Long deletedAdminChatId = userHandler.deleteRoleAdmin(deleteAdminId);

        String result;
        String newStatus;

        if (deletedAdminChatId.equals(chatId)) {
            invalidateUserCache(chatId);
            result = "Ви видалили себе з адмінів";
            newStatus = STATUS_REGISTERED;
        } else if (deletedAdminChatId.equals(0L)){
            result = "Виникла помилка";
            newStatus = STATUS_ADMIN_PANEL;
        } else {
            result = "Користувача " + idUserUpdate + " видалено з адмінів";
            newStatus = STATUS_ADMIN_PANEL;
        }

        Users updatedCurrentUser = getUser(chatId);
        updateUserStatus(updatedCurrentUser, newStatus);

        return createMessageWithKeyboard(
                chatId,
                result + ". Виберіть дію",
                getAppropriateButtons(chatId)
        );
    }

    private void updateUserState(Users user) {
        userCache.put(user.getChatId(), user);
    }

    private void updateUserStatus(Users user, String newStatus) {
        user.setStatus(newStatus);
        userCache.put(user.getChatId(), user);
    }

    private void updateUserFull(Users user) {
        usersRepository.save(user);
        userCache.put(user.getChatId(), user);
    }

    private void invalidateUserCache(Long chatId) {
        userCache.invalidate(chatId);
    }

    private boolean isValidFullName(String text) {
        return FULL_NAME_PATTERN.matcher(text.trim()).matches();
    }

    private boolean isValidPosition(String text) {
        return POSITION_PATTERN.matcher(text.trim()).matches();
    }

    private SendMessage handleAdminPanel(Long chatId, String text, Users user) {
        return createMessageWithKeyboard(chatId, "Оберіть дію: ",  buttonsService.showAdminActionOptions(chatId));
    }

    private InlineKeyboardMarkup getAppropriateButtons(Long chatId) {
        Users user = getUser(chatId);
        if (user.getRole() == Role.ADMIN) {
            return buttonsService.showAdminActionOptions(chatId);
        } else {
            return buttonsService.showUserActionOptions(chatId);
        }
    }

    private SendMessage createMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        return message;
    }

    private SendMessage createMessageWithKeyboard(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = createMessage(chatId, text);
        message.setReplyMarkup(keyboard);

        return message;
    }
}
package chdtu.com.service;

import chdtu.com.enums.Faculty;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Service
public class ButtonsService {

    public InlineKeyboardMarkup showUserActionOptions(Long chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton editButton = new InlineKeyboardButton("Редагувати");
        editButton.setCallbackData("/user_edit");

        InlineKeyboardButton feedbackButton = new InlineKeyboardButton("Відгук");
        feedbackButton.setCallbackData("/user_feedback");

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(editButton);
        row.add(feedbackButton);
        rows.add(row);
        inlineKeyboardMarkup.setKeyboard(rows);

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup showFacultyOptions(Long chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Faculty faculty : Faculty.values()) {
            InlineKeyboardButton button = new InlineKeyboardButton(faculty.getCode());
            button.setCallbackData("/faculty_" + faculty);

            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            rows.add(row);
        }

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup showEditOptions(Long chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton fullNameButton = new InlineKeyboardButton("ПІБ");
        fullNameButton.setCallbackData("/edit_full_name");

        InlineKeyboardButton positionButton = new InlineKeyboardButton("Посада");
        positionButton.setCallbackData("/edit_position");

        InlineKeyboardButton facultyButton = new InlineKeyboardButton("Факультет");
        facultyButton.setCallbackData("/edit_faculty");

        return getInlineKeyboardMarkup(inlineKeyboardMarkup, rows, fullNameButton, positionButton, facultyButton);
    }

    public InlineKeyboardMarkup showAdminActionOptions(Long chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton filtrationButton = new InlineKeyboardButton("Фільтрація");
        filtrationButton.setCallbackData("/admin_filter");

        InlineKeyboardButton addAdminButton = new InlineKeyboardButton("Додати адміна");
        addAdminButton.setCallbackData("/add_admin");

        InlineKeyboardButton deleteAdminButton = new InlineKeyboardButton("Видалити адміна");
        deleteAdminButton.setCallbackData("/delete_admin");

        return getInlineKeyboardMarkup(inlineKeyboardMarkup, rows, filtrationButton, addAdminButton, deleteAdminButton);
    }

    private InlineKeyboardMarkup getInlineKeyboardMarkup(InlineKeyboardMarkup inlineKeyboardMarkup, List<List<InlineKeyboardButton>> rows, InlineKeyboardButton filtrationButton, InlineKeyboardButton addAdminButton, InlineKeyboardButton deleteAdminButton) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(filtrationButton);
        row.add(addAdminButton);
        row.add(deleteAdminButton);
        rows.add(row);
        inlineKeyboardMarkup.setKeyboard(rows);

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup showFilterParameters(Long chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton filterUsersButton = new InlineKeyboardButton("За користувачами");
        filterUsersButton.setCallbackData("/admin_filter_users");

        InlineKeyboardButton filterFeedbackUsersButton = new InlineKeyboardButton("За відгуками");
        filterFeedbackUsersButton.setCallbackData("/admin_filter_feedback_users");

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(filterUsersButton);
        row.add(filterFeedbackUsersButton);
        rows.add(row);
        inlineKeyboardMarkup.setKeyboard(rows);

        return inlineKeyboardMarkup;
    }
}

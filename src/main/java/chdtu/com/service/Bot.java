package chdtu.com.service;

import chdtu.com.config.properties.BotProperties;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class Bot extends TelegramLongPollingBot {

    private final BotProperties botProperties;
    private final MessageService messageService;

    @Override
    public String getBotUsername() {
        return botProperties.name();
    }

    @Override
    public String getBotToken() {
        return botProperties.token();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            Object response = messageService.messageHandler(update);

//            Long chatId = update.getMessage().getChatId();
//            SendChatAction typingAction = new SendChatAction();
//            typingAction.setChatId(chatId.toString());
//            typingAction.setAction(ActionType.TYPING);
//
//            execute(typingAction);

            if (response instanceof SendMessage) {
                execute((SendMessage) response);
            } else if (response instanceof List<?> responses) {
                for (Object item : responses) {
                    if (item instanceof SendMessage) {
                        execute((SendMessage) item);
                    } else if (item instanceof SendDocument) {
                        execute((SendDocument) item);
                    }
                }
            }
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}

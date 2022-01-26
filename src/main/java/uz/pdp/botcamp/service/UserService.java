package uz.pdp.botcamp.service;

import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.pdp.botcamp.constants.MenuConstant;

import java.util.ArrayList;
import java.util.List;

public class UserService {
    public static SendMessage showMenyu(Message message) {
        SendMessage sendMessage=new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(" Select one");
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        sendMessage.setReplyMarkup(UserService.replayMarkapForAdmin(message));
        return sendMessage;
    }

    private static ReplyKeyboard replayMarkapForAdmin(Message message) {
        ReplyKeyboardMarkup keyboardMarkup=new ReplyKeyboardMarkup();
        keyboardMarkup.setOneTimeKeyboard(true);
        keyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow>rows=new ArrayList<>();

        KeyboardRow keyboardRow=new KeyboardRow();
        KeyboardButton button=new KeyboardButton();
        button.setText(MenuConstant.SHOW_CATEGORY);
        keyboardRow.add(button);

        KeyboardButton button2=new KeyboardButton();
        button2.setText(MenuConstant.SHOW_CART);
        keyboardRow.add(button2);
        rows.add(keyboardRow);

        KeyboardRow keyboardRow3=new KeyboardRow();
        KeyboardButton button3=new KeyboardButton();
        button3.setText(MenuConstant.SHOW_OFFER);
        keyboardRow3.add(button3);

        KeyboardButton button4=new KeyboardButton();
        button4.setText(MenuConstant.SHOW_SETTING);
        keyboardRow3.add(button4);
        rows.add(keyboardRow3);

        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;
    }
}

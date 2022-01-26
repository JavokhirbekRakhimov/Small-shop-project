package uz.pdp.botcamp.service;

import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.pdp.botcamp.constants.MenuConstant;

import java.util.ArrayList;
import java.util.List;

public class AdminService {
    public static SendMessage showMenyu(Message message) {
        SendMessage sendMessage=new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText("✅  Select one ");
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        sendMessage.setReplyMarkup(AdminService.replayMarkapForAdmin(message));
        return sendMessage;
    }

    private static ReplyKeyboardMarkup replayMarkapForAdmin(Message message) {
        ReplyKeyboardMarkup keyboardMarkup=new ReplyKeyboardMarkup();
        keyboardMarkup.setOneTimeKeyboard(true);
        keyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow>rows=new ArrayList<>();

        KeyboardRow row1=new KeyboardRow();
        KeyboardButton button=new KeyboardButton();
        button.setText(MenuConstant.CHANGE_USER);
        row1.add(button);

        KeyboardButton button1=new KeyboardButton();
        button1.setText(MenuConstant.CHANGE_CATEGORY);
        row1.add(button1);
        rows.add(row1);

        KeyboardRow row2=new KeyboardRow();
        KeyboardButton button3=new KeyboardButton();
        button3.setText(MenuConstant.CHANGE_PRODUCT);
        row2.add(button3);


        KeyboardButton button4=new KeyboardButton();
        button4.setText(MenuConstant.SHOW_CART_ALL);
        row2.add(button4);
        rows.add(row2);

        KeyboardRow keyboardRow4=new KeyboardRow();
        KeyboardButton button5=new KeyboardButton();
        button5.setText(MenuConstant.SHOW_CATEGORY);
        keyboardRow4.add(button5);
        rows.add(keyboardRow4);

        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;
    }

}

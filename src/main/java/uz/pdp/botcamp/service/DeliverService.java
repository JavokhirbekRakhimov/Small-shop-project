package uz.pdp.botcamp.service;

import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.pdp.botcamp.constants.ConstantWord;
import uz.pdp.botcamp.constants.MyCallBackQuary;
import uz.pdp.botcamp.model.DeliverCart;
import uz.pdp.botcamp.repository.DataBase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeliverService {
    public static SendMessage ShowMenu(Message message) {
        SendMessage sendMessage=new SendMessage();
        sendMessage.setText("✅ Select one");
        sendMessage.setChatId(message.getChatId().toString());
        ReplyKeyboardMarkup replyKeyboardMarkup=new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow>keyboardRows=new ArrayList<>();
        KeyboardButton button=new KeyboardButton();
        KeyboardRow keyboardRow=new KeyboardRow();
        button.setText(ConstantWord.NEW_OFFER);
        KeyboardButton button1=new KeyboardButton();
        button1.setText(ConstantWord.OLD_OFFER);
        keyboardRow.add(button);
        keyboardRow.add(button1);
        keyboardRows.add(keyboardRow);
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        return sendMessage;
    }

    public static SendPhoto showNewOffer(Message message) {
        SendPhoto sendPhoto=new SendPhoto();
        sendPhoto.setChatId(message.getChatId().toString());
        sendPhoto.setPhoto(new InputFile(new File(ConstantWord.DELIVER_CAR_PHOTO)));
        List<DeliverCart> deliverCarts = DataBase.deliverCart.stream().filter(deliverCart ->
                !deliverCart.isDeliveredToUser()).toList();

        if(!deliverCarts.isEmpty()){
            int size = deliverCarts.size();
            InlineKeyboardMarkup inlineKeyboardMarkup=new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>>keyboardRows=new ArrayList<>();
            InlineKeyboardButton keyboardButton=null;
            List<InlineKeyboardButton>list=new ArrayList<>();
            String indexStr = DataBase.rememberIndex.get(message.getChatId());
            if(Objects.isNull(indexStr)){
                indexStr="0";
            }
            int index = Integer.parseInt(indexStr);
            if(index>=size){
                index=0;
            }
            int counter=1;
            for (; index < deliverCarts.size()&&counter<4; index++) {
                keyboardButton=new InlineKeyboardButton();
                keyboardButton.setText("\uD83C\uDD94  "+deliverCarts.get(index).getId());
                keyboardButton.setCallbackData(ConstantWord.SELECT_OFFER_ID+ConstantWord.PEREFIX+
                        deliverCarts.get(index).getId());

                    list.add(keyboardButton);
                counter++;
            }
            DataBase.rememberIndex.put(message.getChatId(),String.valueOf(index));
            keyboardRows.add(list);
            list=new ArrayList<>();
            InlineKeyboardButton button=new InlineKeyboardButton();
            button.setText("-3  ⏮");
            button.setCallbackData(ConstantWord.MINUS);
            if (index-3!=0) {
                list.add(button);
            }
            keyboardButton=new InlineKeyboardButton();
            keyboardButton.setText(index+"-"+size);
            keyboardButton.setCallbackData("Bla Bla");
            list.add(keyboardButton);

            InlineKeyboardButton button1=new InlineKeyboardButton();
            button1.setText("⏭  +3");
            button1.setCallbackData(ConstantWord.PLUS);
            if (size/index>0&&index!=size) {
                list.add(button1);
            }

            keyboardRows.add(list);
            inlineKeyboardMarkup.setKeyboard(keyboardRows);
            sendPhoto.setReplyMarkup(inlineKeyboardMarkup);
            sendPhoto.setCaption("New offer");
        }else {
            sendPhoto.setCaption("We don't have any new offer \n\n" +
                    "Please waite");
        }
        return sendPhoto;
    }

    public static SendMessage selectOperation(Message message, long id) {
        SendMessage sendMessage=new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText("✅  Select one");
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        InlineKeyboardMarkup inlineKeyboardMarkup=new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>>keyboardRows=new ArrayList<>();
        List<InlineKeyboardButton>list=new ArrayList<>();
        InlineKeyboardButton button=new InlineKeyboardButton();
        button.setText("Download information");
        button.setCallbackData(ConstantWord.DOWNLOAD_PDF+ConstantWord.PEREFIX+id);
        list.add(button);

        InlineKeyboardButton button1=new InlineKeyboardButton();
        button1.setText("This offer delivered");
        button1.setCallbackData(ConstantWord.ARRIVED+ConstantWord.PEREFIX+id);
        list.add(button1);
        keyboardRows.add(list);
        InlineKeyboardButton button2=new InlineKeyboardButton();
        button2.setText("🔙 Back");
        button2.setCallbackData(MyCallBackQuary.BACKTOMENU);
        list=new ArrayList<>();
        list.add(button2);
        keyboardRows.add(list);
        inlineKeyboardMarkup.setKeyboard(keyboardRows);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMessage;
    }

    public static SendPhoto showNewOfferMinus(Message message) {
        SendPhoto sendPhoto=new SendPhoto();
        sendPhoto.setChatId(message.getChatId().toString());
        sendPhoto.setPhoto(new InputFile(new File(ConstantWord.DELIVER_CAR_PHOTO)));
        List<DeliverCart> deliverCarts = DataBase.deliverCart.stream().filter(deliverCart ->
                !deliverCart.isDeliveredToUser()).toList();

        if(!deliverCarts.isEmpty()){
            int size = deliverCarts.size();
            InlineKeyboardMarkup inlineKeyboardMarkup=new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>>keyboardRows=new ArrayList<>();
            InlineKeyboardButton keyboardButton=null;
            List<InlineKeyboardButton>list=new ArrayList<>();
            String indexStr = DataBase.rememberIndex.get(message.getChatId());
            if(Objects.isNull(indexStr)){
                indexStr="0";
            }
            int index = Integer.parseInt(indexStr);
            index=((index-3)/3)*3;
            int counter=1;
            for (; index < deliverCarts.size()&&counter<4; index++) {
                keyboardButton=new InlineKeyboardButton();
                keyboardButton.setText("\uD83C\uDD94  "+deliverCarts.get(index).getId());
                keyboardButton.setCallbackData(ConstantWord.SELECT_OFFER_ID+ConstantWord.PEREFIX+
                        deliverCarts.get(index).getId());

                    list.add(keyboardButton);

                counter++;
            }

            DataBase.rememberIndex.put(message.getChatId(),String.valueOf(index));
            keyboardRows.add(list);
            list=new ArrayList<>();
            InlineKeyboardButton button=new InlineKeyboardButton();
            button.setText("-3  ⏮");
            button.setCallbackData(ConstantWord.MINUS);
            if (index-3!=0) {
                list.add(button);
            }
            keyboardButton=new InlineKeyboardButton();
            keyboardButton.setText(index+"-"+size);
            keyboardButton.setCallbackData("Bla Bla");
            list.add(keyboardButton);

            InlineKeyboardButton button1=new InlineKeyboardButton();
            button1.setText("⏭  +3");
            button1.setCallbackData(ConstantWord.PLUS);
                list.add(button1);

            keyboardRows.add(list);
            inlineKeyboardMarkup.setKeyboard(keyboardRows);
            sendPhoto.setReplyMarkup(inlineKeyboardMarkup);
            sendPhoto.setCaption("New offer");
        }else {
            sendPhoto.setCaption("We don't have any new offer \n\n" +
                    "Please waite");
        }
        return sendPhoto;
    }

    public static SendPhoto showNewOfferPlus(Message message) {
        SendPhoto sendPhoto=new SendPhoto();
        sendPhoto.setChatId(message.getChatId().toString());
        sendPhoto.setPhoto(new InputFile(new File(ConstantWord.DELIVER_CAR_PHOTO)));
        List<DeliverCart> deliverCarts = DataBase.deliverCart.stream().filter(deliverCart ->
                !deliverCart.isDeliveredToUser()).toList();
        if(!deliverCarts.isEmpty()){
            int size = deliverCarts.size();
            InlineKeyboardMarkup inlineKeyboardMarkup=new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>>keyboardRows=new ArrayList<>();
            InlineKeyboardButton keyboardButton=null;
            List<InlineKeyboardButton>list=new ArrayList<>();
            String indexStr = DataBase.rememberIndex.get(message.getChatId());
            if(Objects.isNull(indexStr)){
                indexStr="0";
            }
            int index = Integer.parseInt(indexStr);
            int counter=1;
            for (; index < deliverCarts.size()&&counter<4; index++) {
                keyboardButton=new InlineKeyboardButton();
                keyboardButton.setText("\uD83C\uDD94  "+deliverCarts.get(index).getId());
                keyboardButton.setCallbackData(ConstantWord.SELECT_OFFER_ID+ConstantWord.PEREFIX+
                        deliverCarts.get(index).getId());
                    list.add(keyboardButton);
                counter++;
            }
            DataBase.rememberIndex.put(message.getChatId(),String.valueOf(index));
            keyboardRows.add(list);
            list=new ArrayList<>();
            InlineKeyboardButton button=new InlineKeyboardButton();
            button.setText("-3  ⏮");
            button.setCallbackData(ConstantWord.MINUS);
                list.add(button);

            keyboardButton=new InlineKeyboardButton();
            keyboardButton.setText(index+"-"+size);
            keyboardButton.setCallbackData("Bla Bla");
            list.add(keyboardButton);

            InlineKeyboardButton button1=new InlineKeyboardButton();
            button1.setText("⏭  +3");
            button1.setCallbackData(ConstantWord.PLUS);
            if (size/index>0&&index!=size) {
                list.add(button1);
            }
            keyboardRows.add(list);
            inlineKeyboardMarkup.setKeyboard(keyboardRows);
            sendPhoto.setReplyMarkup(inlineKeyboardMarkup);
            sendPhoto.setCaption("New offer");
        }else {
            sendPhoto.setCaption("We don't have any new offer \n\n" +
                    "Please waite");
        }
        return sendPhoto;
    }

    public static SendPhoto showOldOffer(Message message) {
        SendPhoto sendPhoto=new SendPhoto();
        sendPhoto.setChatId(message.getChatId().toString());
        sendPhoto.setPhoto(new InputFile(new File(ConstantWord.DELIVER_CAR_PHOTO)));
        List<DeliverCart> deliverCarts = DataBase.deliverCart.stream().filter(DeliverCart::isDeliveredToUser).toList();

        if(!deliverCarts.isEmpty()){
            int size = deliverCarts.size();
            InlineKeyboardMarkup inlineKeyboardMarkup=new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>>keyboardRows=new ArrayList<>();
            InlineKeyboardButton keyboardButton=null;
            List<InlineKeyboardButton>list=new ArrayList<>();
            String indexStr = DataBase.rememberIndex.get(message.getChatId());
            if(Objects.isNull(indexStr)){
                indexStr="0";
            }
            int index = Integer.parseInt(indexStr);
            if(index>=size){
                index=0;
            }
            int counter=1;
            for (; index < deliverCarts.size()&&counter<4; index++) {
                keyboardButton=new InlineKeyboardButton();
                keyboardButton.setText("\uD83C\uDD94  "+deliverCarts.get(index).getId());
                keyboardButton.setCallbackData(ConstantWord.SELECT_OFFER_ID_OLD+ConstantWord.PEREFIX+
                        deliverCarts.get(index).getId());

                list.add(keyboardButton);
                counter++;
            }
            DataBase.rememberIndex.put(message.getChatId(),String.valueOf(index));
            keyboardRows.add(list);
            list=new ArrayList<>();
            InlineKeyboardButton button=new InlineKeyboardButton();
            button.setText("-3  ⏮");
            button.setCallbackData(ConstantWord.MINUS);
            if (index-3!=0) {
                list.add(button);
            }
            keyboardButton=new InlineKeyboardButton();
            keyboardButton.setText(index+"-"+size);
            keyboardButton.setCallbackData("Bla Bla");
            list.add(keyboardButton);

            InlineKeyboardButton button1=new InlineKeyboardButton();
            button1.setText("⏭  +3");
            button1.setCallbackData(ConstantWord.PLUS);
            if (size/index>0&&index!=size) {
                list.add(button1);
            }

            keyboardRows.add(list);
            inlineKeyboardMarkup.setKeyboard(keyboardRows);
            sendPhoto.setReplyMarkup(inlineKeyboardMarkup);
            sendPhoto.setCaption("✅ Select one");
        }else {
            sendPhoto.setCaption("We don't have any new offer \n\n" +
                    "Please waite");
        }
        return sendPhoto;
    }

    public static SendMessage selectOperationForOld(Message message, long id) {
        SendMessage sendMessage=new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText("✅  Select one");
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        InlineKeyboardMarkup inlineKeyboardMarkup=new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>>keyboardRows=new ArrayList<>();
        List<InlineKeyboardButton>list=new ArrayList<>();
        InlineKeyboardButton button=new InlineKeyboardButton();
        button.setText("Download information");
        button.setCallbackData(ConstantWord.DOWNLOAD_PDF+ConstantWord.PEREFIX+id);
        list.add(button);
        keyboardRows.add(list);
        InlineKeyboardButton button1=new InlineKeyboardButton();
        button1.setText("🔙 Back");
        button1.setCallbackData(MyCallBackQuary.BACKTOMENU);
        list=new ArrayList<>();
        list.add(button1);

        keyboardRows.add(list);

        inlineKeyboardMarkup.setKeyboard(keyboardRows);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMessage;
    }

    public static SendPhoto oldMinus(Message message) {
        SendPhoto sendPhoto=new SendPhoto();
        sendPhoto.setChatId(message.getChatId().toString());
        sendPhoto.setPhoto(new InputFile(new File(ConstantWord.DELIVER_CAR_PHOTO)));
        List<DeliverCart> deliverCarts = DataBase.deliverCart.stream().filter(DeliverCart::isDeliveredToUser).toList();

        if(!deliverCarts.isEmpty()){
            int size = deliverCarts.size();
            InlineKeyboardMarkup inlineKeyboardMarkup=new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>>keyboardRows=new ArrayList<>();
            InlineKeyboardButton keyboardButton=null;
            List<InlineKeyboardButton>list=new ArrayList<>();
            String indexStr = DataBase.rememberIndex.get(message.getChatId());
            if(Objects.isNull(indexStr)){
                indexStr="0";
            }
            int index = Integer.parseInt(indexStr);
            index=((index-3)/3)*3;
            int counter=1;
            for (; index < deliverCarts.size()&&counter<4; index++) {
                keyboardButton=new InlineKeyboardButton();
                keyboardButton.setText("\uD83C\uDD94  "+deliverCarts.get(index).getId());
                keyboardButton.setCallbackData(ConstantWord.SELECT_OFFER_ID_OLD+ConstantWord.PEREFIX+
                        deliverCarts.get(index).getId());

                list.add(keyboardButton);

                counter++;
            }

            DataBase.rememberIndex.put(message.getChatId(),String.valueOf(index));
            keyboardRows.add(list);
            list=new ArrayList<>();
            InlineKeyboardButton button=new InlineKeyboardButton();
            button.setText("-3  ⏮");
            button.setCallbackData(ConstantWord.MINUS);
            if ((index-3)!=0) {
                list.add(button);
            }
            keyboardButton=new InlineKeyboardButton();
            keyboardButton.setText(index+"-"+size);
            keyboardButton.setCallbackData("Bla Bla");
            list.add(keyboardButton);

            InlineKeyboardButton button1=new InlineKeyboardButton();
            button1.setText("⏭  +3");
            button1.setCallbackData(ConstantWord.PLUS);
            list.add(button1);

            keyboardRows.add(list);
            inlineKeyboardMarkup.setKeyboard(keyboardRows);
            sendPhoto.setReplyMarkup(inlineKeyboardMarkup);
            sendPhoto.setCaption("✅ Select");
        }else {
            sendPhoto.setCaption("We don't have any new offer \n\n" +
                    "Please waite");
        }
        return sendPhoto;
    }public static SendPhoto oldPlus(Message message){
        SendPhoto sendPhoto=new SendPhoto();
        sendPhoto.setChatId(message.getChatId().toString());
        sendPhoto.setPhoto(new InputFile(new File(ConstantWord.DELIVER_CAR_PHOTO)));
        List<DeliverCart> deliverCarts = new ArrayList<>();
        for (DeliverCart deliverCart : DataBase.deliverCart) {
            if (deliverCart.isDeliveredToUser()) {
                deliverCarts.add(deliverCart);
            }
        }
        if(!deliverCarts.isEmpty()){
            int size = deliverCarts.size();
            InlineKeyboardMarkup inlineKeyboardMarkup=new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>>keyboardRows=new ArrayList<>();
            InlineKeyboardButton keyboardButton=null;
            List<InlineKeyboardButton>list=new ArrayList<>();
            String indexStr = DataBase.rememberIndex.get(message.getChatId());
            if(Objects.isNull(indexStr)){
                indexStr="0";
            }
            int index = Integer.parseInt(indexStr);
            int counter=1;
            for (; index < deliverCarts.size()&&counter<4; index++) {
                keyboardButton=new InlineKeyboardButton();
                keyboardButton.setText("\uD83C\uDD94  "+deliverCarts.get(index).getId());
                keyboardButton.setCallbackData(ConstantWord.SELECT_OFFER_ID_OLD+ConstantWord.PEREFIX+
                        deliverCarts.get(index).getId());
                list.add(keyboardButton);
                counter++;
            }
            DataBase.rememberIndex.put(message.getChatId(),String.valueOf(index));
            keyboardRows.add(list);
            list=new ArrayList<>();
            InlineKeyboardButton button=new InlineKeyboardButton();
            button.setText("-3  ⏮");
            button.setCallbackData(ConstantWord.MINUS);
            list.add(button);

            keyboardButton=new InlineKeyboardButton();
            keyboardButton.setText(index+"-"+size);
            keyboardButton.setCallbackData("Bla Bla");
            list.add(keyboardButton);

            InlineKeyboardButton button1=new InlineKeyboardButton();
            button1.setText("⏭  +3");
            button1.setCallbackData(ConstantWord.PLUS);
            if (size/index>0 &&size!=index) {
                list.add(button1);
            }

            keyboardRows.add(list);
            inlineKeyboardMarkup.setKeyboard(keyboardRows);
            sendPhoto.setReplyMarkup(inlineKeyboardMarkup);
            sendPhoto.setCaption("New offer");
        }else {
            sendPhoto.setCaption("We don't have any new offer \n\n" +
                    "Please waite");
        }
        return sendPhoto;
    }

    public static SendMessage ShowMenudeliverForAdmin(Message message) {
        SendMessage sendMessage=new SendMessage();
        sendMessage.setText("✅ Select one");
        sendMessage.setChatId(message.getChatId().toString());
        ReplyKeyboardMarkup replyKeyboardMarkup=new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow>keyboardRows=new ArrayList<>();
        KeyboardButton button=new KeyboardButton();
        KeyboardRow keyboardRow=new KeyboardRow();
        button.setText(ConstantWord.NEW_OFFER);
        KeyboardButton button1=new KeyboardButton();
        button1.setText(ConstantWord.OLD_OFFER);
        keyboardRow.add(button);
        keyboardRow.add(button1);
        KeyboardButton button2=new KeyboardButton();
        KeyboardRow keyboardRow2=new KeyboardRow();
        button2.setText(ConstantWord.BACK_MENU);
        keyboardRow2.add(button2);
        keyboardRows.add(keyboardRow);
        keyboardRows.add(keyboardRow2);
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        return sendMessage;
    }

}

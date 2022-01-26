package uz.pdp;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import uz.pdp.botcamp.MyShopTelegramBot;

import uz.pdp.botcamp.repository.DataBase;


public class Main {
    public static void main(String[] args) {
        DataBase.readAllJson();
        try {
            TelegramBotsApi api=new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(new MyShopTelegramBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

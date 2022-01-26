package uz.pdp.botcamp.service;

import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import uz.pdp.botcamp.constants.ConstantWord;
import uz.pdp.botcamp.constants.MyCallBackQuary;
import uz.pdp.botcamp.model.CartProduct;
import uz.pdp.botcamp.model.Product;
import uz.pdp.botcamp.repository.DataBase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class CartProductService {
    public static void addProduct(Message message, String data) {
        String[] split = data.split("/");
        Integer categoryId = Integer.parseInt(split[1]);
        Long productId = Long.parseLong(split[2]);
        Integer ammount = Integer.parseInt(split[3]);
        boolean add = false;
        for (CartProduct cartProduct : DataBase.cartProducts) {
            if (cartProduct.getCardId().equals(message.getChatId()) &&
                    cartProduct.getProductId().equals(productId)) {
                cartProduct.setProductAmount(cartProduct.getProductAmount() + ammount);
                add = true;
                break;
            }
        }

        if (!add) {
            CartProduct cartProduct = new CartProduct(message.getChatId(), categoryId, productId, ammount);
            DataBase.cartProducts.add(cartProduct);
        }
        DataBase.refreshCounter();
        DataBase.writeToJson(ConstantWord.CART_PRODUCT);
        DataBase.writeToJson(ConstantWord.COUNTER_CATEGORY);

    }


    public static SendPhoto showCart(Message message) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(message.getChatId().toString());

        ///Malumotlarni bazadan olish
        String result = "\uD83D\uDE31  No product yet \n\n" +
                "\uD83D\uDDE3 Cart empty";
        List<CartProduct> cartProducts = DataBase.cartProducts.stream().filter(cartProduct
                -> cartProduct.getCardId().equals(message.getChatId())).toList();


        if (!cartProducts.isEmpty()) {
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
            List<InlineKeyboardButton> list = null;
            InlineKeyboardButton button1 = null;
            InlineKeyboardButton button2 = null;
            InlineKeyboardButton button3 = null;

            result = "\uD83D\uDDE3 Cart have\n\n";
            Double total = 0.;
            for (int i = 0; i < cartProducts.size(); i++) {
                sendPhoto.setPhoto(new InputFile(new File(ConstantWord.FULL_CART_PHOTO)));
                String productName = "";
                Double price = 0.0;

                for (Product product : DataBase.products) {
                    if (cartProducts.get(i).getProductId().equals(product.getProductId())) {
                        productName = product.getProductName();
                        price = product.getProductPrice();
                        break;
                    }
                }

                double summ = cartProducts.get(i).getProductAmount() * price;
                result += i + 1 + ". " + productName + "  x  " + cartProducts.get(i).getProductAmount() +
                        "  ==> " +String.format("%9.1f",summ)  + "  SUM\n";
                total += summ;
                list=new ArrayList<>();
                button1=new InlineKeyboardButton();
                button1.setText(" -1 ");
                button1.setCallbackData(ConstantWord.MINUS + ConstantWord.PEREFIX +
                        cartProducts.get(i).getProductId());
                list.add(button1);
                button2=new InlineKeyboardButton();
                button2.setText(productName);
                button2.setCallbackData(productName);
                list.add(button2);
                button3=new InlineKeyboardButton();
                button3.setText(" +1 ");
                button3.setCallbackData(ConstantWord.PLUS + ConstantWord.PEREFIX +
                        cartProducts.get(i).getProductId());
                list.add(button3);
                keyboardRows.add(list);

            }
            result += "\n\n \uD83D\uDCB0  *Total price*= " +String.format("%9.1f",total) + "  SUM";
            sendPhoto.setCaption(result);
            sendPhoto.setParseMode(ParseMode.MARKDOWN);

            list=new ArrayList<>();
            button1=new InlineKeyboardButton();
            button1.setText("✅  Ordering");
            button1.setCallbackData(ConstantWord.SAVE_OFFERCART);
            button2=new InlineKeyboardButton();
            button2.setText("\uD83D\uDEAB  Delete all product");
            button2.setCallbackData(ConstantWord.DELETE_CART);
            list.add(button1);
            list.add(button2);
            keyboardRows.add(list);
            list=new ArrayList<>();
            button1=new InlineKeyboardButton();
            button1.setText("📃  Show all description");
            button1.setCallbackData(MyCallBackQuary.SHOW_DESCRIPTION);
            list.add(button1);
            keyboardRows.add(list);
            inlineKeyboardMarkup.setKeyboard(keyboardRows);
            sendPhoto.setReplyMarkup(inlineKeyboardMarkup);
            return sendPhoto;
        }
        sendPhoto.setPhoto(new InputFile(new File(ConstantWord.EMPTY_CART_PHOTO)));
        sendPhoto.setCaption(result);
        sendPhoto.setParseMode(ParseMode.MARKDOWN);
        return sendPhoto;
    }
}

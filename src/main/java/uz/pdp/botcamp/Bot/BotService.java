package uz.pdp.botcamp.Bot;


import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.pdp.botcamp.constants.ConstantWord;
import uz.pdp.botcamp.constants.MyCallBackQuary;
import uz.pdp.botcamp.constants.UserLastOperation;
import uz.pdp.botcamp.constants.UserRole;
import uz.pdp.botcamp.model.*;
import uz.pdp.botcamp.repository.DataBase;
import uz.pdp.botcamp.service.*;
import uz.pdp.botcamp.twilio.TwilioService;

import java.io.File;
import java.util.*;

public class BotService {

    public static SendMessage askContact(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText("\uD83D\uDCF1 Send your contact to continue ");
        sendMessage.setParseMode(ParseMode.MARKDOWN);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboardRowList = new ArrayList<>();

        KeyboardRow keyboardRow = new KeyboardRow();
        KeyboardButton button = new KeyboardButton();
        button.setText("☎ Share contact");
        button.setRequestContact(true);

        keyboardRow.add(button);
        keyboardRowList.add(keyboardRow);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);

        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        return sendMessage;
    }

    public static void sendSMSCode(Message message, Contact contact) {
        TwilioService.sentSMSCode(message.getChatId(), contact.getPhoneNumber());
    }

    public static User registerUser(Message message, Contact contact) {
        boolean hasUser = false;
        User currentUser = null;
        for (User user : DataBase.users) {
            if (user.getUserId().equals(message.getChatId())) {
                hasUser = true;

                BotService.refreshUser(user, message, contact);
                currentUser = user;
                break;
            }
        }
        if (!hasUser) {
            User user = new User(message, contact);
            currentUser = user;
            DataBase.users.add(user);
        }
        DataBase.writeToJson(ConstantWord.USER);
        return currentUser;
    }

    private static void refreshUser(User user, Message message, Contact contact) {
        org.telegram.telegrambots.meta.api.objects.User from = message.getFrom();
        user.setLastOperation(UserLastOperation.START);
        if (!user.getFirstName().equals(from.getFirstName())) {
            user.setFirstName(from.getFirstName());
        }

        if (from.getLastName() != null) {
            if (!user.getLastName().equals(from.getLastName()))
                user.setLastName(from.getLastName());
        }
        if (from.getUserName() != null) {
            if (!user.getUserName().equals(from.getUserName()))
                user.setUserName(from.getUserName());
        }

        if (!contact.getPhoneNumber().equals(user.getPhoneNumber()))
            user.setPhoneNumber(contact.getPhoneNumber());
    }

    public static SendMessage showMenyuForAdmin(Message message) {

        return AdminService.showMenyu(message);
    }

    public static SendMessage showMenyuForUser(Message message) {
        return UserService.showMenyu(message);
    }

    public static SendMessage wrongPassword(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText("Wrong password try again");
        return sendMessage;
    }

    public static SendMessage sayAboutSendMessage(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText("We send code \n Please enter the code");
        return sendMessage;
    }

    public static SendMessage sayAnyThing(Message message, String say) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(say);
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        return sendMessage;
    }

    public static SendMessage showCategoryForCreate(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText("If the category have parent category select it\n" +
                "else select 0");
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        sendMessage.setReplyMarkup(BotService.CategoryRowForAdmin(text));
        return sendMessage;
    }

    private static ReplyKeyboard CategoryRowForAdmin(String text) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttunList = new ArrayList<>();
        Iterator<Category> iterator = DataBase.category.iterator();
        List<InlineKeyboardButton> list = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("No parent Category");
        button.setCallbackData(ConstantWord.CATEGORY_CREAT + ConstantWord.PEREFIX + text + ConstantWord.PEREFIX + 0);
        list.add(button);
        buttunList.add(list);

        while (iterator.hasNext()) {
            Category cat = iterator.next();
            List<InlineKeyboardButton> list1 = new ArrayList<>();
            InlineKeyboardButton button1 = new InlineKeyboardButton();
            button1.setText(cat.getCategoryName());
            button1.setCallbackData(ConstantWord.CATEGORY_CREAT + ConstantWord.PEREFIX + text + ConstantWord.PEREFIX + cat.getCategoryId());
            list1.add(button1);

            if (iterator.hasNext()) {
                cat = iterator.next();
                InlineKeyboardButton button2 = new InlineKeyboardButton();
                button2.setText(cat.getCategoryName());
                button2.setCallbackData(ConstantWord.CATEGORY_CREAT + ConstantWord.PEREFIX + text + ConstantWord.PEREFIX + cat.getCategoryId());
                list1.add(button2);
            }
            if (iterator.hasNext()) {
                cat = iterator.next();
                InlineKeyboardButton button3 = new InlineKeyboardButton();
                button3.setText(cat.getCategoryName());
                button3.setCallbackData(ConstantWord.CATEGORY_CREAT + ConstantWord.PEREFIX + text + ConstantWord.PEREFIX + cat.getCategoryId());
                list1.add(button3);
            }
            buttunList.add(list1);

        }
        inlineKeyboardMarkup.setKeyboard(buttunList);
        return inlineKeyboardMarkup;
    }

    //bu mehod faqat ko'rish uchun admin ma'lumotlarni o'zgartirishida foydalana olmaydi
    public static SendMessage showCategoryMessage(Message message, Integer parentCategoryId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText("✅  Select category one");
        sendMessage.setReplyMarkup(category(ConstantWord.CATEGORY, parentCategoryId));
        return sendMessage;
    }

    public static EditMessageText showCategory(Message message, Integer parentCategoryId) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setMessageId(message.getMessageId());
        editMessageText.setChatId(message.getChatId().toString());
        editMessageText.setText("✅  *Select one*");
        editMessageText.setParseMode(ParseMode.MARKDOWN);
        editMessageText.setReplyMarkup(category(ConstantWord.CATEGORY, parentCategoryId));
        return editMessageText;

    }

    public static InlineKeyboardMarkup category(String process, Integer Id) {
        List<Category> collect = DataBase.category.stream().filter(category ->
                category.getParentCategoryId().equals(Id)).toList();

        Iterator<Category> iterator = collect.iterator();
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        if (!collect.isEmpty()) {
            while (iterator.hasNext()) {
                Category cat = iterator.next();
                List<InlineKeyboardButton> list1 = new ArrayList<>();
                InlineKeyboardButton button1 = new InlineKeyboardButton();
                button1.setText(cat.getCategoryName());
                button1.setCallbackData(process + ConstantWord.PEREFIX + cat.getCategoryId());
                list1.add(button1);

                if (iterator.hasNext()) {
                    cat = iterator.next();
                    InlineKeyboardButton button2 = new InlineKeyboardButton();
                    button2.setText(cat.getCategoryName());
                    button2.setCallbackData(process + ConstantWord.PEREFIX + cat.getCategoryId());
                    list1.add(button2);
                }
                if (iterator.hasNext()) {
                    cat = iterator.next();
                    InlineKeyboardButton button3 = new InlineKeyboardButton();
                    button3.setText(cat.getCategoryName());
                    button3.setCallbackData(process + ConstantWord.PEREFIX + cat.getCategoryId());
                    list1.add(button3);
                }
                keyboardRows.add(list1);
            }
        } else {
            process = ConstantWord.PRODUCT;
            List<Product> productList = DataBase.products.stream().filter(product ->
                    product.getCategoryId().equals(Id)).toList();
            for (Product product : productList) {
                List<InlineKeyboardButton> list1 = new ArrayList<>();
                InlineKeyboardButton button1 = new InlineKeyboardButton();
                button1.setText(product.getProductName());
                button1.setCallbackData(process + ConstantWord.PEREFIX + Id + ConstantWord.PEREFIX + product.getProductId());
                list1.add(button1);
                keyboardRows.add(list1);
            }

        }
        if (Id != 0) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            List<InlineKeyboardButton> list = new ArrayList<>();
            button.setText("\uD83D\uDD19  BACK");
            button.setCallbackData(process + ConstantWord.PEREFIX + ConstantWord.BACK + ConstantWord.PEREFIX + Id);
            list.add(button);
            keyboardRows.add(list);
        }
        inlineKeyboardMarkup.setKeyboard(keyboardRows);
        return inlineKeyboardMarkup;
    }


    public static SendMessage chooseProductOperation(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText("✅  Select one");
        sendMessage.setParseMode(ParseMode.MARKDOWN);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keybordButtons = new ArrayList<>();
        List<InlineKeyboardButton> list1 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("\uD83D\uDCDD Add product");
        button1.setCallbackData(MyCallBackQuary.ADD_PRODUCT);
        list1.add(button1);

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("\uD83D\uDD04  Edit product");
        button2.setCallbackData(MyCallBackQuary.EDIT_PRODUCT);
        list1.add(button2);
        keybordButtons.add(list1);

        List<InlineKeyboardButton> list3 = new ArrayList<>();

        InlineKeyboardButton button4 = new InlineKeyboardButton();
        button4.setText("➕  Add Description");
        button4.setCallbackData(MyCallBackQuary.ADD_DESCRIPTION);
        list3.add(button4);

        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText("\uD83D\uDEAB Delete product");
        button3.setCallbackData(MyCallBackQuary.DELETE_PRODUCT);
        list3.add(button3);

        keybordButtons.add(list3);
        inlineKeyboardMarkup.setKeyboard(keybordButtons);

        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMessage;
    }

    public static EditMessageText showCategoryForAddProduct(Message message, int parentCategoryId) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setMessageId(message.getMessageId());
        editMessageText.setChatId(message.getChatId().toString());
        editMessageText.setText(" ✅  Select category one");
        editMessageText.setParseMode(ParseMode.MARKDOWN);
        editMessageText.setReplyMarkup(categoryForAddProduct(ConstantWord.CATEGORY, parentCategoryId));
        return editMessageText;
    }

    private static InlineKeyboardMarkup categoryForAddProduct(String process, int Id) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<Category> collect = DataBase.category.stream().filter(category ->
                category.getParentCategoryId().equals(Id)).toList();
        if (collect.size() != 0) {
            Iterator<Category> iterator = collect.iterator();
            while (iterator.hasNext()) {
                Category cat = iterator.next();
                List<InlineKeyboardButton> list1 = new ArrayList<>();
                InlineKeyboardButton button1 = new InlineKeyboardButton();
                button1.setText(cat.getCategoryName());
                button1.setCallbackData(process + ConstantWord.PEREFIX + cat.getCategoryId());
                list1.add(button1);

                if (iterator.hasNext()) {
                    cat = iterator.next();
                    InlineKeyboardButton button2 = new InlineKeyboardButton();
                    button2.setText(cat.getCategoryName());
                    button2.setCallbackData(process + ConstantWord.PEREFIX + cat.getCategoryId());
                    list1.add(button2);
                }
                if (iterator.hasNext()) {
                    cat = iterator.next();
                    InlineKeyboardButton button3 = new InlineKeyboardButton();
                    button3.setText(cat.getCategoryName());
                    button3.setCallbackData(process + ConstantWord.PEREFIX + cat.getCategoryId());
                    list1.add(button3);
                }
                keyboardRows.add(list1);
            }
        } else {
            List<InlineKeyboardButton> list1 = new ArrayList<>();
            InlineKeyboardButton button1 = new InlineKeyboardButton();
            button1.setText("\uD83D\uDCDD Add product this");
            button1.setCallbackData(MyCallBackQuary.ADD_PRODUCT + ConstantWord.PEREFIX + process + ConstantWord.PEREFIX + Id);
            list1.add(button1);
            keyboardRows.add(list1);
        }
        inlineKeyboardMarkup.setKeyboard(keyboardRows);
        return inlineKeyboardMarkup;
    }

    public static SendMessage choosePhotoWay(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(" \uD83D\uDDBC Add photo this product\n" +
                "✅ Select one");
        sendMessage.setParseMode(ParseMode.MARKDOWN);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keybordRows = new ArrayList<>();
        List<InlineKeyboardButton> list = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("\uD83D\uDDBC Photo");
        button1.setCallbackData(String.valueOf(UserLastOperation.ASK_PRODUCT_PHOTO));
        list.add(button1);
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("\uD83C\uDF10 URL");
        button2.setCallbackData(String.valueOf(UserLastOperation.ASK_PRODUCT_URL));
        list.add(button2);
        keybordRows.add(list);

        List<InlineKeyboardButton> list1 = new ArrayList<>();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText(" \uD83D\uDEAB No photo");
        button3.setCallbackData(String.valueOf(UserLastOperation.CHOOSE_DEFAULT_PHOTO));
        list1.add(button3);

        keybordRows.add(list1);

        inlineKeyboardMarkup.setKeyboard(keybordRows);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMessage;
    }

    public static SendPhoto showProduct(Long message, Long categoryId, Long productId) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(message.toString());
        Product productNow = null;
        for (Product product : DataBase.products) {
            if (product.getProductId().equals(productId)) {
                productNow = product;
                break;
            }
        }
        if (!Objects.isNull(productNow)) {

            String text = "\uD83D\uDCCB  Product name: \n▶  " + productNow.getProductName() + "\n\n"
                    + "\uD83D\uDCB0 Cost: \n▶  " + productNow.getProductPrice() + " SUM \n\n" +
                    "✅ Select amount";
            if (productNow.getPhotoURL().contains("src")) {

                sendPhoto.setPhoto(new InputFile(new File(productNow.getPhotoURL())));

            } else if (productNow.getPhotoURL().equals(ConstantWord.NO_PHOTO_PATH)) {

                sendPhoto.setPhoto(new InputFile(new File(ConstantWord.NO_PHOTO_PATH)));
            } else {

                sendPhoto.setPhoto(new InputFile(productNow.getPhotoURL()));
            }

            sendPhoto.setCaption(text);
            sendPhoto.setParseMode(ParseMode.MARKDOWN);
            sendPhoto.setReplyMarkup(selectAmount(categoryId,productId));
        }

        return sendPhoto;
    }

    private static InlineKeyboardMarkup selectAmount(Long categoryId, Long productId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        for (int i = 1; i <= 9; i++) {
            List<InlineKeyboardButton> listKeyboar = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(String.valueOf(i));
            button.setCallbackData(ConstantWord.BUY + ConstantWord.PEREFIX + categoryId + ConstantWord.PEREFIX +
                    productId + ConstantWord.PEREFIX + i);
            listKeyboar.add(button);
            i++;
            InlineKeyboardButton button2 = new InlineKeyboardButton();
            button2.setText(String.valueOf(i));
            button2.setCallbackData(ConstantWord.BUY + ConstantWord.PEREFIX + categoryId + ConstantWord.PEREFIX +
                    productId + ConstantWord.PEREFIX + i);
            listKeyboar.add(button2);
            i++;
            InlineKeyboardButton button3 = new InlineKeyboardButton();
            button3.setText(String.valueOf(i));
            button3.setCallbackData(ConstantWord.BUY + ConstantWord.PEREFIX + categoryId + ConstantWord.PEREFIX +
                    productId + ConstantWord.PEREFIX + i);
            listKeyboar.add(button3);
            keyboardRows.add(listKeyboar);
        }
        InlineKeyboardButton button = new InlineKeyboardButton();
        List<InlineKeyboardButton> list = new ArrayList<>();
        button.setText("\uD83D\uDD19  BACK");
        button.setCallbackData(ConstantWord.PRODUCT + ConstantWord.PEREFIX + ConstantWord.BACK + ConstantWord.PEREFIX + categoryId);
        list.add(button);
        keyboardRows.add(list);
        inlineKeyboardMarkup.setKeyboard(keyboardRows);
        return inlineKeyboardMarkup;
    }


    public static SendPhoto showCart(Message message) {
        return CartProductService.showCart(message);

    }

    public static void addProduct(Message message, String data) {
        CartProductService.addProduct(message, data);
    }

    public static void misusProduct(Message message, Long productId) {

        for (CartProduct cartProduct : DataBase.cartProducts) {
            if (cartProduct.getCardId().equals(message.getChatId()) &&
                    cartProduct.getProductId().equals(productId)) {
                if (cartProduct.getProductAmount().equals(1)) {
                    DataBase.cartProducts.remove(cartProduct);
                } else {
                    cartProduct.setProductAmount(cartProduct.getProductAmount() - 1);
                }
                break;
            }
        }
        DataBase.writeToJson(ConstantWord.CART_PRODUCT);
        DataBase.refreshCounter();
        DataBase.writeToJson(ConstantWord.COUNTER_CATEGORY);
    }

    public static void PlusProduct(Message message, Long productId) {
        for (CartProduct cartProduct : DataBase.cartProducts) {
            if (cartProduct.getCardId().equals(message.getChatId()) &&
                    cartProduct.getProductId().equals(productId)) {

                cartProduct.setProductAmount(cartProduct.getProductAmount() + 1);

                break;
            }
        }
        DataBase.writeToJson(ConstantWord.CART_PRODUCT);
        DataBase.refreshCounter();
        DataBase.writeToJson(ConstantWord.COUNTER_CATEGORY);
    }

    public static void removeCart(Message message) {
        DataBase.cartProducts.removeIf(cartProduct -> cartProduct.getCardId().equals(message.getChatId()));
        DataBase.writeToJson(ConstantWord.CART_PRODUCT);
        DataBase.refreshCounter();
        DataBase.writeToJson(ConstantWord.COUNTER_CATEGORY);
    }

    public static SendMessage sayAboutLocation(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));

        sendMessage.setText("\uD83D\uDCCD Please enter your location");
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> rowList = new ArrayList<>();
        KeyboardButton button = new KeyboardButton();
        KeyboardRow row = new KeyboardRow();
        button.setText("\uD83D\uDEA9 Share Location ");
        button.setRequestLocation(true);
        row.add(button);
        KeyboardButton button1 = new KeyboardButton();

        button1.setText(ConstantWord.TEXT_BACK);
        row.add(button1);
        rowList.add(row);
        replyKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        return sendMessage;
    }

    ///User location jo'natsa uni qabul qilib olish
    public static SendMessage addLocationToDataBase(Message message) {
        Location location = message.getLocation();
        String Latitude = String.valueOf(location.getLatitude());
        String Longitude = String.valueOf(location.getLongitude());
        OfferAddress offerAddress = null;
        for (OfferAddress address : DataBase.OFFER_ADDRESSES) {
            if (address.getId().equals(message.getChatId())) {
                offerAddress = address;
                break;
            }
        }
        if (Objects.isNull(offerAddress)) {
            Geo geo = new Geo(Latitude, Longitude);
            OfferAddress offerAddress1 = new OfferAddress(message.getChatId(), geo);
            DataBase.OFFER_ADDRESSES.add(offerAddress1);
            DataBase.writeToJson(ConstantWord.USER_ADDRESS);
        } else {
            Geo geo = new Geo(Latitude, Longitude);
            offerAddress.setGeo(geo);
            DataBase.writeToJson(ConstantWord.USER_ADDRESS);
        }
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText("Enter the name of the country where the order will be accepted");
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        return sendMessage;
    }

    public static SendMessage setUserCountry(Message message, String text) {
        return UserAddressService.setCountry(message,text);
    }

    public static SendMessage setUserRegion(Message message, String text) {
        return UserAddressService.setRegion(message,text);
    }

    public static SendMessage setUserDistriction(Message message, String text) {
        return UserAddressService.setDistriction(message,text);
    }

    public static SendMessage setUserStreet(Message message, String text) {
        return UserAddressService.setStreet(message,text);
    }

    public static SendMessage setUserHomenumber(Message message, User currentUser, String text) {
        return UserAddressService.setHomeNumber(message,currentUser,text);
    }

    public static void makeOfferPdf(Message message) {
        DeliverCartService.makePdf(message);
    }

    public static void addDescription(Long productId) {
        boolean hasDescription=false;
        for (DescriptionProduct descriptionProduct : DataBase.descriptionProduct) {
            if(descriptionProduct.getProductId().equals(productId)){
                hasDescription=true;
                break;
            }
        }
        if(!hasDescription){
            DescriptionProduct descriptionProduct=new DescriptionProduct(productId);
            DataBase.descriptionProduct.add(descriptionProduct);
            DataBase.writeToJson(ConstantWord.DESCRIPTION_PRODUCT);
        }

    }

    public static void setDescription(Message message, String text) {
        for (DescriptionProduct descriptionProduct : DataBase.descriptionProduct) {
            if(descriptionProduct.getProductId().equals(Long.parseLong(DataBase.rememberForProduct.get(message.getChatId())))){
              descriptionProduct.setDiscription(text);
              DataBase.writeToJson(ConstantWord.DESCRIPTION_PRODUCT);
                break;
            }
        }
    }

    public static SendPhoto showOfferById(Message message) {
       SendPhoto sendPhoto=new SendPhoto();
       sendPhoto.setChatId(message.getChatId().toString());
       sendPhoto.setPhoto(new InputFile(new File(ConstantWord.DELIVER_CAR_PHOTO)));
       String result=" \uD83D\uDE31 You don't have any offer now";
        List<DeliverCart> deliverCarts = DataBase.deliverCart.stream().filter(deliverCart ->
                deliverCart.getCardId().equals(message.getChatId()) && !(deliverCart.isDeliveredToUser())).toList();
        InlineKeyboardMarkup inlineKeyboardMarkup=new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>>keybordButtons=new ArrayList<>();



        if(!deliverCarts.isEmpty()){
            Iterator<DeliverCart> iterator = deliverCarts.iterator();
            result="✅ Please select one";

            while (iterator.hasNext()){
                DeliverCart deliverCart = iterator.next();
               InlineKeyboardButton button=new InlineKeyboardButton();
                List<InlineKeyboardButton>list=new ArrayList<>();
                button.setText(" \uD83C\uDD94   "+deliverCart.getId());
                String pathPdf="";
                for (OfferPdf offerPdf : DataBase.offerPdf) {
                    if(offerPdf.getId().equals(deliverCart.getId())){
                      pathPdf=offerPdf.getPdfPath();
                      break;
                    }
                }
                button.setCallbackData(pathPdf);
                list.add(button);
                if(iterator.hasNext()){
                    DeliverCart deliverCart1 = iterator.next();
                    InlineKeyboardButton button1=new InlineKeyboardButton();
                    button1.setText("\uD83C\uDD94   "+deliverCart1.getId());
                    for (OfferPdf offerPdf : DataBase.offerPdf) {
                        if(offerPdf.getId().equals(deliverCart1.getId())){
                            pathPdf=offerPdf.getPdfPath();
                            break;
                        }
                    }
                    button1.setCallbackData(pathPdf);
                    list.add(button1);
                }
                keybordButtons.add(list);

            }

        }
        InlineKeyboardButton button=new InlineKeyboardButton();
        List<InlineKeyboardButton>list=new ArrayList<>();
        button=new InlineKeyboardButton();
        list=new ArrayList<>();
        button.setText("\uD83D\uDD19  Back");
        button.setCallbackData(ConstantWord.BACK);
        list.add(button);
        keybordButtons.add(list);
        inlineKeyboardMarkup.setKeyboard(keybordButtons);
        sendPhoto.setReplyMarkup(inlineKeyboardMarkup);
        sendPhoto.setCaption(result);

        return sendPhoto;
    }

    public static SendDocument sendPdfForUserByOfferId(Message message, String data) {
        SendDocument sendDocument=new SendDocument();
        sendDocument.setChatId(message.getChatId().toString());
        sendDocument.setDocument(new InputFile(new File(data)));
        return sendDocument;
    }

    public static SendMessage showCategoryMessageAdmin(Message message, int parentCategoryId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText("✅  Select category one");
        sendMessage.setReplyMarkup(categoryForAdmin(ConstantWord.CATEGORY, parentCategoryId));
        return sendMessage;
    }public static InlineKeyboardMarkup categoryForAdmin(String process, Integer Id) {
        List<Category> collect = DataBase.category.stream().filter(category ->
                category.getParentCategoryId().equals(Id)).toList();

        Iterator<Category> iterator = collect.iterator();
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        if (!collect.isEmpty()) {
            while (iterator.hasNext()) {
                Category cat = iterator.next();
                List<InlineKeyboardButton> list1 = new ArrayList<>();
                InlineKeyboardButton button1 = new InlineKeyboardButton();
                button1.setText(cat.getCategoryName());
                button1.setCallbackData(process + ConstantWord.PEREFIX + cat.getCategoryId());
                list1.add(button1);

                if (iterator.hasNext()) {
                    cat = iterator.next();
                    InlineKeyboardButton button2 = new InlineKeyboardButton();
                    button2.setText(cat.getCategoryName());
                    button2.setCallbackData(process + ConstantWord.PEREFIX + cat.getCategoryId());
                    list1.add(button2);
                }
                if (iterator.hasNext()) {
                    cat = iterator.next();
                    InlineKeyboardButton button3 = new InlineKeyboardButton();
                    button3.setText(cat.getCategoryName());
                    button3.setCallbackData(process + ConstantWord.PEREFIX + cat.getCategoryId());
                    list1.add(button3);
                }
                keyboardRows.add(list1);
            }
        } else {
            process = ConstantWord.PRODUCT;
            List<Product> productList = DataBase.products.stream().filter(product ->
                    product.getCategoryId().equals(Id)).toList();
            Iterator<Product> iterator1 = productList.iterator();
            while (iterator1.hasNext()) {
                Product product = iterator1.next();
                List<InlineKeyboardButton> list1 = new ArrayList<>();
                InlineKeyboardButton button1 = new InlineKeyboardButton();
                button1.setText(product.getProductName());
                button1.setCallbackData(process + ConstantWord.PEREFIX + Id + ConstantWord.PEREFIX + product.getProductId());
                list1.add(button1);

                if (iterator.hasNext()) {
                    product = iterator1.next();
                    InlineKeyboardButton button2 = new InlineKeyboardButton();
                    button2.setText(product.getProductName());
                    button2.setCallbackData(process + ConstantWord.PEREFIX + Id + ConstantWord.PEREFIX + product.getProductId());
                    list1.add(button2);
                }
                if (iterator.hasNext()) {
                    product = iterator1.next();
                    InlineKeyboardButton button3 = new InlineKeyboardButton();
                    button3.setText(product.getProductName());
                    button3.setCallbackData(process + ConstantWord.PEREFIX + Id + ConstantWord.PEREFIX + product.getProductId());
                    list1.add(button3);
                }
                keyboardRows.add(list1);
            }

        }
        if (Id != 0) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            List<InlineKeyboardButton> list = new ArrayList<>();
            button.setText("\uD83D\uDD19  BACK");
            button.setCallbackData(process + ConstantWord.PEREFIX + ConstantWord.BACK + ConstantWord.PEREFIX + Id);
            list.add(button);
            keyboardRows.add(list);
        }
        inlineKeyboardMarkup.setKeyboard(keyboardRows);
        return inlineKeyboardMarkup;
    }
    //Admin productni ko'rishi uchun
    public static SendPhoto showProductForAdmin(Long chatId, Long categoryId, Long productId) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(String.valueOf(chatId));
        Product productNow = null;
        for (Product product : DataBase.products) {
            if (product.getProductId().equals(productId)) {
                productNow = product;
                break;
            }
        }
        if (!Objects.isNull(productNow)) {

            String text = "\uD83D\uDCCB  Product name: \n▶  " + productNow.getProductName() + "\n\n"
                    + "\uD83D\uDCB0 Cost: \n▶  " + productNow.getProductPrice() + " SUM \n\n" +
                    "✅ Select amount";
            if (productNow.getPhotoURL().contains("src")) {

                sendPhoto.setPhoto(new InputFile(new File(productNow.getPhotoURL())));

            } else if (productNow.getPhotoURL().equals(ConstantWord.NO_PHOTO_PATH)) {

                sendPhoto.setPhoto(new InputFile(new File(ConstantWord.NO_PHOTO_PATH)));
            } else {

                sendPhoto.setPhoto(new InputFile(productNow.getPhotoURL()));
            }

            sendPhoto.setCaption(text);
            sendPhoto.setParseMode(ParseMode.MARKDOWN);
            InlineKeyboardMarkup inlineKeyboardMarkup=new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>>keyboardRows=new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            List<InlineKeyboardButton> list = new ArrayList<>();
            button.setText("\uD83D\uDD19  BACK");
            button.setCallbackData(ConstantWord.PRODUCT + ConstantWord.PEREFIX + ConstantWord.BACK + ConstantWord.PEREFIX + categoryId);
            list.add(button);
            keyboardRows.add(list);
            inlineKeyboardMarkup.setKeyboard(keyboardRows);
            sendPhoto.setReplyMarkup(inlineKeyboardMarkup);
        }
     return sendPhoto;
    }

    public static EditMessageText showCategoryForDeleteProduct(Message message, int parentCategoryId) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setMessageId(message.getMessageId());
        editMessageText.setChatId(message.getChatId().toString());
        editMessageText.setText(" ✅  Select category one");
        editMessageText.setParseMode(ParseMode.MARKDOWN);
        editMessageText.setReplyMarkup(category(ConstantWord.CATEGORY, parentCategoryId));
        return editMessageText;
    }


    public static SendPhoto showProductForDelete(Long chatId, Long categoryId, Long productId) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId.toString());
        Product productNow = null;
        for (Product product : DataBase.products) {
            if (product.getProductId().equals(productId)) {
                productNow = product;
                break;
            }
        }
        if (!Objects.isNull(productNow)) {

            String text = "\uD83D\uDCCB  Product name: \n▶  " + productNow.getProductName() + "\n\n"
                    + "\uD83D\uDCB0 Cost: \n▶  " + productNow.getProductPrice() + " SUM \n\n" +
                    "✅ Select amount";
            if (productNow.getPhotoURL().contains("src")) {

                sendPhoto.setPhoto(new InputFile(new File(productNow.getPhotoURL())));

            } else if (productNow.getPhotoURL().equals(ConstantWord.NO_PHOTO_PATH)) {

                sendPhoto.setPhoto(new InputFile(new File(ConstantWord.NO_PHOTO_PATH)));
            } else {

                sendPhoto.setPhoto(new InputFile(productNow.getPhotoURL()));
            }

            sendPhoto.setCaption(text);
            sendPhoto.setParseMode(ParseMode.MARKDOWN);
            InlineKeyboardMarkup inlineKeyboardMarkup=new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton >>keyboardRows=new ArrayList<>();
            InlineKeyboardButton button=new InlineKeyboardButton();
            List<InlineKeyboardButton>list=new ArrayList<>();
            button.setText("❌ Delete this product");
            button.setCallbackData(MyCallBackQuary.DELETE_PRODUCT+ConstantWord.PEREFIX+
                    productId);
            list.add(button);
            InlineKeyboardButton button1 = new InlineKeyboardButton();
            List<InlineKeyboardButton>list2=new ArrayList<>();
            button1.setText("\uD83D\uDD19  BACK");
            button1.setCallbackData(ConstantWord.PRODUCT + ConstantWord.PEREFIX + ConstantWord.BACK + ConstantWord.PEREFIX + categoryId);
            list2.add(button1);
            keyboardRows.add(list);
            keyboardRows.add(list2);
            inlineKeyboardMarkup.setKeyboard(keyboardRows);
            sendPhoto.setReplyMarkup(inlineKeyboardMarkup);
        }

        return sendPhoto;
    }

    public static SendMessage showMenuForDeliver(Message message) {
     return DeliverService.ShowMenu(message);
    }

    public static SendPhoto showNewOffer(Message message) {
        return DeliverService.showNewOffer(message);
    }

    public static SendMessage SelectOperationDeliver(Message message, long id) {
        return DeliverService.selectOperation(message,id);
    }

    public static SendDocument sendPdf(Message message, long id) {
        String path="";
        for (OfferPdf offerPdf : DataBase.offerPdf) {
          if(offerPdf.getId().equals(id)){
                path= offerPdf.getPdfPath();
                break;
            }
        }
        return sendPdfForUserByOfferId(message,path);
    }

    public static SendPhoto showNewOfferPlus(Message message) {
        return DeliverService.showNewOfferPlus(message);
    }

    public static SendPhoto showNewOfferMinus(Message message) {
        return DeliverService.showNewOfferMinus(message);
    }

    public static SendPhoto showOldOffer(Message message) {
        return DeliverService.showOldOffer(message);
    }

    public static SendMessage SelectOperationDeliverForOld(Message message, long id) {
        return DeliverService.selectOperationForOld(message,id);
    }

    public static SendPhoto showOldOfferMinus(Message message) {
        return DeliverService.oldMinus(message);
    }

    public static SendPhoto showOldOfferPlus(Message message) {
        return DeliverService.oldPlus(message);
    }
///Edit product
    public static SendMessage editProduct(Message message, int categoryId, Long productId) {
        SendMessage sendMassage=new SendMessage();
        sendMassage.setChatId(message.getChatId().toString());
        sendMassage.setText("Select one");
        InlineKeyboardMarkup inlineKeyboardMarkup=new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>>keyboardRows=new ArrayList<>();
        List<InlineKeyboardButton>list=new ArrayList<>();
        InlineKeyboardButton button=new InlineKeyboardButton();
        button.setText("\uD83D\uDD01  Name");
        button.setCallbackData(ConstantWord.EDIT_NAME+ConstantWord.PEREFIX+productId);
        list.add(button);

        InlineKeyboardButton button1=new InlineKeyboardButton();
        button1.setText("\uD83D\uDD01 Price");
        button1.setCallbackData(ConstantWord.EDIT_PRICE+ConstantWord.PEREFIX+productId);
        list.add(button1);

        InlineKeyboardButton button21=new InlineKeyboardButton();
        button21.setText("🖼️ Photo ");
        button21.setCallbackData(ConstantWord.EDIT_PHOTO);
        list.add(button21);

        keyboardRows.add(list);

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        List<InlineKeyboardButton>list2=new ArrayList<>();
        button2.setText("\uD83D\uDD19  BACK");
        button2.setCallbackData(ConstantWord.PRODUCT + ConstantWord.PEREFIX + ConstantWord.BACK + ConstantWord.PEREFIX + categoryId);
        list2.add(button2);
        keyboardRows.add(list2);
        inlineKeyboardMarkup.setKeyboard(keyboardRows);
        sendMassage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMassage;
    }

    public static SendMessage menuForChangeUser(Message message, User changeUser) {
        SendMessage sendMessage=new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText("Select one");
        InlineKeyboardMarkup inlineKeyboardMarkup=new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>>keyboardRows=new ArrayList<>();
        List<InlineKeyboardButton>list=new ArrayList<>();
        InlineKeyboardButton button=new InlineKeyboardButton();
        if(changeUser.isActive()){
            button.setText("Active--> false");
            button.setCallbackData(ConstantWord.OPERATION+ConstantWord.PEREFIX+
                    ConstantWord.FALSE+ConstantWord.PEREFIX+changeUser.getPhoneNumber());
            list.add(button);
        }else {
            button.setText("Active--> true");
            button.setCallbackData(ConstantWord.OPERATION+ConstantWord.PEREFIX+
                    ConstantWord.TRUE+ConstantWord.PEREFIX+changeUser.getPhoneNumber());
            list.add(button);
        }
        List<UserRole> userRoles = Arrays.stream(UserRole.values()).toList();
        for (UserRole userRole : userRoles) {
            if(!changeUser.getRole().contains(userRole)){
                InlineKeyboardButton button1=new InlineKeyboardButton();
                button1.setText(userRole.toString());
                button1.setCallbackData(ConstantWord.OPERATION+ConstantWord.PEREFIX+userRole+
                        ConstantWord.PEREFIX+changeUser.getPhoneNumber());
                list.add(button1);
            }
        }
        List<InlineKeyboardButton>list2=new ArrayList<>();
        InlineKeyboardButton button2=new InlineKeyboardButton();
        button2.setText("Back");
        button2.setCallbackData(ConstantWord.OPERATION+ConstantWord.PEREFIX+ConstantWord.BACK);
        list2.add(button2);
        keyboardRows.add(list);
        keyboardRows.add(list2);
        inlineKeyboardMarkup.setKeyboard(keyboardRows);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMessage;
    }

    public static SendMessage changeCtegoryMenu(Message message) {
        SendMessage sendMessage=new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText("✅  Select one");
        InlineKeyboardMarkup inlineKeyboardMarkup=new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>>keyboardRows=new ArrayList<>();
        List<InlineKeyboardButton>list=new ArrayList<>();
        InlineKeyboardButton button=new InlineKeyboardButton();
        button.setText("️✍️ Add Category");
        button.setCallbackData(MyCallBackQuary.ADD_CATEGORY);
        list.add(button);

        InlineKeyboardButton button1=new InlineKeyboardButton();
        button1.setText("❌   Delete Category");
        button1.setCallbackData(MyCallBackQuary.DELETE_CATEGORY);
        list.add(button1);
        keyboardRows.add(list);
        List<InlineKeyboardButton>list1=new ArrayList<>();
        InlineKeyboardButton button2=new InlineKeyboardButton();
        button2.setText("✏️ Edit Name");
        button2.setCallbackData(MyCallBackQuary.EDIT_CATEGORY_NAME);
        list1.add(button2);
        keyboardRows.add(list1);
        inlineKeyboardMarkup.setKeyboard(keyboardRows);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMessage;
    }

    public static EditMessageText showCategoryForDelete(Message message, int Id) {
        EditMessageText editMessageText=new EditMessageText();
        editMessageText.setChatId(message.getChatId().toString());
        editMessageText.setMessageId(message.getMessageId());
        editMessageText.setText(" Select one");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<Category> collect = DataBase.category.stream().filter(category ->
                category.getParentCategoryId().equals(Id)).toList();
        if (collect.size() != 0) {
            Iterator<Category> iterator = collect.iterator();
            while (iterator.hasNext()) {
                Category cat = iterator.next();
                List<InlineKeyboardButton> list1 = new ArrayList<>();
                InlineKeyboardButton button1 = new InlineKeyboardButton();
                button1.setText(cat.getCategoryName());
                button1.setCallbackData(ConstantWord.CATEGORY+ ConstantWord.PEREFIX + cat.getCategoryId());
                list1.add(button1);

                if (iterator.hasNext()) {
                    cat = iterator.next();
                    InlineKeyboardButton button2 = new InlineKeyboardButton();
                    button2.setText(cat.getCategoryName());
                    button2.setCallbackData(ConstantWord.CATEGORY+ ConstantWord.PEREFIX + cat.getCategoryId());
                    list1.add(button2);
                }
                if (iterator.hasNext()) {
                    cat = iterator.next();
                    InlineKeyboardButton button3 = new InlineKeyboardButton();
                    button3.setText(cat.getCategoryName());
                    button3.setCallbackData(ConstantWord.CATEGORY+ ConstantWord.PEREFIX + cat.getCategoryId());
                    list1.add(button3);
                }
                keyboardRows.add(list1);
            }
        } else {
            List<InlineKeyboardButton> list1 = new ArrayList<>();
            InlineKeyboardButton button1 = new InlineKeyboardButton();
            button1.setText("❌  Delete this product");
            button1.setCallbackData(MyCallBackQuary.DELETE_CATEGORY +ConstantWord.PEREFIX + Id);
            list1.add(button1);
            keyboardRows.add(list1);
        }
        inlineKeyboardMarkup.setKeyboard(keyboardRows);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);
        return editMessageText;
    }

    public static void deleteCategory(int id) {
        DataBase.products.removeIf(product -> product.getCategoryId().equals(id));
        DataBase.category.removeIf(category -> category.getCategoryId().equals(id));
        DataBase.writeToJson(ConstantWord.PRODUCT);
        DataBase.writeToJson(ConstantWord.CATEGORY);
    }

    public static EditMessageText showCategoryForEdit(Message message, int Id) {
        EditMessageText editMessageText=new EditMessageText();
        editMessageText.setChatId(message.getChatId().toString());
        editMessageText.setMessageId(message.getMessageId());
        editMessageText.setText(" Select one");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<Category> collect = DataBase.category.stream().filter(category ->
                category.getParentCategoryId().equals(Id)).toList();
        if (collect.size() != 0) {
            Iterator<Category> iterator = collect.iterator();
            while (iterator.hasNext()) {
                Category cat = iterator.next();
                List<InlineKeyboardButton> list1 = new ArrayList<>();
                InlineKeyboardButton button1 = new InlineKeyboardButton();
                button1.setText(cat.getCategoryName());
                button1.setCallbackData(ConstantWord.CATEGORY+ ConstantWord.PEREFIX + cat.getCategoryId());
                list1.add(button1);

                if (iterator.hasNext()) {
                    cat = iterator.next();
                    InlineKeyboardButton button2 = new InlineKeyboardButton();
                    button2.setText(cat.getCategoryName());
                    button2.setCallbackData(ConstantWord.CATEGORY+ ConstantWord.PEREFIX + cat.getCategoryId());
                    list1.add(button2);
                }
                if (iterator.hasNext()) {
                    cat = iterator.next();
                    InlineKeyboardButton button3 = new InlineKeyboardButton();
                    button3.setText(cat.getCategoryName());
                    button3.setCallbackData(ConstantWord.CATEGORY+ ConstantWord.PEREFIX + cat.getCategoryId());
                    list1.add(button3);
                }
                keyboardRows.add(list1);
            }
        } else {
            List<InlineKeyboardButton> list1 = new ArrayList<>();
            InlineKeyboardButton button1 = new InlineKeyboardButton();
            button1.setText("Edit this category");
            button1.setCallbackData(MyCallBackQuary.EDIT_CATEGORY_NAME +ConstantWord.PEREFIX + Id);
            list1.add(button1);
            keyboardRows.add(list1);
        }
        inlineKeyboardMarkup.setKeyboard(keyboardRows);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);
        return editMessageText;
    }

    public static void editCategoryName(String text, int parseInt) {
        for (Category category : DataBase.category) {
            if(category.getCategoryId().equals(parseInt)){
                category.setCategoryName(text);
                break;
            }
        }
        DataBase.writeToJson(ConstantWord.CATEGORY);
    }

    public static SendDocument sendDescription(Message message) {
        return DeliverCartService.makeDescription(message);
    }

    public static SendMessage editPhoto(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(" \uD83D\uDDBC Add photo this product\n" +
                "✅ Select one");
        sendMessage.setParseMode(ParseMode.MARKDOWN);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keybordRows = new ArrayList<>();
        List<InlineKeyboardButton> list = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("\uD83D\uDDBC Photo");
        button1.setCallbackData(MyCallBackQuary.NEW_PHOTO);
        list.add(button1);
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("\uD83C\uDF10 URL");
        button2.setCallbackData(MyCallBackQuary.NEW_URL);
        list.add(button2);
        keybordRows.add(list);

        List<InlineKeyboardButton> list1 = new ArrayList<>();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText(" \uD83D\uDEAB No photo");
        button3.setCallbackData(MyCallBackQuary.NO_PHOTO);
        list1.add(button3);

        keybordRows.add(list1);

        inlineKeyboardMarkup.setKeyboard(keybordRows);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMessage;
    }

    public static void setNoPhotoToProduct(long productId) {
        String oldPhto="";
        for (Product product : DataBase.products) {
            if(product.getProductId().equals(productId)){
                oldPhto=product.getPhotoURL();
                product.setPhotoURL(ConstantWord.NO_PHOTO_PATH);
                break;
            }
        }
        DataBase.writeToJson(ConstantWord.PRODUCT);
        File file=new File(oldPhto);
        if(!oldPhto.isEmpty()){
            file.delete();
        }
    }

    public static void setNewUrlToPrduct(String text, long productId) {
        for (Product product : DataBase.products) {
            if(product.getProductId().equals(productId)){
                product.setPhotoURL(text);
                break;
            }
        }
        DataBase.writeToJson(ConstantWord.PRODUCT);
    }

    public static SendMessage showMenuForDeliverForAdmin(Message message) {
        return DeliverService.ShowMenudeliverForAdmin(message);
    }
}

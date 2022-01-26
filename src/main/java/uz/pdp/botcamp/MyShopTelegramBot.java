package uz.pdp.botcamp;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.botcamp.Bot.BotKey;
import uz.pdp.botcamp.Bot.BotService;
import uz.pdp.botcamp.constants.*;
import uz.pdp.botcamp.model.*;
import uz.pdp.botcamp.model.User;
import uz.pdp.botcamp.repository.DataBase;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;


public class MyShopTelegramBot extends TelegramLongPollingBot {
    @Override
    public void onUpdateReceived(Update update) {
        User currentUser = null;
        if (update.hasMessage()) {
            SendMessage sendMessage = new SendMessage();
            SendPhoto sendPhoto;
            Message message = update.getMessage();
            if (message.hasText()) {
                String text = message.getText();
                System.out.println("text = " + text);

                if (text.equals(ConstantWord.START)) {
                    sendMessage = BotService.askContact(message);
                } else {
                    for (User user : DataBase.users) {
                        if (user.getUserId().equals(message.getChatId())) {
                            currentUser = user;
                            break;
                        }
                    }

                    assert currentUser != null;
                    if (currentUser.isActive()) {
                        if (currentUser.getRole().equals(List.of(UserRole.USER))) {
                            //Show cart user uchun ham admin uchun ham
                            if (text.equals(MenuConstant.SHOW_CART)) {
                                currentUser.setLastOperation(UserLastOperation.SHOW_CART);
                                DataBase.writeToJson(ConstantWord.USER);
                                sendPhoto = BotService.showCart(message);
                                try {
                                    execute(sendPhoto);
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                            }
                            //Admin va User uchun categoriya va productlarni ko'rishi uchun
                            if (text.equals(MenuConstant.SHOW_CATEGORY)) {
                                currentUser.setLastOperation(UserLastOperation.SHOW_CATEGORY);
                                sendMessage = BotService.showCategoryMessage(message, 0);
                            }
                            if (text.equals(MenuConstant.SHOW_OFFER)) {
                                BotService.makeOfferPdf(message);
                                currentUser.setLastOperation(UserLastOperation.SHOW_OFFER);
                                DataBase.writeToJson(ConstantWord.USER);
                                sendPhoto = BotService.showOfferById(message);
                                try {
                                    execute(sendPhoto);
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                            }
                            //Location bo'limidan ortga qaytish
                            if (text.equals(ConstantWord.TEXT_BACK)) {
                                sendMessage = BotService.showMenyuForUser(message);
                            }
                            ///Malumot jo'natish
                            if (currentUser.getLastOperation().equals(UserLastOperation.SEND_COUNTRY)) {
                                sendMessage = BotService.setUserCountry(message, text);
                                currentUser.setLastOperation(UserLastOperation.SEND_REGION);
                                DataBase.writeToJson(ConstantWord.USER);
                            } else
                                ///set region
                                if (currentUser.getLastOperation().equals(UserLastOperation.SEND_REGION)) {
                                    sendMessage = BotService.setUserRegion(message, text);
                                    currentUser.setLastOperation(UserLastOperation.SEND_DISTRICT);
                                    DataBase.writeToJson(ConstantWord.USER);

                                } else
                                    ///set distriction
                                    if (currentUser.getLastOperation().equals(UserLastOperation.SEND_DISTRICT)) {
                                        sendMessage = BotService.setUserDistriction(message, text);
                                        currentUser.setLastOperation(UserLastOperation.SEND_STREET);
                                        DataBase.writeToJson(ConstantWord.USER);

                                    } else
                                        ///set street
                                        if (currentUser.getLastOperation().equals(UserLastOperation.SEND_STREET)) {
                                            sendMessage = BotService.setUserStreet(message, text);
                                            currentUser.setLastOperation(UserLastOperation.SEND_HOMENUMBER);
                                            DataBase.writeToJson(ConstantWord.USER);

                                        } else
                                            ///set homeNmber
                                            if (currentUser.getLastOperation().equals(UserLastOperation.SEND_HOMENUMBER)) {
                                                sendMessage = BotService.setUserHomenumber(message, currentUser, text);
                                                currentUser.setLastOperation(UserLastOperation.SELECT_MENU);
                                                DataBase.writeToJson(ConstantWord.USER);
                                                try {
                                                    execute(sendMessage);
                                                } catch (TelegramApiException e) {
                                                    e.printStackTrace();
                                                }

                                            }
                            ///jarayonni yakunlash
                            if (currentUser.getLastOperation().equals(UserLastOperation.SELECT_MENU)) {
                                BotService.makeOfferPdf(message);

                                sendMessage = BotService.showMenyuForUser(message);

                            }

                        }
                        //Admin operations
                        if (currentUser.getRole().equals(List.of(UserRole.ADMIN))) {

                            if (currentUser.getLastOperation().equals(UserLastOperation.NEW_URL)) {
                                long productId = Long.parseLong(DataBase.rememberForProduct.get(message.getChatId()));
                                BotService.setNewUrlToPrduct(text, productId);
                                sendMessage = BotService.sayAnyThing(message, "Add new Url");
                                currentUser.setLastOperation(UserLastOperation.SELECT_MENU);
                                DataBase.writeToJson(ConstantWord.USER);
                            }

                            if (text.equals(MenuConstant.CHANGE_USER)) {
                                currentUser.setLastOperation(UserLastOperation.ASK_CODE);
                                DataBase.writeToJson(ConstantWord.USER);
                                sendMessage = BotService.sayAnyThing(message, "Please enter secret password");
                            } else if (currentUser.getLastOperation().equals(UserLastOperation.ASK_CODE)) {
                                if (text.equals(BotKey.CODE_FOR_CHANGE)) {
                                    sendMessage = BotService.sayAnyThing(message, "Please enter number");
                                    currentUser.setLastOperation(UserLastOperation.ASK_PHONENUMBER_FOR);
                                    DataBase.writeToJson(ConstantWord.USER);
                                } else {
                                    sendMessage = BotService.sayAnyThing(message, "Wrong password");
                                }
                            } else if (currentUser.getLastOperation().equals(UserLastOperation.ASK_PHONENUMBER_FOR)) {
                                User changeUser = null;
                                for (User user : DataBase.users) {
                                    if (user.getPhoneNumber().equals(text)) {
                                        changeUser = user;
                                        break;
                                    }
                                }
                                if (!Objects.isNull(changeUser)) {
                                    sendMessage = BotService.menuForChangeUser(message, changeUser);
                                    currentUser.setLastOperation(UserLastOperation.CHAGE_USER_OPERATION);
                                    DataBase.writeToJson(ConstantWord.USER);
                                } else {
                                    sendMessage = BotService.sayAnyThing(message, "Our server doesn't have this number");
                                }
                            }


                            if (text.equals(MenuConstant.SHOW_CART_ALL)) {
                                currentUser.setLastOperation(UserLastOperation.SELECT_MENU);
                                sendMessage = BotService.showMenuForDeliverForAdmin(message);
                                DataBase.writeToJson(ConstantWord.USER);
                            } else if (text.equals(ConstantWord.NEW_OFFER)) {
                                currentUser.setLastOperation(UserLastOperation.SHOW_NEW_OFER);
                                DataBase.writeToJson(ConstantWord.USER);
                                sendPhoto = BotService.showNewOffer(message);
                                try {
                                    execute(sendPhoto);
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                            } else if (text.equals(ConstantWord.OLD_OFFER)) {
                                sendPhoto = BotService.showOldOffer(message);
                                try {
                                    execute(sendPhoto);
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                            } else if (text.equals(ConstantWord.BACK_MENU)) {
                                sendMessage = BotService.showMenyuForAdmin(message);
                            }

                            //Show categoriya va productlarni ko'rishi uchun
                            if (text.equals(MenuConstant.SHOW_CATEGORY)) {
                                currentUser.setLastOperation(UserLastOperation.SHOW_CATEGORY);
                                sendMessage = BotService.showCategoryMessageAdmin(message, 0);
                            } else if (text.equals(MenuConstant.CHANGE_PRODUCT)) {

                                currentUser.setLastOperation(UserLastOperation.OPERATION_WITH_PRODUCT);
                                DataBase.writeToJson(ConstantWord.USER);
                                sendMessage = BotService.chooseProductOperation(message);
                            } else


                                //change category
                                if (text.equals(MenuConstant.CHANGE_CATEGORY)) {
                                    sendMessage = BotService.changeCtegoryMenu(message);
                                    currentUser.setLastOperation(UserLastOperation.CREATE_CATEGORY);
                                    DataBase.writeToJson(ConstantWord.USER);
                                }
                            if (currentUser.getLastOperation().equals(UserLastOperation.ADD_CATEGORY)) {
                                sendMessage = BotService.showCategoryForCreate(message, text);
                            } else if (currentUser.getLastOperation().equals(UserLastOperation.EDIT_CATEGORY_NAME)) {
                                BotService.editCategoryName(text, Integer.parseInt(DataBase.rememberForProduct.get(message.getChatId())));
                                sendMessage = BotService.sayAnyThing(message, "Edit name");
                                currentUser.setLastOperation(UserLastOperation.SELECT_MENU);
                                DataBase.writeToJson(ConstantWord.USER);
                            }

                            if (currentUser.getLastOperation().equals(UserLastOperation.ASK_PRODUCT_NAME)) {
                                String lastInfo = DataBase.rememberForProduct.get(message.getChatId());
                                lastInfo += ConstantWord.PEREFIX + text;
                                DataBase.rememberForProduct.put(message.getChatId(), lastInfo);
                                currentUser.setLastOperation(UserLastOperation.ASK_PRODUCT_PRICE);
                                DataBase.writeToJson(ConstantWord.USER);
                                sendMessage = BotService.sayAnyThing(message, "Enter product price");

                            } else if (currentUser.getLastOperation().equals(UserLastOperation.ASK_PRODUCT_PRICE)) {
                                String lastInfo = DataBase.rememberForProduct.get(message.getChatId());
                                lastInfo += ConstantWord.PEREFIX + text;
                                DataBase.rememberForProduct.put(message.getChatId(), lastInfo);
                                currentUser.setLastOperation(UserLastOperation.ASK_PRODUCT_PHOTO);
                                DataBase.writeToJson(ConstantWord.USER);
                                sendMessage = BotService.choosePhotoWay(message);
                            } else
                                ///Url ni productga qo'shish
                                if (currentUser.getLastOperation().equals(UserLastOperation.ASK_PRODUCT_URL)) {
                                    String info = DataBase.rememberForProduct.get(message.getChatId());
                                    String[] split = info.split("/");
                                    int categoryId = Integer.parseInt(split[2]);
                                    String productName = split[3];
                                    String price = split[4];
                                    while (price.contains(" ")) {
                                        price = price.replaceAll(" ", "");
                                    }
                                    double productPrice = Double.parseDouble(price);
                                    Product product = new Product(productName, productPrice, categoryId, text);
                                    DataBase.products.add(product);

                                    DataBase.refreshCounter();
                                    DataBase.writeToJson(ConstantWord.PRODUCT);
                                    DataBase.refreshCounter();
                                    DataBase.writeToJson(ConstantWord.COUNTER_CATEGORY);
                                }

                            ///Description ni ushlab olib qo'shib qo'yish
                            if (currentUser.getLastOperation().equals(UserLastOperation.WRITE_DESCRIPTION)) {
                                BotService.setDescription(message, text);
                                currentUser.setLastOperation(UserLastOperation.SELECT_MENU);
                                DataBase.writeToJson(ConstantWord.USER);
                                sendMessage = BotService.sayAnyThing(message, "\uD83D\uDCDD  Description add");
                            }

                            //edit name
                            if (currentUser.getLastOperation().equals(UserLastOperation.EDIT_NAME)) {
                                Long productId = Long.parseLong(DataBase.rememberForProduct.get(message.getChatId()));
                                for (Product product : DataBase.products) {
                                    if (product.getProductId().equals(productId)) {
                                        product.setProductName(text);
                                        break;
                                    }
                                }
                                DataBase.writeToJson(ConstantWord.PRODUCT);
                                try {
                                    execute(BotService.sayAnyThing(message, "✅  Add new name"));
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                            } else
                                //edit price
                                if (currentUser.getLastOperation().equals(UserLastOperation.EDIT_PRICE)) {
                                    String newPrrice = text;
                                    while (newPrrice.contains(" ")) {
                                        newPrrice = newPrrice.replaceAll(" ", "");
                                    }
                                    if (Pattern.matches("[0-9]*", newPrrice)) {
                                        Long productId = Long.parseLong(DataBase.rememberForProduct.get(message.getChatId()));
                                        for (Product product : DataBase.products) {
                                            if (product.getProductId().equals(productId)) {
                                                product.setProductPrice(Double.parseDouble(newPrrice));
                                                break;
                                            }
                                        }
                                        DataBase.writeToJson(ConstantWord.PRODUCT);
                                        try {
                                            execute(BotService.sayAnyThing(message, "✅  Add new name"));
                                        } catch (TelegramApiException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        try {
                                            execute(BotService.sayAnyThing(message, " Price invalid"));

                                        } catch (TelegramApiException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                        }
                        //Deliver operations
                        if (currentUser.getRole().equals(List.of(UserRole.DELIVER))) {
                            if (text.equals(ConstantWord.NEW_OFFER)) {
                                currentUser.setLastOperation(UserLastOperation.SHOW_NEW_OFER);
                                DataBase.writeToJson(ConstantWord.USER);
                                sendPhoto = BotService.showNewOffer(message);
                                try {
                                    execute(sendPhoto);
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                            } else if (text.equals(ConstantWord.OLD_OFFER)) {
                                currentUser.setLastOperation(UserLastOperation.SHOW_OLD_OFER);
                                DataBase.writeToJson(ConstantWord.USER);
                                sendPhoto = BotService.showOldOffer(message);
                                try {
                                    execute(sendPhoto);
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    } else {
                        sendMessage = BotService.sayAnyThing(message, "😱 Your account is blocked by admin \n\n" +
                                "📲  Call center +998997834961");
                    }
                }
            }
            if (message.hasContact()) {
                Contact contact = message.getContact();
                currentUser = BotService.registerUser(message, contact);
                if (currentUser.getRole().contains(UserRole.ADMIN)) {
                    currentUser.setLastOperation(UserLastOperation.SELECT_MENU);
                    sendMessage = BotService.showMenyuForAdmin(message);
                }
                if (currentUser.getRole().contains(UserRole.USER)) {
                    currentUser.setLastOperation(UserLastOperation.SELECT_MENU);
                    sendMessage = BotService.showMenyuForUser(message);
                }
                if (currentUser.getRole().contains(UserRole.DELIVER)) {
                    currentUser.setLastOperation(UserLastOperation.SELECT_MENU);
                    sendMessage = BotService.showMenuForDeliver(message);

                }
                DataBase.writeToJson(ConstantWord.USER);

            }
            if (message.hasDocument() || message.hasPhoto()) {

                for (User user : DataBase.users) {
                    if (user.getUserId().equals(message.getChatId())) {
                        currentUser = user;
                        break;
                    }
                }
                assert currentUser != null;
                if (currentUser.getLastOperation().equals(UserLastOperation.ASK_PRODUCT_PHOTO)) {
                    String info = DataBase.rememberForProduct.get(message.getChatId());
                    String[] split = info.split("/");
                    int categoryId = Integer.parseInt(split[2]);
                    String productName = split[3];
                    String price = split[4];
                    while (price.contains(" ")) {
                        price = price.replaceAll(" ", "");
                    }
                    double productPrice = Double.parseDouble(price);

                    //rasmni qabul qilib olish
                    Document document = new Document();
                    document.setFileId(message.getDocument().getFileId());
                    document.setFileName(message.getDocument().getFileName());
                    document.setMimeType(message.getDocument().getMimeType());
                    document.setFileSize(message.getDocument().getFileSize());

                    GetFile getFile = new GetFile();
                    getFile.setFileId(document.getFileId());
                    String newDocumentPath = ConstantWord.IMAGE_PATH + document.getFileId() + message.getDocument().getFileName();
                    try {
                        File file = execute(getFile);
                        downloadFile(file, new java.io.File(newDocumentPath));
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }

                    Product product = new Product(productName, productPrice, categoryId, newDocumentPath);
                    DataBase.products.add(product);
                    DataBase.refreshCounter();
                    DataBase.writeToJson(ConstantWord.PRODUCT);
                    DataBase.writeToJson(ConstantWord.COUNTER_CATEGORY);
                    sendMessage = BotService.sayAnyThing(message, "\uD83D\uDDE3  Product add");
                } else if (currentUser.getLastOperation().equals(UserLastOperation.NEW_PHOTO)) {
                    String oldOhotopath = "";
                    //rasmni qabul qilib olish
                    Document document = new Document();
                    document.setFileId(message.getDocument().getFileId());
                    document.setFileName(message.getDocument().getFileName());
                    document.setMimeType(message.getDocument().getMimeType());
                    document.setFileSize(message.getDocument().getFileSize());

                    GetFile getFile = new GetFile();
                    getFile.setFileId(document.getFileId());
                    String newDocumentPath = ConstantWord.IMAGE_PATH + document.getFileId() + message.getDocument().getFileName();
                    try {
                        File file = execute(getFile);
                        downloadFile(file, new java.io.File(newDocumentPath));
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    long productId = Long.parseLong(DataBase.rememberForProduct.get(message.getChatId()));
                    for (Product product : DataBase.products) {
                        if (product.getProductId().equals(productId)) {
                            oldOhotopath = product.getPhotoURL();
                            product.setPhotoURL(newDocumentPath);
                            break;
                        }
                    }
                    currentUser.setLastOperation(UserLastOperation.SELECT_MENU);
                    DataBase.writeToJson(ConstantWord.USER);
                    sendMessage = BotService.sayAnyThing(message, "Add new product");
                    java.io.File file = new java.io.File(oldOhotopath);
                    if (!oldOhotopath.isEmpty()) {
                        file.delete();
                    }
                }

            }
            if (message.hasLocation()) {

                for (User user : DataBase.users) {
                    if (user.getUserId().equals(message.getChatId())) {
                        currentUser = user;
                        break;
                    }
                }
                assert currentUser != null;
                if (currentUser.getLastOperation().equals(UserLastOperation.SEND_LOCATION)) {
                    sendMessage = BotService.addLocationToDataBase(message);
                    currentUser.setLastOperation(UserLastOperation.SEND_COUNTRY);
                    DataBase.writeToJson(ConstantWord.USER);
                    DataBase.writeToJson(ConstantWord.PRODUCT);
                }
            }
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        if (update.hasCallbackQuery()) {
            EditMessageText editMessageText = new EditMessageText();
            SendMessage sendMessage = new SendMessage();
            SendPhoto sendPhoto;
            Message message = update.getCallbackQuery().getMessage();
            String data = update.getCallbackQuery().getData();
            for (User user : DataBase.users) {
                if (user.getUserId().equals(message.getChatId())) {
                    currentUser = user;
                    break;
                }
            }
            System.out.println("data = " + data);

            assert currentUser != null;
            if (currentUser.isActive()) {
                if (currentUser.getRole().equals(List.of(UserRole.USER))) {

                    if (data.startsWith(MyCallBackQuary.SHOW_DESCRIPTION)) {
                        SendDocument sendDocument = new SendDocument();
                        sendDocument = BotService.sendDescription(message);
                        try {
                            execute(BotService.sayAnyThing(message, "⌛  Please wait sending file..."));
                            execute(sendDocument);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        String path = DataBase.rememberForProduct.get(message.getChatId());
                        java.io.File file = new java.io.File(path);
                        file.delete();
                    }
                    // Admin productni ko'rishi uchun
                    if (data.startsWith(ConstantWord.PRODUCT) && !data.contains(ConstantWord.BACK) && !currentUser.getLastOperation().equals(UserLastOperation.ADD_DESCRIPTION)) {
                        currentUser.setLastOperation(UserLastOperation.SHOW_PRODUCT);
                        DataBase.writeToJson(ConstantWord.USER);
                        Long productId = Long.parseLong(data.split("/")[2]);
                        Long categoryId = Long.parseLong(data.split("/")[1]);
                        sendPhoto = BotService.showProduct(message.getChatId(), categoryId, productId);
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(sendPhoto);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                    //show offerdagi amallar
                    if (currentUser.getLastOperation().equals(UserLastOperation.SHOW_OFFER)) {
                        if (data.equals(ConstantWord.BACK)) {
                            sendMessage = BotService.showMenyuForUser(message);
                            try {
                                execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                                execute(sendMessage);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        } else {
                            SendDocument sendDocument;
                            sendDocument = BotService.sendPdfForUserByOfferId(message, data);
                            try {
                                execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                                execute(BotService.sayAnyThing(message, " ⏲ Please waiting sending your file..."));
                                execute(sendDocument);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        }
                        currentUser.setLastOperation(UserLastOperation.SELECT_MENU);
                        DataBase.writeToJson(ConstantWord.USER);
                    }
                    if (data.startsWith(ConstantWord.CATEGORY)) {
                        if (currentUser.getLastOperation().equals(UserLastOperation.SHOW_CATEGORY)) {
                            String[] split = data.split("/");
                            int n = split.length;
                            if (n == 2) {
                                editMessageText = BotService.showCategory(message, Integer.parseInt(split[1]));
                            }
                        }
                    }
                    //Kiritilgan miqdordagi productni karzinkaga qo'shish
                    if (data.startsWith(ConstantWord.BUY)) {
                        BotService.addProduct(message, data);
                        sendPhoto = BotService.showCart(message);
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(sendPhoto);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                    }//show cart qiganda produktga - yoki + qilishi
                    if (data.startsWith(ConstantWord.MINUS)) {
                        Long productId = Long.parseLong(data.split("/")[1]);
                        BotService.misusProduct(message, productId);
                        sendPhoto = BotService.showCart(message);
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(sendPhoto);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    } else /// plus qilish
                        if (data.startsWith(ConstantWord.PLUS)) {
                            Long productId = Long.parseLong(data.split("/")[1]);
                            BotService.PlusProduct(message, productId);
                            sendPhoto = BotService.showCart(message);
                            try {
                                execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                                execute(sendPhoto);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }

                        }//hamma buyurtmani bekor qilish
                    if (data.equals(ConstantWord.DELETE_CART)) {

                        BotService.removeCart(message);

                        sendPhoto = BotService.showCart(message);
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(sendPhoto);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                    //Buyutmani tasdiqlash jarayoni ni boshlash
                    if (data.equals(ConstantWord.SAVE_OFFERCART)) {
                        currentUser.setLastOperation(UserLastOperation.SEND_LOCATION);
                        DataBase.writeToJson(ConstantWord.USER);
                        sendMessage = BotService.sayAboutLocation(message);
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(sendMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                    }
                    //Back Operation product
                    if (data.startsWith(ConstantWord.PRODUCT + ConstantWord.PEREFIX + ConstantWord.BACK) ||
                            data.startsWith(ConstantWord.CATEGORY + ConstantWord.PEREFIX + ConstantWord.BACK)) {

                        String s = data.split("/")[2];

                        if (currentUser.getLastOperation().equals(UserLastOperation.SHOW_PRODUCT)) {
                            currentUser.setLastOperation(UserLastOperation.SHOW_CATEGORY);
                            DataBase.writeToJson(ConstantWord.USER);
                            SendMessage editMessageText1;
                            editMessageText1 = BotService.showCategoryMessageAdmin(message, Integer.parseInt(s));
                            try {
                                execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                                execute(editMessageText1);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        } else {
                            SendMessage editMessageText1;
                            editMessageText1 = BotService.showCategoryMessageAdmin(message, 0);
                            try {
                                execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                                execute(editMessageText1);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                //Admin uchun
                if (currentUser.getRole().equals(List.of(UserRole.ADMIN))) {

                    if (data.startsWith(ConstantWord.SELECT_OFFER_ID) && currentUser.getLastOperation().equals(UserLastOperation.SHOW_NEW_OFER)) {
                        long Id = Long.parseLong(data.split("/")[1]);
                        sendMessage = BotService.SelectOperationDeliver(message, Id);

                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(sendMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                    }
                    if (data.startsWith(ConstantWord.SELECT_OFFER_ID_OLD)) {
                        long Id = Long.parseLong(data.split("/")[1]);
                        sendMessage = BotService.SelectOperationDeliverForOld(message, Id);

                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(sendMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                    }


                    if (data.startsWith(ConstantWord.DOWNLOAD_PDF)) {
                        long Id = Long.parseLong(data.split("/")[1]);
                        SendDocument document;
                        document = BotService.sendPdf(message, Id);
                        sendMessage = BotService.showMenuForDeliver(message);
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(document);
                            execute(sendMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    } else if (data.startsWith(ConstantWord.ARRIVED)) {
                        long id = Long.parseLong(data.split("/")[1]);
                        for (DeliverCart deliverCart : DataBase.deliverCart) {
                            if (deliverCart.getId().equals(id)) {
                                deliverCart.setDeliveredToUser(true);
                                sendMessage = BotService.sayAnyThing(message, "God job");
                                break;
                            }
                        }
                        DataBase.writeToJson(ConstantWord.DELIVER_CART);
                        SendMessage sendMessage1;
                        sendMessage1 = BotService.showMenuForDeliver(message);
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(sendMessage);
                            execute(sendMessage1);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }

                    if (data.equals(MyCallBackQuary.BACKTOMENU)) {
                        sendMessage = BotService.showMenuForDeliverForAdmin(message);
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(sendMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }

                    if (data.equals(ConstantWord.MINUS)) {
                        if (currentUser.getLastOperation().equals(UserLastOperation.SHOW_NEW_OFER)) {
                            sendPhoto = BotService.showNewOfferMinus(message);
                        } else {
                            sendPhoto = BotService.showOldOfferMinus(message);
                        }
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(sendPhoto);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                    if (data.equals(ConstantWord.PLUS)) {
                        if (currentUser.getLastOperation().equals(UserLastOperation.SHOW_NEW_OFER)) {
                            sendPhoto = BotService.showNewOfferPlus(message);
                        } else {
                            sendPhoto = BotService.showOldOfferPlus(message);
                        }
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(sendPhoto);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }


                    ///Add new category
                    if (data.equals(MyCallBackQuary.ADD_CATEGORY)) {
                        currentUser.setLastOperation(UserLastOperation.ADD_CATEGORY);
                        DataBase.writeToJson(ConstantWord.USER);
                        sendMessage = BotService.sayAnyThing(message, "Enter new category name");
                        try {
                            execute(sendMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                    if (data.equals(MyCallBackQuary.DELETE_CATEGORY)) {
                        currentUser.setLastOperation(UserLastOperation.DELETE_CATEGORY);
                        DataBase.writeToJson(ConstantWord.USER);
                        editMessageText = BotService.showCategoryForDelete(message, 0);
                    } else if (data.startsWith(ConstantWord.CATEGORY) && currentUser.getLastOperation().equals(UserLastOperation.DELETE_CATEGORY)) {
                        editMessageText = BotService.showCategoryForDelete(message, Integer.parseInt(data.split("/")[1]));
                    } else if (data.startsWith(MyCallBackQuary.DELETE_CATEGORY) && currentUser.getLastOperation().equals(UserLastOperation.DELETE_CATEGORY)) {
                        BotService.deleteCategory(Integer.parseInt(data.split("/")[1]));
                        sendMessage = BotService.sayAnyThing(message, " Delete product");
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(sendMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                    if (data.equals(MyCallBackQuary.EDIT_CATEGORY_NAME)) {
                        currentUser.setLastOperation(UserLastOperation.EDIT_CATEGORY_NAME);
                        DataBase.writeToJson(ConstantWord.USER);
                        editMessageText = BotService.showCategoryForEdit(message, 0);
                    } else if (data.startsWith(ConstantWord.CATEGORY) && currentUser.getLastOperation().equals(UserLastOperation.EDIT_CATEGORY_NAME)) {
                        editMessageText = BotService.showCategoryForEdit(message, Integer.parseInt(data.split("/")[1]));
                    } else if (data.startsWith(MyCallBackQuary.EDIT_CATEGORY_NAME) && currentUser.getLastOperation().equals(UserLastOperation.EDIT_CATEGORY_NAME)) {
                        sendMessage = BotService.sayAnyThing(message, " Enter new Name");
                        DataBase.rememberForProduct.put(message.getChatId(), data.split("/")[1]);
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(sendMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }


                    if (data.startsWith(ConstantWord.OPERATION + ConstantWord.PEREFIX + ConstantWord.TRUE)) {
                        String phoneNumber = data.split("/")[2];
                        for (User user : DataBase.users) {
                            if (user.getPhoneNumber().equals(phoneNumber)) {
                                user.setActive(true);
                                break;
                            }
                        }
                        DataBase.writeToJson(ConstantWord.USER);
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(sendMessage = BotService.sayAnyThing(message, "God job"));
                            sendMessage = BotService.showMenyuForAdmin(message);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    } else if (data.startsWith(ConstantWord.OPERATION + ConstantWord.PEREFIX + ConstantWord.FALSE)) {
                        String phoneNumber = data.split("/")[2];
                        for (User user : DataBase.users) {
                            if (user.getPhoneNumber().equals(phoneNumber)) {
                                user.setActive(false);
                                break;
                            }
                        }
                        DataBase.writeToJson(ConstantWord.USER);
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(sendMessage = BotService.sayAnyThing(message, "God job"));

                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    } else if (data.startsWith(ConstantWord.OPERATION + ConstantWord.PEREFIX + UserRole.USER)) {
                        String phoneNumber = data.split("/")[2];
                        for (User user : DataBase.users) {
                            if (user.getPhoneNumber().equals(phoneNumber)) {
                                user.setRole(List.of(UserRole.USER));
                                break;
                            }
                        }
                        DataBase.writeToJson(ConstantWord.USER);
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(sendMessage = BotService.sayAnyThing(message, "God job"));
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    } else if (data.startsWith(ConstantWord.OPERATION + ConstantWord.PEREFIX + UserRole.ADMIN)) {
                        String phoneNumber = data.split("/")[2];
                        for (User user : DataBase.users) {
                            if (user.getPhoneNumber().equals(phoneNumber)) {
                                user.setRole(List.of(UserRole.ADMIN));
                                break;
                            }
                        }
                        DataBase.writeToJson(ConstantWord.USER);
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(sendMessage = BotService.sayAnyThing(message, "God job"));
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    } else if (data.startsWith(ConstantWord.OPERATION + ConstantWord.PEREFIX + UserRole.DELIVER)) {
                        String phoneNumber = data.split("/")[2];
                        for (User user : DataBase.users) {
                            if (user.getPhoneNumber().equals(phoneNumber)) {
                                user.setRole(List.of(UserRole.DELIVER));
                                break;
                            }
                        }
                        DataBase.writeToJson(ConstantWord.USER);
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(sendMessage = BotService.sayAnyThing(message, "God job"));
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    } else if (data.startsWith(ConstantWord.OPERATION + ConstantWord.PEREFIX + ConstantWord.BACK)) {
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(sendMessage = BotService.sayAnyThing(message, "God job"));
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                    }


                    //Edit product
                    if (data.equals(MyCallBackQuary.EDIT_PRODUCT)) {
                        currentUser.setLastOperation(UserLastOperation.EDITE_PRODUCT);
                        DataBase.writeToJson(ConstantWord.USER);
                        editMessageText = BotService.showCategoryForAddProduct(message, 0);
                    } else if (currentUser.getLastOperation().equals(UserLastOperation.EDITE_PRODUCT) &&
                            data.startsWith(ConstantWord.CATEGORY) && !data.contains(ConstantWord.BACK)) {
                        int parentId = Integer.parseInt(data.split("/")[1]);
                        editMessageText = BotService.showCategory(message, parentId);
                    } else if (currentUser.getLastOperation().equals(UserLastOperation.EDITE_PRODUCT) &&
                            data.startsWith(ConstantWord.PRODUCT) && !data.contains(ConstantWord.BACK)) {
                        Long productId = Long.parseLong(data.split("/")[2]);
                        int categoryId = Integer.parseInt(data.split("/")[1]);
                        sendMessage = BotService.editProduct(message, categoryId, productId);
                        DataBase.rememberForProduct.put(message.getChatId(), productId.toString());
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(sendMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                    //EditName
                    if (data.startsWith(ConstantWord.EDIT_NAME)) {
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(BotService.sayAnyThing(message, "\uD83D\uDD89  Enter new product name"));
                            currentUser.setLastOperation(UserLastOperation.EDIT_NAME);
                            DataBase.writeToJson(ConstantWord.USER);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    } else
                        //EditPrice
                        if (data.startsWith(ConstantWord.EDIT_PRICE)) {
                            try {
                                execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                                execute(BotService.sayAnyThing(message, "\uD83D\uDD89  Enter new product price"));
                                currentUser.setLastOperation(UserLastOperation.EDIT_PRICE);
                                DataBase.writeToJson(ConstantWord.USER);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        } else if (data.equals(ConstantWord.EDIT_PHOTO)) {
                            sendMessage = BotService.editPhoto(message);
                            try {
                                execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                                execute(sendMessage);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }

                            currentUser.setLastOperation(UserLastOperation.EDIT_PHOTO);
                            DataBase.writeToJson(ConstantWord.USER);
                        }

                    if (data.equals(MyCallBackQuary.NEW_PHOTO)) {
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(BotService.sayAnyThing(message, "Send new photo"));
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        currentUser.setLastOperation(UserLastOperation.NEW_PHOTO);
                        DataBase.writeToJson(ConstantWord.USER);
                    } else if (data.equals(MyCallBackQuary.NEW_URL)) {
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(BotService.sayAnyThing(message, "Enter new Url"));
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        currentUser.setLastOperation(UserLastOperation.NEW_URL);
                        DataBase.writeToJson(ConstantWord.USER);
                    } else if (data.equals(MyCallBackQuary.NO_PHOTO)) {
                        long productId = Long.parseLong(DataBase.rememberForProduct.get(message.getChatId()));
                        BotService.setNoPhotoToProduct(productId);
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(BotService.sayAnyThing(message, "Add photo"));
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        currentUser.setLastOperation(UserLastOperation.SELECT_MENU);
                        DataBase.writeToJson(ConstantWord.USER);
                    }


                    //Categoriya operation
                    if (data.startsWith(ConstantWord.CATEGORY_CREAT)) {
                        String[] split = data.split("/");
                        String categoryName = split[1];
                        int parentCategoryId = Integer.parseInt(split[2]);
                        if (parentCategoryId == 0) {
                            Category category = new Category(categoryName);
                            DataBase.category.add(category);
                            DataBase.refreshCounter();

                        } else {
                            Category category = new Category(categoryName, parentCategoryId);
                            DataBase.category.add(category);
                            DataBase.refreshCounter();
                        }
                        DataBase.writeToJson(ConstantWord.CATEGORY);
                        DataBase.refreshCounter();
                        DataBase.writeToJson(ConstantWord.COUNTER_CATEGORY);

                        sendMessage = BotService.sayAnyThing(message, "Add Category");
                        try {
                            execute(sendMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }

                    //Add product
                    if (data.equals(MyCallBackQuary.ADD_PRODUCT)) {

                        currentUser.setLastOperation(UserLastOperation.ADD_PRODUCT);
                        DataBase.writeToJson(ConstantWord.USER);
                        editMessageText = BotService.showCategoryForAddProduct(message, 0);
                    }
                    //Delete product
                    if (data.equals(MyCallBackQuary.DELETE_PRODUCT)) {

                        currentUser.setLastOperation(UserLastOperation.DELETE_PRODUCT);
                        DataBase.writeToJson(ConstantWord.USER);
                        editMessageText = BotService.showCategoryForDeleteProduct(message, 0);

                    }
                    ///categoriyani mo'rish
                    if (currentUser.getLastOperation().equals(UserLastOperation.DELETE_PRODUCT)) {

                        if (data.startsWith(ConstantWord.CATEGORY + ConstantWord.PEREFIX)) {
                            editMessageText = BotService.showCategory(message, Integer.parseInt(data.split("/")[1]));
                        }

                    }

                    //Delete qilishi uchun
                    if (data.startsWith(ConstantWord.PRODUCT) && !data.contains(ConstantWord.BACK) && currentUser.getLastOperation().equals(UserLastOperation.DELETE_PRODUCT)) {
                        Long productId = Long.parseLong(data.split("/")[2]);
                        Long categoryId = Long.parseLong(data.split("/")[1]);
                        sendPhoto = BotService.showProductForDelete(message.getChatId(), categoryId, productId);
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(sendPhoto);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }

                    //delete product result
                    if (data.startsWith(MyCallBackQuary.DELETE_PRODUCT + ConstantWord.PEREFIX)) {
                        Long productId = Long.parseLong(data.split("/")[1]);
                        for (DeliverCart deliverCart : DataBase.deliverCart) {
                            deliverCart.getCartProduct().removeIf(cartProduct -> cartProduct.getProductId().equals(productId));
                        }
                        DataBase.products.removeIf(product -> product.getProductId().equals(productId));
                        DataBase.descriptionProduct.removeIf(descriptionProduct -> descriptionProduct.getProductId().equals(productId));

                        DataBase.writeToJson(ConstantWord.DELIVER_CART);
                        DataBase.writeToJson(ConstantWord.PRODUCT);
                        DataBase.writeToJson(ConstantWord.DESCRIPTION_PRODUCT);
                        sendMessage = BotService.sayAnyThing(message, "Delete product");
                        SendMessage sendMessage1;
                        sendMessage1 = BotService.showMenyuForAdmin(message);
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(sendMessage);
                            execute(sendMessage1);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                    }
                    ///Admin
                    if (currentUser.getLastOperation().equals(UserLastOperation.ADD_PRODUCT)) {

                        String[] split = data.split("/");
                        int n = split.length;
                        if (n == 2) {
                            editMessageText = BotService.showCategoryForAddProduct(message, Integer.parseInt(split[1]));

                        }
                    }
                    //Admin categoriyani ko'rishi uchun
                    if (currentUser.getLastOperation().equals(UserLastOperation.SHOW_CATEGORY)) {

                        String[] split = data.split("/");
                        int n = split.length;
                        if (n == 2) {
                            editMessageText = BotService.showCategory(message, Integer.parseInt(split[1]));
                        }

                    }

                    // Admin productni ko'rishi uchun
                    if (data.startsWith(ConstantWord.PRODUCT) && !data.contains(ConstantWord.BACK) &&
                            !currentUser.getLastOperation().equals(UserLastOperation.ADD_DESCRIPTION)
                    ) {
                        currentUser.setLastOperation(UserLastOperation.SHOW_PRODUCT);
                        DataBase.writeToJson(ConstantWord.USER);
                        Long productId = Long.parseLong(data.split("/")[2]);
                        Long categoryId = Long.parseLong(data.split("/")[1]);
                        sendPhoto = BotService.showProductForAdmin(message.getChatId(), categoryId, productId);
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(sendPhoto);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }

                    if (data.contains(MyCallBackQuary.ADD_PRODUCT) &&
                            data.contains(ConstantWord.CATEGORY)) {
                        DataBase.rememberForProduct.put(message.getChatId(), data);
                        currentUser.setLastOperation(UserLastOperation.ASK_PRODUCT_NAME);
                        DataBase.writeToJson(ConstantWord.USER);
                        sendMessage = BotService.sayAnyThing(message, " \uD83D\uDD89 Enter product product name ");
                        try {
                            execute(sendMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                    if (data.equals(UserLastOperation.ASK_PRODUCT_PHOTO.toString())) {

                        currentUser.setLastOperation(UserLastOperation.ASK_PRODUCT_PHOTO);
                        DataBase.writeToJson(ConstantWord.USER);
                        sendMessage = BotService.sayAnyThing(message, " \uD83D\uDDBC Send photo");
                        try {
                            execute(sendMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                    if (data.equals(UserLastOperation.ASK_PRODUCT_URL.toString())) {
                        currentUser.setLastOperation(UserLastOperation.ASK_PRODUCT_URL);
                        DataBase.writeToJson(ConstantWord.USER);
                        sendMessage = BotService.sayAnyThing(message, "\uD83C\uDF10 Send URL");
                        try {
                            execute(sendMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                    }
                    if (data.equals(UserLastOperation.CHOOSE_DEFAULT_PHOTO.toString())) {

                        String info = DataBase.rememberForProduct.get(message.getChatId());
                        String[] split = info.split("/");
                        int categoryId = Integer.parseInt(split[2]);
                        String productName = split[3];
                        String price = split[4];
                        while (price.contains(" ")) {
                            price = price.replaceAll(" ", "");
                        }
                        double productPrice = Double.parseDouble(price);

                        Product product = new Product(categoryId, productName, productPrice);
                        DataBase.products.add(product);
                        DataBase.refreshCounter();
                        DataBase.writeToJson(ConstantWord.PRODUCT);
                        DataBase.refreshCounter();
                        DataBase.writeToJson(ConstantWord.COUNTER_CATEGORY);
                        sendMessage = BotService.sayAnyThing(message, "\uD83D\uDDE3 Product add");
                        try {
                            execute(sendMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                    }
                    //fhoto kegandi uni productga qo'shish
                    if (data.equals(UserLastOperation.ASK_PRODUCT_PHOTO.toString())) {
                        BotService.sayAnyThing(message, "\uD83D\uDDBC  Send product photo");
                        currentUser.setLastOperation(UserLastOperation.ASK_PRODUCT_PHOTO);
                        DataBase.writeToJson(ConstantWord.USER);
                    }//url keganda uni productga qo'shish
                    if (data.equals(UserLastOperation.ASK_PRODUCT_URL.toString())) {
                        currentUser.setLastOperation(UserLastOperation.ASK_PRODUCT_URL);
                        DataBase.writeToJson(ConstantWord.USER);

                        sendMessage = BotService.sayAnyThing(message, "\uD83C\uDF10 Enter URL path");
                        try {
                            execute(sendMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                    //Add dicription
                    if (data.equals(MyCallBackQuary.ADD_DESCRIPTION)) {
                        currentUser.setLastOperation(UserLastOperation.ADD_DESCRIPTION);
                        DataBase.writeToJson(ConstantWord.USER);
                        editMessageText = BotService.showCategoryForAddProduct(message, 0);
                    } else if (currentUser.getLastOperation().equals(UserLastOperation.ADD_DESCRIPTION) &&
                            data.startsWith(ConstantWord.CATEGORY)) {
                        int parentId = Integer.parseInt(data.split("/")[1]);
                        editMessageText = BotService.showCategory(message, parentId);
                    } else if (currentUser.getLastOperation().equals(UserLastOperation.ADD_DESCRIPTION) &&
                            data.startsWith(ConstantWord.PRODUCT)) {
                        Long productId = Long.parseLong(data.split("/")[2]);
                        BotService.addDescription(productId);
                        DataBase.rememberForProduct.put(message.getChatId(), productId.toString());
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(BotService.sayAnyThing(message, "\uD83D\uDD89  Write description please"));
                            currentUser.setLastOperation(UserLastOperation.WRITE_DESCRIPTION);
                            DataBase.writeToJson(ConstantWord.USER);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                    //Back Operation product
                    if (data.startsWith(ConstantWord.PRODUCT + ConstantWord.PEREFIX + ConstantWord.BACK) ||
                            data.startsWith(ConstantWord.CATEGORY + ConstantWord.PEREFIX + ConstantWord.BACK)) {

                        String s = data.split("/")[2];

                        if (currentUser.getLastOperation().equals(UserLastOperation.SHOW_PRODUCT) ||
                                currentUser.getLastOperation().equals(UserLastOperation.EDITE_PRODUCT)) {
                            if (!currentUser.getLastOperation().equals(UserLastOperation.EDITE_PRODUCT)) {

                                currentUser.setLastOperation(UserLastOperation.SHOW_CATEGORY);
                                DataBase.writeToJson(ConstantWord.USER);
                            }
                            SendMessage editMessageText1;
                            if (data.startsWith(ConstantWord.CATEGORY)) {
                                editMessageText1 = BotService.showCategoryMessageAdmin(message, 0);

                            } else {
                                editMessageText1 = BotService.showCategoryMessageAdmin(message, Integer.parseInt(s));
                            }
                            try {
                                execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                                execute(editMessageText1);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        } else {
                            SendMessage editMessageText1;
                            editMessageText1 = BotService.showCategoryMessageAdmin(message, 0);
                            try {
                                execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                                execute(editMessageText1);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
                //Deliver uchun
                if (currentUser.getRole().equals(List.of(UserRole.DELIVER))) {
                    if (data.startsWith(ConstantWord.SELECT_OFFER_ID) && currentUser.getLastOperation().equals(UserLastOperation.SHOW_NEW_OFER)) {
                        long Id = Long.parseLong(data.split("/")[1]);
                        sendMessage = BotService.SelectOperationDeliver(message, Id);

                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(sendMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                    }
                    if (data.startsWith(ConstantWord.SELECT_OFFER_ID_OLD)) {
                        long Id = Long.parseLong(data.split("/")[1]);
                        sendMessage = BotService.SelectOperationDeliverForOld(message, Id);

                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(sendMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                    }


                    if (data.startsWith(ConstantWord.DOWNLOAD_PDF)) {
                        long Id = Long.parseLong(data.split("/")[1]);
                        SendDocument document;
                        document = BotService.sendPdf(message, Id);
                        sendMessage = BotService.showMenuForDeliver(message);
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(document);
                            execute(sendMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                    if (data.startsWith(ConstantWord.ARRIVED)) {
                        long id = Long.parseLong(data.split("/")[1]);
                        for (DeliverCart deliverCart : DataBase.deliverCart) {
                            if (deliverCart.getId().equals(id)) {
                                deliverCart.setDeliveredToUser(true);
                                sendMessage = BotService.sayAnyThing(message, "God job");
                                break;
                            }
                        }
                        DataBase.writeToJson(ConstantWord.DELIVER_CART);
                        SendMessage sendMessage1;
                        sendMessage1 = BotService.showMenuForDeliver(message);
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(sendMessage);
                            execute(sendMessage1);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                    if (data.equals(MyCallBackQuary.BACKTOMENU)) {
                        sendMessage = BotService.showMenuForDeliver(message);
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(sendMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }

                    if (data.equals(ConstantWord.MINUS)) {
                        if (currentUser.getLastOperation().equals(UserLastOperation.SHOW_NEW_OFER)) {
                            sendPhoto = BotService.showNewOfferMinus(message);
                        } else {
                            sendPhoto = BotService.showOldOfferMinus(message);
                        }
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(sendPhoto);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                    if (data.equals(ConstantWord.PLUS)) {
                        if (currentUser.getLastOperation().equals(UserLastOperation.SHOW_NEW_OFER)) {
                            sendPhoto = BotService.showNewOfferPlus(message);
                        } else {
                            sendPhoto = BotService.showOldOfferPlus(message);
                        }
                        try {
                            execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                            execute(sendPhoto);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                }

                try {
                    execute(editMessageText);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else {
                sendMessage = BotService.sayAnyThing(message, "😱 Your account is blocked by admin \n\n" +
                        "📲  Call center +998997834961");
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    public String getBotUsername() {
        return BotKey.BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BotKey.BOT_TOKEN;
    }
    //code ni ulasha
                       /*
                       if(code.equals(DataBase.smsCode.get(message.getChatId()))){
                           currentUser.setLastOperation(UserLastOperation.SELECT_MENU);
                           if(currentUser.getRole().contains(UserRole.ADMIN)){
                               sendMessage=BotService.showMenyuForAdmin(message);
                           }
                           if(currentUser.getRole().contains(UserRole.USER)){
                               currentUser.setLastOperation(UserLastOperation.SELECT_MENU);
                               sendMessage=BotService.showMenyuForUser(message);

                           }
                           DataBase.writeToJson(ConstantWord.USER);
                       }else {
                           sendMessage=BotService.wrongPassword(message);
                       }*/
}



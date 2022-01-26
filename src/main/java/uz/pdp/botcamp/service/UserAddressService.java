package uz.pdp.botcamp.service;

import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.pdp.botcamp.Bot.BotService;
import uz.pdp.botcamp.constants.ConstantWord;
import uz.pdp.botcamp.model.CartProduct;
import uz.pdp.botcamp.model.DeliverCart;
import uz.pdp.botcamp.model.OfferAddress;
import uz.pdp.botcamp.model.User;
import uz.pdp.botcamp.repository.DataBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserAddressService {
    public static SendMessage setCountry(Message message, String text) {
            OfferAddress offerAddressNow =null;
        for (OfferAddress offerAddress : DataBase.OFFER_ADDRESSES) {
            offerAddressNow = offerAddress;
            offerAddress.setCountry(text);
            break;
        }
        if(Objects.isNull(offerAddressNow)){
            return BotService.showMenyuForUser(message);
        }else {
            SendMessage sendMessage=new SendMessage();
            sendMessage.setChatId(message.getChatId().toString());
            sendMessage.setText("Enter the name of the region where the order will be accepted");
            sendMessage.setParseMode(ParseMode.MARKDOWN);

            return sendMessage;
        }

    }
    ///Region
    public static SendMessage setRegion(Message message, String text) {
        OfferAddress offerAddressNow =null;
        for (OfferAddress offerAddress : DataBase.OFFER_ADDRESSES) {
            offerAddressNow = offerAddress;
            offerAddress.setRegion(text);
            break;
        }
        if(Objects.isNull(offerAddressNow)){
            return BotService.showMenyuForUser(message);
        }else {
            SendMessage sendMessage=new SendMessage();
            sendMessage.setChatId(message.getChatId().toString());
            sendMessage.setText("Enter the name of the distriction where the order will be accepted");
            sendMessage.setParseMode(ParseMode.MARKDOWN);
            return sendMessage;
        }

    }
    //Distriction
    public static SendMessage setDistriction(Message message, String text) {
        OfferAddress offerAddressNow =null;
        for (OfferAddress offerAddress : DataBase.OFFER_ADDRESSES) {
            offerAddressNow = offerAddress;
            offerAddress.setDistrict(text);
            break;
        }
        if(Objects.isNull(offerAddressNow)){
            return BotService.showMenyuForUser(message);
        }else {
            SendMessage sendMessage=new SendMessage();
            sendMessage.setChatId(message.getChatId().toString());
            sendMessage.setText("Enter the name of the street where the order will be accepted");
            sendMessage.setParseMode(ParseMode.MARKDOWN);
            return sendMessage;
        }

    }
    //Street
    public static SendMessage setStreet(Message message, String text) {
        OfferAddress offerAddressNow =null;
        for (OfferAddress offerAddress : DataBase.OFFER_ADDRESSES) {
            offerAddressNow = offerAddress;
            offerAddress.setStreet(text);
            break;
        }
        if(Objects.isNull(offerAddressNow)){
            return BotService.showMenyuForUser(message);
        }else {
            SendMessage sendMessage=new SendMessage();
            sendMessage.setChatId(message.getChatId().toString());
            sendMessage.setText("Enter the name of the homeNumber where the order will be accepted");
            sendMessage.setParseMode(ParseMode.MARKDOWN);
            return sendMessage;
        }

    }
    public static SendMessage setHomeNumber(Message message, User currentUser, String text) {
        OfferAddress offerAddressNow =null;
        for (OfferAddress offerAddress : DataBase.OFFER_ADDRESSES) {
            offerAddressNow = offerAddress;
            offerAddress.setHomeNumber(text);
            break;
        }

            List<CartProduct> cartProductList=new ArrayList<>();
            for (CartProduct cartProduct : DataBase.cartProducts) {
                if(cartProduct.getCardId().equals(message.getChatId())) {
                    cartProductList.add(cartProduct);
                }
            }

            DeliverCart deliverCart=new DeliverCart(message.getChatId(),currentUser,cartProductList,offerAddressNow);
            DataBase.deliverCart.add(deliverCart);
            SendMessage sendMessage=new SendMessage();
            sendMessage.setChatId(message.getChatId().toString());
            sendMessage.setText("God job\n\n" +"Offer Id= "+deliverCart.getId()+
                    "\n\nOur staff will contact you within 24 hours\n\n" +
                    "\uD83D\uDCF2 Call center +998997834961");
            sendMessage.setParseMode(ParseMode.MARKDOWN);

        DataBase.refreshCounter();
        BotService.removeCart(message);
        DataBase.writeToJson(ConstantWord.DELIVER_CART);
        DataBase.writeToJson(ConstantWord.COUNTER_CATEGORY);
        DataBase.writeToJson(ConstantWord.PRODUCT);
        return sendMessage;

    }

}

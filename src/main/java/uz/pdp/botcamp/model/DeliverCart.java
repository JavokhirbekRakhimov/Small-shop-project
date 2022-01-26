package uz.pdp.botcamp.model;

import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DeliverCart {
    public static Long counter=1L;

    private Long cardId;//userId
    private Long Id; //buyurtma raqami
    private User user;
    private List<CartProduct> cartProduct;
    private OfferAddress address;
    private Timestamp creatAt;
    private boolean deliveredToUser;

    public DeliverCart(Long cardId, User user, List<CartProduct> cartProduct, OfferAddress address) {
        this.cardId = cardId;
        this.user = user;
        this.cartProduct = cartProduct;
        this.address = address;
        this.deliveredToUser=false;
        creatAt=Timestamp.valueOf(LocalDateTime.now());
        Id = counter;
        counter++;
    }
}

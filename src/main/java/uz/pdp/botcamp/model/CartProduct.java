package uz.pdp.botcamp.model;

import lombok.Data;

@Data
public class CartProduct {
public static Long counter=1L;
private Long cardId;//userId
private Long Id;
private Long productId;
private Integer categoryId;
private Integer productAmount;
private Long descriptionId=0L;

    public CartProduct(Long cardId, Integer categoryId,Long productId, Integer productAmount) {
        this.cardId = cardId;
        this.categoryId=categoryId;
        this.productId = productId;
        this.productAmount = productAmount;
        this.Id=counter;
        counter++;
    }


}

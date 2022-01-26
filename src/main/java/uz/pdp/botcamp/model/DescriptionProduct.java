package uz.pdp.botcamp.model;

import lombok.Data;

@Data
public class DescriptionProduct {
    private Long productId;
    private String discription="";

    public DescriptionProduct(Long productId, String discription) {
        this.productId = productId;
        this.discription = discription;
    }

    public DescriptionProduct(Long productId) {
        this.productId = productId;

    }
}

package uz.pdp.botcamp.model;

import lombok.Data;
import uz.pdp.botcamp.constants.ConstantWord;
@Data
public class Product {
    public static Long counter=1L;

    private Long productId;
    private String productName;
    private Double productPrice;
    private Integer categoryId;
    private Integer descriptionId=0;
    private String photoURL= ConstantWord.NO_PHOTO_PATH;

    public Product(Integer categoryId,String productName, Double productPrice) {
        this.productName = productName;
        this.productPrice = productPrice;
        this.categoryId = categoryId;
        this.productId=counter;
        counter++;

    }

    public Product(String productName, Double productPrice, Integer categoryId, String phothURL) {
        this.productName = productName;
        this.productPrice = productPrice;
        this.categoryId = categoryId;
        this.photoURL = phothURL;
        this.productId=counter;
        counter++;
    }
}

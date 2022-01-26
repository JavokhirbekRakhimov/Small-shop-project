package uz.pdp.botcamp.model;

import lombok.Data;

@Data
public class CounterCategory {
    private Integer counterCategory;
    private Long counterProduct;
    private Long counterCart;
    private Long deliverCart;

    public CounterCategory(Integer counterCategory, Long counterProduct, Long counterCart, Long deliverCart) {
        this.counterCategory = counterCategory;
        this.counterProduct = counterProduct;
        this.counterCart = counterCart;
        this.deliverCart = deliverCart;
    }
}

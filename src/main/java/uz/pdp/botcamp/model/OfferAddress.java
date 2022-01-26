package uz.pdp.botcamp.model;

import lombok.Data;
import uz.pdp.botcamp.constants.ConstantWord;

@Data
public class OfferAddress {
    private Long id;//userMessageId
    private String country= ConstantWord.DEFAULT_WORD;
    private String region= ConstantWord.DEFAULT_WORD;
    private String district= ConstantWord.DEFAULT_WORD;
    private String street= ConstantWord.DEFAULT_WORD;
    private String homeNumber= ConstantWord.DEFAULT_WORD;
    private Geo geo;

    public OfferAddress(Long id, Geo geo) {
        this.id = id;
        this.geo = geo;
    }
}

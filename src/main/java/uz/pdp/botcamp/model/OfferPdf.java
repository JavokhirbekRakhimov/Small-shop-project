package uz.pdp.botcamp.model;

import lombok.Data;

@Data
public class OfferPdf {
    private Long userId;//messageId
    private Long Id;//oferId siga teng
    private String pdfPath;

    public OfferPdf(Long userId, Long id, String pdfPath) {
        this.userId = userId;
        Id = id;
        this.pdfPath = pdfPath;
    }
}

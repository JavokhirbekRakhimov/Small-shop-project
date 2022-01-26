package uz.pdp.botcamp.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Tab;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import org.glassfish.grizzly.http.util.Chunk;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.pdp.botcamp.constants.ConstantWord;
import uz.pdp.botcamp.model.*;
import uz.pdp.botcamp.repository.DataBase;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;


public class DeliverCartService {
    public static void makePdf(Message message) {

        List<DeliverCart> deliverCarts = DataBase.deliverCart.stream().filter(deliverCart ->
                deliverCart.getCardId().equals(message.getChatId())).toList();
        Iterator<DeliverCart> iterator = deliverCarts.stream().iterator();
        while (iterator.hasNext()){
            DeliverCart next = iterator.next();
            Long id = next.getId();
            boolean hasPdf=false;
            for (OfferPdf offerPdf : DataBase.offerPdf) {
                if(offerPdf.getId().equals(id)){
                    hasPdf=true;
                    break;
                }
            }
            if(!hasPdf){
                creatNewPdf(next);
            }

        }


    }

    private static void creatNewPdf(DeliverCart next) {
        String path= ConstantWord.DOCUMENT_PDF;
        path+=next.getCardId()+"_"+ next.getId()+".pdf";
        File file=new File(path);
        try {
            PdfWriter writer=new PdfWriter(file);
            PdfDocument pdfdocument=new PdfDocument(writer);
            Document document=new Document(pdfdocument);
            Paragraph paragraph=new Paragraph();
            paragraph.add("Offer Id:  "+next.getId());
            paragraph.setTextAlignment(TextAlignment.CENTER);
            paragraph.setBold();
            document.add(paragraph);
            float []columb={10f,40f,20f,20f};

            int counter=1;
            double totalSum=0.0;
            for (CartProduct cartProduct : next.getCartProduct()) {
                Table table=new Table(UnitValue.createPercentArray(columb));
                table.addCell("T/r");
                table.addCell("Product name");
                table.addCell("Amount");
                table.addCell("Price");
                Product productNow=null;
                for (Product products : DataBase.products) {
                    if(products.getProductId().equals(cartProduct.getProductId())){
                        productNow=products;
                        break;
                    }
                }
                table.addCell(String.valueOf(counter));
                if (productNow != null) {
                    table.addCell(productNow.getProductName());
                }
                table.addCell(String.valueOf(cartProduct.getProductAmount()));
                double sum= 0;
                if (productNow != null) {
                    sum = cartProduct.getProductAmount()* productNow.getProductPrice();
                }
                totalSum+=sum;
                table.addCell(String.format("%10.1f",sum));
                DescriptionProduct descriptionProductNow=null;
                for (DescriptionProduct descriptionProduct : DataBase.descriptionProduct) {
                    if(descriptionProduct.getProductId().equals(cartProduct.getProductId())){
                        descriptionProductNow=descriptionProduct;
                        break;
                    }
                }
                document.add(table);
                if (descriptionProductNow != null) {
                    Paragraph paragraph1=new Paragraph();
                    paragraph1.add("! Description");
                    paragraph1.setBold();
                    document.add(paragraph1);
                    Paragraph paragraph2=new Paragraph();
                    paragraph2.add(".    "+descriptionProductNow.getDiscription());
                    document.add(paragraph2);
                }
                counter++;
            }


            Paragraph paragraph1=new Paragraph();
            paragraph1.add("Total price: "+String.format("%11.1f",totalSum)+" SUM  ");
            paragraph1.setTextAlignment(TextAlignment.RIGHT);
            paragraph1.setBold();
            document.add(paragraph1);

            Paragraph paragraph2=new Paragraph("Customer personal information");
            paragraph2.setBold();
            paragraph2.setTextAlignment(TextAlignment.CENTER);
            document.add(paragraph2);
            User user = next.getUser();

            float[]userColumb={25f,25f,25f,25f};
            Table userTable=new Table(UnitValue.createPercentArray(userColumb));
            userTable.addCell("First_Name");
            userTable.addCell("Last_Name");
            userTable.addCell("User_Name");
            userTable.addCell("Phone_Number");

            userTable.addCell(user.getFirstName());
            userTable.addCell(user.getLastName());
            userTable.addCell(user.getUserName());
            userTable.addCell(user.getPhoneNumber());
            document.add(userTable);

            Paragraph paragraph6=new Paragraph();
            paragraph6.add("Address");
            paragraph6.setTextAlignment(TextAlignment.CENTER);
            paragraph6.setBold();
            document.add(paragraph6);

            float[]addressColumb={20f,20f,20f,20f,20f};
            Table addressTable=new Table(UnitValue.createPercentArray(addressColumb));
            addressTable.addCell("Country");
            addressTable.addCell("Region");
            addressTable.addCell("District");
            addressTable.addCell("Street");
            addressTable.addCell("Home_Number");

            OfferAddress address = next.getAddress();

            addressTable.addCell(address.getCountry());
            addressTable.addCell(address.getRegion());
            addressTable.addCell(address.getDistrict());
            addressTable.addCell(address.getStreet());
            addressTable.addCell(address.getHomeNumber());
            document.add(addressTable);

            Paragraph paragraph3=new Paragraph();
            paragraph3.add("Geo");
            paragraph3.setBold();
            paragraph3.setTextAlignment(TextAlignment.LEFT);
            document.add(paragraph3);

            float[]geoClumb={50f,50f};
            Table geoTable=new Table(UnitValue.createPercentArray(geoClumb));
            geoTable.addCell("Latitude");
            geoTable.addCell("Longitude");

            geoTable.addCell(address.getGeo().getLatitude());
            geoTable.addCell(address.getGeo().getLongitude());
            document.add(geoTable);
            Paragraph paragraph14=new Paragraph();

            paragraph14.add("Creat time this offer: "+
                    new SimpleDateFormat("yyyy/MM/dd  hh.mm.ss").format(next.getCreatAt()));
            paragraph14.setTextAlignment(TextAlignment.RIGHT);
            paragraph14.setBold();
            document.add(paragraph14);

            pdfdocument.close();;
           writer.close();
           OfferPdf offerPdf=new OfferPdf(next.getCardId(),next.getId(),path);
           DataBase.offerPdf.add(offerPdf);
           DataBase.writeToJson(ConstantWord.OFFERPDF);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static SendDocument makeDescription(Message message) {
        List<CartProduct> cartProductList = DataBase.cartProducts.stream().filter(cartProduct ->
                cartProduct.getCardId().equals(message.getChatId())).toList();
        String path="src/main/resources/"+message.getChatId()+".pdf";
        File file=new File(path);

        try {
            PdfWriter pdfWriter=new PdfWriter(file);
            PdfDocument pdfDocument=new PdfDocument(pdfWriter);
            Document document=new Document(pdfDocument);
            for (int i = 0; i < cartProductList.size(); i++) {
                Long productId = cartProductList.get(i).getProductId();
                String name="";
                for (Product product : DataBase.products) {
                    if(product.getProductId().equals(productId)){
                        name=product.getProductName();
                        break;
                    }
                }
                Paragraph paragraph=new Paragraph();
                paragraph.add(i+1+". "+name);
                paragraph.setBold();
                document.add(paragraph);
                String decsrip="";
                for (DescriptionProduct descriptionProduct : DataBase.descriptionProduct) {
                    if (descriptionProduct.getProductId().equals(productId)){
                        decsrip=descriptionProduct.getDiscription();
                        break;
                    }
                }
                Paragraph paragraph1=new Paragraph();
                paragraph1.add(decsrip);
                document.add(paragraph1);
            }
            document.close();
            pdfDocument.close();
            pdfWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SendDocument sendDocument=new SendDocument();
        sendDocument.setChatId(message.getChatId().toString());
        sendDocument.setDocument(new InputFile(new File(path)));
        DataBase.rememberForProduct.put(message.getChatId(),path);

       return sendDocument;
    }
}

   /* String path= ConstantWord.DOCUMENT_PDF;
        path+=next.getCardId()+"_"+ next.getId()+".pdf";
                File file=new File(path);
                try {
                PdfWriter writer=new PdfWriter(file);
                PdfDocument pdfdocument=new PdfDocument(writer);
                Document document=new Document(pdfdocument);
                Paragraph paragraph=new Paragraph();
                paragraph.add("Offer Id:  "+next.getId());
                paragraph.setTextAlignment(TextAlignment.CENTER);
                paragraph.setBold();
                document.add(paragraph);
                float []columb={8f,20f,12f,50f,10f};
                Table table=new Table(UnitValue.createPercentArray(columb));
                table.addCell("T/r");
                table.addCell("Product name");
                table.addCell("Amount");
                table.addCell("Description");
                table.addCell("Price");
                int counter=1;
                double totalSum=0.0;
                for (CartProduct cartProduct : next.getCartProduct()) {
                Product productNow=null;
                for (Product products : DataBase.products) {
                if(products.getProductId().equals(cartProduct.getProductId())){
                productNow=products;
                break;
                }
                }
                table.addCell(String.valueOf(counter));
                if (productNow != null) {
                table.addCell(productNow.getProductName());
                }
                table.addCell(String.valueOf(cartProduct.getProductAmount()));
                DescriptionProduct descriptionProductNow=null;
                for (DescriptionProduct descriptionProduct : DataBase.descriptionProduct) {
                if(descriptionProduct.getProductId().equals(cartProduct.getProductId())){
                descriptionProductNow=descriptionProduct;
                break;
                }
                }
                if (descriptionProductNow != null) {
                table.addCell(descriptionProductNow.getDiscription());
                }
                double sum= 0;
                if (productNow != null) {
                sum = cartProduct.getProductAmount()* productNow.getProductPrice();
                }
                totalSum+=sum;
                table.addCell(String.format("%10.1f",sum));
                counter++;
                }
                document.add(table);


                Paragraph paragraph1=new Paragraph();
                paragraph1.add("Total price: "+String.format("%11.1f",totalSum)+" SUM  ");
                paragraph1.setTextAlignment(TextAlignment.RIGHT);
                paragraph1.setBold();
                document.add(paragraph1);

                Paragraph paragraph2=new Paragraph("Customer personal information");
                paragraph2.setBold();
                paragraph2.setTextAlignment(TextAlignment.CENTER);
                document.add(paragraph2);
                User user = next.getUser();

                float[]userColumb={25f,25f,25f,25f};
                Table userTable=new Table(UnitValue.createPercentArray(userColumb));
                userTable.addCell("First_Name");
                userTable.addCell("Last_Name");
                userTable.addCell("User_Name");
                userTable.addCell("Phone_Number");

                userTable.addCell(user.getFirstName());
                userTable.addCell(user.getLastName());
                userTable.addCell(user.getUserName());
                userTable.addCell(user.getPhoneNumber());
                document.add(userTable);

                Paragraph paragraph6=new Paragraph();
                paragraph6.add("Address");
                paragraph6.setTextAlignment(TextAlignment.CENTER);
                paragraph6.setBold();
                document.add(paragraph6);

                float[]addressColumb={20f,20f,20f,20f,20f};
                Table addressTable=new Table(UnitValue.createPercentArray(addressColumb));
                addressTable.addCell("Country");
                addressTable.addCell("Region");
                addressTable.addCell("District");
                addressTable.addCell("Street");
                addressTable.addCell("Home_Number");

                OfferAddress address = next.getAddress();

                addressTable.addCell(address.getCountry());
                addressTable.addCell(address.getRegion());
                addressTable.addCell(address.getDistrict());
                addressTable.addCell(address.getStreet());
                addressTable.addCell(address.getHomeNumber());
                document.add(addressTable);

                Paragraph paragraph3=new Paragraph();
                paragraph3.add("Geo");
                paragraph3.setBold();
                paragraph3.setTextAlignment(TextAlignment.LEFT);
                document.add(paragraph3);

                float[]geoClumb={50f,50f};
                Table geoTable=new Table(UnitValue.createPercentArray(geoClumb));
                geoTable.addCell("Latitude");
                geoTable.addCell("Longitude");

                geoTable.addCell(address.getGeo().getLatitude());
                geoTable.addCell(address.getGeo().getLongitude());
                document.add(geoTable);
                Paragraph paragraph14=new Paragraph();

                paragraph14.add("Creat time this offer: "+
                new SimpleDateFormat("yyyy/MM/dd  hh.mm.ss").format(next.getCreatAt()));
                paragraph14.setTextAlignment(TextAlignment.RIGHT);
                paragraph14.setBold();
                document.add(paragraph14);

                pdfdocument.close();;
                writer.close();
                OfferPdf offerPdf=new OfferPdf(next.getCardId(),next.getId(),path);
                DataBase.offerPdf.add(offerPdf);
                DataBase.writeToJson(ConstantWord.OFFERPDF);
                } catch (IOException e) {
                e.printStackTrace();
                }
                }*/

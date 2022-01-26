package uz.pdp.botcamp.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import uz.pdp.botcamp.constants.ConstantWord;
import uz.pdp.botcamp.model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface DataBase {
    List<User>users=new ArrayList<>();
    List<CartProduct>cartProducts=new ArrayList<>();
    List<DescriptionProduct> descriptionProduct=new ArrayList<>();
    List<Category>category=new ArrayList<>();
    List<Product>products=new ArrayList<>();
    List<CounterCategory>counters=new ArrayList<>();
    List<DeliverCart>deliverCart=new ArrayList<>();
    List<OfferAddress> OFFER_ADDRESSES =new ArrayList<>();
    List<OfferPdf>offerPdf=new ArrayList<>();

    HashMap<Long,Integer>smsCode=new HashMap<>();
    HashMap<Long,String>rememberForProduct=new HashMap<>();
    HashMap<Long,String>rememberIndex=new HashMap<>();
    HashMap<String,List>map =new HashMap<>();

     static void readAllJson(){
        String []fileName={ConstantWord.USER,ConstantWord.CATEGORY,
                ConstantWord.PRODUCT,ConstantWord.CART_PRODUCT,
                ConstantWord.DESCRIPTION_PRODUCT,ConstantWord.DELIVER_CART,ConstantWord.COUNTER_CATEGORY,ConstantWord.USER_ADDRESS,
        ConstantWord.OFFERPDF};
        try {
            String path=ConstantWord.JSON_PATH+fileName[0]+ConstantWord.JSON_PATH_LAST;
            File file=new File(path);
            Gson gson=new Gson();
            BufferedReader bufferedReader=new BufferedReader(new FileReader(file));
            users.addAll(gson.fromJson(bufferedReader, new TypeToken<List<User>>(){}.getType()));
            bufferedReader.close();
            map.put(ConstantWord.USER,users);

        } catch (IOException e) {
            e.printStackTrace();
        }
        ////
         try {
             String path=ConstantWord.JSON_PATH+fileName[1]+ConstantWord.JSON_PATH_LAST;
             File file=new File(path);
             Gson gson=new Gson();
             BufferedReader bufferedReader=new BufferedReader(new FileReader(file));
             category.addAll(gson.fromJson(bufferedReader, new TypeToken<List<Category>>(){}.getType()));
             bufferedReader.close();
             map.put(ConstantWord.CATEGORY,category);

         } catch (IOException e) {
             e.printStackTrace();
         }
         ////
         try {
             String path=ConstantWord.JSON_PATH+fileName[2]+ConstantWord.JSON_PATH_LAST;
             File file=new File(path);
             Gson gson=new Gson();
             BufferedReader bufferedReader=new BufferedReader(new FileReader(file));
             products.addAll(gson.fromJson(bufferedReader, new TypeToken<List<Product>>(){}.getType()));
             bufferedReader.close();
             map.put(ConstantWord.PRODUCT,products);

         } catch (IOException e) {
             e.printStackTrace();
         }
         ////
         try {
             String path=ConstantWord.JSON_PATH+fileName[3]+ConstantWord.JSON_PATH_LAST;
             File file=new File(path);
             Gson gson=new Gson();
             BufferedReader bufferedReader=new BufferedReader(new FileReader(file));
             cartProducts.addAll(gson.fromJson(bufferedReader, new TypeToken<List<CartProduct>>(){}.getType()));
             bufferedReader.close();
             map.put(ConstantWord.CART_PRODUCT,cartProducts);

         } catch (IOException e) {
             e.printStackTrace();
         }
         ////
         try {
             String path=ConstantWord.JSON_PATH+fileName[4]+ConstantWord.JSON_PATH_LAST;
             File file=new File(path);
             Gson gson=new Gson();
             BufferedReader bufferedReader=new BufferedReader(new FileReader(file));
             descriptionProduct.addAll(gson.fromJson(bufferedReader, new TypeToken<List<DescriptionProduct>>(){}.getType()));
             bufferedReader.close();
             map.put(ConstantWord.DESCRIPTION_PRODUCT,descriptionProduct);

         } catch (IOException e) {
             e.printStackTrace();
         }
         ////
         ////
         try {
             String path=ConstantWord.JSON_PATH+fileName[5]+ConstantWord.JSON_PATH_LAST;
             File file=new File(path);
             Gson gson=new Gson();
             BufferedReader bufferedReader=new BufferedReader(new FileReader(file));
             deliverCart.addAll(gson.fromJson(bufferedReader, new TypeToken<List<DeliverCart>>(){}.getType()));
             bufferedReader.close();
             map.put(ConstantWord.DELIVER_CART,deliverCart);

         } catch (IOException e) {
             e.printStackTrace();
         }
         ////
         try {
             String path=ConstantWord.JSON_PATH+fileName[6]+ConstantWord.JSON_PATH_LAST;
             File file=new File(path);
             Gson gson=new Gson();
             BufferedReader bufferedReader=new BufferedReader(new FileReader(file));
             counters.addAll(gson.fromJson(bufferedReader, new TypeToken<List<CounterCategory>>(){}.getType()));
             bufferedReader.close();
             map.put(ConstantWord.COUNTER_CATEGORY,counters);

         } catch (IOException e) {
             e.printStackTrace();
         }
         ////
         try {
             String path=ConstantWord.JSON_PATH+fileName[7]+ConstantWord.JSON_PATH_LAST;
             File file=new File(path);
             Gson gson=new Gson();
             BufferedReader bufferedReader=new BufferedReader(new FileReader(file));
             OFFER_ADDRESSES.addAll(gson.fromJson(bufferedReader, new TypeToken<List<OfferAddress>>(){}.getType()));
             bufferedReader.close();
             map.put(ConstantWord.USER_ADDRESS, OFFER_ADDRESSES);

         } catch (IOException e) {
             e.printStackTrace();
         }
         ////
         try {
             String path=ConstantWord.JSON_PATH+fileName[8]+ConstantWord.JSON_PATH_LAST;
             File file=new File(path);
             Gson gson=new Gson();
             BufferedReader bufferedReader=new BufferedReader(new FileReader(file));
             offerPdf.addAll(gson.fromJson(bufferedReader, new TypeToken<List<OfferPdf>>(){}.getType()));
             bufferedReader.close();
             map.put(ConstantWord.OFFERPDF, offerPdf);

         } catch (IOException e) {
             e.printStackTrace();
         }
          if(!counters.isEmpty()){
              Category.counter=DataBase.counters.get(counters.size()-1).getCounterCategory()+1;
              Product.counter=DataBase.counters.get(counters.size()-1).getCounterProduct()+1;
              CartProduct.counter=DataBase.counters.get(counters.size()-1).getCounterCart()+1;
              DeliverCart.counter=DataBase.counters.get(counters.size()-1).getDeliverCart()+1;
;          }

     }static void refreshCounter(){
        Integer categoryId=0;
        if(!DataBase.category.isEmpty()){
            categoryId=DataBase.category.get(DataBase.category.size()-1).getCategoryId();
        }
        Long productId=0L;
        if(!DataBase.products.isEmpty()){
            productId=DataBase.products.get(DataBase.products.size()-1).getProductId();
        }
        Long CartId=0L;
        if(!DataBase.cartProducts.isEmpty()){
            CartId=DataBase.cartProducts.get(DataBase.cartProducts.size()-1).getId();
        }
        Long deliverId=0L;
        if(!DataBase.deliverCart.isEmpty()){
            deliverId=DataBase.deliverCart.get(DataBase.deliverCart.size()-1).getId();
        }
        DataBase.counters.add(new CounterCategory(categoryId,productId,CartId,deliverId));
    }static void writeToJson(String name){
        String path=ConstantWord.JSON_PATH+name+ConstantWord.JSON_PATH_LAST;
        Gson gson= new GsonBuilder().setPrettyPrinting().create();
        try {
            BufferedWriter bufferedWriter=new BufferedWriter(new FileWriter(path));
            bufferedWriter.write(gson.toJson(map.get(name)));
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

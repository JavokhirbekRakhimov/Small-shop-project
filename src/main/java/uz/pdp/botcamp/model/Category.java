package uz.pdp.botcamp.model;

import lombok.Data;

@Data
public class Category {
    public static Integer counter=1;
    private Integer categoryId;
    private String categoryName;
    private Integer parentCategoryId=0;

    public Category(String categoryName, Integer parentCategory) {
        this.categoryId=counter;
        this.categoryName = categoryName;
        this.parentCategoryId = parentCategory;
        counter++;
    }
    public Category(String categoryName) {
        this.categoryId=counter;
        this.categoryName = categoryName;
        counter++;
    }
}

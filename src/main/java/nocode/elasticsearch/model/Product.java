package nocode.elasticsearch.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Product {
    private String id;
    private String name;
    private String description;
    private double price;
    private int stockAvailable;
}
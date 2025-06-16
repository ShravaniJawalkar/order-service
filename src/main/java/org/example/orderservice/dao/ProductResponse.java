package org.example.orderservice.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponse {
    @JsonProperty("product-name")
    private String productName;
    @JsonProperty("product-description")
    private String productDescription;
    @JsonProperty("category")
    private String categoryName;
    @JsonProperty("price")
    private Double price;
    @JsonProperty("quantity")
    private Integer quantity;
}

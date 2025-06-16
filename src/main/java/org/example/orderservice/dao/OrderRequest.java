package org.example.orderservice.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequest {
    @NotNull
    @JsonProperty("user_id")
    private Long userId;
    @NotNull
    @JsonProperty("product_id")
    private Long productId;
    @NotNull
    @JsonProperty("quantity")
    private Integer quantity;
    @NotNull
    @JsonProperty("price")
    private BigDecimal price;
}

package com.commerce4retail.cart.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartItemDto {

    private String id;
    private String productId;
    private String title;
    private String image;
    private BigDecimal price;
    private int quantity;
    private String size;
    private String color;
    private BigDecimal subtotal;
}

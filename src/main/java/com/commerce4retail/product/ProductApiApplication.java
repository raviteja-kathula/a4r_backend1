package com.commerce4retail.product;

import com.commerce4retail.Commerce4RetailApplication;
import org.springframework.boot.SpringApplication;

/**
 * Legacy entry point kept for backward compatibility.
 * The canonical application launcher is {@link Commerce4RetailApplication}.
 */
public class ProductApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(Commerce4RetailApplication.class, args);
    }
}

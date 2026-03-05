package com.commerce4retail.product;

import com.commerce4retail.Commerce4RetailApplication;
import org.springframework.boot.SpringApplication;

public class TestProductApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(Commerce4RetailApplication::main).run(args);
    }
}

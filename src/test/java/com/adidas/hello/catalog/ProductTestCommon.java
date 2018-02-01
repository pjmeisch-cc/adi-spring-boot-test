package com.adidas.hello.catalog;

import java.math.BigDecimal;
import java.util.Arrays;

public class ProductTestCommon {

    public static Product createProduct() {

        final Product product = new Product();

        product.setId(1);
        product.setUrl("/products/1");
        product.setCreated_at("2017-01-24T14:42:25+00:00");
        product.setName("Daily Shoe");
        product.setPrice(new BigDecimal(60.00));

        final Tag tag1 = new Tag();
        final Tag tag2 = new Tag();
        tag1.setId(1);
        tag1.setName("male");
        tag1.setUrl("/products/1/tags/1");
        tag2.setId(2);
        tag2.setName("shoe");
        tag2.setUrl("/products/1/tags/2");
        product.setTags(
            Arrays.asList(
                tag1,
                tag2
            )
        );

        final Size size1 = new Size();
        final Size size2 = new Size();
        final Size size3 = new Size();
        size1.setId(1);
        size1.setUrl("/products/1/sizes/1");
        size1.setSize("39");
        size1.setStock(1024);
        size2.setId(2);
        size2.setUrl("/products/1/sizes/2");
        size2.setSize("40");
        size2.setStock(1024);
        size3.setId(3);
        size3.setUrl("/products/1/sizes/3");
        size3.setSize("41");
        size3.setStock(1024);
        product.setSizes(
            Arrays.asList(
                size1,
                size2,
                size3
            )
        );

        return product;

    }
}

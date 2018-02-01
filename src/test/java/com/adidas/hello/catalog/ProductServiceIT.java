package com.adidas.hello.catalog;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProductService.class)
@RestClientTest(ProductService.class)
public class ProductServiceIT {

    @Autowired
    private ProductService productService;

    @Autowired
    private MockRestServiceServer mockRestServiceServer;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {


    }

    @Test
    public void getProductDetails() throws Exception {

        // Given
        Product product = ProductTestCommon.createProduct();
        String responseJson = objectMapper.writeValueAsString(product);

        URI productsEndpoint = CatalogURI.getProductsURI();

        mockRestServiceServer
            .expect(requestTo(productsEndpoint + "/1"))
            .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON_UTF8));

        // When
        Product productResponse = productService.getProductDetails(1);

        // Then
        // deep equals by comparing their JSON AST Representation
        assertEquals(
            objectMapper.readTree(responseJson),
            objectMapper.readTree(objectMapper.writeValueAsString(productResponse)
            )
        );

    }

    @Test
    public void getProducts() throws Exception {

        // Given
        List<Product> products = Arrays.asList(ProductTestCommon.createProduct());
        String responseJson = objectMapper.writeValueAsString(products);

        URI productsEndpoint = CatalogURI.getProductsURI();

        mockRestServiceServer
            .expect(requestTo(productsEndpoint))
            .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON_UTF8));

        // When
        List<Product> productResponse = productService.getProducts();

        // Then
        // deep equals by comparing their JSON AST Representation
        assertEquals(
            objectMapper.readTree(responseJson),
            objectMapper.readTree(objectMapper.writeValueAsString(productResponse)
            )
        );
    }
}

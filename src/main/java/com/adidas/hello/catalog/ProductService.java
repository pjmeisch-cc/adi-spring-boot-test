package com.adidas.hello.catalog;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class ProductService {

    private final RestTemplate restTemplate;
    private final URI productsEndpoint;

    public ProductService(RestTemplateBuilder restTemplateBuilder) throws URISyntaxException {
        this.restTemplate = restTemplateBuilder.build();

        this.productsEndpoint = CatalogURI.getProductsURI();
    }

    public Product getProductDetails(int id) {
        return this.restTemplate.getForObject(
            this.productsEndpoint + "/{id}",
            Product.class, id);
    }

    @HystrixCommand(fallbackMethod = "getProductsFallback")
    public List<Product> getProducts() {
        Product[] products = this.restTemplate.getForObject(
            this.productsEndpoint,
            Product[].class);
        return Arrays.asList(products);
    }

    @SuppressWarnings("unused")
    public List<Product> getProductsFallback() {
        return Collections.emptyList();
    }

}

package com.adidas.hello.catalog;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProductService.class)
@EnableAutoConfiguration
@AutoConfigureMockRestServiceServer
@EnableHystrix
public class ProductServiceCircuitBreakerIT {

    @Autowired
    private ProductService productService;

    @Autowired
    private MockRestServiceServer mockRestServiceServer;

    @Test
    public void getProductsShouldUseFallbackOnError() throws URISyntaxException {

        // given
        URI productsEndpoint = CatalogURI.getProductsURI();
        mockRestServiceServer.expect(requestTo(productsEndpoint)).andRespond(withServerError());

        // when
        List<Product> response = productService.getProducts();

        // then
        assertThat(response, is(empty()));
    }
}

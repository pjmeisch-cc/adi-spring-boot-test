package com.adidas.hello.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    classes = {CatalogController.class, ProductService.class})
@RestClientTest(ProductService.class)
public class ProductListAcceptanceTest {


    @Autowired
    private MockRestServiceServer mockRestServiceServer;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .build();
    }

    @Test
    public void displaySingleProductInTable() throws Exception {

        // Given: Product List
        Product product = ProductTestCommon.createProduct();
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");

        String responseJson = objectMapper.writeValueAsString(Arrays.asList(product));

        URI productsEndpoint = CatalogURI.getProductsURI();

        mockRestServiceServer
            .expect(requestTo(productsEndpoint))
            .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON_UTF8));

        // when: call endpoint
        String htmlResult = mockMvc.perform(get("/products", String.class))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        // Then: asserts result
        Document document = Jsoup.parse(htmlResult);
        Element productTable = document.getElementById("product-table");
        Element row = productTable.select("tr").get(1);
        assertThat(row.children().get(0).text(), is(equalTo("Daily Shoe")));
        assertThat(row.children().get(1).text(), is(equalTo(decimalFormat.format(new BigDecimal(60.00)))));

    }
}

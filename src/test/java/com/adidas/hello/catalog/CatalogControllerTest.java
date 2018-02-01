package com.adidas.hello.catalog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    classes = {CatalogControllerTest.class, WebApplicationContext.class, CatalogController.class})
public class CatalogControllerTest {


    private MockMvc mockMvc;
    @Autowired
    private ProductService productServiceMock;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Bean
    public ProductService todoService() {
        return Mockito.mock(ProductService.class);
    }

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void productPage_shouldDisplayProductsInAProductTable() throws Exception {

        // Given: Product
        Product product = ProductTestCommon.createProduct();
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");

        when(productServiceMock.getProducts()).thenReturn(Arrays.asList(product));


        // when
        String htmlResult = mockMvc.perform(get("/products", String.class))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        // then
        Document document = Jsoup.parse(htmlResult);
        Element productTable = document.getElementById("product-table");
        Element row = productTable.select("tr").get(1);
        assertThat(row.children().get(0).text(), is(equalTo("Daily Shoe")));
        assertThat(row.children().get(1).text(), is(equalTo(decimalFormat.format(new BigDecimal(60.00)))));
    }

}


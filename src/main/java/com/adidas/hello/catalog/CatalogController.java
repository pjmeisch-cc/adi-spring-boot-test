package com.adidas.hello.catalog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.DecimalFormat;
import java.util.List;

@Controller
public class CatalogController {

    private DecimalFormat decimalFormat = new DecimalFormat("#0.00");

    @Autowired
    private ProductService productService;

    @RequestMapping(path = "/products", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String productPage() {

        List<Product> products = productService.getProducts();
        if (!products.isEmpty()) {
            Document document = makeProductTable(products);
            return document.html();
        } else {
            return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "  <body>\n" +
                "    <h1>Products</h1>\n" +
                "    <p id='error'>Can not load catalog!</p>" +
                "    <p>Can not load products.</p>" +
                "  </body>\n" +
                "</html>";
        }
    }

    private Document makeProductTable(List<Product> products) {
        String htmlPage =
            "<!DOCTYPE html>\n" +
                "<html>\n" +
                "  <body>\n" +
                "    <h1>Products</h1>\n" +
                "    <table id='product-table'>\n" +
                "      <tr>\n" +
                "        <th>Name</th>\n" +
                "        <th>Price</th>\n" +
                "      </tr>\n" +
                "    </table>\n" +
                "  </body>\n" +
                "</html>";

        Document document = Jsoup.parse(htmlPage);

        Element table = document.getElementById("product-table");

        for (Product product : products) {
            table.append("<tr>\n" +
                "        <td>\n" +
                "          " + product.getName() + "\n" +
                "        </td>\n" +
                "        <td>\n" +
                "          " + decimalFormat.format(product.getPrice()) + "\n" +
                "        </td>\n" +
                "      </tr>");
        }
        return document;
    }
}

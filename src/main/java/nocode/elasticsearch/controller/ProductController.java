package nocode.elasticsearch.controller;

import lombok.RequiredArgsConstructor;
import nocode.elasticsearch.model.Product;
import nocode.elasticsearch.service.Page;
import nocode.elasticsearch.service.ProductService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class ProductController {
    private final ProductService productService;

    @GetMapping(value = "/key/{id}")
    public Product findId(@PathVariable String id) throws IOException {
        return productService.findById(id);
    }

    @GetMapping(value = "/q/{query}")
    public Page<Product> search(@PathVariable String query) throws IOException {
        return productService.search(query);
    }


    @PostMapping(value="/save")
    public ResponseEntity<Product> save() throws IOException {
        Product product = new Product();
        product.setId("1");
        product.setName("Name of product");
        product.setDescription("Description of product");
        product.setPrice(1.2);
        product.setStockAvailable(10);
        productService.save(product);
        HttpHeaders headers= new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        return new ResponseEntity<>(product, headers, HttpStatus.OK);
    }
}

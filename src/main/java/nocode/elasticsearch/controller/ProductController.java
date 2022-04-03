package nocode.elasticsearch.controller;

import lombok.RequiredArgsConstructor;
import nocode.elasticsearch.model.Product;
import nocode.elasticsearch.service.ProductJsonService;
import nocode.elasticsearch.service.ProductRestService;
import nocode.elasticsearch.service.ProductService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class ProductController {
    private final ProductService productService;
    private final ProductRestService productRestService;
    private final ProductJsonService productJsonService;

    @GetMapping(value = "/id/{id}", produces = "application/json")
    public @ResponseBody ResponseEntity<Product> findId(@PathVariable String id) throws IOException {
        return ResponseEntity.ok().body(productService.findById(id));
    }

    @GetMapping(value = "/q1/{query}", produces = "application/json")
    public @ResponseBody ResponseEntity<List<Product>> searchType1(@PathVariable String query) throws IOException {
        return ResponseEntity.ok().body(productService.search(query));
    }

    @GetMapping(value = "/q2/{query}", produces = "application/json")
    public @ResponseBody ResponseEntity<List<Map<String, Object>>> searchType2(@PathVariable String query) throws IOException {
        return ResponseEntity.ok().body(productRestService.search(query));
    }

    @GetMapping(value = "/q3/{query}", produces = "application/json")
    public @ResponseBody ResponseEntity<List<Product>> searchType3(@PathVariable String query) throws IOException {
        return ResponseEntity.ok().body(productJsonService.search(query));
    }

    @PostMapping(value="/save", produces = "application/json")
    public @ResponseBody ResponseEntity<Product> save() throws IOException {
        Product product = new Product();
        List<Product> list = new ArrayList<>();
        for(int i=1; i <= 5; i++) {
            product = new Product();
            product.setId(""+i);
            product.setName("Name "+i+" of product");
            product.setDescription("Description of product");
            product.setPrice(1.2);
            product.setStockAvailable(10);
            list.add(product);
        }
        productService.save(list);
        HttpHeaders headers= new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        return new ResponseEntity<>(product, headers, HttpStatus.OK);
    }
}

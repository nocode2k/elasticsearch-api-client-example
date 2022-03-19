package nocode.elasticsearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import nocode.elasticsearch.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    @Value("${config.indexName}")
    private String indexName;
    private final ElasticsearchClient elasticsearchClient;

    public Product findById(String id) throws IOException {
        final GetResponse<Product> getResponse = elasticsearchClient.get(builder -> builder.index(indexName).id(id), Product.class);
        Product product =null;
        if(getResponse.found()) {
            product = getResponse.source();
            assert product != null;
            product.setId(id);
        }
        return product;
    }

    public Page<Product> search(String input) throws IOException {
        return createPage(createSearchRequest(input, 0, 10), input);
    }

    public Page<Product> next(Page page) throws IOException {
        int from = page.getFrom() + page.getSize();
        final SearchRequest request = createSearchRequest(page.getInput(), from, page.getSize());
        return createPage(request, page.getInput());
    }

    private Page<Product> createPage(SearchRequest searchRequest, String input) throws IOException {
        final SearchResponse<Product> response = elasticsearchClient.search(searchRequest, Product.class);
        if (response.hits().total().value() == 0) {
            return Page.EMPTY;
        }
        if (response.hits().hits().isEmpty()) {
            return Page.EMPTY;
        }

        response.hits().hits().forEach(hit -> hit.source().setId(hit.id()));
        final List<Product> products = response.hits().hits().stream().map(Hit::source).collect(Collectors.toList());
        return new Page(products, input, searchRequest.from(), searchRequest.size());
    }

    private SearchRequest createSearchRequest(String input, int from, int size) {
        return new SearchRequest.Builder()
                .from(from)
                .size(size)
                .query(qb -> qb.multiMatch(mmqb -> mmqb.query(input).fields("name", "description")))
                .build();
    }

    public void save(Product product) throws IOException {
        save(Collections.singletonList(product));
    }

    public void save(List<Product> products) throws IOException {
        final BulkResponse response = elasticsearchClient.bulk(builder -> {
            for (Product product : products) {
                builder.operations(ob -> ob.create(cb -> cb.index(indexName).document(product)));
            }
            return builder;
        });
        final int size = products.size();
        for (int i = 0; i < size; i++) {
            products.get(i).setId(response.items().get(i).id());
        }
    }
}

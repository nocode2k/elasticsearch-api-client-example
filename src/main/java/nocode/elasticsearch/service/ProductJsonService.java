package nocode.elasticsearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import nocode.elasticsearch.model.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductJsonService {

    @Value("${config.indexName}")
    private String indexName;
    private final ElasticsearchClient elasticsearchClient;

    public Product findById(String id) throws IOException {
        final GetResponse<Product> getResponse
                = elasticsearchClient.get(builder -> builder.index(indexName).id(id), Product.class);
        Product product =null;
        if(getResponse.found()) {
            product = getResponse.source();
            assert product != null;
            product.setId(id);
        }
        return product;
    }

    public List<Product> search(String input) throws IOException {
        final SearchResponse<Product> response
                = elasticsearchClient.search(createSearchRequest(input, 0, 10), Product.class);
        response.hits().hits().forEach(hit -> hit.source().setId(hit.id()));
        final List<Product> products = response.hits().hits().stream().map(Hit::source).collect(Collectors.toList());
        return products;
    }

    private SearchRequest createSearchRequest(String input, int from, int size) {
        String queryString = "{" +
                "  \"query\": {" +
                "    \"multi_match\": {" +
                "      \"query\": \"%s\"," +
                "      \"fields\": [\"name\", \"description\"]" +
                "    }" +
                "  }" +
                "}";
        String requestBody = String.format(queryString, input);
        Reader queryJson = new StringReader(requestBody);

        return SearchRequest.of(q->q
                .withJson(queryJson)
                .size(size)
                .from(from)
        );
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

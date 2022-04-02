package nocode.elasticsearch.service;

import lombok.RequiredArgsConstructor;
import nocode.elasticsearch.component.ElasticSearchResponse;
import nocode.elasticsearch.component.ElasticsearchCustomRestClient;
import nocode.elasticsearch.component.generic.Hit;
import nocode.elasticsearch.model.Product;
import org.elasticsearch.client.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductRestService {

    @Value("${config.indexName}")
    private String indexName;
    private final ElasticsearchCustomRestClient elasticsearchCustomRestClient;

    public Page<Product> search(String input) throws IOException {
        return createPage(createSearchRequest(input, 10, 0), input);
    }

    public Page<Product> next(Page page) throws IOException {
        int from = page.getFrom() + page.getSize();
        final Request request = createSearchRequest(page.getInput(), page.getSize(), page.getFrom());
        return createPage(request, page.getInput());
    }

    private Page<Product> createPage(Request searchRequest, String input) throws IOException {
        final ElasticSearchResponse response = elasticsearchCustomRestClient.performRequest(searchRequest, ElasticSearchResponse.class, true);
        String total = String.valueOf(response.getHits().getTotal().get("value"));
        if (Integer.parseInt(total) == 0) {
            return Page.EMPTY;
        }
        if (response.getHits().getHits().isEmpty()) {
            return Page.EMPTY;
        }

        List<Hit> hitsList = response.getHits().getHits();
        final List<Map<String, Object>> products = hitsList.stream().map(Hit::getDataSource).collect(Collectors.toList());
        return new Page(products, input, 0, 10);
    }

    private Request createSearchRequest(String query, int size, int from) {
        Request request = new Request("GET","/"+this.indexName+"/_search");
        String queryString = "{" +
                "  \"size\": %d," +
                "  \"from\": %d," +
                "  \"query\": {" +
                "    \"multi_match\": {" +
                "      \"query\": \"%s\"," +
                "      \"fields\": [\"name\", \"description\"]" +
                "    }" +
                "  }" +
                "}";
        String requestBody = String.format(queryString, size, from, query);
        request.setJsonEntity(requestBody);
        return request;
    }
}

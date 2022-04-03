package nocode.elasticsearch.service;

import lombok.RequiredArgsConstructor;
import nocode.elasticsearch.component.ElasticSearchResponse;
import nocode.elasticsearch.component.ElasticsearchCustomRestClient;
import nocode.elasticsearch.component.generic.Hit;
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

    public List<Map<String, Object>> search(String input) throws IOException {
        final ElasticSearchResponse response
                = elasticsearchCustomRestClient.performRequest(createSearchRequest(input, 10, 0), ElasticSearchResponse.class, true);
        List<Hit> hitsList = response.getHits().getHits();
        final List<Map<String, Object>> products = hitsList.stream().map(Hit::getDataSource).collect(Collectors.toList());
        return products;
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

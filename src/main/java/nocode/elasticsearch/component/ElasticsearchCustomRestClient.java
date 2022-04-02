package nocode.elasticsearch.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class ElasticsearchCustomRestClient {
    private final RestClient elasticsearchRestClient;
    protected static final ObjectMapper mapper = new ObjectMapper();

    //request performRequest method example
    public <T> T performRequest(Request request, Class<T> responseClass, boolean assertSuccess) throws IOException {
        final Response response = elasticsearchRestClient.performRequest(request);
        if (assertSuccess) {
            assertSuccess(response);
        }
        return mapper.readValue(IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8), responseClass);
    }

    private void assertSuccess(Response response) throws IOException {
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new IOException("Return status is " + response.getStatusLine().getStatusCode());
        }
    }
}

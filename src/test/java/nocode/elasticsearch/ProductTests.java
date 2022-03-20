package nocode.elasticsearch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import nocode.elasticsearch.model.Product;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ProductTests {
    private final String json = "{" +
            "\"name\":\"Best name ever\"," +
            "\"description\":\"This is a wonderful description\"," +
            "\"price\":123.32," +
            "\"stock_available\":123" +
            "}";

    @Test
    public void testObjectMapperToProduct() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        Product product = mapper.readValue(json, Product.class);
        assertThat(product.getId()).isNull();
        assertThat(product.getName()).isEqualTo("Best name ever");
        assertThat(product.getDescription()).isEqualTo("This is a wonderful description");
        assertThat(product.getPrice()).isEqualTo(123.32);
        assertThat(product.getStockAvailable()).isEqualTo(123);

        // now vice versa. serialize out again
        final String serializedJson = mapper.writeValueAsString(product);
        assertThat(serializedJson).isEqualTo(json);
    }
}

package se.uddtronic.placeholder.mocks;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import se.uddtronic.placeholder.config.PlaceholderConfigProperties;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
public class MocksService {

    private final PlaceholderConfigProperties configProperties;

    public MocksService(PlaceholderConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    public List<MockData> getMocks() {
        if (configProperties.getFile() == null) {
            return Collections.emptyList();
        }

        try (InputStream inputStream = Files.newInputStream(Paths.get(configProperties.getFile()))) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(inputStream, new TypeReference<List<MockData>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}

package se.uddtronic.placeholder.mocks;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import se.uddtronic.placeholder.config.MockConfigurationException;
import se.uddtronic.placeholder.config.PlaceholderConfigProperties;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
public class MocksService {

    private final PlaceholderConfigProperties configProperties;
    private List<MockData> mocks = Collections.emptyList();

    public MocksService(PlaceholderConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    @PostConstruct
    public void init() {
        if (configProperties.getFile() == null) {
            return;
        }

        try (InputStream inputStream = Files.newInputStream(Paths.get(configProperties.getFile()))) {
            ObjectMapper mapper = new ObjectMapper();
            mocks = mapper.readValue(inputStream, new TypeReference<List<MockData>>() {});
        } catch (Exception e) {
            throw new MockConfigurationException(configProperties.getFile(), e);
        }
    }

    public List<MockData> getMocks() {
        return mocks;
    }
}

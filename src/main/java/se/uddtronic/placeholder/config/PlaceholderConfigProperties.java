package se.uddtronic.placeholder.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("placeholder.config")
public class PlaceholderConfigProperties {

    private String file;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}

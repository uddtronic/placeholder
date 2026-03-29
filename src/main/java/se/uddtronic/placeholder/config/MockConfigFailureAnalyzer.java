package se.uddtronic.placeholder.config;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

public class MockConfigFailureAnalyzer extends AbstractFailureAnalyzer<MockConfigurationException> {
    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, MockConfigurationException cause) {
        return new FailureAnalysis(
            "Failed to load mock configuration file: " + cause.getMessage(),
            "Ensure the file exists at the path specified in your properties and is valid JSON.",
            cause
        );
    }
}

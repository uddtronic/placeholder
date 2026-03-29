package se.uddtronic.placeholder.mocks;

public class ValidationConfig {
    private String openApiSpec;
    private ValidationAction action = ValidationAction.WARN;
    private MockResponse customErrorResponse;

    public String getOpenApiSpec() {
        return openApiSpec;
    }

    public void setOpenApiSpec(String openApiSpec) {
        this.openApiSpec = openApiSpec;
    }

    public ValidationAction getAction() {
        return action;
    }

    public void setAction(ValidationAction action) {
        this.action = action;
    }

    public MockResponse getCustomErrorResponse() {
        return customErrorResponse;
    }

    public void setCustomErrorResponse(MockResponse customErrorResponse) {
        this.customErrorResponse = customErrorResponse;
    }
}

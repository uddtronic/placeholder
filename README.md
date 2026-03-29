# Placeholder Mock

This is an early preview of the mocking tool Placeholder Mock.

## Things to Do

This project is published early to meet a deadline, but multiple features are currently missing.

- Animate the request counter when it updates (important!)
- Support matching query parameters in some way
- Validate against OpenAPI spec
- More flexible return types than just JSON
- Show responses sent in adition to the requests
- General code cleanup (it got a bit messy)
- Add Docker support
- Support multiple config files

## Build and Run

For Placeholder Mock to work a configuration file is needed. Check `mocks.json` for an example.
The location of the configuration file is passed as the `PLACEHOLDER_CONFIG_FILE` environment
variable.

Either build the project and start the *jar*:

```bash
./mvnw clean package
PLACEHOLDER_CONFIG_FILE=mocks.json java -jar target/placeholder-1.0-SNAPSHOT.jar
```

Or run in *dev mode*:
```bash
PLACEHOLDER_CONFIG_FILE=mocks.json ./mvnw
```

Once started, you can send queries:

```bash
curl -sv "http://localhost:8080/api/users"
```

Open the UI at http://localhost:8080/ to see the configured mocks and requests as they come in.

![Placeholder Mock](screenshot.png)

## Configuration

The configuration file (e.g., `mocks.json`) contains a JSON array of mock objects. Each mock object defines an endpoint and its expected behavior.

### Structure of a Mock Entry

Each mock definition in the JSON array supports the following fields:

- **`path`**: The endpoint path to match. It supports regular expressions with capture groups (e.g., `/api/user/([0-9]+)`).
- **`method`**: The HTTP method to match (e.g., `GET`, `POST`).
- **`name`** *(Optional)*: A descriptive name for the mock.
- **`priority`** *(Optional)*: Determines which mock takes precedence when multiple paths overlap. Higher values have higher priority.
- **`variables`** *(Optional)*: An object containing static key-value pairs that can be referenced within the response.
- **`response`**: An object containing the HTTP `status` code and either a `json` object to return directly, or a `file` string pointing to an external template file relative to the execution directory.

### Dynamic Response Templating

The response body (whether defined via `json` or loaded from a `file`) supports dynamic templating using Mustache-style `{{...}}` syntax to inject request details or variables:

- **`{{groups.n}}`**: Access regular expression capture groups from the `path` (e.g., `{{groups.1}}`).
- **`{{body.propertyName}}`**: Access values from the incoming JSON request body. Supports nested properties (e.g., `{{body.address.city}}`).
- **`{{headers.headerName}}`**: Access HTTP request headers (e.g., `{{headers.host}}`).
- **`{{request.paramName}}`**: Access query parameters from the request URL (e.g., `{{request.q}}`).
- **`{{variables.variableName}}`**: Inject custom variables defined in the mock's `variables` block.
- **`{{now}}`**: Inject the current server timestamp.

**Returning Numbers in JSON**:
Since values in JSON must be valid types, you can't normally inject a raw number directly into a JSON string via templating (e.g., `"id": {{groups.1}}` is invalid JSON).
To solve this, prefix your template string with `#num#`. After the template is evaluated, the quotes and the prefix will be stripped, converting it into a valid JSON number:
`"id": "#num#{{groups.1}}"` becomes `"id": 42` in the final output. If you are using the `file` property, you don't need this prefix as external files don't strictly need to parse as JSON during initialization; you can simply write `"id": {{groups.1}}` directly in the file.

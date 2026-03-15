# Placeholder Mock

This is an early preview of the mocking tool Placeholder Mock.

## Things to Do

This project is published early to meet a deadline, but multiple features are currently missing.

- Support matching paths with regexp (instead of exact strings)
- Configure priority for mocks (needed when multiple can match)
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

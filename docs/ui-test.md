# UI-Tests

To conduct the UI-Tests, follow the steps below:

1. Start Springboot seed in a container:
    - Build the project:
    
        `mvn package -DskipTests`
    - Build the image:

        `docker build -t springboot-seed -f src/main/docker/Dockerfile .`

    - Start a container:

        `docker run --rm --name springboot-seed -p 8080:8080 springboot-seed`
2. In a new terminal, start a selenium container, linking to the springboot seed container:

    `docker run --rm --name selenium --link springboot-seed:springboot-seed -p 4444:4444 selenium/standalone-chrome:3.2.0`
3. In a new terminal, run the UI-Tests:
```
    export SPRINGBOOT_SEED_URL=http://springboot-seed:8080
    export SELENIUM_URL=http://localhost:4444/wd/hub
    mvn -f ui-test/pom.xml test

```

4. Stop the springboot and selenium containers with `CTRL+C`

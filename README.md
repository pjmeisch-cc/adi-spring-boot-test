# Spring Boot Seed Project

Develop a containerized Spring Boot project based on this template project.

A basic starter app that provides an index.html, as well as a JSON response on GET `/greeting`.

Includes `actuator` endpoints on another port  (8081):

  - `/_manage/info`, that provides build information (such as git commit id), and
  - `/_manage/metrics` for runtime metrics.

Includes [Distributed tracing](http://opentracing.io) Using Zipkin as the implementation
## Getting started

**Note:** Before building this project in Jenkins, change the project name in the `Jenkinsfile`!

### Download and install

1. `git clone https://tools.adidas-group.com/bitbucket/scm/dof/spring-boot-seed.git --depth 1`
2. `cd spring-boot-seed`
3. Edit `Jenkinsfile` replacing springboot-seed with your new project name
4. `mvn package`
5. `java -jar target/springboot-seed-0.1.0.jar`
6. **Optional (only for tracing)** `docker run -d -p 9411:9411 openzipkin/zipkin` 
7. Open [http://localhost:8080](http://localhost:8080) in your browser

### Run in Docker and Kubernetes

See [Deployment](docs/deployment.md) for how to deploy on a local k8s cluster or onto the Giantswarm cluster.

### Build with Jenkins

Setting up as a Jenkins2 pipeline job, it uses the `Jenkinsfile` which is part of the source code.

More details: https://jenkins.io/doc/book/pipeline/

## Automated Tests

### Code Coverage
Run `mvn jacoco:report`

For code coverage the jacoco Maven plugin has been included. This plugin creates the file `target/jacoco.exec` which can be uploaded to sonarqube.

Additionally in `target/site/jacoco` a human readable report is created.

### Integration Tests

Place integration tests in src/test/**/*IT.java, so the maven failsafe plugin picks them up.
To execute integration tests, run `mvn compile compiler:testCompile failsafe:integration-test failsafe:verify`.

### UI-Tests
Automated UI-Tests are done using selenium and cucumber-jvm.
Our working example is an adaptation of [java-webbit-websockets-selenium](https://github.com/cucumber/cucumber-jvm/tree/master/examples/java-webbit-websockets-selenium). To run it locally, follow the [UI-Test instructions](docs/ui-test.md).

## Detailed features

### <a name='products_page'></a> Products Page
* Products Page `/products` shows a list of products. The products data is fetched from another service.
To configure this service endpoint, you need to set the environment variables `K8S_CATALOG_SERVICE_PROTOCOL`,
`K8S_CATALOG_SERVICE_HOST`, `K8S_CATALOG_SERVICE_PORT`. An example configuration could be:

```
K8S_CATALOG_SERVICE_PROTOCOL=http
K8S_CATALOG_SERVICE_HOST=adidas-springboot-api-seed-master.dof
K8S_CATALOG_SERVICE_PORT=8080
```

### Circuit Breaker
Service classes should have a fallback method when performing network calls to other services (Circuit breaker pattern).
An example implementation is given in the ProductService class, its test in the ProductServiceCircuitBreakerIT class.

### Management endpoints

**Note:** Sensitive data, such as system metrics is mounted on a different port (`8081`) than the main app.

- Metrics endpoint ':8081/_manage/metrics` shows system metrics of the running process. Example response:
    ```json
    {
        "mem": 649250,
        "mem.free": 131163,
        "processors": 8,
        // ...
        "gauge.response._manage.info": 91,
        "counter.status.200._manage.info": 1
    }
    ```

- Info endpoint :8080/_manage/info shows project info of the app. Example response:
    ```json
    {
        "build": {
            "artifact": "springboot-seed",
            "name": "springboot-seed",
            "description": "...",
            "version": "0.1.0"
        }
    }
    ```
    These data are created from the maven pom file and from the git meta info created while building: ('git.properties').

### OAuth2

OAuth2 connectors are configured via the `oauth2` section of the [application.yml](src/main/resources/application.yml). An example SSO endpoint is included with the project. For detailed documentation, see [OAuth2](docs/oauth2.md).

### Logging

Spring recommends naming the log4j2 configuration file log4j2-spring.xml - however this leads to an error message during startup:

`ERROR StatusLogger No log4j2 configuration file found. Using default configuration: logging only errors to the console.`

This is a known and open issue: https://github.com/spring-projects/spring-boot/issues/4809

For this reason, the logging configuration file uses the standard log4j2 naming, i.e. log4j2.xml

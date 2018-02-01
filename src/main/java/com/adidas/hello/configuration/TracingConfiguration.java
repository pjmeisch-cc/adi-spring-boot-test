package com.adidas.hello.configuration;

import brave.Tracing;
import brave.opentracing.BraveTracer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.okhttp3.OkHttpSender;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class TracingConfiguration {

    @Value("${zipkin.protocol:http}")
    private String zipkinProtocol;

    @Value("${zipkin.host:localhost}")
    private String zipkinHost;

    @Value("${zipkin.port:9411}")
    private String zipkinPort;

    @Bean
    public io.opentracing.Tracer zipkinTracer() throws URISyntaxException {

        int parsedPort = Integer.parseInt(zipkinPort);

        final String zipkinURL = new URI(
            zipkinProtocol,
            null,
            zipkinHost,
            parsedPort,
            "/api/v1/spans",
            null,
            null
        ).toString();

        OkHttpSender okHttpSender = OkHttpSender.create(zipkinURL);
        AsyncReporter<Span> reporter = AsyncReporter.builder(okHttpSender).build();
        Tracing braveTracer = Tracing
            .newBuilder()
            .localServiceName("spring-boot-seed")
            .reporter(reporter)
            .build();
        return BraveTracer.create(braveTracer);
    }
}

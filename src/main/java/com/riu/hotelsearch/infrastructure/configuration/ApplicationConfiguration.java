package com.riu.hotelsearch.infrastructure.configuration;

import com.riu.hotelsearch.application.port.in.CountSearchesUseCase;
import com.riu.hotelsearch.application.port.in.PersistSearchUseCase;
import com.riu.hotelsearch.application.port.in.RegisterSearchUseCase;
import com.riu.hotelsearch.application.port.out.SearchEventPublisher;
import com.riu.hotelsearch.application.port.out.SearchRepository;
import com.riu.hotelsearch.application.service.CountSearchesService;
import com.riu.hotelsearch.application.service.PersistSearchService;
import com.riu.hotelsearch.application.service.RegisterSearchService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration(proxyBeanMethods = false)
public class ApplicationConfiguration {

    @Bean
    RegisterSearchUseCase registerSearchUseCase(SearchEventPublisher publisher) {
        return new RegisterSearchService(publisher, UUID::randomUUID);
    }

    @Bean
    PersistSearchUseCase persistSearchUseCase(SearchRepository repository) {
        return new PersistSearchService(repository);
    }

    @Bean
    CountSearchesUseCase countSearchesUseCase(SearchRepository repository) {
        return new CountSearchesService(repository);
    }
}

package com.riu.hotelsearch.infrastructure.configuration;

import com.riu.hotelsearch.application.port.in.CountSearchesUseCase;
import com.riu.hotelsearch.application.port.in.PersistSearchUseCase;
import com.riu.hotelsearch.application.port.in.RegisterSearchUseCase;
import com.riu.hotelsearch.application.port.out.SearchEventPublisher;
import com.riu.hotelsearch.application.service.CountSearchesService;
import com.riu.hotelsearch.application.service.PersistSearchService;
import com.riu.hotelsearch.application.service.RegisterSearchService;
import com.riu.hotelsearch.domain.port.out.SearchRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.UUID;

@Configuration(proxyBeanMethods = false)
public class ApplicationConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    RegisterSearchUseCase registerSearchUseCase(SearchEventPublisher publisher, Clock clock) {
        return new RegisterSearchService(publisher, UUID::randomUUID, clock);
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

package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.config;

import com.pszymczyk.consul.ConsulProcess;
import com.pszymczyk.consul.ConsulStarterBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.annotation.PreDestroy;

@TestConfiguration
@ConditionalOnProperty(name = "zuul.ratelimit.repository", havingValue = "consul")
public class ConsulConfig {

    private ConsulProcess consulProcess;

    @Bean
    public ConsulProcess consulProcess() {
        if (consulProcess == null) {
            consulProcess = ConsulStarterBuilder.consulStarter().withHttpPort(8500).build().start();
        }
        return consulProcess;
    }

    @PreDestroy
    public void cleanup() {
        consulProcess.close();
    }
}

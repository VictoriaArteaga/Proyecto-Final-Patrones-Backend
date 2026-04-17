package com.proyectofinal.backendapi.render3d.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

//   CONFIGURACIÓN DE MULTITAREA (ASÍNCRONA)

//  Este archivo permite que el sistema genere el renderizado 3D en "segundo plano".
//  el usuario puede seguir usando la app mientras la IA trabaja.

    @Bean(name = "renderExecutor")
    public Executor renderExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("render-3d-");
        executor.initialize();
        return executor;
    }
}
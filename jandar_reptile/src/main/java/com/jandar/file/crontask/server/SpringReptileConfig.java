package com.jandar.file.crontask.server;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class SpringReptileConfig {

    @Value("${app.cron}")
    private String cron;
}

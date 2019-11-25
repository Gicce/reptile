package com.jandar.file.utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Properties;

@Service
public class ResourceUtils {

    public String StringValue(String key) {
        Resource resource = new ClassPathResource("application.properties");
        try {
            Properties props = PropertiesLoaderUtils.loadProperties(resource);
            return props.getProperty(key);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }
}

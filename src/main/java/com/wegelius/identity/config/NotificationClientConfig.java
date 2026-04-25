package com.wegelius.identity.config;

import com.wegelius.notifications.api.NotificationApi;
import com.wegelius.notifications.invoker.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationClientConfig {

    @Bean
    public ApiClient notificationApiClient(
            @Value("${notification.service.base-url:http://localhost:8082}") String notificationServiceBaseUrl
    ) {
        return new ApiClient().setBasePath(notificationServiceBaseUrl);
    }

    @Bean
    public NotificationApi notificationApi(ApiClient notificationApiClient) {
        return new NotificationApi(notificationApiClient);
    }
}

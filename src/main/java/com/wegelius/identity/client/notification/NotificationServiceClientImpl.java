package com.wegelius.identity.client.notification;

import com.wegelius.notifications.api.NotificationApi;
import com.wegelius.notifications.invoker.ApiException;
import com.wegelius.notifications.model.RegistrationConfirmationRequest;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class NotificationServiceClientImpl implements NotificationServiceClient {

    private final NotificationApi notificationApi;

    public NotificationServiceClientImpl(NotificationApi notificationApi) {
        this.notificationApi = notificationApi;
    }

    @Override
    public void sendRegistrationConfirmation(String email, String confirmationLink) {
        try {
            var request = new RegistrationConfirmationRequest();
            request.setEmail(email);
            request.setConfirmationLink(URI.create(confirmationLink));
            notificationApi.sendRegistrationConfirmation(request);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid confirmation link URI: " + confirmationLink, e);
        } catch (ApiException e) {
            throw new RuntimeException("Failed to send registration confirmation to notification service", e);
        }
    }
}

package com.wegelius.identity.client.notification;

public interface NotificationServiceClient {
    void sendRegistrationConfirmation(String email, String confirmationLink);
}

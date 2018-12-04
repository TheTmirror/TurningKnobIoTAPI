package org.eclipse.smarthome.binding.drehbinding.internal.REST;

import java.io.IOException;
import java.util.Map;

import org.eclipse.smarthome.binding.drehbinding.internal.REST.implementation.RESTResponse;

public interface RESTIOService {

    public RESTResponse callService(String serviceIdentifier) throws IOException;

    public RESTResponse callService(String serviceIdentifier, Map<String, String> params) throws IOException;

    public void addSubscription(RESTIOParticipant participant, String topic);

    public void removeSubscription(RESTIOParticipant participant, String topic);

    public void hasSubscribed(RESTIOParticipant participant, String topic);

    public void getSubscribedServices(RESTIOParticipant participant);

    public boolean isDeviceOnlineAndReachable();

}
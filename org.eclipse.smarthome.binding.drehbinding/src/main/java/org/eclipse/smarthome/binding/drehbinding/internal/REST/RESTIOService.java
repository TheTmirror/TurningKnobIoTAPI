package org.eclipse.smarthome.binding.drehbinding.internal.REST;

import java.util.Map;

public interface RESTIOService {

    public void callService(String serviceIdentifier, Map<String, String> params);

    public void addSubscription(RESTIOParticipant participant, String topic);

    public void removeSubscription(RESTIOParticipant participant, String topic);

    public void hasSubscribed(RESTIOParticipant participant, String topic);

    public void getSubscribedServices(RESTIOParticipant participant);

    public boolean isDeviceOnlineAndReachable();

}
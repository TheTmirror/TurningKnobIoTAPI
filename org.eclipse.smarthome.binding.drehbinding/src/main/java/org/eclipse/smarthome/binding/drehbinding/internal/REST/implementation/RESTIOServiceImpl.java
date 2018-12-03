package org.eclipse.smarthome.binding.drehbinding.internal.REST.implementation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.binding.drehbinding.internal.REST.RESTIOParticipant;
import org.eclipse.smarthome.binding.drehbinding.internal.REST.RESTIOService;
import org.eclipse.smarthome.binding.drehbinding.internal.REST.RESTService;

public class RESTIOServiceImpl implements RESTIOService {

    RESTService restService;
    SubscriptionService subService;

    private static final String GET = "GET";
    private static final String PUT = "PUT";
    private static final String POST = "POST";
    private static final String DELETE = "DELETE";

    public RESTIOServiceImpl(RESTService restService) {
        this.restService = restService;
        subService = SubscriptionService.getInstance();
    }

    @Override
    public void callService(String serviceIdentifier, Map<String, String> params) {
        String urlString = "http://localhost:9090/webapi/test";
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        RESTRequest request = new RESTRequest(GET, url, params);

        restService.makeRestCall(request);
    }

    @Override
    public void addSubscription(RESTIOParticipant participant, String topic) {
        String urlString = "http://localhost:9090/webapi/subscriptionService";
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Map<String, String> params = new HashMap<>();
        params.put("identifier", participant.getIdentifier());
        params.put("topic", topic);
        params.put("callbackPort", "" + SubscriptionService.getInstance().getCallbackPort());
        RESTRequest request = new RESTRequest(POST, url, params);

        restService.makeRestCall(request);
        subService.addSubscription(participant, topic);
    }

    @Override
    public void removeSubscription(RESTIOParticipant participant, String topic) {
        String urlString = "http://localhost:9090/webapi/subscriptionService";
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Map<String, String> params = new HashMap<>();
        params.put("identifier", participant.getIdentifier());
        params.put("topic", topic);
        RESTRequest request = new RESTRequest(DELETE, url, params);

        restService.makeRestCall(request);
    }

    @Override
    public void hasSubscribed(RESTIOParticipant participant, String subscriptionServiceIdentifier) {
    }

    @Override
    public void getSubscribedServices(RESTIOParticipant participant) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isDeviceOnlineAndReachable() {
        callService("onlineAndReachable", null);
        return true;
    }

}
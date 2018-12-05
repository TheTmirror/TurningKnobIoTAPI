package org.eclipse.smarthome.binding.drehbinding.internal.REST.implementation;

import java.io.IOException;
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
    public RESTResponse callService(String serviceIdentifier) throws IOException {
        return callService(serviceIdentifier, null);
    }

    @Override
    public RESTResponse callService(String serviceIdentifier, Map<String, String> params) throws IOException {
        // url muss iwie aus den Discovery Configs gewonnen werden
        String urlString = "http://localhost:9090/webapi/functions/" + serviceIdentifier;
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        RESTRequest request = new RESTRequest(GET, url, params);

        return restService.makeRestCall(request);
    }

    @Override
    public void addSubscription(RESTIOParticipant participant, String topic) {
        // url muss iwie aus den Discovery Configs gewonnen werden
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

        RESTResponse response;
        try {
            response = restService.makeRestCall(request);
        } catch (IOException e) {
            participant.onFailedSubscription();
            return;
        }

        if (response.getResponseCode() == 200 || response.getResponseCode() == 204) {
            subService.addSubscription(participant, topic);
            participant.onSuccessfulSubscription();
        } else {
            participant.onFailedSubscription();
        }
    }

    @Override
    public void removeSubscription(RESTIOParticipant participant, String topic) {
        // url muss iwie aus den Discovery Configs gewonnen werden
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

        RESTResponse response;
        try {
            response = restService.makeRestCall(request);
        } catch (IOException e) {
            participant.onFailedUnsubscription();
            return;
        }

        if (response.getResponseCode() == 200 || response.getResponseCode() == 204) {
            // subService.removeSubscription(participant, topic);
            participant.onSuccessfulUnsubscription();
        } else {
            participant.onFailedUnsubscription();
        }
    }

    @Override
    public void hasSubscribed(RESTIOParticipant participant, String subscriptionServiceIdentifier) {
    }

    @Override
    public void getSubscribedServices(RESTIOParticipant participant) {
        // TODO Auto-generated method stub

    }

    @Override
    public synchronized boolean isDeviceOnlineAndReachable() {
        try {
            return (callService("onlineAndReachable", null).getResponseCode() == 204);
        } catch (IOException ex) {
            return false;
        }
    }

}
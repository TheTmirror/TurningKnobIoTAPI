package org.eclipse.smarthome.binding.drehbinding.internal.REST.implementation;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map.Entry;

import org.eclipse.smarthome.binding.drehbinding.internal.REST.RESTService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class RESTServiceImpl implements RESTService {

    private final Logger logger = LoggerFactory.getLogger(RESTServiceImpl.class);

    @Override
    public Gson makeRestCall(RESTRequest request) {
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) request.getUrl().openConnection();
            connection.setRequestMethod(request.getMethod());
            connection.setConnectTimeout(5000);

            if (request.getParams() != null) {
                for (Entry<String, String> param : request.getParams().entrySet()) {
                    connection.setRequestProperty(param.getKey(), param.getValue());
                }
            }
            logger.debug("About to connect!!!!!!!!!!!");
            logger.debug("" + connection.getResponseCode());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;

    }

    @Override
    public void GET() {
        // TODO Auto-generated method stub

    }

    @Override
    public void PUT() {
        // TODO Auto-generated method stub

    }

    @Override
    public void POST() {
        // TODO Auto-generated method stub

    }

    @Override
    public void DELETE() {
        // TODO Auto-generated method stub

    }

}
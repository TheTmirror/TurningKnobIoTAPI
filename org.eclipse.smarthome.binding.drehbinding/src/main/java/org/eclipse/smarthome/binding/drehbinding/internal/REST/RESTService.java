package org.eclipse.smarthome.binding.drehbinding.internal.REST;

import java.io.IOException;

import org.eclipse.smarthome.binding.drehbinding.internal.REST.implementation.RESTRequest;
import org.eclipse.smarthome.binding.drehbinding.internal.REST.implementation.RESTResponse;

public interface RESTService {

    public RESTResponse makeRestCall(RESTRequest request) throws IOException;

    public void GET();

    public void PUT();

    public void POST();

    public void DELETE();

}
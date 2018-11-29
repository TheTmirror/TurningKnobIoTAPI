package org.eclipse.smarthome.binding.drehbinding.internal.REST;

import org.eclipse.smarthome.binding.drehbinding.internal.REST.implementation.RESTRequest;

import com.google.gson.Gson;

public interface RESTService {

    public Gson makeRestCall(RESTRequest request);

    public void GET();

    public void PUT();

    public void POST();

    public void DELETE();

}
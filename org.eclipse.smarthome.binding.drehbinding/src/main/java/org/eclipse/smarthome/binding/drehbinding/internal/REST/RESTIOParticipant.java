package org.eclipse.smarthome.binding.drehbinding.internal.REST;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingUID;

public interface RESTIOParticipant {

    /**
     * Called after successful subscription to the desired subscriptionservice
     */
    public void onSuccessfulSubscription();

    /**
     * Called after failed subscription to the desired subscriptionservice
     */
    public void onFailedSubscription();

    /**
     * Called when a event happend which refers to the subscription
     *
     * @param Identifier of the service that was subscriped to
     * @param values
     */
    public void onSubcriptionEvent(@NonNull String topic, @Nullable String values);

    public ThingUID getThingUID();

}

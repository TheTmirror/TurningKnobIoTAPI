package org.tris.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.tris.REST.SubscriptionService;

public class SubscriptionManager {

	private static SubscriptionManager instance;
	private Map<String, Map<String, Subscription>> subs;
	private Lock lock;
	private Lock subLock;

	// Bootid workaround shit
	private Map<String, Long> currentBootids;

	private SubscriptionManager() {
		subs = new HashMap<>();
		currentBootids = new HashMap<>();

		lock = new ReentrantLock();
		subLock = new ReentrantLock();
	}

	public static synchronized SubscriptionManager getInstance() {
		if (instance == null) {
			instance = new SubscriptionManager();
		}

		return instance;
	}

	private boolean isSameBootid(Subscription subscription) {
		boolean result = false;
		synchronized (currentBootids) {
			/*
			 * Spezialfall. Wenn noch keine Subscription von diesem Subscriber vorhanden ist
			 * egal für welches topic, ist auch die bootid egal und deshalb gilt die neue
			 * bootid gleich der aktuellen
			 */
			if (currentBootids.get(subscription.getSubscriberIdentifier()) == null) {
				result = true;
			} else {
				result = subscription.getBootid() == currentBootids.get(subscription.getSubscriberIdentifier());
			}
		}
		return result;
	}

	public void addSubscription(Subscription subscription) {
		if (!isSameBootid(subscription)) {
			/*
			 * Wenn die Bootid nicht mehr der selben entspricht sind alle Subscriptions des
			 * selben Subscribers nicht mehr gültig. Sie müssen für jedes Topic entfernt
			 * werden.
			 * 
			 * Aufgrund der potentiellen Raceconditions wird hier sicherheitshalber eine
			 * vollständige Sperrung beider Locks vorgenommen. Dies geht bestimmt auch
			 * efizienter, ist mir gerade aber zu komplex und ich will erstmal ne generelle
			 * Lösung bevor ich mich mit Optimierung auseinander setze.
			 */

			lock.lock();
			subLock.lock();
			for (Entry<String, Map<String, Subscription>> e1 : subs.entrySet()) {
				String topic = e1.getKey();
				Map<String, Subscription> subsForTopicX = e1.getValue();

				if (subsForTopicX.containsKey(subscription.getSubscriberIdentifier())) {
					subsForTopicX.remove(subscription).getSubscriberIdentifier();
				}
			}
		}

		lock.lock();
		Map<String, Subscription> subsForTopicX = subs.get(subscription.getTopic());
		if (subsForTopicX == null) {
			subsForTopicX = new HashMap<>();
			subs.put(subscription.getTopic(), subsForTopicX);
		}

		subLock.lock();
		subsForTopicX.put(subscription.getSubscriberIdentifier(), subscription);
		subLock.unlock();
		lock.unlock();
	}

	public void removeSubscription(String topic, String subscriberIdentifier) {
		lock.lock();
		Map<String, Subscription> subsForTopicX = subs.get(topic);
		if (subsForTopicX == null) {
			lock.unlock();
			return;
		}

		subLock.lock();
		for (Entry<String, Subscription> e : subsForTopicX.entrySet()) {
			String subscriberId = e.getKey();
			Subscription sub = e.getValue();

			if (subscriberId.equals(subscriberIdentifier)) {
				subsForTopicX.remove(subscriberIdentifier);
			}
		}

		if (subsForTopicX.isEmpty()) {
			subs.remove(subsForTopicX);
		}

		subLock.unlock();
		lock.unlock();
	}

	public void removeSubscription(Subscription subscription) {
		lock.lock();
		Map<String, Subscription> subsForTopicX = subs.get(subscription.getTopic());
		if (subsForTopicX == null) {
			lock.unlock();
			return;
		}

		subLock.lock();
		subsForTopicX.remove(subscription.getSubscriberIdentifier());
		subLock.unlock();
		lock.unlock();
	}

	public Map<String, Map<String, Subscription>> getCopyOfSubs() {
		Map<String, Map<String, Subscription>> result = new HashMap<>();
		lock.lock();

		for (Entry<String, Map<String, Subscription>> e1 : subs.entrySet()) {
			String topic = e1.getKey();
			Map<String, Subscription> subsForTopicX = e1.getValue();

			Map<String, Subscription> subResult = new HashMap<>();
			subLock.lock();
			for (Entry<String, Subscription> e2 : subsForTopicX.entrySet()) {
				String subscriberIdentifier = e2.getKey();
				Subscription sub = e2.getValue();

				Subscription resultSub = new Subscription();
				resultSub.setSubscriberIdentifier(sub.getSubscriberIdentifier());
				resultSub.setTopic(sub.getTopic());
				resultSub.setCallbackAddress(sub.getCallbackAddress());
				resultSub.setPort(sub.getPort());
				resultSub.setBootid(sub.getBootid());
				subResult.put(sub.getSubscriberIdentifier(), resultSub);
			}

			subLock.unlock();
			result.put(topic, subResult);
		}

		lock.unlock();
		return result;
	}

	public Map<String, Subscription> getCopyOfSubsForTopic(String topic) {
		lock.lock();
		Map<String, Subscription> subsForTopicX = subs.get(topic);

		if (subsForTopicX == null) {
			lock.unlock();
			return null;
		}

		Map<String, Subscription> subResult = new HashMap<>();
		subLock.lock();
		for (Entry<String, Subscription> e2 : subsForTopicX.entrySet()) {
			String subscriberIdentifier = e2.getKey();
			Subscription sub = e2.getValue();

			Subscription resultSub = new Subscription();
			resultSub.setSubscriberIdentifier(sub.getSubscriberIdentifier());
			resultSub.setTopic(sub.getTopic());
			resultSub.setCallbackAddress(sub.getCallbackAddress());
			resultSub.setPort(sub.getPort());
			subResult.put(sub.getSubscriberIdentifier(), resultSub);
		}

		subLock.unlock();
		lock.unlock();

		return subResult;
	}

}
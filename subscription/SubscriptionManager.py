# -*- coding: utf-8 -*-
from threading import Lock
import logging

class SubscriptionManager:

    newLock = Lock()
    _instance = None
    def __new__(self):
        self.newLock.acquire()
        if not self._instance:
            self._instance = super(SubscriptionManager, self).__new__(self)
            self._instance.initialize()
            logging.debug('Created a new instance of SSubscriptionManager')
        self.newLock.release()
        return self._instance

    def initialize(self):
        #First topic, then identifier
        self.subs = dict()
        self.lock = Lock()

    def _partial_match(self, key, d):
        for k, v in d.items():
            if all(k1 == k2 or k2 is None for k1, k2 in zip(k, key)):
                yield v

    def partial_match_values(self, key, d):
        return list(self._partial_match(key, d))

    def partial_match_keys(self, key, d):
        result = list()
        matches = list(self._partial_match(key, d))
        for sub in matches:
            key = (sub.topic, sub.subscriberIdentifier, sub.bootid)
            result.append(key)

        return result

    def isSameBootid(self, sub):
        #Dies ist der Spezialfall wenn der Subscriber noch überhaupt nicht enthalten ist
        self.lock.acquire()
        key_matches = self.partial_match_keys((None, sub.subscriberIdentifier, None), self.subs)
        self.lock.release()
        if len(key_matches) == 0:
            return True

        #Hier kann dann überprüft werden ob er schon jemals mit der gewünschten Bootid subscribed hat
        #Sollte dies nicht der Fall sein, handelt es sich definitiv nicht um die schon vorhandenen bootid
        self.lock.acquire()
        key_matches = self.partial_match_keys((None, sub.subscriberIdentifier, sub.bootid), self.subs)
        self.lock.release()
        logging.debug('Found these keys: %s' % (key_matches))
        if len(key_matches) > 0:
            return True
        else:
            return False
            
    def addSubscription(self, sub):
        if not self.isSameBootid(sub):
            logging.info('New Bootid is available - removing old entries')
            self.lock.acquire()
            keysToRemove = self.partial_match_keys((None, sub.subscriberIdentifier, None), self.subs)
            logging.debug('Keys to remove are: %s' % keysToRemove)
            for topic, subid, bootid in keysToRemove:
                del self.subs[(topic, subid, bootid)]
                logging.debug('%s - %s removed' % (topic, subid))
            self.lock.release()

        self.lock.acquire()
        self.subs[(sub.topic, sub.subscriberIdentifier, sub.bootid)] = sub
        self.lock.release()
        logging.info('Added subscription')

    def removeSubscription(self, topic, subscriber, bootid):
        self.lock.acquire()
        if self.subs.get((topic, subscriber, bootid), None) is None:
            logging.debug('Nothing to remove')
        else:
            del self.subs[(topic, subscriber, bootid)]
            logging.debug('Requested Subscription got removed')
        self.lock.release()

    def getSubscriptionsForTopicX(self, topic):
        self.lock.acquire()
        result = self.partial_match_values((topic, None, None), self.subs)
        self.lock.release()
        return result

if __name__ == '__main__':
    from Subscription import Subscription
    logging.basicConfig(level = logging.DEBUG)
    sm = SubscriptionManager()
    s1 = Subscription(subscriberIdentifier='sa', topic='te', bootid=1)
    s2 = Subscription(subscriberIdentifier='sb', topic='tf', bootid=1)
    s3 = Subscription(subscriberIdentifier='sc', topic='tg', bootid=1)
    s4 = Subscription(subscriberIdentifier='sd', topic='th', bootid=1)
    s5 = Subscription(subscriberIdentifier='sd', topic='ti', bootid=1)
    s6 = Subscription(subscriberIdentifier='sd', topic='th', bootid=2)
    s7 = Subscription(subscriberIdentifier='sr', topic='th', bootid=2)

    sm.addSubscription(s1)
    logging.debug(sm.subs)
    sm.addSubscription(s2)
    sm.addSubscription(s3)
    sm.addSubscription(s4)
    sm.addSubscription(s5)
    logging.debug(sm.subs)
    sm.addSubscription(s6)
    logging.debug(sm.subs)

    sm.removeSubscription('a', 'b', 'c')
    sm.removeSubscription(s1.topic, s1.subscriberIdentifier, s1.bootid)
    sm.removeSubscription(s4.topic, s4.subscriberIdentifier, s4.bootid)
    sm.removeSubscription(s6.topic, s6.subscriberIdentifier, s6.bootid)
    logging.debug(sm.subs)

    sm.addSubscription(s6)
    sm.addSubscription(s7)
    logging.debug(sm.getSubscriptionsForTopicX('th'))

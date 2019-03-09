class Subscription:

    def __init__(self, subscriberIdentifier = None, topic = None, callbackAddress = None, port = None, bootid = None):
        self.subscriberIdentifier = subscriberIdentifier
        self.topic = topic
        self.callbackAddress = callbackAddress
        self.port = port
        self.bootid = bootid

    def __repr__(self):
        return 'SubscriberIdentifier: %s - Topic: %s - Bootid: %d' % (self.subscriberIdentifier, self.topic, self.bootid)

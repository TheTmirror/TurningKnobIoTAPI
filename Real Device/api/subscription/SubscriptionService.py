import logging
from SubscriptionManager import SubscriptionManager
from Subscription import Subscription
from flask import Blueprint, request
import socket

class SubscriptionService:

    api = Blueprint('api', __name__)
    
    @api.route("/subscribe", methods=['POST'])
    def subscribe():
        topic = request.headers.get('topic')
        subscriberIdentifier = request.headers.get('identifier')
        bootid = int(request.headers.get('bootid'))
        callbackAddress = request.remote_addr
        callbackPort = int(request.headers.get('callbackPort'))

        logging.debug('topic: %s' % (topic))
        logging.debug('subscriberIdentifier: %s' % (subscriberIdentifier))
        logging.debug('bootid: %s' % (bootid))
        logging.debug('callbackAddress: %s' % (callbackAddress))
        logging.debug('callbackPort: %s' % (callbackPort))

        sub = Subscription(topic=topic, subscriberIdentifier=subscriberIdentifier, bootid=bootid, callbackAddress=callbackAddress, port=callbackPort)
        sm = SubscriptionManager()
        sm.addSubscription(sub)
        logging.info('Subscribed successfully')
        logging.debug(sm.subs)

        return ('', 204)

    @api.route('/unsubscribe', methods=['DELETE'])
    def unsubscribe():
        topic = request.headers.get('topic')
        subscriberIdentifier = request.headers.get('identifier')
        bootid = int(request.headers.get('bootid'))
        callbackAddress = request.remote_addr
        callbackPort = int(request.headers.get('callbackPort'))

        sm = SubscriptionManager()
        sm.removeSubscription(topic, subscriberIdentifier, bootid)

        logging.info('Unsubscribed successfully')
        logging.debug(sm.subs)

        return ('', 204)

    def onEvent(self, event):
        #It's not a real copy. The objects can change while they
        #are worked on here
        sm = SubscriptionManager()
        subs = sm.getSubscriptionsForTopicX(event.topic)

        if len(subs) == 0:
            return

        for sub in subs:
            self.sendEventNotification(sub, event)

    def sendEventNotification(self, sub, event):
        logging.info("I'm notifying %s" % (sub.subscriberIdentifier))
        sentence = 'topic:' + event.topic + ';'

        for key, value in event.values.items():
            logging.debug('Key: %s, Value: %s' % (key, value))
            sentence = sentence + key + ':' + value + ';'

        logging.debug('My Response %s' % (sentence))
        sendSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sendSocket.connect((sub.callbackAddress, sub.port))
        sendSocket.send(sentence.encode('utf-8'))
        sendSocket.close()

if __name__ == '__main__':
    logging.basicConfig(level=logging.DEBUG)
    sm = SubscriptionManager()
    from Subscription import Subscription
    s1 = Subscription(topic='t1', subscriberIdentifier='s1', bootid=1)
    s2 = Subscription(topic='t2', subscriberIdentifier='s2', bootid=1)
    s3 = Subscription(topic='t1', subscriberIdentifier='s3', bootid=1)
    s4 = Subscription(topic='t4', subscriberIdentifier='s4', bootid=1)

    #sm.addSubscription(s1)
    #sm.addSubscription(s2)
    #sm.addSubscription(s3)
    #sm.addSubscription(s4)

    #print(sm.subs)

    ss = SubscriptionService()
    from Event import Event
    e1 = Event('t1')
    #ss.onEvent(e1)

    from flask import Flask
    path = '/home/pi/Desktop/api/deviceInformations'
    app = Flask(__name__, static_folder=path, static_url_path='')
    app.register_blueprint(ss.api)
    app.run(host='0.0.0.0')

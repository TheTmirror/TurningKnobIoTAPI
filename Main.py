import sys
sys.path.insert(0, '/home/pi/Desktop/api/background')
sys.path.insert(0, '/home/pi/Desktop/api/subscription')

import logging

class APIController:

    def setupAPI(self):
        from XML import DeviceInformations, DiscoveryDescriptionCreator, DiscoveryDescriptionInformations
        logging.basicConfig(level=logging.INFO)
        logging.info('Setting up device description')
        di = DeviceInformations()
        ddc = DiscoveryDescriptionCreator()
        ddc.createDeviceDescription(di)
        ddi = DiscoveryDescriptionInformations()

    def startAPI(self):
        from threading import Thread
        from flask import Flask
        from SubscriptionService import SubscriptionService
        from UpnpManager import UpnpManager
    
        logging.info('Starting SubscriptionService')
        ss =  SubscriptionService()
        path = '/home/pi/Desktop/api/deviceInformations'
        app = Flask(__name__, static_folder=path, static_url_path='')
        app.register_blueprint(ss.api)
        args = dict()
        args['host'] = '0.0.0.0'
        t1 = Thread(target=app.run, kwargs=args)
        t1.start()

        logging.info('Starting UpnpManager')
        upnpManager = UpnpManager()
        upnpManager.start()

if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO)
    c = APIController()
    c.setupAPI()
    c.startAPI()

    #import time
    #time.sleep(20)
    #from Event import Event
    #values = dict()
    #values['name'] = 'HierStehtEinName'
    #e = Event('newMotionEvent', values)
    #ss.onEvent(e)

    #time.sleep(10)
    #e.values['name'] = 'AnotherName'
    #ss.onEvent(e)

from threading import Thread
import time

from SearchListener import SearchListener
from Announcer import Announcer

import logging

class UpnpManager(Thread):

    expiration_time = 60;

    def __init__(self):
        Thread.__init__(self)

    def run(self):
        logging.info('UpnpManager started')
        self.delayBoot()

        bootid = int(time.time())
        logging.debug(bootid)

        searchListener = SearchListener(self.expiration_time, bootid)
        announcer = Announcer(self.expiration_time, bootid)

        searchListener.start()
        announcer.start()

    def delayBoot(self):
        logging.info('Delaying boot for {} seconds. This makes sure, that all old alive messages expired'.format(self.expiration_time))
        time.sleep(self.expiration_time)

if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO)
    manager = UpnpManager()
    manager.start()

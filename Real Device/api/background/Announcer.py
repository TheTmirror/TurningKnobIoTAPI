from threading import Thread, Lock
import random
import time

from AliveAnnouncer import AliveAnnouncer
from ByeByeAnnouncer import ByeByeAnnouncer

import logging

class Announcer(Thread):

    ALIVE_TYPE = 'alive'
    SHUTDOWN_TYPE = 'shutdown'

    shutdownLock = Lock()
    shutdownFlag = False

    def __init__(self, expirationTime, bootid):
        Thread.__init__(self)

        self.expirationTime = expirationTime
        self.bootid = bootid

        self.alive = AliveAnnouncer(expirationTime, bootid)
        self.bye = ByeByeAnnouncer(bootid)

    def run(self):
        logging.info('Announcer started')
        Thread(target=self.onEvent, args=(self.ALIVE_TYPE,)).start()
        Thread(target=self.timer).start()

    def timer(self):
        while True:
            timeout = random.randint(0, self.expirationTime / 2)
            logging.info('Next alive msg in: %d' % (timeout))
            time.sleep(timeout)

            self.shutdownLock.acquire()
            if not self.shutdownFlag:
                Thread(target=self.onEvent, args=(self.ALIVE_TYPE,)).start()
                self.shutdownLock.release()
            else:
                self.shutdownLock.release()
                logging.debug('Finishing Timer due to shutdown')
                break

    def onEvent(self, type):
        if type == self.ALIVE_TYPE:
            self.alive.sendAliveMessageBundle()
        else:
            self.shutdownLock.acquire()
            self.shutdownFlag = True
            self.bye.sendByeByeMessageBundle()
            self.shutdownLock.release()

    def shutdown(self):
        self.onEvent(self.SHUTDOWN_TYPE)

if __name__ == '__main__':
    logging.basicConfig(level=logging.DEBUG)
    a = Announcer(10, 1)
    a.start()
    time.sleep(15)
    a.shutdown()

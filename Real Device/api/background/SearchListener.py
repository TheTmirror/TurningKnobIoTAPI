# -*- coding: utf-8 -*-
from threading import Thread
import sys
sys.path.insert(0, '/home/pi/Desktop/api')
from XML import DeviceInformations, DiscoveryDescriptionInformations
import socket
import struct
import time
from queue import Queue
from concurrent.futures import ThreadPoolExecutor
import logging

class SearchListener(Thread):

    NEW_LINE = '\r\n'
    BASE = "HTTP/1.1 200 OK" + NEW_LINE + "CACHE-CONTROL: max-age = %d" + NEW_LINE + "EXT:" + NEW_LINE + "LOCATION: %s" + NEW_LINE + "SERVER: %s" + NEW_LINE + "ST: %s" + NEW_LINE + "USN: %s" + NEW_LINE + "BOOTID.UPNP.ORG: %d" + NEW_LINE + NEW_LINE

    MULTICAST_ADDRESS = '239.255.255.250'
    MULTICAST_PORT = 1900

    DDD_LOCATION = 'http://localhost:9090/discovery.xml'
    SERVER = 'WINDOWS/7, UPnP/1.0, Drehknopf/1.0'
    ST_ALL = 'ssdp:all'
    ST_1 = 'upnp:rootdevice'

    def __init__(self, expirationTime, bootId):
        Thread.__init__(self)

        deviceInfos = DeviceInformations()
        descriptionInfos = DiscoveryDescriptionInformations()

        self.expirationTime = expirationTime
        self.bootId = bootId

        self.UUID = deviceInfos.UUID
        self.domainName = deviceInfos.domainName
        self.deviceType = deviceInfos.deviceType
        self.descriptionDeviceType = descriptionInfos.deviceType
        self.version = deviceInfos.version

        self.ST_2 = 'uuid:' + self.UUID
        self.ST_3 = self.descriptionDeviceType
        self.USN_1 = self.ST_2 + '::upnp:rootdevice'
        self.USN_2 = self.ST_2
        self.USN_3 = self.ST_2 + '::' + self.descriptionDeviceType

        #MULTICAST STUFF
        #Receiver
        MCAST_GRP = '239.255.255.250'
        MCAST_PORT = 1900
 
        sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
        sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        sock.bind((MCAST_GRP, MCAST_PORT))

        mreq = struct.pack("4sl", socket.inet_aton(MCAST_GRP), socket.INADDR_ANY)
 
        sock.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, mreq)
        self.recvsock = sock
        #SendSock
        MCAST_GRP = '239.255.255.250'
        MCAST_PORT = 1900
 
        sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
        sock.setsockopt(socket.IPPROTO_IP, socket.IP_MULTICAST_TTL, 32)
        #sock.sendto("Hello World", (MCAST_GRP, MCAST_PORT))
        self.sendsock = sock

        #exectuor
        threads = 9
        self.exec = ThreadPoolExecutor(threads)

        #QUEUE
        self.jobList = Queue()
        self.doneJobs = Queue()

    def run(self):
        logging.info('SearchListener started')
        #print(self.BASE % (3, 'LOCATION', 'SERVER', 'ST', 'USN', 3))

        #self.exec.submit(self.receive)
        #self.receive()
        #self.exec.submit(self.checkQueue)
        #self.exec.submit(self.process)
        #self.process()
        #self.exec.submit(self.send)
        #self.send()


        Thread(target=self.receive).start()
        for i in range(0, 4):
            Thread(target=self.process).start()
            Thread(target=self.send).start()
        #Benutze eher den ThreadPool, wird hier nur nicht benutzt, da
        #irgendwie keine Exceptions geprintet werden
        #self.exec.submit(self.receive)
        #for i in range(0, 4):
        #    self.exec.submit(self.process)
        #    self.exec.submit(self.send)

    def receive(self):
        while True:
            logging.debug('Waiting to receive message')
            data, address = self.recvsock.recvfrom(1024)
            logging.debug('Received %s bytes from %s' % (len(data), address))
            #print('DATA:')
            #print(data.decode() + '\n\n')
            self.jobList.put((data.decode(), address))
    def process(self):
        logging.debug('Processing')
        while True:
            data, addr = self.jobList.get(True)

            if self.isSearch(data):
                logging.info('Received M-SEARCH')
                self.processMessage(data, addr)
            

    def send(self):
        logging.debug('Sending started')
        while True:
            response = self.doneJobs.get(True)
            self.sendsock.sendto(response.msg.encode('utf-8'), (response.addr, response.port))
            logging.info('Sent response')

    def isSearch(self, job):
        return job.startswith('M-SEARCH')

    def getST(self, message):
        logging.debug('Searching for ST')

        while len(message) > 0:
            indexN = message.index('\n')
            indexR = message.index('\r')
            if indexN < indexR:
                index = indexN
            else:
                index = indexR
            nextRow = message[0:index]
            message = message[index + len('\n\r'):]

            #print('DATA')
            #print('Index: %d' % (index))
            #print('Next Row: %s!!!!!' % (nextRow))
            #print('Message: %s!!!!!!' % (message))

            if nextRow.startswith('ST:'):
                return nextRow[len('ST: '):]
            

    def processMessage(self, message, addr):
        logging.debug('Processing Message')
        st = self.getST(message)
        logging.debug('ST is %s' % (st))

        if st == self.ST_ALL:
            msgA1 = self.buildMessage1()
            msgA2 = self.buildMessage2()
            msgA3 = self.buildMessage3()

            self.doneJobs.put(Response(msgA1, addr))
            self.doneJobs.put(Response(msgA2, addr))
            self.doneJobs.put(Response(msgA3, addr))

            logging.debug('Processed messages')
        elif st == self.ST_1:
            msgA1 = self.buildMessage1()
            self.doneJobs.put(Response(msgA1, addr))
        elif st == self.ST_2:
            msgA2 = self.buildMessage2()
            self.doneJobs.put(Response(msgA2, addr))
        elif st == self.ST_3:
            msgA3 = self.buildMessage3()
            self.doneJobs.put(Response(msgA3, addr))
        else:
            logging.warning('ST -- %s -- was unknown. No response is sent' % (st))

        logging.debug('Message was processed')

    def buildMessage1(self):
        return self.BASE % (self.expirationTime, self.DDD_LOCATION, self.SERVER, self.ST_1, self.USN_1, self.bootId)

    def buildMessage2(self):
        return self.BASE % (self.expirationTime, self.DDD_LOCATION, self.SERVER, self.ST_2, self.USN_3, self.bootId)

    def buildMessage3(self):
        return self.BASE % (self.expirationTime, self.DDD_LOCATION, self.SERVER, self.ST_3, self.USN_3, self.bootId)

class Response:

    def __init__(self, msg, addr):
        addr, port = addr
        self.msg = msg
        self.addr = addr
        self.port = port

if __name__ == '__main__':
        logging.basicConfig(level=logging.DEBUG)
        sl = SearchListener(1, 1)
        sl.start()

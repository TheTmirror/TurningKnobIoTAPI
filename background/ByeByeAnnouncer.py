import sys
sys.path.insert(0, '/home/pi/Desktop/api')
from XML import DeviceInformations, DiscoveryDescriptionInformations
import logging

import socket

class ByeByeAnnouncer:

    NEW_LINE = '\r\n'
    BASE = "NOTIFY * HTTP/1.1" + NEW_LINE + "HOST: %s%d" + NEW_LINE + "NT: %s" + NEW_LINE + "NTS: %s" + NEW_LINE + "USN: %s" + NEW_LINE + "BOOTID.UPNP.ORG: %d" + NEW_LINE + "CONFIGID.UPNP.ORG: %d" + NEW_LINE + NEW_LINE

    MULTICAST_ADDRESS = '239.255.255.250'
    MULTICAST_PORT = 1900

    NT_1 = 'upnp:rootdevice'
    NTS = 'ssdp:byebye'

    def __init__(self, bootId):
        deviceInfos = DeviceInformations()
        descriptionInfos = DiscoveryDescriptionInformations()

        self.bootId = bootId
        self.configId = deviceInfos.configId

        self.UUID = deviceInfos.UUID
        self.domainName = deviceInfos.domainName
        self.deviceType = deviceInfos.deviceType
        self.descriptionDeviceType = descriptionInfos.deviceType
        self.version = deviceInfos.version

        self.NT_2 = 'uuid:' + self.UUID
        self.NT_3 = self.descriptionDeviceType
        self.USN_1 = self.NT_2 + '::upnp:rootdevice'
        self.USN_2 = self.NT_2
        self.USN_3 = self.NT_2 + '::' + self.descriptionDeviceType

        #Socketstuff
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
        self.sock.setsockopt(socket.IPPROTO_IP, socket.IP_MULTICAST_TTL, 32)

    def sendByeByeMessageBundle(self):
        msg1 = self.buildByeByeMessage1()
        msg2 = self.buildByeByeMessage2()
        msg3 = self.buildByeByeMessage3()

        self.sock.sendto(msg1.encode('utf-8'), (self.MULTICAST_ADDRESS, self.MULTICAST_PORT))
        self.sock.sendto(msg2.encode('utf-8'), (self.MULTICAST_ADDRESS, self.MULTICAST_PORT))
        self.sock.sendto(msg3.encode('utf-8'), (self.MULTICAST_ADDRESS, self.MULTICAST_PORT))
        logging.info('Sent ByeBye Message Bundle')

    def buildByeByeMessage1(self):
        return self.BASE % (self.MULTICAST_ADDRESS, self.MULTICAST_PORT, self.NT_1, self.NTS, self.USN_1, self.bootId, int(float(self.configId)))

    def buildByeByeMessage2(self):
        return self.BASE % (self.MULTICAST_ADDRESS, self.MULTICAST_PORT, self.NT_2, self.NTS, self.USN_2, self.bootId, int(float(self.configId)))

    def buildByeByeMessage3(self):
        return self.BASE % (self.MULTICAST_ADDRESS, self.MULTICAST_PORT, self.NT_3, self.NTS, self.USN_3, self.bootId, int(float(self.configId)))
    
if __name__ == '__main__':
    a = ByeByeAnnouncer(31)
    a.sendByeByeMessageBundle()


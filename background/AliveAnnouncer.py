import sys
sys.path.insert(0, '/home/pi/Desktop/api')
from XML import DeviceInformations, DiscoveryDescriptionInformations
import socket
import logging

class AliveAnnouncer:

    NEW_LINE = '\r\n'
    BASE = "NOTIFY * HTTP/1.1" + NEW_LINE + "HOST: %s%d" + NEW_LINE + "CACHE-CONTROL: max-age = %d" + NEW_LINE + "LOCATION: %s" + NEW_LINE + "NT: %s" + NEW_LINE + "NTS: %s" + NEW_LINE + "SERVER: %s" + NEW_LINE + "USN: %s" + NEW_LINE + "BOOTID.UPNP.ORG: %d" + NEW_LINE + "CONFIGID.UPNP.ORG: %d" + NEW_LINE + NEW_LINE

    MULTICAST_ADDRESS = '239.255.255.250'
    MULTICAST_PORT = 1900

    DDD_LOCATION = 'http://192.168.2.109:5000/discovery.xml'
    SERVER = 'WINDOWS/7, UPnP/1.0, Drehknopf/1.0'
    NT_1 = 'upnp:rootdevice'
    NTS = 'ssdp:alive'

    def __init__(self, expirationTime, bootId):
        deviceInfos = DeviceInformations()
        descriptionInfos = DiscoveryDescriptionInformations()

        self.expirationTime = expirationTime
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

    def sendAliveMessageBundle(self):
        msg1 = self.buildAliveMessage1()
        msg2 = self.buildAliveMessage2()
        msg3 = self.buildAliveMessage3()

        self.sock.sendto(msg1.encode('utf-8'), (self.MULTICAST_ADDRESS, self.MULTICAST_PORT))
        self.sock.sendto(msg2.encode('utf-8'), (self.MULTICAST_ADDRESS, self.MULTICAST_PORT))
        self.sock.sendto(msg3.encode('utf-8'), (self.MULTICAST_ADDRESS, self.MULTICAST_PORT))

        logging.info('Sent Alive Message Bundle')

    def buildAliveMessage1(self):
        return self.BASE % (self.MULTICAST_ADDRESS, self.MULTICAST_PORT, self.expirationTime, self.DDD_LOCATION, self.NT_1, self.NTS, self.SERVER, self.USN_1, self.bootId, int(float(self.configId)))

    def buildAliveMessage2(self):
        return self.BASE % (self.MULTICAST_ADDRESS, self.MULTICAST_PORT, self.expirationTime, self.DDD_LOCATION, self.NT_2, self.NTS, self.SERVER, self.USN_2, self.bootId, int(float(self.configId)))

    def buildAliveMessage3(self):
        return self.BASE % (self.MULTICAST_ADDRESS, self.MULTICAST_PORT, self.expirationTime, self.DDD_LOCATION, self.NT_3, self.NTS, self.SERVER, self.USN_3, self.bootId, int(float(self.configId)))
    
if __name__ == '__main__':
    a = AliveAnnouncer(30, 1)
    a.sendAliveMessageBundle()

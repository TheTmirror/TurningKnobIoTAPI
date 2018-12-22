import logging

class DeviceInformations:

    informationSource = '/home/pi/Desktop/api/deviceInformations/informations.xml'

    _instance = None
    def __new__(self):
        if not self._instance:
            self._instance = super(DeviceInformations, self).__new__(self)
            self._instance.fill()
        return self._instance
    
    def fill(self):
        logging.info('Filling Device Informations')
        def getNamespace(element):
            import re
            m = re.match('\{.*\}', element.tag)
            return m.group(0) if m else ''

        import xml.etree.ElementTree as ET
        tree = ET.parse(self.informationSource)
        root = tree.getroot()
        namespace = getNamespace(root)

        for child in root:
            if child.tag == 'configId':
                self.configId = child.text
            elif child.tag == 'upnpVersion':
                self.upnpVersion = child.text
            elif child.tag == 'domainName':
                self.domainName = child.text
            elif child.tag == 'deviceType':
                self.deviceType = child.text
            elif child.tag == 'version':
                self.version = child.text
            elif child.tag == 'friendlyName':
                self.friendlyName = child.text
            elif child.tag == 'manufacturer':
                self.manufacturer = child.text
            elif child.tag == 'modelName':
                self.modelName = child.text
            elif child.tag == 'modelNumber':
                self.modelNumber = child.text
            elif child.tag == 'serialNumber':
                self.serialNumber = child.text
            elif child.tag == 'UUID':
                self.UUID = child.text
            elif child.tag == 'UDN':
                self.UDN = child.text

class DiscoveryDescriptionCreator:

    targetFile = '/home/pi/Desktop/api/deviceInformations/discovery.xml'

    def createDeviceDescription(self, deviceInformations):
        logging.info('Creating desscription.xml')

        import xml.etree.ElementTree as ET
        root = ET.Element('root')
        tree = ET.ElementTree(root)

        #ET.register_namespace('xmlns', 'urn:schemas-upnp-org:device-1-0')
        root.set('xmls', 'urn:schemas-upnp-org:device-1-0')
        root.set('configID', infos.configId)

        specVersion = ET.SubElement(root, 'specVersion')
        major = ET.SubElement(specVersion, 'major')
        minor = ET.SubElement(specVersion, 'minor')

        device = ET.SubElement(root, 'device')
        deviceType =ET.SubElement(device, 'deviceType')
        friendlyName = ET.SubElement(device, 'friendlyName')
        manufacturer = ET.SubElement(device, 'manufacturer')
        modelName = ET.SubElement(device, 'modelName')
        modelNumber = ET.SubElement(device, 'modelNumber')
        serialNumber = ET.SubElement(device, 'serialNumber')
        UDN = ET.SubElement(device, 'UDN')

        major.text = ''
        minor.text = ''
        deviceType.text = 'urn:' + infos.domainName + ':device:' + infos.deviceType + ':' + infos.version
        friendlyName.text = infos.friendlyName
        manufacturer.text = infos.manufacturer
        modelName.text = infos.modelName
        modelNumber.text = infos.modelNumber
        serialNumber.text = infos.serialNumber
        UDN.text = infos.UDN

        #print(ET.tostring(root, encoding='utf8').decode())
        tree.write(self.targetFile, encoding='utf-8', xml_declaration=True)

class DiscoveryDescriptionInformations:

    informationSource = '/home/pi/Desktop/api/deviceInformations/discovery.xml'

    _instance = None
    def __new__(self):
        if not self._instance:
            self._instance = super(DiscoveryDescriptionInformations, self).__new__(self)
            self._instance.fill()
        return self._instance

    def getNamespace(self, element):
            import re
            m = re.match('\{.*\}', element.tag)
            return m.group(0) if m else ''

    def fill(self):
        logging.info('Filling Discovery Description Informations')
        
        import xml.etree.ElementTree as ET
        tree = ET.parse(self.informationSource)
        root = tree.getroot()
        namespace = self.getNamespace(root)

        for child in root.find(namespace + 'specVersion'):
            if child.tag == 'major':
                self.major = child.text
            elif child.tag == 'minor':
                self.minor = child.text

        for child in root.find(namespace + 'device'):
            if child.tag == 'deviceType':
                self.deviceType = child.text
            elif child.tag == 'friendlyName':
                self.friendlyName = child.text
            elif child.tag == 'manufacturer':
                self.manufacturer = child.text
            elif child.tag == 'modelName':
                self.modelName = child.text
            elif child.tag == 'modelNumber':
                self.modelNumber = child.text
            elif child.tag == 'serialNumber':
                self.serialNumber = child.text
            elif child.tag == 'UDN':
                self.UDN = child.text

if __name__ == '__main__':
    logging.basicConfig(level=logging.DEBUG)
    
    infos = DeviceInformations()
    print(infos.UUID)
    infos = DeviceInformations()
    print(infos.UUID)
    infos = DeviceInformations()
    print(infos.UUID)
    
    creator = DiscoveryDescriptionCreator()
    creator.createDeviceDescription(infos)

    infos = DiscoveryDescriptionInformations()
    print(infos.UDN)
    infos = DiscoveryDescriptionInformations()
    print(infos.UDN)
    infos = DiscoveryDescriptionInformations()
    print(infos.UDN)

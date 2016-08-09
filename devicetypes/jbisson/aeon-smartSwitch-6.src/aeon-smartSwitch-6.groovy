/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Z-Wave Metering Dimmer
 *
 *  Copyright 2014 SmartThings
 * 
 * modified by jbisson 2016-08-08 version 3.0
 * modified by lg kahn 2015-09 version 2
 * modified to support the modes of the aeon smart switch 6, night light, 
 *
 * The brightness intensity only works in energy mode (does not work in nightlight mode)
 * You can only change the led color in nightlight mode
*/
metadata {
	definition (name: "Aeon Labs Smart Switch 6", namespace: "jbisson", author: "Jonathan Bisson") {
		capability "Switch"
		capability "Polling"
		capability "Power Meter"
		capability "Energy Meter"
		capability "Refresh"
		capability "Switch Level"
		capability "Sensor"
		capability "Actuator"
        capability "Configuration"
        capability "Color Control"
      
       command "energy"
       command "momentary"
       command "nightLight"
       command "setColor"
       
	   command "reset"
       command "factoryReset"
       command "setBrightnessLevel"
       command "getDeviceInfo"
       
       attribute "deviceMode", "String"
  
	   fingerprint inClusters: "0x26,0x32"
	}

	simulator {
		status "on":  "command: 2603, payload: FF"
		status "off": "command: 2603, payload: 00"
		status "09%": "command: 2603, payload: 09"
		status "10%": "command: 2603, payload: 0A"
		status "33%": "command: 2603, payload: 21"
		status "66%": "command: 2603, payload: 42"
		status "99%": "command: 2603, payload: 63"

		for (int i = 0; i <= 10000; i += 1000) {
			status "power  ${i} W": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 4, scale: 2, size: 4).incomingMessage()
		}
		for (int i = 0; i <= 100; i += 10) {
			status "energy  ${i} kWh": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 0, scale: 0, size: 4).incomingMessage()
		}

		["FF", "00", "09", "0A", "21", "42", "63"].each { val ->
			reply "2001$val,delay 100,2602": "command: 2603, payload: $val"
		}
	}

    tiles(scale: 2) {
        multiAttributeTile(name:"mainPanel", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
            }
            tileAttribute ("device.power", key: "SECONDARY_CONTROL") {
                attributeState "power", label:'Power level: ${currentValue}W', icon: "st.Appliances.appliances17"
            }
        }
        standardTile("deviceMode", "deviceMode", canChangeIcon: true, canChangeBackground: true, width: 2, height: 2) {
            state "energy", label:'energy', action:"momentary", icon: "http://mail.lgk.com/aeonv6orange.png"
            state "momentary", label:'momentary', action:"nightLight", icon: "http://mail.lgk.com/aeonv6white.png"
            state "nightLight", label:'NightLight', action:"energy", icon: "http://mail.lgk.com/aeonv6blue.png"      
        }

        valueTile("power", "device.power", width: 2, height: 1) {
            state "default", label:'${currentValue} W'
        }

        valueTile("energy", "device.energy", width: 1, height: 1, decoration: "flat") {
            state "default", label:'${currentValue} kWh'
        }

        valueTile("statusText2", "device.statusText2", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
            state "statusText2", label:'Since\n${currentValue}\nago'
        }

        valueTile("amperage", "device.amperage", width: 2, height: 1) {
            state "default", label:'${currentValue} a'
        }

        valueTile("voltage", "device.voltage", width: 2, height: 1) {
            state "default", label:'${currentValue} v'
        }

        controlTile("levelSliderControl", "device.brightnessLevel", "slider", width: 2, height: 1) {
            state "level", action:"switch level.setLevel"
        }

        valueTile("levelSliderTxt", "device.brightnessLevel", decoration: "flat") {
            state "brightnessLevel", label:'${currentValue} %'
        }

        standardTile("refresh", "device.switch", decoration: "flat", width: 1, height: 1) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        standardTile("reset", "device.energy", decoration: "flat", width: 1, height: 1) {
            state "default", label:'reset', action:"reset"
        }

        controlTile("rgbSelector", "device.color", "color", height: 3, width: 3) {
            state "color", action:"setColor"
        }

        standardTile("deviceMode", "deviceMode", canChangeIcon: true, canChangeBackground: true) {
            state "energy", label:'energy', action:"momentary", icon: "http://mail.lgk.com/aeonv6orange.png"
            state "momentary", label:'momentary', action:"nightLight", icon: "http://mail.lgk.com/aeonv6white.png"
            state "nightLight", label:'NightLight', action:"energy", icon: "http://mail.lgk.com/aeonv6blue.png"      
        }

        standardTile("configure", "device.power", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
            state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
        }
        
        valueTile("deviceInfo", "deviceInfo", decoration: "flat", width: 3, height: 1) {
            state "default", label:'${currentValue}', action:"getDeviceInfo"
        }
        
        main(["mainPanel", "power","energy","voltage","amperage"] )
        details(["mainPanel", "deviceMode", "power", "energy", "statusText2", "amperage","voltage", 
                 "rgbSelector", "levelSliderControl","levelSliderTxt", "refresh","reset","configure", "deviceInfo"])  
    }
}

preferences {
	input description: "Once you change values on this page, the \"Synced\" Status will become \"Pending\" status. You can then force the sync by clicking the device button or just wait for the next WakeUp (60 minutes).",    displayDuringSetup: false,    type: "paragraph",    element: "paragraph"  
	
    input name: "refreshInterval", type: "number", title: "Time interval \n\nSet the time interval (secondes) between each reports.\n", required: true
    
    input name: "onlySendReportIfValueChange", type: "bool", title: "Only send report if value change (either in terms of wattage or a %)\n", defaultValue: "false"
    
    input description: "The next two parameters are only working if the only send report is set to true.", type: "paragraph", element: "paragraph"
    
    input name: "minimumChangeWatts", type: "number", title: "Minimum change in wattage for a report to be sent (0 - 100).\n", defaultValue: "25", range: "0..100"
    input name: "minimumChangePercent", type: "number", title: "Minimum change in percentage for a report to be sent (0 - 60000)\n", defaultValue: "5", range: "0..60000"
    
    input name: "includeWattInReport", type: "bool", title: "Include energy meter (W) in report?\n", defaultValue: "true"
    input name: "includeVoltageInReport", type: "bool", title: "Include voltage (V) in report?\n", defaultValue: "true"
    input name: "includeCurrentInReport", type: "bool", title: "Include current (A) in report?\n", defaultValue: "true"
    input name: "includeCurrentUsageInReport", type: "bool", title: "Include current usage (kWh) in report?\n", defaultValue: "true"    
}

// parse events into attributes
def parse(String description) {
	def result = null
	log.debug "in parse got message string : '$description'"
    
	if (description != "updated") {
		def cmd = zwave.parse(description, [0x20: 1, 0x26: 3, 0x70: 1, 0x32:3])
        log.debug "got command = '$cmd'"
      
		if (cmd) {
			result = zwaveEvent(cmd)
	        //log.debug("'$description' parsed to $result $result?.name")
		} else {
			log.debug("Couldn't zwave.parse '$description'")
		}
	}
    
    updateStatus()    
    result
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	log.debug "in SwitchBinaryReport"
}


def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	log.debug "in SensorMultilevelReport"
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	//log.debug "in basic report"
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	//log.debug "in basic set"
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
	//log.debug "in multi level report"
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	log.debug "in meter report cmd = '$cmd'"
	if (cmd.meterType == 1) {
		if (cmd.scale == 0) {
     	    log.debug " got kwh $cmd.scaledMeterValue"
			return createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
		} else if (cmd.scale == 1) {
        	log.debug " got kVAh $cmd.scaledMeterValue"
			return createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kVAh")
		} else if (cmd.scale == 2) { 
        	log.debug " got wattage $cmd.scaledMeterValue"
			return createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
		} else if (cmd.scale == 4) { // Volts
            log.debug " got voltage $cmd.scaledMeterValue"
           return createEvent(name: "voltage", value: Math.round(cmd.scaledMeterValue), unit: "V")
		} else if (cmd.scale == 5) { //amps scale 5 is amps even though not documented
            log.debug " got amperage = $cmd.scaledMeterValue"
           return createEvent(name: "amperage", value: cmd.scaledMeterValue, unit: "A")
		} else {
			return createEvent(name: "electric", value: cmd.scaledMeterValue, unit: ["pulses", "V", "A", "R/Z", ""][cmd.scale - 3])
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	log.debug "on ConfigurationReport()"
    
    switch (cmd.parameterNumber) {
    	case 0x51:
        	log.debug "received device mode event"
        	if (cmd.configurationValue[0] == 0) {
            	return createEvent(name: "deviceMode", value: "energy", displayed: true)
            } else if (cmd.configurationValue[0] == 1) {
            	return createEvent(name: "deviceMode", value: "momentary", displayed: true)
            } else if (cmd.configurationValue[0] == 2) {
            	return createEvent(name: "deviceMode", value: "nightLight", displayed: true)
            }
    	break;
        case 0x54:
        	log.debug "received brightness level event"
        	return createEvent(name: "level", value: cmd.configurationValue[0], displayed: true)
        break;
    }
}

def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {	
    log.debug "Switch button was pressed"
    return createEvent(name: "hail", value: "hail", descriptionText: "Switch button was pressed", displayed: false)
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {	
    log.debug "applicationVersion:      ${cmd.applicationVersion}"
    log.debug "applicationSubVersion:   ${cmd.applicationSubVersion}"
    log.debug "zWaveLibraryType:        ${cmd.zWaveLibraryType}"
    log.debug "zWaveProtocolVersion:    ${cmd.zWaveProtocolVersion}"
    log.debug "zWaveProtocolSubVersion: ${cmd.zWaveProtocolSubVersion}"
    
    if (state.deviceInfo == null) {
    	state.deviceInfo = [:]
	}
    
    state.deviceInfo['applicationVersion'] = "${cmd.applicationVersion}"
    state.deviceInfo['applicationSubVersion'] = "${cmd.applicationSubVersion}"
    state.deviceInfo['zWaveLibraryType'] = "${cmd.zWaveLibraryType}"
    state.deviceInfo['zWaveProtocolVersion'] = "${cmd.zWaveProtocolVersion}"
    state.deviceInfo['zWaveProtocolSubVersion'] = "${cmd.zWaveProtocolSubVersion}"
    
    return updateDeviceInfo()
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) { 
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
    log.debug "manufacturerName: ${cmd.manufacturerName}"
    log.debug "productId:        ${cmd.productId}"
    log.debug "productTypeId:    ${cmd.productTypeId}"
    
    if (state.deviceInfo == null) {
    	state.deviceInfo = [:]
	}
    
    state.deviceInfo['manufacturerId'] = "${cmd.manufacturerId}"
    state.deviceInfo['manufacturerName'] = "${cmd.manufacturerName}"
    state.deviceInfo['productId'] = "${cmd.productId}"
    state.deviceInfo['productTypeId'] = "${cmd.productTypeId}"
    
    return updateDeviceInfo()
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.DeviceSpecificReport cmd) { 
	log.debug "deviceIdData:  	           ${cmd.deviceIdData}"
    log.debug "deviceIdDataFormat:         ${cmd.deviceIdDataFormat}"
    log.debug "deviceIdDataLengthIndicator:${cmd.deviceIdDataLengthIndicator}"
    log.debug "deviceIdType:               ${cmd.deviceIdType}"
    
    return updateDeviceInfo()
}

def configure() {
	log.debug "configure()"
	updateDeviceInfo()
    
    def switchAllMode = physicalgraph.zwave.commands.switchallv1.SwitchAllSet.MODE_INCLUDED_IN_THE_ALL_ON_ALL_OFF_FUNCTIONALITY
    if (switchAll == "Disabled") {
		switchAllMode = physicalgraph.zwave.commands.switchallv1.SwitchAllSet.MODE_EXCLUDED_FROM_THE_ALL_ON_ALL_OFF_FUNCTIONALITY
    } else if (switchAll == "Off Enabled") {
		switchAllMode = physicalgraph.zwave.commands.switchallv1.SwitchAllSet.MODE_EXCLUDED_FROM_THE_ALL_ON_FUNCTIONALITY_BUT_NOT_ALL_OFF
    } else if (switchAll == "On Enabled") {
		switchAllMode = physicalgraph.zwave.commands.switchallv1.SwitchAllSet.MODE_EXCLUDED_FROM_THE_ALL_OFF_FUNCTIONALITY_BUT_NOT_ALL_ON
    }

	def reportGroup;
    reportGroup = ("$includeVoltageInReport" == "true" ? 1 : 0)
	reportGroup += ("$includeCurrentInReport" == "true" ? 2 : 0)
	reportGroup += ("$includeWattInReport" == "true" ? 4 : 0)
	reportGroup += ("$includeCurrentUsageInReport" == "true" ? 8 : 0)
    
    log.debug "setting configuration refresh interval: " + new BigInteger("$refreshInterval")
    
    /***************************************************************
    Device specific configuration parameters
    ----------------------------------------------------------------
    Param   Size    Default Description
    ------- ------- ------- ----------------------------------------
    0x03 (3)    1       0   Current Overload Protection. Load will be closed when the Current overrun (US: 15.5A, other country: 16.2A) and the 
    						time more than 2 minutes (0=disabled, 1=enabled).
    0x14 (20)   1       0   Configure the output load status after re-power on (0=last status, 1=always on, 2=always off)
    0x21 (33)   4           Set the RGB LED color value for testing. alternate rgb color level ie res,blue,green,red ie 00ffffff
    0x50 (80)   1       0       Enable to send notifications to associated devices in Group 1 when load changes (0=nothing, 1=hail CC, 2=basic CC report)
    0x51 (81)   1       0       mode 0 - energy, 1 - momentary indicator, 2 - night light
    0x53 (83)   3       0      hex value ffffff00 .. only night light mode 
    0x54 (84)   1       50       dimmer level 0 -100 (doesn't work in night light mode)
    0x5A (90)   1       1       Enables/disables parameter 0x5A and 0x5B below
    0x5B (91)   2       25      The value here represents minimum change in wattage (in terms of wattage) for a REPORT to be sent (default 50W, size 2 bytes).
    0x5C (92)   1       5      The value here represents minimum change in wattage (in terms of percentage) for a REPORT to be sent (default 10%, size 1 byte).
    0x65 (101)  4       0x00 00 00 04 Which reports need to send in Report group 1
    0x66 (102)  4       0x00 00 00 08 Which reports need to send in Report group 2
    0x67 (103)  4       0       Which reports need to send in Report group 3
    0x6F (111)  4       0x00 00 02 58 The time interval in seconds for sending Report group 1 (Valid values 0x01-0x7FFFFFFF).
    0x70 (112)  4       0x00 00 02 58 The time interval in seconds for sending Report group 2 (Valid values 0x01-0x7FFFFFFF).
    0x71 (113)  4       0x00 00 02 58 The time interval in seconds for sending Report group 3 (Valid values 0x01-0x7FFFFFFF).
    0xC8 (200)  1       0  Partner ID
    0xFC (252)  1       0  Enable/disable Configuration Locked (0 =disable, 1 =enable).
    0xFE (254)  2       0  Device Tag.
    0xFF (255)  1       N/A     Reset to factory default setting
    
    
    Configuration Values for parameters 0x65-0x67:
    BYTE  | 7  6  5  4  3  2  1  0
    ===============================
    MSB 0 | 0  0  0  0  0  0  0  0
    Val 1 | 0  0  0  0  0  0  0  0
    VAL 2 | 0  0  0  0  0  0  0  0
    LSB 3 | 0  0  0  0  A  B  C  0
    
    Bit A - Send Meter REPORT (for kWh) at the group time interval
    Bit B - Send Meter REPORT (for watt) at the group time interval
    Bit C - Automatically send(1) or don't send(0) Multilevel Sensor Report Command
    ***************************************************************/
    
    delayBetween([
        zwave.switchAllV1.switchAllSet(mode: switchAllMode).format(),
        zwave.configurationV1.configurationSet(parameterNumber: 0x50, size: 1, scaledConfigurationValue: 1).format(),	//Enable to send notifications to associated devices when load changes (0=nothing, 1=hail CC, 2=basic CC report)
        zwave.configurationV1.configurationSet(parameterNumber: 0x5A, size: 1, scaledConfigurationValue: ("$onlySendReportIfValueChange" == "true" ? 1 : 0)).format(),	//Enables parameter 0x5B and 0x5C (0=disabled, 1=enabled)
        zwave.configurationV1.configurationSet(parameterNumber: 0x5B, size: 2, scaledConfigurationValue: new BigInteger("$minimumChangeWatts")).format(),	//Minimum change in wattage for a REPORT to be sent (Valid values 0 - 60000)
        zwave.configurationV1.configurationSet(parameterNumber: 0x5C, size: 1, scaledConfigurationValue: "$minimumChangePercent").format(),	//Minimum change in percentage for a REPORT to be sent (Valid values 0 - 100)
        
        zwave.configurationV1.configurationSet(parameterNumber: 0x65, size: 4, scaledConfigurationValue: reportGroup).format(),	//Which reports need to send in Report group 1
        zwave.configurationV1.configurationSet(parameterNumber: 0x66, size: 4, scaledConfigurationValue: 0).format(),	//Which reports need to send in Report group 2
        zwave.configurationV1.configurationSet(parameterNumber: 0x67, size: 4, scaledConfigurationValue: 0).format(),	//Which reports need to send in Report group 3
        
        zwave.configurationV1.configurationSet(parameterNumber: 0x6F, size: 4, scaledConfigurationValue: new BigInteger("$refreshInterval")).format(),	// change reporting time
        zwave.configurationV1.configurationSet(parameterNumber: 0x70, size: 4, scaledConfigurationValue: 0).format(),
        zwave.configurationV1.configurationSet(parameterNumber: 0x71, size: 4, scaledConfigurationValue: 0).format(),
        
        zwave.configurationV1.configurationSet(parameterNumber: 0x3, size: 1, scaledConfigurationValue: 0).format(),      // Current Overload Protection.		
 ])    
}

def refresh() {
 	log.debug "refresh()"    
    updateDeviceInfo()
    
    sendEvent(name: "power", value: "0", displayed: true,, unit: "W")    
    sendEvent(name: "energy", value: "0", displayed: true,, unit: "kWh")
    sendEvent(name: "amperage", value: "0", displayed: true,, unit: "A")
    sendEvent(name: "voltage", value: "0", displayed: true, unit: "V")
    
	delayBetween([
		zwave.switchMultilevelV1.switchMultilevelGet().format(),
		zwave.meterV3.meterGet(scale: 0).format(), // energy kWh
		zwave.meterV3.meterGet(scale: 1).format(), // energy kVAh
        zwave.meterV3.meterGet(scale: 2).format(), // watts
		zwave.meterV3.meterGet(scale: 4).format(), // volts
        zwave.meterV3.meterGet(scale: 5).format(), // amps
        zwave.configurationV1.configurationGet(parameterNumber: 0x51).format(), // device state
        zwave.configurationV1.configurationGet(parameterNumber: 0x53).format(), // night light RGB value
        zwave.configurationV1.configurationGet(parameterNumber: 0x54).format(), // led brightness        
	], 1000)
}

def updated() {
	log.debug "updated()"
    
    updateStatus()
    response(configure())
}

/**
 *  reset - Resets the devices energy usage meter and attempt to reset device
 *
 *  Defined by the custom command "reset"
 */
def reset() {
	state.energyMeterRuntimeStart = now()
    return [
        zwave.meterV3.meterReset().format(),
		zwave.meterV3.meterGet(scale: 0).format(), // energy kWh
		zwave.meterV3.meterGet(scale: 1).format(), // energy kVAh
        zwave.meterV3.meterGet(scale: 2).format(), // watts
		zwave.meterV3.meterGet(scale: 4).format(), // volts
        zwave.meterV3.meterGet(scale: 5).format(), // amps
    ]
}

def factoryReset() {  
	log.debug "in factory reset"

	zwave.configurationV1.configurationSet(parameterNumber: 0xFF, size: 4, scaledConfigurationValue: 1).format()	//factory reset
	configure()
}

def setLevel(level) {
	log.debug "in setlevel level = $level"
    setBrightnessLevel(level) 
}

def getDeviceInfo() {
	log.debug "getDeviceInfo()"
    return [
		zwave.versionV1.versionGet().format(),
    	//zwave.manufacturerSpecificV2.deviceSpecificGet().format(),
    	zwave.manufacturerSpecificV2.manufacturerSpecificGet().format()
    ]
}

private updateStatus() {
	if (state.energyMeterRuntimeStart != null) {
    	sendEvent(name:"statusText2", value: "${getBatteryRuntime()}", displayed:false)
    } else {
    	state.energyMeterRuntimeStart = now()
    }
}

private updateDeviceInfo() {
	log.debug "updateDeviceInfo()"
    
    def buffer = "Get Device Info";    
    if (state.deviceInfo != null) {
    	buffer = "application Version: ${state.deviceInfo['applicationVersion']} Sub Version: ${state.deviceInfo['applicationSubVersion']}\n";
        buffer += "zWaveLibrary Type: ${state.deviceInfo['zWaveLibraryType']}\n";
        buffer += "zWaveProtocol Version: ${state.deviceInfo['zWaveProtocolVersion']} Sub Version: ${state.deviceInfo['zWaveProtocolSubVersion']}\n";
                
        buffer += "manufacturer Name: ${state.deviceInfo['manufacturerName']}\n";
        buffer += "manufacturer Id: ${state.deviceInfo['manufacturerId']}\n";        
        buffer += "product Id: ${state.deviceInfo['productId']} Type Id: ${state.deviceInfo['productTypeId']}\n";        
    }
        
	return sendEvent(name:"deviceInfo", value: "$buffer", displayed:false)
}

private getBatteryRuntime() {
   def currentmillis = now() - state.energyMeterRuntimeStart
   def days=0
   def hours=0
   def mins=0
   def secs=0
   secs = (currentmillis/1000).toInteger() 
   mins=(secs/60).toInteger() 
   hours=(mins/60).toInteger() 
   days=(hours/24).toInteger() 
   secs=(secs-(mins*60)).toString().padLeft(2, '0') 
   mins=(mins-(hours*60)).toString().padLeft(2, '0') 
   hours=(hours-(days*24)).toString().padLeft(2, '0')  

  if (days>0) { 
      return "$days days and $hours:$mins:$secs"
  } else {
      return "$hours:$mins:$secs"
  }
}

def dimmerEvents(physicalgraph.zwave.Command cmd) {
	log.debug "in zwave cmd handler dimmer events cmd.value = $cmd.value"
	// lgk ignore dimming events as not working anyway 0 = off 255 = on .
	def result = []
	def value = ""
    
	if (cmd.value == 0 || cmd.value == 255) {
		if (cmd.value == 255) 	
      		value = "on"
     	if (cmd.value == 0)
      		value = "off"
  		//  log.debug "value = $value level = $cmd.value"
		def switchEvent = createEvent(name: "switch", value: value, descriptionText: "$device.displayName was turned $value")
		result << switchEvent
		if (cmd.value) {
			result << createEvent(name: "level", value: cmd.value, unit: "%")
		}
		if (switchEvent.isStateChange) {
			result << response(["delay 3000", zwave.meterV2.meterGet(scale: 2).format()])
		}
		return result
	}
 	return null
}
    
def on() {
	log.debug "switching it on"
	delayBetween([
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.switchMultilevelV1.switchMultilevelGet().format(),
	], 5000)
   
}

def off() {
	log.debug "switching it off"
	delayBetween([
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.switchMultilevelV1.switchMultilevelGet().format(),
	], 5000)
}


def poll() {
 	log.debug "in poll"
	delayBetween([
		zwave.switchBinaryV1.switchBinaryGet().format(),
		zwave.meterV3.meterGet(scale: 0).format(), // energy kWh
		zwave.meterV3.meterGet(scale: 1).format(), // energy kVAh
        zwave.meterV3.meterGet(scale: 2).format(), // watts
		zwave.meterV3.meterGet(scale: 4).format(), // volts
        zwave.meterV3.meterGet(scale: 5).format(), // amps
	],1000)
}

def nightLight() {
	log.debug "in set nightlight mode" 
	sendEvent(name: "deviceMode", value: "nightLight", displayed: true)
	setDeviceMode(2)
}

def energy() {
	log.debug "in set energy mode"
    sendEvent(name: "deviceMode", value: "energy", displayed: true)
    setDeviceMode(0)
}

def momentary() {
	log.debug "in momentary mode"  
    sendEvent(name: "deviceMode", value: "momentary", displayed: true)
     setDeviceMode(1) 
}

def setDeviceMode(mode) {    
	log.debug "set current mode to '$mode'"
	zwave.configurationV1.configurationSet(parameterNumber: 0x51, size: 1, scaledConfigurationValue: mode).format()
}

def setColor(colormap) {
    log.debug " in setColor: hex =  ${colormap.hex}"
   // log.debug "red = ${colormap.red}"
   // log.debug "green = ${colormap.green}"
   // log.debug "blue = ${colormap.blue}"
         
   if (colormap.hex) { 
   		sendEvent(name: "color", value: colormap.hex)
     	zwave.configurationV1.configurationSet(parameterNumber: 0x53, size: 3, configurationValue: [colormap.red, colormap.green, colormap.blue]).format()
	}
}

def setBrightnessLevel(newLevel) {
	log.debug "in set setlevel newlevel = '$newLevel'"
	sendEvent(name: "brightnessLevel", value: newLevel.toInteger(), displayed: true)	         
    	
    // There seems to have an error in the documentation where this config should be a size = 1
    zwave.configurationV1.configurationSet(parameterNumber: 0x54, size: 3, configurationValue: [newLevel,newLevel,newLevel]).format()  	
}

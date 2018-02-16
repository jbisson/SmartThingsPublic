/**
 *
 *  Aeon Labs Smart Energy Switch (for DSB06xxx-ZWUS/DSC24-ZWAU/DSC24-ZWEU)
 * 
 *  Copyright 2016 jbisson
 *  based on James P ('elasticdev'), Mr Lucky, lg kahn code.
 *
 *
 *  *  The energy cost (current) represent the amount of money you'll pay if the current load stay the same all the time
 *  vs
 *  The energy cost (cumulative) is the amount of money you'll pay based on the amount of energy used between now and
 *  the last time you did a reset. You can clear this counter by hitting the reset icon.
 *  This will take into account the variable aspect of your energy consumption.
 * 
 *  works with: DSB06xxx-ZWUS/DSC24-ZWAU/DSC24-ZWEU
 *
 *  Revision History
 *  ==============================================
 *  2018-02-15 Version 5.1.2  Typo fix (reported by: Ron_Darling)
 *  2018-02-10 Version 5.1.1  Small Crash protection fix (reported by: dkorunic)
 *  2017-02-08 Version 5.1.0  Added energy meter cost per hours/week/month/year feature, fixed display issues
 *  2016-11-13 Version 4.0.5  Added force refresh report notification update preference
 *  2016-08-31 Version 4.0.4  Fixed fingerprint number.
 *  2016-08-12 Version 4.0.2  Added version in preference setting
 *  2016-08-11 Version 4.0.1  Added switch disabled visual on the main tile, added firmware version
 *  2016-08-11 Version 4.0.0  Added log preference, enable/disable switch preference, added dev documentation, changed fingerprint
 *  2016-08-08 Version 3.0.1  Adapt device handler to support gen5 version 3.1
 *  2016-08-08 Version 3.0
 *  2015-09-01 version 2 - lg kahn
 * 
 *
 *  Developer's Notes
 *  Raw Description	0 0 0x1001 0 0 0 a 0x25 0x31 0x32 0x27 0x70 0x85 0x72 0x86 0xEF 0x82
 *  Z-Wave Supported Command Classes:
 *  Code Name					Version
 *  ==== ======================================	=======
 *  0x25 COMMAND_CLASS_SWITCH_BINARY		 V1                 Implemented
 *  0x31 COMMAND_CLASS_SENSOR_MULTILEVEL	 V5                 Implemented - not used
 *  0x32 COMMAND_CLASS_METER			     V3                 Implemented
 *  0x70 COMMAND_CLASS_CONFIGURATION		 V1                 Implemented
 *  0x27 COMMAND_CLASS_SWITCH_ALL		     V1                 Not implemented
 *  0x85 COMMAND_CLASS_ASSOCIATION		     V2                 Not implemented
 *  0x72 COMMAND_CLASS_MANUFACTURER_SPECIFIC V2                 Implemented
 *  0x86 COMMAND_CLASS_VERSION			     V2                 Implemented
 *  0xEF COMMAND_CLASS_MARK			         V1                 Not implemented
 *  0x82 COMMAND_CLASS_HAIL			         V1                 Implemented
 *
 */
 
 def clientVersion() {
    return "5.1.2"
}

metadata {
	definition (name: "Aeon Labs Smart Switch dsc06106", namespace: "jbisson", author: "Jonathan Bisson") {
		capability "Switch"
		capability "Polling"
		capability "Power Meter"
		capability "Energy Meter"
		capability "Refresh"
		capability "Switch Level"
		capability "Sensor"
		capability "Actuator"
        capability "Configuration"
      
       command "reset"
       command "factoryReset"       
       command "getDeviceInfo"
       
       attribute "deviceMode", "String"
  
  	   // Base on https://community.smartthings.com/t/new-z-wave-fingerprint-format/48204
  	   fingerprint mfr: "0086", prod: "0003", model: "0006" // Aeon Labs Smart Energy Switch DSB06xxx       
       fingerprint mfr: "0086", prod: "0003", model: "0018" // Smart Energy Switch G2 - DSC24-ZWEU - DSC24-ZWAU
       // http://products.z-wavealliance.org/products/778
       // http://products.z-wavealliance.org/products/770
       // http://products.z-wavealliance.org/products/133
	   fingerprint type: "1001", cc: "25,31,32,27,70,85,72,86,EF,82"	   
	}

    tiles(scale: 2) {
        multiAttributeTile(name:"mainPanel", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
            }
            tileAttribute ("statusText3", key: "SECONDARY_CONTROL") {
                attributeState "statusText3", label:'${currentValue}', icon: "st.Appliances.appliances17"
            }
        }

        valueTile("power", "device.power", width: 3, height: 1) {
            state "default", label:'${currentValue} W'
        }

        valueTile("energy", "device.energy", width: 3, height: 1, decoration: "flat") {
            state "default", label:'${currentValue} kWh'
        }

        standardTile("currentEnergyCostTxt", "currentEnergyCostTxt", width: 2, height: 1) {
            state "default", label: 'Energy Cost (Current):'
        }

        valueTile("currentEnergyCostHour", "currentEnergyCostHour", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'Per Hour \n$${currentValue}'
        }

        valueTile("currentEnergyCostWeek", "currentEnergyCostWeek", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'Per Week \n$${currentValue}'
        }

        valueTile("currentEnergyCostMonth", "currentEnergyCostMonth", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'Per Month \n$${currentValue}'
        }

        valueTile("currentEnergyCostYear", "currentEnergyCostYear", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'Per Year \n$${currentValue}'
        }

        valueTile("cumulativeEnergyCostTxt", "cumulativeEnergyCostTxt", width: 2, height: 1) {
            state "default", label: 'Energy Cost (Cumulative)\nSince ${currentValue}:'
        }

        valueTile("cumulativeEnergyCostHour", "cumulativeEnergyCostHour", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'Per Hour \n$${currentValue}'
        }

        valueTile("cumulativeEnergyCostWeek", "cumulativeEnergyCostWeek", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'Per Week \n$${currentValue}'
        }

        valueTile("cumulativeEnergyCostMonth", "cumulativeEnergyCostMonth", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'Per Month \n$${currentValue}'
        }

        valueTile("cumulativeEnergyCostYear", "cumulativeEnergyCostYear", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'Per Year \n$${currentValue}'
        }

        standardTile("refresh", "device.switch", decoration: "flat", width: 1, height: 1) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        standardTile("reset", "device.energy", decoration: "flat", width: 1, height: 1) {
            state "default", label:'reset', action:"reset"
        }

        standardTile("configure", "device.power", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
            state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
        }
        
        valueTile("deviceInfo", "deviceInfo", decoration: "flat", width: 3, height: 1) {
            state "default", label:'${currentValue}', action:"getDeviceInfo"
        }
        
        main(["mainPanel", "power","energy"] )
        details(["mainPanel", "power", "energy", "currentEnergyCostTxt", "currentEnergyCostHour",
                 "currentEnergyCostWeek", "currentEnergyCostMonth", "currentEnergyCostYear", "cumulativeEnergyCostTxt",
                 "cumulativeEnergyCostHour", "cumulativeEnergyCostWeek", "cumulativeEnergyCostMonth",
                 "cumulativeEnergyCostYear", "refresh","reset","configure", "deviceInfo"])
    }
}

preferences {
	input title: "", description: "Aeon Smart Switch DSC06106 v${clientVersion()}", displayDuringSetup: true, type: "paragraph", element: "paragraph"

    input name: "switchDisabled", type: "bool", title: "Disable switch on/off\n", defaultValue: "false"
    input name: "refreshInterval", type: "number", title: "Refresh interval \n\nSet the refresh time interval (secondes) between each reports.\n", required: true, displayDuringSetup: true
    input name: "switchAll", type: "enum", title: "Respond to switch all?\n", description: "How does switch respond to the 'Switch All' command", options:["Disabled", "Off Enabled", "On Enabled", "On and Off Enabled"], required: false, defaultValue: "On and Off Enabled", displayDuringSetup: true
    input name: "forceStateChangeOnReport", type: "bool", title: "Force state change when receiving a report ? If true, you'll always get notification even if report data doesn't change.\n", defaultValue: "false", displayDuringSetup: true
	
	input name: "onlySendReportIfValueChange", type: "bool", title: "Only send report if value change (either in terms of wattage or a %)\n", defaultValue: "false"
    
    input title: "", description: "The next two parameters are only working if the only send report is set to true.", type: "paragraph", element: "paragraph"
    
    input name: "minimumChangeWatts", type: "number", title: "Minimum change in wattage for a report to be sent (0 - 100).\n", defaultValue: "25", range: "0..100"
    input name: "minimumChangePercent", type: "number", title: "Minimum change in percentage for a report to be sent (0 - 60000)\n", defaultValue: "5", range: "0..60000"

    input name: "costPerKwh", type: "decimal", title: "Cost per kWh (Used for energy cost /per kWh)\n", defaultValue: "0.12", displayDuringSetup: true

    input name: "includeWattInReport", type: "bool", title: "Include energy meter (W) in report?\n", defaultValue: "true"        
    input name: "includeCurrentUsageInReport", type: "bool", title: "Include current usage (kWh) in report?\n", defaultValue: "true"    
	
	input title: "", description: "Logging", type: "paragraph", element: "paragraph"
    input name: "isLogLevelTrace", type: "bool", title: "Show trace log level ?\n", defaultValue: "false", displayDuringSetup: true
    input name: "isLogLevelDebug", type: "bool", title: "Show debug log level ?\n", defaultValue: "true", displayDuringSetup: true
}

/*******************************************************************************
 *	Z-WAVE PARSE / EVENTS                                                      *
 ******************************************************************************/
 
/**
 *  parse - Called when messages from a device are received from the hub
 *
 *  The parse method is responsible for interpreting those messages and returning Event definitions.
 *
 *  String	description		The message from the device
 */
def parse(String description) {
	def result = null
	logTrace "parse: '$description'"
    
	if (description != "updated") {
		def cmd = zwave.parse(description, [0x20: 1, 0x26: 3, 0x70: 1, 0x32:3])
        logTrace "cmd: '$cmd'"
      
		if (cmd) {
			result = zwaveEvent(cmd)	        
		} else {
			log.error "Couldn't zwave.parse '$description'"
		}
	}
    
    updateStatus()    
    result
}

/**
 *  COMMAND_CLASS_SWITCH_BINARY (0x25)
 *
 *  Short	value	0xFF for on, 0x00 for off
 */
def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinarySet cmd) {
	return createEvent(name: "switch", value: cmd.value ? "on" : "off")
}

/**
 *  COMMAND_CLASS_SENSOR_MULTILEVEL  (0x31)
 *	
 *  Short	sensorType	Supported Sensor: 0x04 (power Sensor)
 *  Short	scale		Supported scale:  0x00 (W) and 0x01 (BTU/h)   
 */
def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	
}

/**
 *  COMMAND_CLASS_SWITCH_BINARY (0x25)
 *
 *  Short	value	0xFF for on, 0x00 for off
 */
def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {	
    createEvent(name: "switch", value: cmd.value ? "on" : "off", displayed: false, isStateChange: true)
}

/**
 *  COMMAND_CLASS_BASIC (0x20)
 *
 *  Short	value	0xFF for on, 0x00 for off
 */
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {	
	return createEvent(name: "switch", value: cmd.value ? "on" : "off", displayed: false)
}

/**
 *  COMMAND_CLASS_BASIC (0x20)
 *
 *  Short	value	0xFF for on, 0x00 for off
 */
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {	
	return createEvent(name: "switch", value: cmd.value ? "on" : "off")
}

/**
 *  COMMAND_CLASS_METER (0x32)
 *
 *  Integer	deltaTime		    Time in seconds since last report
 *  Short	meterType		    Unknown = 0, Electric = 1, Gas = 2, Water = 3
 *  List<Short>	meterValue		    Meter value as an array of bytes
 *  Double	scaledMeterValue	    Meter value as a double
 *  List<Short>	previousMeterValue	    Previous meter value as an array of bytes
 *  Double	scaledPreviousMeterValue    Previous meter value as a double
 *  Short	size			    The size of the array for the meterValue and previousMeterValue
 *  Short	scale			    The scale of the values: "kWh"=0, "kVAh"=1, "Watts"=2, "pulses"=3, "Volts"=4, "Amps"=5, "Power Factor"=6, "Unknown"=7
 *  Short	precision		    The decimal precision of the values
 *  Short	rateType		    ???
 *  Boolean	scale2			    ???
 */
def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	if (cmd.meterType == 1) {
        def eventList = []
		if (cmd.scale == 0) {
     	    logDebug " got kwh $cmd.scaledMeterValue"

            BigDecimal costDecimal = ( costPerKwh as BigDecimal )
            def batteryRunTimeHours = getBatteryRuntimeInHours()
            eventList.push(internalCreateEvent([name: "energy", value: cmd.scaledMeterValue, unit: "kWh"]));

            eventList.push(internalCreateEvent([name: "cumulativeEnergyCostTxt", value: getBatteryRuntime()]));
            eventList.push(internalCreateEvent([name: "cumulativeEnergyCostHour", value: String.format("%5.2f", cmd.scaledMeterValue / batteryRunTimeHours * costDecimal)]));
            eventList.push(internalCreateEvent([name: "cumulativeEnergyCostWeek", value: String.format("%5.2f", cmd.scaledMeterValue / batteryRunTimeHours * costDecimal * 24 * 7)]));
            eventList.push(internalCreateEvent([name: "cumulativeEnergyCostMonth", value: String.format("%5.2f", cmd.scaledMeterValue / batteryRunTimeHours * costDecimal * 24 * 30)]));
            eventList.push(internalCreateEvent([name: "cumulativeEnergyCostYear", value: String.format("%5.2f", cmd.scaledMeterValue / batteryRunTimeHours * costDecimal * 24 * 360)]));
		} else if (cmd.scale == 1) {
        	logDebug " got kVAh $cmd.scaledMeterValue"
            eventList.push(internalCreateEvent([name: "energy", value: cmd.scaledMeterValue, unit: "kVAh"]));
		} else if (cmd.scale == 2) { 
        	logDebug " got wattage $cmd.scaledMeterValue"
            updatePowerStatus(Math.round(cmd.scaledMeterValue))

            eventList.push(internalCreateEvent([name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W"]));
            BigDecimal costDecimal = ( costPerKwh as BigDecimal )
            eventList.push(internalCreateEvent([name: "currentEnergyCostHour", value: String.format("%5.2f", (cmd.scaledMeterValue / 1000) * costDecimal)]));
            eventList.push(internalCreateEvent([name: "currentEnergyCostWeek", value: String.format("%5.2f", (cmd.scaledMeterValue / 1000) * 24 * 7 * costDecimal)]));
            eventList.push(internalCreateEvent([name: "currentEnergyCostMonth", value: String.format("%5.2f", (cmd.scaledMeterValue / 1000) * 24 * 30 * costDecimal)]));
            eventList.push(internalCreateEvent([name: "currentEnergyCostYear", value: String.format("%5.2f", (cmd.scaledMeterValue / 1000) * 24 * 360 * costDecimal)]));
		} else if (cmd.scale == 4) { // Volts
            logDebug " got voltage $cmd.scaledMeterValue"
            eventList.push(internalCreateEvent([name: "voltage", value: Math.round(cmd.scaledMeterValue), unit: "V"]));
		} else if (cmd.scale == 5) { //amps scale 5 is amps even though not documented
            logDebug " got amperage = $cmd.scaledMeterValue"
            eventList.push(internalCreateEvent([name: "amperage", value: cmd.scaledMeterValue, unit: "A"]));
		} else {
            eventList.push(internalCreateEvent([name: "electric", value: cmd.scaledMeterValue, unit: ["pulses", "V", "A", "R/Z", ""][cmd.scale - 3]]));
		}
		
        return eventList
	}
}

/**
 *  COMMAND_CLASS_CONFIGURATION (0x70)
 *
 *  List<Short>	configurationValue
 *  Short	parameterNumber
 *  Short	size
 */
def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    logTrace "received ConfigurationReport for " + cmd.parameterNumber + " (hex:" + Integer.toHexString(cmd.parameterNumber) + ") cmd: " + cmd

    switch (cmd.parameterNumber) {
    	case 0x51:
        	logTrace "received device mode event"
        	if (cmd.configurationValue[0] == 0) {
            	return createEvent(name: "deviceMode", value: "energy", displayed: true)
            } else if (cmd.configurationValue[0] == 1) {
            	return createEvent(name: "deviceMode", value: "momentary", displayed: true)
            } else if (cmd.configurationValue[0] == 2) {
            	return createEvent(name: "deviceMode", value: "nightLight", displayed: true)
            }
    	break;
        case 0x54:
        	logTrace "received brightness level event"
        	return createEvent(name: "level", value: cmd.configurationValue[0], displayed: true)
        break;
    }
}

/**
 *  COMMAND_CLASS_HAIL (0x82)
 * 
 */
def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {	
    logDebug "Switch button was pressed"
    return createEvent(name: "hail", value: "hail", descriptionText: "Switch button was pressed")
}

/**
 *  COMMAND_CLASS_VERSION (0x86)
 *
 *  Short	applicationSubVersion
 *  Short	applicationVersion
 *  Short	zWaveLibraryType
 *  Short	zWaveProtocolSubVersion
 *  Short	zWaveProtocolVersion
 */
def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {	
    state.deviceInfo['applicationVersion'] = "${cmd.applicationVersion}"
    state.deviceInfo['applicationSubVersion'] = "${cmd.applicationSubVersion}"
    state.deviceInfo['zWaveLibraryType'] = "${cmd.zWaveLibraryType}"
    state.deviceInfo['zWaveProtocolVersion'] = "${cmd.zWaveProtocolVersion}"
    state.deviceInfo['zWaveProtocolSubVersion'] = "${cmd.zWaveProtocolSubVersion}"
    
    return updateDeviceInfo()
}

/**
 *  COMMAND_CLASS_MANUFACTURER_SPECIFIC (0x72)
 *
 *  Integer	manufacturerId
 *  Integer	productId
 *  Integer	productTypeId
 *
 */
def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {    
    state.deviceInfo['manufacturerId'] = "${cmd.manufacturerId}"
    state.deviceInfo['manufacturerName'] = "${cmd.manufacturerName}"
    state.deviceInfo['productId'] = "${cmd.productId}"
    state.deviceInfo['productTypeId'] = "${cmd.productTypeId}"
    
    return updateDeviceInfo()
}

/**
 *  COMMAND_CLASS_MANUFACTURER_SPECIFIC (0x72)
 *
 * List<Short>	deviceIdData
 * Short	deviceIdDataFormat
 * Short	deviceIdDataLengthIndicator
 * Short	deviceIdType
 *
 */
def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.DeviceSpecificReport cmd) { 
	logTrace "deviceIdData:  	          ${cmd.deviceIdData}"
    logTrace "deviceIdDataFormat:         ${cmd.deviceIdDataFormat}"
    logTrace "deviceIdDataLengthIndicator:${cmd.deviceIdDataLengthIndicator}"
    logTrace "deviceIdType:               ${cmd.deviceIdType}"
    
    return updateDeviceInfo()
}

/*******************************************************************************
 *	CAPABILITITES                                                              *
 ******************************************************************************/

 /**
 *  configure - Configures the parameters of the device
 *
 *  Required for the "Configuration" capability
 */
def configure() {
	logDebug "configure()"
	updateDeviceInfo()
    
    def switchAllMode = physicalgraph.zwave.commands.switchallv1.SwitchAllSet.MODE_INCLUDED_IN_THE_ALL_ON_ALL_OFF_FUNCTIONALITY
    if (switchAll == "Disabled") {
		switchAllMode = physicalgraph.zwave.commands.switchallv1.SwitchAllSet.MODE_EXCLUDED_FROM_THE_ALL_ON_ALL_OFF_FUNCTIONALITY
    } else if (switchAll == "Off Enabled") {
		switchAllMode = physicalgraph.zwave.commands.switchallv1.SwitchAllSet.MODE_EXCLUDED_FROM_THE_ALL_ON_FUNCTIONALITY_BUT_NOT_ALL_OFF
    } else if (switchAll == "On Enabled") {
		switchAllMode = physicalgraph.zwave.commands.switchallv1.SwitchAllSet.MODE_EXCLUDED_FROM_THE_ALL_OFF_FUNCTIONALITY_BUT_NOT_ALL_ON
    }
	
	logTrace "forceStateChangeOnReport value: " + forceStateChangeOnReport	
	logTrace "switchAll value: " + switchAll

	def reportGroup = 0;
	reportGroup += ("$includeWattInReport" == "true" ? 4 : 0)
	reportGroup += ("$includeCurrentUsageInReport" == "true" ? 8 : 0)
    
    logTrace "setting configuration refresh interval: " + new BigInteger("$refreshInterval")
    
    /***************************************************************
    Device specific configuration parameters
    ----------------------------------------------------------------
    Param   Size    Default Description
    ------- ------- ------- ----------------------------------------
    0x01 (01)   1       0       The content of "Multilevel Sensor Report Command" after SES receives "Multilevel Sensor Get Command".
    0x02 (02)   1       N/A     Make SES blink
    0x50 (80)   1       0       Enable to send notifications to associated devices in Group 1 when load changes (0=nothing, 1=hail CC, 2=basic CC report)
    0x5A (90)   1       1       Enables/disables parameter 0x5A and 0x5B below
    0x5B (91)   2       50      The value here represents minimum change in wattage (in terms of wattage) for a REPORT to be sent (default 50W, size 2 bytes).
    0x5C (92)   1       10      The value here represents minimum change in wattage (in terms of percentage) for a REPORT to be sent (default 10%, size 1 byte).
    0x64 (100)  1       0       Set 101-103 to default.
    0x65 (101)  4       8       Which reports need to send in Report group 1    
    0x66 (102)  4       0       Which reports need to send in Report group 2
    0x67 (103)  4       0       Which reports need to send in Report group 3
    0x6E (110)  1       0       Set 111-113 to default.
    0x6F (111)  4       0x00 00 02 58 The time interval in seconds for sending Report group 1 (Valid values 0x01-0x7FFFFFFF).
    0x70 (112)  4       0x00 00 02 58 The time interval in seconds for sending Report group 2 (Valid values 0x01-0x7FFFFFFF).
    0x71 (113)  4       0x00 00 02 58 The time interval in seconds for sending Report group 3 (Valid values 0x01-0x7FFFFFFF).
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
        zwave.configurationV1.configurationSet(parameterNumber: 0x50, size: 1, scaledConfigurationValue: 0).format(),	//Enable to send notifications to associated devices when load changes (0=nothing, 1=hail CC, 2=basic CC report)
        zwave.configurationV1.configurationSet(parameterNumber: 0x5A, size: 1, scaledConfigurationValue: ("$onlySendReportIfValueChange" == "true" ? 1 : 0)).format(),	//Enables parameter 0x5B and 0x5C (0=disabled, 1=enabled)
        zwave.configurationV1.configurationSet(parameterNumber: 0x5B, size: 2, scaledConfigurationValue: new BigInteger("$minimumChangeWatts")).format(),	//Minimum change in wattage for a REPORT to be sent (Valid values 0 - 60000)
        zwave.configurationV1.configurationSet(parameterNumber: 0x5C, size: 1, scaledConfigurationValue: new BigInteger("$minimumChangePercent")).format(),	//Minimum change in percentage for a REPORT to be sent (Valid values 0 - 100)
        
        zwave.configurationV1.configurationSet(parameterNumber: 0x65, size: 4, scaledConfigurationValue: reportGroup).format(),	//Which reports need to send in Report group 1
        zwave.configurationV1.configurationSet(parameterNumber: 0x66, size: 4, scaledConfigurationValue: 0).format(),	//Which reports need to send in Report group 2
        zwave.configurationV1.configurationSet(parameterNumber: 0x67, size: 4, scaledConfigurationValue: 0).format(),	//Which reports need to send in Report group 3
        
        zwave.configurationV1.configurationSet(parameterNumber: 0x6F, size: 4, scaledConfigurationValue: new BigInteger("$refreshInterval")).format(),	// change reporting time
        zwave.configurationV1.configurationSet(parameterNumber: 0x70, size: 4, scaledConfigurationValue: 0).format(),
        zwave.configurationV1.configurationSet(parameterNumber: 0x71, size: 4, scaledConfigurationValue: 0).format(),
        
        zwave.configurationV1.configurationSet(parameterNumber: 0x3, size: 1, scaledConfigurationValue: 0).format(),      // Current Overload Protection.		
        zwave.configurationV1.configurationSet(parameterNumber: 0x1, size: 1, scaledConfigurationValue: 0).format()       
 ])    
}

/**
 *  on - Turns on the switch
 *
 *  Required for the "Switch" capability
 */
def on() {
    if (switchDisabled) {
    	logDebug "switch disabled, doing nothing"
		delayBetween([
			zwave.basicV1.basicGet().format(),
			zwave.switchBinaryV1.switchBinaryGet().format()
        ], 100)
	} else {
    	logDebug "switching it on"
        delayBetween([
            zwave.basicV1.basicSet(value: 0xFF).format(),
            zwave.switchBinaryV1.switchBinaryGet().format()
        ])
    }   
}

/**
 *  off - Turns off the switch
 *
 *  Required for the "Switch" capability
 */
def off() {
    if (switchDisabled) {
    	logDebug "switch disabled, doing nothing"
		delayBetween([
			zwave.basicV1.basicGet().format(),
			zwave.switchBinaryV1.switchBinaryGet().format()
        ], 100)
	} else {
    	logDebug "switching it off"
        delayBetween([
            zwave.basicV1.basicSet(value: 0x00).format(),
            zwave.switchBinaryV1.switchBinaryGet().format()
        ])
    } 
}

/**
 *  poll - Polls the device
 *
 *  Required for the "Polling" capability
 */
def poll() {
 	logTrace "poll()"
    
	delayBetween([
		zwave.switchBinaryV1.switchBinaryGet().format(),
		zwave.meterV3.meterGet(scale: 0).format(), // energy kWh
		zwave.meterV3.meterGet(scale: 1).format(), // energy kVAh
        zwave.meterV3.meterGet(scale: 2).format(), // watts
		zwave.meterV3.meterGet(scale: 4).format(), // volts
        zwave.meterV3.meterGet(scale: 5).format(), // amps
	],1000)
}

/**
 *  refresh - Refreshed values from the device
 *
 *  Required for the "Refresh" capability
 */
def refresh() {
 	logDebug "refresh()"    
    updateDeviceInfo()
    updatePowerStatus(0)
    
    sendEvent(name: "power", value: "0", displayed: true,, unit: "W")    
    sendEvent(name: "energy", value: "0", displayed: true,, unit: "kWh")

    sendEvent(name: "currentEnergyCostHour", value: "0", displayed: true)
    sendEvent(name: "currentEnergyCostWeek", value: "0", displayed: true)
    sendEvent(name: "currentEnergyCostMonth", value: "0", displayed: true)
    sendEvent(name: "currentEnergyCostYear", value: "0", displayed: true)

    sendEvent(name: "cumulativeEnergyCostHour", value: "0", displayed: true)
    sendEvent(name: "cumulativeEnergyCostWeek", value: "0", displayed: true)
    sendEvent(name: "cumulativeEnergyCostMonth", value: "0", displayed: true)
    sendEvent(name: "cumulativeEnergyCostYear", value: "0", displayed: true)

	delayBetween([
		zwave.switchMultilevelV1.switchMultilevelGet().format(),
		zwave.meterV3.meterGet(scale: 0).format(), // energy kWh		
        zwave.meterV3.meterGet(scale: 2).format(), // watts		
	], 100)
}

/*******************************************************************************
 *	Methods                                                                    *
 ******************************************************************************/

/**
 *  installed - Called when the device handling is being installed
 */
def installed() {
    log.debug "installed() called"

    if (state.deviceInfo == null) {
        state.deviceInfo = [:]
    }
}

/**
 *  updated - Called when the preferences of the device type are changed
 */
def updated() {
	logDebug "updated()"
    
    updateStatus()
    updatePowerStatus(0)
    response(configure())
}

/**
 *  reset - Resets the devices energy usage meter and attempt to reset device
 *
 *  Defined by the custom command "reset"
 */
def reset() {
	logDebug "reset()"

	state.energyMeterRuntimeStart = now()
    return [
        zwave.meterV3.meterReset().format(),
		zwave.meterV3.meterGet(scale: 0).format(), // energy kWh		
        zwave.meterV3.meterGet(scale: 2).format(), // watts		
    ]
}

def factoryReset() {  
	logDebug "factoryReset()"

	zwave.configurationV1.configurationSet(parameterNumber: 0xFF, size: 4, scaledConfigurationValue: 1).format()	//factory reset
	configure()
}

def getDeviceInfo() {
	logDebug "getDeviceInfo()"
    return [
		zwave.versionV1.versionGet().format(),
    	//zwave.manufacturerSpecificV2.deviceSpecificGet().format(),
    	zwave.manufacturerSpecificV2.manufacturerSpecificGet().format()   
    ]
}

private updateStatus() {
    def sinceTime = ''
    if (state.energyMeterRuntimeStart != null) {
        sinceTime = "${getBatteryRuntime()}"
    } else {
        sinceTime = now()
    }

    sendEvent(name: "statusText3", value: "Energy meter since: $sinceTime", displayed: false)
}

private updatePowerStatus(val) {
    if (switchDisabled) {
    	sendEvent(name:"statusText3", value: "$val W *** SWITCH DISABLED ***", displayed:false)
    } else {
    	sendEvent(name:"statusText3", value: "$val W", displayed:false)
    }
}

private updateDeviceInfo() {
	logTrace "updateDeviceInfo()"
    
    def buffer = "Get Device Info";
    def switchStatus = "SWITCH ENABLED"
    if (switchDisabled) {
        switchStatus = "SWITCH DISABLED"
    }

    if (state.deviceInfo != null) {
        buffer = "$switchStatus\n";
    	buffer += "application Version: ${state.deviceInfo['applicationVersion']} Sub Version: ${state.deviceInfo['applicationSubVersion']}\n";
        buffer += "zWaveLibrary Type: ${state.deviceInfo['zWaveLibraryType']}\n";
        buffer += "zWaveProtocol Version: ${state.deviceInfo['zWaveProtocolVersion']} Sub Version: ${state.deviceInfo['zWaveProtocolSubVersion']}\n";
                
        buffer += "manufacturer Name: ${state.deviceInfo['manufacturerName']}\n";
        buffer += "manufacturer Id: ${state.deviceInfo['manufacturerId']}\n";        
        buffer += "product Id: ${state.deviceInfo['productId']} Type Id: ${state.deviceInfo['productTypeId']}\n";        
    } else {
        getDeviceInfo()
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

private getBatteryRuntimeInHours() {
    def currentmillis = now() - state.energyMeterRuntimeStart
    def days = 0
    def hours = 0
    def mins = 0
    def secs = 0
    secs = (currentmillis / 1000)
    mins = (secs / 60)
    hours = (mins / 60)
    return hours
}

void logDebug(str) {	
	if (isLogLevelDebug) {    	
        log.debug str
	}
}

void logTrace(str) {
	if (isLogLevelTrace) {
        log.trace str 
	}
}

def internalCreateEvent(event) {
    if (forceStateChangeOnReport) {
        event.isStateChange = true
    }

    return createEvent(event)
}
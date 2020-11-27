/**
 *  Elasticsearch Event Logger
 *
 *  Author:
 *    Kevin LaFramboise (krlaframboise) modified by Jonathan Bisson (jbisson)
 *
 *  URL to documentation:
 *    https://github.com/krlaframboise/SmartThings/tree/master/smartapps/krlaframboise/simple-event-logger.src#simple-event-logger
 *
 *  Revision History
 *  ==============================================
 *  2020-11-27 Version 2.0.0 Added occupany attributes
 *  2020-10-22 Version 1.1.1 Changed event format
 *
 *
 */
 
 def version() {
    return "2.0.0 - 2020-11-27"
}

include 'asynchttp_v1'
 
definition(
    name: "Elasticsearch Event Shipper",
    namespace: "jbisson",
    author: "Jonathan Bisson",
    description: "Allows you to send event from smartthings to any http endpoint",
    category: "My Apps",
    iconUrl: "https://raw.githubusercontent.com/krlaframboise/Resources/master/simple-event-logger/app-SimpleEventLogger.png",
    iconX2Url: "https://raw.githubusercontent.com/krlaframboise/Resources/master/simple-event-logger/app-SimpleEventLogger@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/krlaframboise/Resources/master/simple-event-logger/app-SimpleEventLogger@3x.png")		

preferences {
  page(name: "mainPage")
  page(name: "aboutPage")
	page(name: "devicesPage")
	page(name: "attributesPage")
	page(name: "attributeExclusionsPage")
	page(name: "optionsPage")  
}

def aboutPage() {
	page(name:"aboutPage") {
    getAboutPageContent()
	}
}

private getAboutPageContent() {
  section("About") {
    input title: "Elasticsearch Event Shipper", description: "v${version()}", displayDuringSetup: true, type: "paragraph", element: "paragraph", required: false
  }
}

def mainPage() {
	dynamicPage(name:"mainPage", uninstall:true, install:true) {
    getAboutPageContent()

		if (state.allConfigured && state.loggingStatus) {
			getLoggingStatusContent()
		}
		if (state.devicesConfigured) {
			section("Selected Devices") {
				getPageLink("devicesPageLink", "Tap to change", "devicesPage", null, buildSummary(getSelectedDeviceNames()))
			}
		}
		else {			
			getDevicesPageContent()
		}
		
		if (state.attributesConfigured) {
			section("Selected Events") {
				getPageLink("attributesPageLink", "Tap to change", "attributesPage", null, buildSummary(settings?.allowedAttributes?.sort()))
			}
			section ("Event Device Exclusions") {
				getPageLink("attributeExclusionsPageLink", "Select devices to exclude for specific events.", "attributeExclusionsPage")
			}
		}
		else {
			getAttributesPageContent()
		}
				
		if (!state.optionsConfigured) {
			getOptionsPageContent()
		}
		
		section("  ") {
			if (state.optionsConfigured) {
				getPageLink("optionsPageLink", "Other Options", "optionsPage", null, "Tap to set")
			}
			label title: "Assign a name", required: false
			mode title: "Set for specific mode(s)", required: false
			if (state.installed) {		
				getPageLink("aboutPageLink", "About Simple Event Logger", "aboutPage", null, "Tap to view documentation, version and additional information.", "https://raw.githubusercontent.com/krlaframboise/Resources/master/simple-event-logger/app-SimpleEventLogger@3x.png")
			}
		}
		section("  ") {
			paragraph "  ", required: false
		}
	}
}

private getLoggingStatusContent() {
	if (state.loggingStatus?.success != null) {
		section("Logging Status") {			
			def status = getFormattedLoggingStatus()
			
			paragraph required: false,
				"Total Events Logged: ${status.totalEventsLogged}\nLast Execution:\n - Result: ${status.result}\n - Events From: ${status.start}\n - Events To: ${status.end}\n - Logged: ${status.eventsLogged}\n - Run Time: ${status.runTime}"
		}
	}
}

def devicesPage() {
	dynamicPage(name:"devicesPage") {
		getDevicesPageContent()
	}
}

private getDevicesPageContent() {
	section("Choose Devices") {
		paragraph "Selecting a device from one of the fields below lets the SmartApp know that the device should be included in the logging process."
		paragraph "Each device only needs to be selected once and which field you select it from has no effect on which events will be logged for it."
		paragraph "There's a field below for every capability, but you should be able to locate most of your devices in either the 'Actuators' or 'Sensors' fields at the top."		
		
		getCapabilities().each { 
			try {
				if (it.cap) {
					input "${it.cap}Pref", "capability.${it.cap}",
						title: "${it.title}:",
						multiple: true,
						hideWhenEmpty: true,
						required: false,
						submitOnChange: true
				}
			}
			catch (e) {
				logTrace "Failed to create input for ${it}: ${e.message}"
			}
		}
			
	}
}

def attributesPage() {
	dynamicPage(name:"attributesPage") {
		getAttributesPageContent()
	}
}

private getAttributesPageContent() {
	//def supportedAttr = getAllAttributes()?.sort()
	def supportedAttr = getSupportedAttributes()?.sort()
	if (supportedAttr) {
		section("Choose Events") {
			paragraph "Select all the events that should get logged for all devices that support them."
			paragraph "If the event you want to log isn't shown, verify that you've selected a device that supports it because only supported events are included."
			input "allowedAttributes", "enum",
				title: "Which events should be logged?",
				required: true,
				multiple: true,					
				submitOnChange: true,
				options: supportedAttr
		}
	}
	else {
		section("Choose Events") {
			paragraph "You need to select devices before you can choose events."
		}
	}
}

def attributeExclusionsPage() {
	dynamicPage(name:"attributeExclusionsPage") {		
		section ("Device Exclusions (Optional)") {
			
			def startTime = new Date().time
			
			if (settings?.allowedAttributes) {				
				paragraph "If there are some events that should't be logged for specific devices, use the corresponding event fields below to exclude them."
				paragraph "You can also use the fields below to see which devices support each event."
				
				def devices = getSelectedDevices()?.sort { it.displayName }
				
				settings?.allowedAttributes?.sort()?.each { attr ->
				
					if (startTime && (new Date().time - startTime) > 15000) {
						paragraph "The SmartApp was able to load all the fields within the allowed time.  If the event you're looking for didn't get loaded, select less devices or attributes."
						startTime = null
					}
					else if (startTime) {				
						try {
							def attrDevices = (isAllDeviceAttr("$attr") ? devices : (devices?.findAll{ device ->
								device.hasAttribute("${attr}")
							}))?.collect { it.displayName }?.unique()
							if (attrDevices) {
								input "${attr}Exclusions", "enum",
									title: "Exclude ${attr} events:",
									required: false,
									multiple: true,
									options: attrDevices
							}
						}
						catch (e) {
							logWarn "Error while getting device exclusion list for attribute ${attr}: ${e.message}"
						}
					}
				}
			}
		}
	}
}

def optionsPage() {
	dynamicPage(name:"optionsPage") {
		getOptionsPageContent()
	}
}

private getOptionsPageContent() {
	section ("Logging Options") {
		input "logFrequency", "enum",
			title: "Log Events Every:",
			required: false,
			defaultValue: "5 Minutes",
			options: ["5 Minutes", "10 Minutes", "15 Minutes", "30 Minutes", "1 Hour", "3 Hours"]
		input "logCatchUpFrequency", "enum",
			title: "Maximum Catch-Up Interval:\n(Must be greater than 'Log Events Every':",
			required: false,
			defaultValue: logCatchUpFrequencySetting,
			options: ["15 Minutes", "30 Minutes", "1 Hour", "2 Hours", "6 Hours"]
		input "maxEvents", "number",
			title: "Maximum number of events to log for each device per execution. (1 - 200)",
			range: "1..200",
			defaultValue: maxEventsSetting,
			required: false
		input "logDesc", "bool",
			title: "Log Event Descripion?",
			defaultValue: true,
			required: false
		input "useValueUnitDesc", "bool",
			title: "Use Value and Unit for Description?",
			defaultValue: true,
			required: false
		input "logReporting", "bool",
			title: "Include additional columns for short date and hour?",
			defaultValue: false,
			required: false
		input "deleteExtraColumns", "bool",
			title: "Delete Extra Columns?",
			description: "Enable this setting to increase the log size.",
			defaultValue: true,
			required: false
		input "archiveType", "enum",
			title: "Archive Type:",
			defaultValue: "None",
			submitOnChange: true,
			required: false,
			options: ["None", "Out of Space", "Events"]
		if (settings?.archiveType && !(settings?.archiveType in ["None", "Out of Space"])) {
			input "archiveInterval", "number",
				title: "Archive After How Many Events?",
				defaultValue: 50000,
				required: false,
				range: "100..100000"
		}
	}

  section("Http endpoint") {		
		input "endpointUrl", "text",
			title: "Url",
			required: true
		paragraph "Enter the http endpoint you would like to receive the events from"
	}

	section("Live Logging Options") {
		input "logging", "enum",
			title: "Types of messages to write to Live Logging:",
			multiple: true,
			required: false,
			defaultValue: ["debug", "info"],
			options: ["debug", "info", "trace"]
	}
}

private getPageLink(linkName, linkText, pageName, args=null,desc="",image=null) {
	def map = [
		name: "$linkName", 
		title: "$linkText",
		description: "$desc",
		page: "$pageName",
		required: false
	]
	if (args) {
		map.params = args
	}
	if (image) {
		map.image = image
	}
	href(map)
}

private buildSummary(items) {
	def summary = ""
	items?.each {
		summary += summary ? "\n" : ""
		summary += "   ${it}"
	}
	return summary
}

def uninstalled() {
	logTrace "Executing uninstalled()"
}

def installed() {	
	logTrace "Executing installed()"	
	state.installed = true
}

def updated() {
	logTrace "Executing updated()"
	state.installed = true
	
	unschedule()
	unsubscribe()
	
	if (settings?.allowedAttributes) {
    logDebug "Configured - Choose Events"
		state.attributesConfigured = true
	}
	else {
		logDebug "Unconfigured - Choose Events"
	}
	
	if (getSelectedDevices()) {
    logDebug "Configured - Choose Devices"
		state.devicesConfigured = true
	}
	else {
		logDebug "Unconfigured - Choose Devices"
	}
	
  if (state.optionsConfigured) {
    logDebug "Configured - options"
		state.devicesConfigured = true
	}
	else {
		logDebug "Unconfigured - options"
	}

	state.allConfigured = (state.attributesConfigured && state.devicesConfigured)
	
  if  (state.allConfigured) {
		def logFrequency = (settings?.logFrequency ?: "5 Minutes").replace(" ", "")
		
		"runEvery${logFrequency}"(logNewEvents)
		logDebug "run in 10secs..."
    startLogNewEvents();
		//runIn(10, startLogNewEvents)
	}
	else {
		logDebug "Event Logging is disabled because there are unconfigured settings."
	}
}

def startLogNewEvents() {
	logNewEvents()
}

def logNewEvents() {
  logDebug "logNewEvents..."
	def status = state.loggingStatus ?: [:]
	
	// Move the date range to the next position unless the google script failed last time or was skipped due to the sheet being archived.
	if (!status.success || status.eventsArchived) {
		status.lastEventTime = status.firstEventTime
	}
	
	status.success = null
	status.finished = null
	status.eventsArchived = null
	status.eventsLogged = 0
	status.started = new Date().time
	
	status.firstEventTime = getFirstEventTimeMS(status.lastEventTime)	
	status.lastEventTime = getNewLastEventTimeMS(status.started, (status.firstEventTime + 1000))
	
	def startDate = new Date(status.firstEventTime + 1000)
	def endDate = new Date(status.lastEventTime)
	
	state.loggingStatus = status

	def events = getNewEvents(startDate, endDate)
	def eventCount = events?.size ?: 0
	def actionMsg = eventCount > 0 ? ", posting them to " : ""
	
	logDebug "SmartThings found ${String.format('%,d', eventCount)} events between ${getFormattedLocalTime(startDate.time)} and ${getFormattedLocalTime(endDate.time)}${actionMsg}"
	
	if (events) {
		postEventsToHttpEndpoint(events)
	}
	else {		
		state.loggingStatus.success = true
		state.loggingStatus.finished = new Date().time
	}
}

private getFirstEventTimeMS(lastEventTimeMS) {
	def firstRunMS = (3 * 60 * 60 * 1000) // 3 Hours 
	return safeToLong(lastEventTimeMS) ?: (new Date(new Date().time - firstRunMS)).time 
}

private getNewLastEventTimeMS(startedMS, firstEventMS) {
	if ((startedMS - firstEventMS) > logCatchUpFrequencySettingMS) {
		return (firstEventMS + logCatchUpFrequencySettingMS)
	}
	else {
		return startedMS
	}
}

private getLogCatchUpFrequencySetting() {
	return settings?.logCatchUpFrequency ?: "1 Hour"
}

private getLogCatchUpFrequencySettingMS() {
	def minutesVal
	switch (logCatchUpFrequencySetting) {
		case "15 Minutes":
			minutesVal = 15
			break
		case "30 Minutes":
			minutesVal = 30
			break
		case "1 Hour":
			minutesVal = 60
			break
		case "2 Hours":
			minutesVal = 120
			break
		case "6 Hours":
			minutesVal = 360
			break
		default:
			minutesVal = 60
	}
	return (minutesVal * 60 * 1000)
}

private postEventsToHttpEndpoint(events) {
  def payload = "";
	def jsonOutput = new groovy.json.JsonOutput()

  def jsonData = jsonOutput.toJson([
		index: "",
	])

  logInfo "${events.size()} events to send."

  for (event in events ) {
    payload += "{ \"index\": {}}\n"
    payload += "${new groovy.json.JsonBuilder(event).toString()}\n"    
  }

  def pair ="elastic:0t^9&pPaHFXOe81h"
  def basicAuth = pair.bytes.encodeBase64();

	def params = [
		uri: "https://iotanalytics.dappsolution.com",
    path: "/iot_analytics-2020-smartthings/_bulk",
		contentType: "application/x-ndjson",
    headers: ['Authorization': "Basic " + basicAuth],
    body: payload
  ]

  //logTrace "params: ${params}"
  	
  asynchttp_v1.post(processLogEventsResponse, params)
  //asynchttp_v1.get(processLogEventsResponse, params)
  //asynchttp_v1.get(responseHandler, [uri: 'https://files.jonathanbisson.com'])
}

def processLogEventsResponse(response, data) {
  def status = state.loggingStatus ?: [:]

  logInfo "Response: ${response} statusCode: ${response.status}"

  if (response.hasError()) {
    status.success = false
    logError "response has error: ${response.getErrorMessage()}"
  } else {
    status.success = true
    status.eventsLogged = 0
    status.eventsErrors = 0
    
    for (index in response.json.items.index) {
      if (index.status != 201) {
        // logError "response status for index id: ${index._id} was: ${index.status} result: ${index.result} error :${index.error}"
        status.eventsErrors = status.eventsErrors + 1;
      }  else {
        status.eventsLogged = status.eventsLogged + index._shards.successful
      }      
    }
    
    logInfo "event logged: ${status.eventsLogged} errors: ${status.eventsErrors}"
  }

  state.loggingStatus = status
}

private getFormattedLoggingStatus() {
	def status = state.loggingStatus ?: [:]
	return [
		result: status?.success ? "Successful" : "Failed",
		start:  getFormattedLocalTime(safeToLong(status.firstEventTime)),
		end:  getFormattedLocalTime(safeToLong(status.lastEventTime)),
		runTime: "${((safeToLong(status.finished) - safeToLong(status.started)) / 1000)} seconds",
		eventsLogged: "${String.format('%,d', safeToLong(status.eventsLogged))}",
		totalEventsLogged: "${String.format('%,d', safeToLong(status.totalEventsLogged))}"
	]
}

private getNewEvents(startDate, endDate) {	
	def events = []
	
	logInfo "Retrieving Events from ${startDate} to ${endDate}"
	
  def start = new Date() - 1
  def end = new Date()

  def devices = []
  getSelectedDevices()?.each  { device ->
    /*if (device.label != "Aeotec Multi6 - Outdoor") {
     return 
    }*/
    //logWarn "getSupportedAttributes: ${device.getSupportedAttributes()}"
    getDeviceAllowedAttrs(device?.displayName)?.each { attr ->          
			//device.statesBetween("dewPoint", startDate, endDate, [max: maxEventsSetting])?.each { event ->
      //device.events([max: maxEventsSetting])?.each { event ->
      device.statesBetween("${attr}", startDate, endDate, [max: maxEventsSetting])?.each { event ->
      //device.statesBetween("${attr}", start, end, [max: 5])?.each { event ->
        //logWarn "device: ${device}\n id: ${device.id}\n label: ${device.label}\n manufacturerName: ${device.manufacturerName}\n modelName: ${device.modelName}\n name: ${device.name}\n typeName: ${device.typeName}\n"
        //logWarn "descriptionText: ${event.descriptionText}\n eventName: ${event.name}\n data: ${event.data}\n value: ${event.value}\n unit: ${event.unit}"
				def eventJson = [
					timestamp: new Date(),
          sensor: [
            id: device.id,
            name: device.name,
            label: device.label,
            type: getSensorType(device.typeName),
          ],
          event: [
            timestamp: event.isoDate,
            type: event.name,
            data: event.value,
            unit: event.unit ? event.unit : "",
          ]
				]
        events << eventJson
			}
		}
	}
  logWarn "devices: ${devices}"
  logWarn "Nb events found: ${events.size()}"
  //logWarn "Events: ${events}"

	return events?.unique()?.sort { it.timestamp }
}

private getSensorType(type) {
  switch(type) {
    case "Aeon Labs Smart Switch 6 Gen5":
    case "Aeon Labs Smart Switch dsc06106":
      return "switch"
    case "Aeon Labs MultiSensor 6":
      return "multisensor"
    case "Lifx Color Bulb":
      return "light"
    case "eGauge Energy Register":
      return "power meter"
    case "Lutron Caseta Wall Dimmer":
      return "switch"
    case "Universal Z-Wave Lock With Alarms":
      return "lock"
    case "Ecobee Thermostat":
      return "thermostat"
    case "Mobile Precense Occupancy":
      return "precense"
    default:
      return type;
  }
}
private getEventDesc(event) {
	if (settings?.useValueUnitDesc != false) {
		return "${event.value}" + (event.unit ? " ${event.unit}" : "")
	}
	else {
		def desc = "${event?.descriptionText}"
		if (desc.contains("{")) {
			desc = replaceToken(desc, "linkText", event.displayName)
			desc = replaceToken(desc, "displayName", event.displayName)
			desc = replaceToken(desc, "name", event.name)
			desc = replaceToken(desc, "value", event.value)
			desc = replaceToken(desc, "unit", event.unit)
		}
		return desc
	}
}

private replaceToken(desc, token, value) {
	desc = "$desc".replace("{{", "|").replace("}}", "|")
	return desc.replace("| ${token} |", "$value")
}

private getMaxEventsSetting() {
	return settings?.maxEvents ?: 200
}
	
private getFormattedLocalTime(utcTime) {
	if (utcTime) {
		try {
			def localTZ = TimeZone.getTimeZone(location.timeZone.ID)
			def localDate = new Date(utcTime + localTZ.getOffset(utcTime))	
			return localDate.format("MM/dd/yyyy HH:mm:ss")
		}
		catch (e) {
			logWarn "Unable to get formatted local time for ${utcTime}: ${e.message}"
			return "${utcTime}"
		}
	}
	else {
		return ""
	}
}

private getDeviceAllowedAttrs(deviceName) {
	def deviceAllowedAttrs = []
	try {
		settings?.allowedAttributes?.each { attr ->
			try {
				def attrExcludedDevices = settings?."${attr}Exclusions"
				
				if (!attrExcludedDevices?.find { it?.toLowerCase() == deviceName?.toLowerCase() }) {
					deviceAllowedAttrs << "${attr}"
				}
			}
			catch (e) {
				logWarn "Error while getting device allowed attributes for ${device?.displayName} and attribute ${attr}: ${e.message}"
			}
		}
	}
	catch (e) {
		logWarn "Error while getting device allowed attributes for ${device.displayName}: ${e.message}"
	}
	return deviceAllowedAttrs
}

private getSupportedAttributes() {
	def supportedAttributes = []
	def devices = getSelectedDevices()
	
	if (devices) {	
		getAllAttributes()?.each { attr ->
			try {
				if (isAllDeviceAttr("$attr") || devices?.find { it?.hasAttribute("${attr}") }) {
					supportedAttributes << "${attr}"
				}
			}
			catch (e) {
				logWarn "Error while finding supported devices for ${attr}: ${e.message}"
			}
			
		}
	}
	
	return supportedAttributes?.unique()?.sort()
}

private isAllDeviceAttr(attr) { 
	return getCapabilities().find { it.allDevices && it.attr == attr } ? true : false
}

private getAllAttributes() {
	def attributes = []	
	
	getCapabilities().each { cap ->
		try {		
			if (cap?.attr) {
				if (cap.attr instanceof Collection) {
					cap.attr.each { attr ->
						attributes << "${attr}"
					}
				}
				else {
					attributes << "${cap?.attr}"
				}
			}
		}
		catch (e) {
			logWarn "Error while getting attributes for capability ${cap}: ${e.message}"
		}
	}	
	return attributes
}

private getSelectedDeviceNames() {
	try {
		return getSelectedDevices()?.collect { it?.displayName }?.sort()
	}
	catch (e) {
		logWarn "Error while getting selected device names: ${e.message}"
		return []
	}
}

private getSelectedDevices() {
	def devices = []
	getCapabilities()?.each {    
		try {
			if (it.cap && settings?."${it.cap}Pref") {        
				devices << settings?."${it.cap}Pref"
			}
		}
		catch (e) {
			logWarn "Error while getting selected devices for capability ${it}: ${e.message}"
		}
	}	
	return devices?.flatten()?.unique { it.displayName }
}

private getCapabilities() {
	[
		[title: "Actuators", cap: "actuator"],
		[title: "Sensors", cap: "sensor"],
		[title: "Acceleration Sensors", cap: "accelerationSensor", attr: "acceleration"],
		[title: "Device Activity", attr: "activity", allDevices: true],
		[title: "Alarms", cap: "alarm", attr: "alarm"],
		[title: "Batteries", cap: "battery", attr: "battery"],
		[title: "Beacons", cap: "beacon", attr: "presence"],
		[title: "Bulbs", cap: "bulb", attr: "switch"],
		[title: "Buttons", cap: "button", attr: ["button", "numberOfButtons"]],
		[title: "Carbon Dioxide Measurement Sensors", cap: "carbonDioxideMeasurement", attr: "carbonDioxide"],
		[title: "Carbon Monoxide Detectors", cap: "carbonMonoxideDetector", attr: "carbonMonoxide"],
		[title: "Color Control Devices", cap: "colorControl", attr: ["color", "hue", "saturation"]],
		[title: "Color Temperature Devices", cap: "colorTemperature", attr: "colorTemperature"],
		[title: "Consumable Devices", cap: "consumable", attr: "consumableStatus"],
		[title: "Contact Sensors", cap: "contactSensor", attr: "contact"],
		[title: "Doors", cap: "doorControl", attr: "door"],
		[title: "Energy Meters", cap: "energyMeter", attr: "energy"],
		[title: "Garage Doors", cap: "garageDoorControl", attr: "door"],
		[title: "Illuminance Measurement Sensors", cap: "illuminanceMeasurement", attr: "illuminance"],
		[title: "Image Capture Devices", cap: "imageCapture", attr: "image"],		
		[title: "Indicators", cap: "indicator", attr: "indicatorStatus"],
		[title: "Lights", cap: "light", attr: "switch"],
		[title: "Locks", cap: "lock", attr: "lock"],
		[title: "Media Controllers", cap: "mediaController", attr: "currentActivity"],
		[title: "Motion Sensors", cap: "motionSensor", attr: "motion"],
		[title: "Music Players", cap: "musicPlayer", attr: ["level", "mute", "status", "trackDescription"]],
		[title: "Outlets", cap: "outlet", attr: "switch"],
		[title: "pH Measurement Sensors", cap: "phMeasurement", attr: "pH"],
		[title: "Power Meters", cap: "powerMeter", attr: "power"],
		[title: "Power Sources", cap: "powerSource", attr: "powerSource"],
		[title: "Presence Sensors", cap: "presenceSensor", attr: ["occupancy", "presence"]],
		[title: "Relative Humidity Measurement Sensors", cap: "relativeHumidityMeasurement", attr: "humidity"],
		[title: "Relay Switches", cap: "relaySwitch", attr: "switch"],
		[title: "Shock Sensors", cap: "shockSensor", attr: "shock"],
		[title: "Signal Strength Sensors", cap: "signalStrength", attr: ["lqi", "rssi"]],
		[title: "Sleep Sensors", cap: "sleepSensor", attr: "sleeping"],
		[title: "Smoke Detectors", cap: "smokeDetector", attr: "smoke"],
		[title: "Sound Pressure Level Sensors", cap: "soundPressureLevel", attr: "soundPressureLevel"],
		[title: "Sound Sensors", cap: "soundSensor", attr: "sound"],
		[title: "Speech Recognition Sensors", cap: "speechRecognition", attr: "phraseSpoken"],
		[title: "Switches", cap: "switch", attr: "switch"],
		[title: "Switch Level Sensors", cap: "switchLevel", attr: "level"],
		[title: "Tamper Alert Sensors", cap: "tamperAlert", attr: "tamper"],
		[title: "Temperature Measurement Sensors", cap: "temperatureMeasurement", attr: "temperature"],
		[title: "Thermostats", cap: "thermostat", attr: ["coolingSetpoint", "heatingSetpoint", "temperature", "thermostatFanMode", "thermostatMode", "thermostatOperatingState", "thermostatSetpoint"]],
		[title: "Three Axis Sensors", cap: "threeAxis", attr: "threeAxis"],
		[title: "Touch Sensors", cap: "touchSensor", attr: "touch"],
		[title: "Ultraviolet Index Sensors", cap: "ultravioletIndex", attr: "ultravioletIndex"],
		[title: "Valves", cap: "valve", attr: "valve"],
		[title: "Voltage Measurement Sensors", cap: "voltageMeasurement", attr: "voltage"],
		[title: "Water Sensors", cap: "waterSensor", attr: "water"],
		[title: "Window Shades", cap: "windowShade", attr: "windowShade"]
	]
}

// private averageSupportedAttributes() {
	// [
		// "battery",
		// "carbonDioxide",
		// "colorTemperature",
		// "coolingSetpoint",
		// "energy",
		// "heatingSetpoint",
		// "humidity",
		// "illuminance",
		// "level",
		// "lqi",
		// "pH",
		// "power",
		// "rssi",
		// "soundPressureLevel",
		// "temperature",
		// "thermostatSetpoint",
		// "ultravioletIndex",
		// "voltage"
	// ]
// }

private getArchiveTypeOptions() {
	[
		[name: "None"],
		[name: "Out of Space"],
		[name: "Weeks", defaultVal: 2, range: "1..52"],
		[name: "Events", defaultVal: 25000, range: "1000..100000"]
	]
}

long safeToLong(val, defaultVal=0) {
	try {
		if (val && (val instanceof Long || "${val}".isLong())) {
			return "$val".toLong()
		}
		else {
			return defaultVal
		}
	}
	catch (e) {
		return defaultVal
	}
}

private logTrace(msg) {
	if (loggingTypeEnabled("trace")) {
		log.trace msg
	}
}

private logDebug(msg) {
	if (loggingTypeEnabled("debug")) {
		log.debug msg
	}
}

private logInfo(msg) {
	if (loggingTypeEnabled("info")) {
		log.info msg
	}
}

private logWarn(msg) {
	log.warn msg
}

private logError(str) {
  log.error str
}

private loggingTypeEnabled(loggingType) {
	return (!settings?.logging || settings?.logging?.contains(loggingType))
}
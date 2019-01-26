/**
 *
 *  eGauge Energy Meter
 *
 *  Copyright 2019 jbisson
 *
 *
 *  Provide full energy power consumption statistics for eGauge devices. 
 *  This smartApp allow you to install (as a device handler) all of your eGauge registers into the smartThing ecosystem. 
 *  You can then use all other smartthing apps to trigger various actions or different reporting, some apps can be found under:
 *  - webCoRE https://wiki.webcore.co/webCoRE
 *  - SmartThings Data Visualisation using InfluxDB and Grafana https://community.smartthings.com/t/smartthings-data-visualisation-using-influxdb-and-grafana
 *  - Simple Event Logger https://community.smartthings.com/t/release-simple-event-logger
 *  
 * Installation instructions:
 * 1) You'll first need to find your eGauge ip address. Refer to the eGauge instruction manual: https://www.egauge.net/support/#docs
 *    The mDns name (ie: eGaugeName.local will not work in smartthing), I strongly recommend you set an ip address (within the eGauge Settings page)
 * 2) Make sure both egauge-energy-meter connect (this smartapp) as well as the egauge-energy-meter are both installed
 * 
 *
 * Limitations:
 * 1) Power & Apparent Power register type are currently supported. Adding more register type would be easy, just ask if you have a real use case for it.
 * 2) The refresh time interval for the different event are as follow:
 *    - Current energy power: every minute
 *    - last hour energy report: every 5 minutes
 *    - last 24 hours, week, & month energy report: every hour
 * 
 * 
 *  Been tested against an eGauge Core device but should work for eGauge Pro as well.  
 *  Visit https://www.egauge.net/ for more information about their devices lineup.
 *
 *
 *
 *  Revision History
 *  ==============================================
 *  2019-01-26 Version 1.0.0  Initial version.
 *
 *
 *  Developer's Notes
 *  
 *  eGauge api specs: https://www.egauge.net/media/support/docs/egauge-xml-api.pdf
 *
 */
 
def version() {
    return "1.0.0 [2019-01-26]"
}

definition(
		name: "eGauge Energy Meter (Connect)",
		namespace: "jbisson",
		author: "Jonathan Bisson",
		description: "App used to configure all of the different eGauge register",
		category: "SmartThings Labs",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")		
	
	preferences {
		page(name: "mainPageConfiguration", title: "eGauge energy configuration", nextPage: "deviceDiscovery", uninstall: true, content: "mainPageConfiguration") {	}
		page(name: "deviceDiscovery", title: "UPnP Device Setup", content: "deviceDiscovery")
	}

def mainPageConfiguration() {
	log.info "mainPageConfiguration()"
	
	state.refresh = 0
		
	return dynamicPage(name: "mainPageConfiguration", title: "eGauge energy configuration", nextPage: "deviceDiscovery", uninstall: true) {			
		section("About") {
				input title: "eGauge Energy Connect Version", description: "v${version()}", displayDuringSetup: true, type: "paragraph", element: "paragraph", required: false
		}
			
		section("Configuration") {
			input name: "eGaugeIpAddress", type: "text", title: "eGauge hostname or Ip Address (IE: eGaugeName.d.egauge.net))\n", defaultValue: "192.168.15.111", displayDuringSetup: true, required: true
			input name: "costPerKwh", type: "decimal", title: "Cost per kWh (Used for energy cost /per kWh) [Default (0.12)]\n", defaultValue: 0.12, displayDuringSetup: true, required: true
		}
		
		section("Logging") {		
			input name: "isLogLevelDebug", type: "bool", title: "Show debug log level ?\n", defaultValue: "true", displayDuringSetup: true, required: true
		}
	}
}

/*******************************************************************************
 * 	SmartApp Methods                                                           *
 ******************************************************************************/
 
def installed() {
	log.info "installed()"

	initialize()
}

def uninstalled() {
	log.info "uninstalled()"

    getChildDevices().each {
		log.info "removing: {it.deviceNetworkId}"
        deleteChildDevice(it.deviceNetworkId)
    }
}

def deviceDiscovery() {
	log.info "deviceDiscovery ${state.refresh} eGaugeIpAddress: ${eGaugeIpAddress} size: ${getRegisters().size()}"
	
	if (state.refresh == 0) {
		searchDevices()
	}
	
	def registerOptions = [:]
	getRegisters().each {
		registerOptions["${it.value["name"]}"] = it.value["name"]
	}
	
	if (state.refresh > 6) {
		return dynamicPage(name: "deviceDiscovery", title: "Cannot connect...", nextPage: "", uninstall: true) {
			section("Connection to the eGauge energy device, connection timed out. Please make sure you have the right ip address.") {}		
		}
    } else if (getRegisters().size() > 0) {
		return dynamicPage(name: "deviceDiscovery", title: "Discovery Finished", nextPage: "", refreshInterval: 0, install: true, uninstall: true) {
			section("Please select the different eGauge registers you want to add...") {
				input "selectedRegisters", "enum", required: true, title: "Select Registers (${registerOptions.size() ?: 0} found)", multiple: true, options: registerOptions
			}
		}
	} else {
		state.refresh = state.refresh + 2
		log.info "deviceDiscovery2 ${state.refresh}"
		return dynamicPage(name: "deviceDiscovery", title: "Discovery Started...", nextPage: "", refreshInterval: 2, install: false, uninstall: false) {
			section("Please wait while we discover your eGauge registers...") {}
		}
	}
}

def refreshAllCounters() {
	refreshRegisterNow()
	refreshOneHourEnergyReport()
	refresh24HourEnergyReport()
	refreshLastWeekEnergyReport()
	refreshLastMonthEnergyReport()
	refreshSinceStartEnergyReport()
}

def updated() {
	log.info "updated()"

	runEvery1Minute(refreshRegisterNow)
	refreshRegisterNow()
	
	runEvery5Minutes(refreshOneHourEnergyReport)
	refreshOneHourEnergyReport()
	
	runEvery1Hour(refresh24HourEnergyReport)
	refresh24HourEnergyReport()
	
	runEvery1Hour(refreshLastWeekEnergyReport)
	refreshLastWeekEnergyReport()
	
	runEvery1Hour(refreshLastMonthEnergyReport)
	refreshLastMonthEnergyReport()
	
	runEvery1Hour(refreshSinceStartEnergyReport)
	refreshSinceStartEnergyReport()
	
	syncRegisters()
	
}

void deviceDescriptionHandler(physicalgraph.device.HubResponse hubResponse) {
    logInfo "deviceDescriptionHandler()status ${hubResponse.status}"
	
	def parser=new XmlSlurper()
	parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false) 
	def statusrsp = parser.parseText(hubResponse.body)   
	
	state.registers = [:]
	statusrsp.data.cname.each { thing ->
		logInfo "v2 eq $thing ${thing.@t}"
		
		def type
		switch (thing.@t) {
			case "P":				
				type = "Power"
				break
			case "S":			
				type = "Apparent Power"
				break
			default:			
				break;		
		}
		if (type) {			
			state.registers["eGauge.register.${thing}"] = [:]
			state.registers["eGauge.register.${thing}"].type = type
			state.registers["eGauge.register.${thing}"].name = thing.text()
		}		
	}
}

def initialize() {	
	log.info "initialize()"
	
	syncRegisters()	
}

void registerNowCallback(physicalgraph.device.HubResponse hubResponse) {
	logInfo "registerNowCallback() with status: ${hubResponse.status}"
	
	def parser=new XmlSlurper()
	parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false) 
	def statusrsp = parser.parseText(hubResponse.body)
	
	
	def i = 0
	statusrsp.data.cname.each { thing ->
		def currentPower = Long.parseLong(statusrsp.data.r[0].c[i].text()) - Long.parseLong(statusrsp.data.r[1].c[i].text())
		i = i + 1
		
		if (state.registers["eGauge.register.${thing}"] && state.registers["eGauge.register.${thing}"].alive) {
			logDebug "Need update for ${state.registers["eGauge.register.${thing}"]} ${currentPower} watts}"
			
			def device = getChildDevices()?.find {it.deviceNetworkId == "eGauge.register.${thing}"}
			device.updateCurrentPower("${currentPower}")
		}
	}
}

def refreshRegisterNow() {	
	logDebug "refreshRegisterNow() Trying to connect to eGauge meter ip address: ${eGaugeIpAddress}"
	def result1 = new physicalgraph.device.HubAction("""GET /cgi-bin/egauge-show?a&S&n=2 HTTP/1.1\r\nHOST: ${eGaugeIpAddress}:80\r\n\r\n""",
		physicalgraph.device.Protocol.LAN,  
		"${eGaugeIpAddress}",
		[callback: registerNowCallback])
	sendHubCommand(result1)	
}

def searchDevices() {	
	logDebug "searchDevices() Trying to connect to eGauge meter ip address: ${eGaugeIpAddress}"
	def result = new physicalgraph.device.HubAction("""GET /cgi-bin/egauge-show?a&d&n=1 HTTP/1.1\r\nHOST: ${eGaugeIpAddress}:80\r\n\r\n""",
		physicalgraph.device.Protocol.LAN, 
		"${eGaugeIpAddress}",
		[callback: deviceDescriptionHandler])
	sendHubCommand(result)	
}

def refreshOneHourEnergyReport() {	
	logDebug "refreshOneHourEnergyReport() Trying to connect to eGauge meter ip address: ${eGaugeIpAddress}"
	
	def result1 = new physicalgraph.device.HubAction("""GET /cgi-bin/egauge-show?a&S&n=3&s=3598 HTTP/1.1\r\nHOST: ${eGaugeIpAddress}:80\r\n\r\n""",
		physicalgraph.device.Protocol.LAN,  
		"${eGaugeIpAddress}",
		[callback: refreshOneHourEnergyReportCallback])
	sendHubCommand(result1)	
}

def refresh24HourEnergyReport() {	
	logDebug "refresh24HourEnergyReport() Trying to connect to eGauge meter ip address: ${eGaugeIpAddress}"
	def result1 = new physicalgraph.device.HubAction("""GET /cgi-bin/egauge-show?a&h&n=3&s=23 HTTP/1.1\r\nHOST: ${eGaugeIpAddress}:80\r\n\r\n""",
		physicalgraph.device.Protocol.LAN,  
		"${eGaugeIpAddress}",
		[callback: refresh24HourEnergyReportCallback])
	sendHubCommand(result1)	
}

def refreshLastWeekEnergyReport() {	
	logDebug "refreshLastWeekEnergyReport() Trying to connect to eGauge meter ip address: ${eGaugeIpAddress}"
	def result1 = new physicalgraph.device.HubAction("""GET /cgi-bin/egauge-show?a&d&n=3&s=6 HTTP/1.1\r\nHOST: ${eGaugeIpAddress}:80\r\n\r\n""",
		physicalgraph.device.Protocol.LAN,  
		"${eGaugeIpAddress}",
		[callback: refreshLastWeekEnergyReportCallback])
	sendHubCommand(result1)	
}

def refreshLastMonthEnergyReport() {
	logDebug "refreshLastWeekEnergyReport() Trying to connect to eGauge meter ip address: ${eGaugeIpAddress}"
	def result1 = new physicalgraph.device.HubAction("""GET /cgi-bin/egauge-show?a&d&n=3&s=30 HTTP/1.1\r\nHOST: ${eGaugeIpAddress}:80\r\n\r\n""",
		physicalgraph.device.Protocol.LAN,  
		"${eGaugeIpAddress}",
		[callback: refreshLastMonthEnergyReportCallback])
	sendHubCommand(result1)	
}

def refreshSinceStartEnergyReport() {
	logDebug "refreshSinceStartEnergyReport() Trying to connect to eGauge meter ip address: ${eGaugeIpAddress}"
	def result1 = new physicalgraph.device.HubAction("""GET /cgi-bin/egauge-show?a&d&s=30 HTTP/1.1\r\nHOST: ${eGaugeIpAddress}:80\r\n\r\n""",
		physicalgraph.device.Protocol.LAN,  
		"${eGaugeIpAddress}",
		[callback: refreshSinceStartEnergyReportCallback])
	sendHubCommand(result1)	
}

void refreshOneHourEnergyReportCallback(physicalgraph.device.HubResponse hubResponse) {	
	logInfo "refreshOneHourEnergyReportCallback() with status: ${hubResponse.status}"
		
	def parser = new XmlSlurper()
	parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false) 
	def statusrsp = parser.parseText(hubResponse.body)
	
	def i = 0
	def time_delta = Long.parseLong(statusrsp.data.@time_delta[0].text())
	
	statusrsp.data.cname.each { thing ->
		def lastHourEnergyReport = String.format("%5.3f", ((Long.parseLong(statusrsp.data.r[0].c[i].text()) - Long.parseLong(statusrsp.data.r[1].c[i].text())).div(time_delta*1000)))
		i = i + 1
		
		if (state.registers["eGauge.register.${thing}"] && state.registers["eGauge.register.${thing}"].alive) {
			logDebug "Need update for ${state.registers["eGauge.register.${thing}"]} with ${lastHourEnergyReport} kWh"
			
			def device = getChildDevices()?.find {it.deviceNetworkId == "eGauge.register.${thing}"}
			device.updateOneHourEnergyReport("${lastHourEnergyReport}")
		}
	}
}

void refresh24HourEnergyReportCallback(physicalgraph.device.HubResponse hubResponse) {
	logInfo "refresh24HourEnergyReportCallback() with status: ${hubResponse.status}"
	
	def parser = new XmlSlurper()
	parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false) 
	def statusrsp = parser.parseText(hubResponse.body)
	
	def i = 0
	def time_delta = Long.parseLong(statusrsp.data.@time_delta[0].text())
	statusrsp.data.cname.each { thing ->
		def last24HourEnergyReport = String.format("%5.3f", ((Long.parseLong(statusrsp.data.r[0].c[i].text()) - Long.parseLong(statusrsp.data.r[1].c[i].text())).div(time_delta*1000)))
		i = i + 1
		
		if (state.registers["eGauge.register.${thing}"] && state.registers["eGauge.register.${thing}"].alive) {
			logDebug "refresh24HourEnergyReportCallback() Need update for ${state.registers["eGauge.register.${thing}"]} ${last24HourEnergyReport} kWh"
			
			def device = getChildDevices()?.find {it.deviceNetworkId == "eGauge.register.${thing}"}
			device.update24HourEnergyReport("${last24HourEnergyReport}")
		}
	}
}

void refreshLastWeekEnergyReportCallback(physicalgraph.device.HubResponse hubResponse) {
	logInfo "refreshLastWeekEnergyReportCallback() with status: ${hubResponse.status}"
	
	def parser = new XmlSlurper()
	parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false) 
	def statusrsp = parser.parseText(hubResponse.body)
	
	def i = 0
	
	def time_delta = Long.parseLong(statusrsp.data.@time_delta[0].text())
	statusrsp.data.cname.each { thing ->
		def lastWeekEnergyReport = String.format("%5.3f", ((Long.parseLong(statusrsp.data.r[0].c[i].text()) - Long.parseLong(statusrsp.data.r[1].c[i].text())).div(time_delta*1000)))
		i = i + 1
		
		if (state.registers["eGauge.register.${thing}"] && state.registers["eGauge.register.${thing}"].alive) {
			logDebug "refreshLastWeekEnergyReportCallback() Need update for ${state.registers["eGauge.register.${thing}"]} ${lastWeekEnergyReport} kWh"
			
			def device = getChildDevices()?.find {it.deviceNetworkId == "eGauge.register.${thing}"}
			device.updateLastWeekEnergyReport("${lastWeekEnergyReport}")
		}
	}
}

void refreshLastMonthEnergyReportCallback(physicalgraph.device.HubResponse hubResponse) {
	logInfo "refreshLastMonthEnergyReportCallback() with status: ${hubResponse.status}"
	
	def parser = new XmlSlurper()
	parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false) 
	def statusrsp = parser.parseText(hubResponse.body)
	
	def i = 0
	
	def time_delta = Long.parseLong(statusrsp.data.@time_delta[0].text())
	statusrsp.data.cname.each { thing ->
		def lastMonthEnergyReport = String.format("%5.3f", ((Long.parseLong(statusrsp.data.r[0].c[i].text()) - Long.parseLong(statusrsp.data.r[1].c[i].text())).div(time_delta*1000)))
		i = i + 1
		
		if (state.registers["eGauge.register.${thing}"] && state.registers["eGauge.register.${thing}"].alive) {
			logDebug "refreshLastMonthEnergyReportCallback() Need update for ${state.registers["eGauge.register.${thing}"]} ${lastMonthEnergyReport} kWh"
			
			def device = getChildDevices()?.find {it.deviceNetworkId == "eGauge.register.${thing}"}
			device.updateLastMonthEnergyReport("${lastMonthEnergyReport}")
		}
	}
}

void refreshSinceStartEnergyReportCallback(physicalgraph.device.HubResponse hubResponse) {
	logInfo "refreshSinceStartEnergyReportCallback() with status: ${hubResponse.status}"
	
	def parser = new XmlSlurper()
	parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false) 
	def statusrsp = parser.parseText(hubResponse.body)
	
	def i = 0
		
	def epoch = Long.decode(statusrsp.data.@epoch[0].text())
	def time_stamp = Long.decode(statusrsp.data.@time_stamp[0].text())	
	def nbDays = new Long(((time_stamp - epoch) / (60 * 60 * 24)).intValue())
	
	statusrsp.data.cname.each { thing ->
		def sinceStartKwhEnergyReport = String.format("%5.3f", ((Long.parseLong(statusrsp.data.r[0].c[i].text()) - Long.parseLong(statusrsp.data.r[statusrsp.data.r.size() - 1].c[i].text())).div(60 * 60 * 1000 * 24 * nbDays)))
		def sinceStartKwEnergyReport = String.format("%5.1f", ((Long.parseLong(statusrsp.data.r[0].c[i].text()) - Long.parseLong(statusrsp.data.r[statusrsp.data.r.size() - 1].c[i].text())).div(60 * 60 * 1000)))
		i = i + 1
		
		if (state.registers["eGauge.register.${thing}"] && state.registers["eGauge.register.${thing}"].alive) {
			logDebug "refreshSinceStartEnergyReportCallback() Need update for ${state.registers["eGauge.register.${thing}"]} ${sinceStartKwEnergyReport} kW ${sinceStartKwhEnergyReport} kWh"
			
			def device = getChildDevices()?.find {it.deviceNetworkId == "eGauge.register.${thing}"}
			def result = [:]
			result["sinceDate"] = new Date(epoch * 1000).format('yyyy-MM-dd', location.timeZone)	
			result["nbDays"] = nbDays
			result["kW"] = "${sinceStartKwEnergyReport}"
			result["kWh"] = "${sinceStartKwhEnergyReport}"
			
			device.updateSinceStartEnergyReport(result)
		}
	}
}

def getRegisters() {
	if (!state.registers) {
		state.registers = [:]
	}
	state.registers
}

def syncRegisters() {
	state.registers.each { register ->
		register.value.alive = false
	}

	selectedRegisters.each { selectedRegister ->
		def device = getChildDevices()?.find {
				it.deviceNetworkId == "eGauge.register.${selectedRegister.value}"
		}
		
		registers["eGauge.register.${selectedRegister.value}"].alive = true
				
		if (!device) {			
			logInfo "Creating eGauge register: ${selectedRegister.value} of type: ${state.registers["eGauge.register.${selectedRegister.value}"]}"
		
			addChildDevice("eGauge Energy Register", "eGauge.register.${selectedRegister.value}", null, [
				completedSetup: true,
				"label": "eGauge R(${selectedRegister.value})",
				"data": [
					"type": state.registers["eGauge.register.${selectedRegister.value}"]					
				]
			])
		}
	}
	
	def allDevicesToDelete = getChildDevices().findAll {
		!(registers["${it.deviceNetworkId}"] && registers["${it.deviceNetworkId}"].alive)
	}

	allDevicesToDelete.each {
		logInfo "Removing: ${it.deviceNetworkId}"
		deleteChildDevice(it.deviceNetworkId)
	}
}


/*******************************************************************************
 * 	Utilities Methods                                                          *
 ******************************************************************************/

void logInfo(str) {
    log.info str
}

void logWarn(str) {
    log.warn str
}

void logError(str) {
    log.error str
}

void logDebug(str) {
    if (isLogLevelDebug) {
        log.debug str
    }
}


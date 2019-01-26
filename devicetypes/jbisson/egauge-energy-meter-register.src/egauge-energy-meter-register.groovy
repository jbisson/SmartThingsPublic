/**
 *
 *  eGauge Energy Register
 *
 *  Copyright 2019 jbisson
 *
 *
 *  Provide full energy power consumption statistics for eGauge devices. 
 *  This device handler require you to install the eGauge-energy-meter-connect smartApp. 
 *
 * Note that you'll see two type of energy cost cell: "Actual" and "PJ". Actual means the energy has already been consumed where as the "PJ" refer to the 
 * "Projection" at that current energy rate that we are estimate. 
 * For example, if you are pulling 1000 watts of energy at any given time, we will project a cost estimate in a per hour, per week, per month, per year.
 * 
 *  
 * 
 * Limitations:
 * 1) Power & Apparent Power register type are currently supported. Adding more register type would be easy, just ask if you have a real use case for it.
 * 2) The refresh time interval for the different event are as follow:
 *    - Current energy power: every minute
 *    - last hour energy report: every 5 minutes
 *    - last 24 hours, week, & month energy report: every hour
 *  
 *  Visit https://www.egauge.net/ for more information about their devices lineup.
 *
 *
 *  Revision History
 *  ==============================================
 *  2019-01-26 Version 1.0.0  Initial version.
 *
 *
 */

def clientVersion() {
    return "1.0.0 [2019-01-26]"
}

metadata {
    definition(name: "eGauge Energy Register", namespace: "jbisson", author: "Jonathan Bisson") {
        capability "Power Meter"
        capability "Energy Meter"		
        capability "Refresh"
		//capability "Power Consumption Report"
        
        command "reset"
		command "updateCurrentPower"
		command "updateOneHourEnergyReport"
		command "update24HourEnergyReport"
		command "updateLastWeekEnergyReport"
		command "updateLastMonthEnergyReport"
		command "updateSinceStartEnergyReport"
    }

    tiles(scale: 2) {
        valueTile("power", "device.power", width: 6, height: 4, decoration: "flat") {
            state "default", label: '${currentValue} W', backgroundColors:[
					[value: -3000, color: "#fe1400"],
					[value: -2000, color: "#fe4500"],
					[value: -1000, color: "#fe5800"],
					[value: -800, color: "#fe8000"],
					[value: -600, color: "#fe9800"],
					[value: -500, color: "#fec500"],
					[value: -300, color: "#fefb00"],
					[value: 0, color: "#44b621"]
            ]
        }

        valueTile("currentEnergyCostTxt", "currentEnergyCostTxt", width: 2, height: 1, decoration: "flat") {
            state "default", label: 'Energy Cost\n(Current):'
        }

        valueTile("currentEnergyCostHour", "currentEnergyCostHour", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'PJ Per\nHour\n$${currentValue}'
        }

        valueTile("currentEnergyCostWeek", "currentEnergyCostWeek", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'PJ Per\nWeek\n$${currentValue}'
        }

        valueTile("currentEnergyCostMonth", "currentEnergyCostMonth", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'PJ Per\nMonth\n$${currentValue}'
        }

        valueTile("currentEnergyCostYear", "currentEnergyCostYear", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'PJ Per\nYear\n$${currentValue}'
        }

        valueTile("lastHourEnergyValue", "device.energy", width: 2, height: 1, decoration: "flat") {
            state "default", label: 'Last hour\n${currentValue}'
        }

		valueTile("lastHourEnergyCostHour", "lastHourEnergyCostHour", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'Actual\n Last Hour\n$${currentValue}'
        }

        valueTile("lastHourEnergyCostWeek", "lastHourEnergyCostWeek", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'PJ Per\nWeek\n$${currentValue}'
        }

        valueTile("lastHourEnergyCostMonth", "lastHourEnergyCostMonth", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'PJ Per\nMonth\n$${currentValue}'
        }

        valueTile("lastHourEnergyCostYear", "lastHourEnergyCostYear", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'PJ Per\nYear \n$${currentValue}'
        }
		
		valueTile("last24HourEnergyValue", "last24HourEnergyValue", width: 2, height: 1, decoration: "flat") {
            state "default", label: 'Last 24 hours\n${currentValue}'
        }

		valueTile("last24HourEnergyCostDay", "last24HourEnergyCostDay", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'Actual\nDay\n$${currentValue}'
        }
		
        valueTile("last24HourEnergyCostWeek", "last24HourEnergyCostWeek", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'PJ Per\nWeek\n$${currentValue}'
        }

        valueTile("last24HourEnergyCostMonth", "last24HourEnergyCostMonth", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'PJ Per\nMonth\n$${currentValue}'
        }

        valueTile("last24HourEnergyCostYear", "last24HourEnergyCostYear", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'PJ Per\nYear \n$${currentValue}'
        }
		
		valueTile("lastWeekEnergyValue", "lastWeekEnergyValue", width: 2, height: 1, decoration: "flat") {
            state "default", label: 'Last Week\n${currentValue}'
        }
		
		valueTile("lastWeekEnergyCostWeek", "lastWeekEnergyCostWeek", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'Actual\nWeek\n$${currentValue}'
        }
		
		valueTile("lastMonthEnergyValue", "lastMonthEnergyValue", width: 2, height: 1, decoration: "flat") {
            state "default", label: 'Last Month\n${currentValue}'
        }
		
		valueTile("lastMonthEnergyCostMonth", "lastMonthEnergyCostMonth", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'Actual\nMonth\n$${currentValue}'
        }
		
		valueTile("sinceStartEnergyValue", "sinceStartEnergyValue", width: 4, height: 2, decoration: "flat") {
            state "default", label: '${currentValue}'
        }

        standardTile("refresh", "device.switch", decoration: "flat", width: 2, height: 2) {
            state "default", label: "", action: "refresh.refresh", icon: "st.secondary.refresh"
        }

        main(["power"])
        details(["power",
                 "currentEnergyCostTxt", "currentEnergyCostHour", "currentEnergyCostWeek", "currentEnergyCostMonth", "currentEnergyCostYear",
                 "lastHourEnergyValue", "lastHourEnergyCostHour", "lastHourEnergyCostWeek", "lastHourEnergyCostMonth", "lastHourEnergyCostYear",
				 "last24HourEnergyValue", "last24HourEnergyCostDay", "last24HourEnergyCostWeek", "last24HourEnergyCostMonth", "last24HourEnergyCostYear",
                 "lastWeekEnergyValue", "lastWeekEnergyCostWeek", "lastMonthEnergyValue", "lastMonthEnergyCostMonth", "sinceStartEnergyValue", "refresh"])
    }
}

preferences {
    input title: "", description: "eGauge Energy Register v${clientVersion()}", displayDuringSetup: true, type: "paragraph", element: "paragraph"

    input title: "", description: "Logging", type: "paragraph", element: "paragraph"    
    input name: "isLogLevelDebug", type: "bool", title: "Show debug log level ?\n", defaultValue: "true", displayDuringSetup: true, required: true
}

/*******************************************************************************
 * 	CAPABILITITES                                                              *
 ******************************************************************************/

/**
 *  refresh - Refreshed values from the device
 *
 *  Required for the "Refresh" capability
 */
def refresh() {
    logInfo "refresh()"

	parent.refreshAllCounters()
}

/*******************************************************************************
 * 	Methods                                                                    *
 ******************************************************************************/

/**
 *  installed - Called when the device handling is being installed
 */
def installed() {
    logInfo "installed() called"

}

def updateCurrentPower(power) {
	logInfo "updateCurrentPower() ${power} cost per kWh: ${parent.costPerKwh}"
	
	BigDecimal costDecimal = ( parent.costPerKwh as BigDecimal )
	sendEvent(name: "currentEnergyCostHour", value: String.format("%5.2f", (new BigDecimal(power) / 1000).abs() * costDecimal))
	sendEvent(name: "currentEnergyCostWeek", value: String.format("%5.2f", (new BigDecimal(power) / 1000).abs() * 24 * 7 * costDecimal))
	sendEvent(name: "currentEnergyCostMonth", value: String.format("%5.2f", (new BigDecimal(power) / 1000).abs() * 24 * 30.42 * costDecimal))
	sendEvent(name: "currentEnergyCostYear", value: String.format("%5.2f", (new BigDecimal(power) / 1000).abs() * 24 * 365 * costDecimal))
	
	sendEvent(name: "power", value: "${power}", displayed: true)
}

def updateOneHourEnergyReport(energy) {
	logInfo "updateOneHourEnergyReport() ${energy} kWh"
	
	BigDecimal costDecimal = ( parent.costPerKwh as BigDecimal )
	sendEvent(name: "lastHourEnergyCostHour", value: String.format("%5.2f", new BigDecimal(energy).abs() * costDecimal))
	sendEvent(name: "lastHourEnergyCostWeek", value: String.format("%5.2f", new BigDecimal(energy).abs() * 24 * 7 * costDecimal))
	sendEvent(name: "lastHourEnergyCostMonth", value: String.format("%5.2f", new BigDecimal(energy).abs() * 24 * 30.42 * costDecimal))
	sendEvent(name: "lastHourEnergyCostYear", value: String.format("%5.2f", new BigDecimal(energy).abs() * 24 * 365 * costDecimal))
	
	sendEvent(name: "energy", value: "${energy} kWh", displayed: true)
}

def update24HourEnergyReport(energy) {
	logInfo "update24HourEnergyReport() ${energy} kWh"
	
	BigDecimal costDecimal = ( parent.costPerKwh as BigDecimal )
	sendEvent(name: "last24HourEnergyCostDay", value: String.format("%5.2f", new BigDecimal(energy).abs() * 24 * costDecimal))
	sendEvent(name: "last24HourEnergyCostWeek", value: String.format("%5.2f", new BigDecimal(energy).abs() * 24 * 7 * costDecimal))
	sendEvent(name: "last24HourEnergyCostMonth", value: String.format("%5.2f", new BigDecimal(energy).abs() * 24 * 30.42 * costDecimal))
	sendEvent(name: "last24HourEnergyCostYear", value: String.format("%5.2f", new BigDecimal(energy).abs() * 24 * 365 * costDecimal))
	
	sendEvent(name: "last24HourEnergyValue", value: "${new BigDecimal(energy) * 24} kW\n${energy} kWh", displayed: true)
}

def updateLastWeekEnergyReport(energy) {
	logInfo "updateLastWeekEnergyReport() ${energy} kWh"
	
	BigDecimal costDecimal = ( parent.costPerKwh as BigDecimal )
	sendEvent(name: "lastWeekEnergyCostWeek", value: String.format("%5.2f", new BigDecimal(energy).abs() * 24 * 7 * costDecimal))
	
	sendEvent(name: "lastWeekEnergyValue", value: "${new BigDecimal(energy) * 24 * 7} kW\n${energy} kWh", displayed: true)
}

def updateLastMonthEnergyReport(energy) {
	logInfo "updateLastMonthEnergyReport() ${energy} kWh"
	
	BigDecimal costDecimal = ( parent.costPerKwh as BigDecimal )
	sendEvent(name: "lastMonthEnergyCostMonth", value: String.format("%5.2f", new BigDecimal(energy).abs() * 24 * 30 * costDecimal))
	
	sendEvent(name: "lastMonthEnergyValue", value: "${new BigDecimal(energy) * 24 * 30} kW\n${energy} kWh", displayed: true)
}

def updateSinceStartEnergyReport(energyMap) {
	logInfo "updateSinceStartEnergyReport() ${energyMap} kWh"
	
	BigDecimal costDecimal = ( parent.costPerKwh as BigDecimal )
	sendEvent(name: "sinceStartEnergyValue", value: "Since: ${energyMap["sinceDate"]}\n(${energyMap["nbDays"]} days)\n ${energyMap["kW"]} kW\n${energyMap["kWh"]} kWh", displayed: true)
}

/*******************************************************************************
 * 	Utilities Methods                                                          *
 ******************************************************************************/

void logDebug(str) {
    if (isLogLevelDebug) {
        log.debug str
    }
}

void logInfo(str) {
    log.info str
}

void logWarn(str) {
    log.warn str
}

void logError(str) {
    log.error str
}

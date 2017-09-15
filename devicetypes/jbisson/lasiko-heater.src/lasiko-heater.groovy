/**
 *  Logitech Harmony Activity
 *
 *  Copyright 2015 Juan Risso
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
 *  modified by: jbisson
 *
 *
 *  Revision History
 *  ==============================================
 *  2017-02-08 Version 1.0.0 Initial check in
 *
 *
 *
 *
 */
 
  def clientVersion() {
    return "1.0.0"
}

metadata {
        definition (name: "Lasiko Heater", namespace: "jbisson", author: "Juan Risso (Modified by jbisson)") {
        capability "Switch"
        capability "SwitchLevel"
        capability "Actuator"
		capability "Refresh"

		command "realOn"
		command "realOff"
        command "huboff"
        command "alloff"
        command "refresh"
		command "flipLogicalState"
	}

	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles {
		standardTile("realState", "device.level", width: 2, height: 2, canChangeIcon: true) {
			state "0", label: 'Off', action: "realOn", icon: "st.harmony.harmony-hub-icon", backgroundColor: "#ffffff", nextState: "1"
			state "1", label: 'On', action: "realOff", icon: "st.harmony.harmony-hub-icon", backgroundColor: "#79b821", nextState: "0"
		}
		
		standardTile("button", "device.switch", width: 1, height: 1, canChangeIcon: true) {
			state "off", label: 'Activity Off', action: "switch.on", icon: "st.harmony.harmony-hub-icon", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'Activity On', action: "switch.off", icon: "st.harmony.harmony-hub-icon", backgroundColor: "#79b821", nextState: "off"
		}
		standardTile("flipLogicalState", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'flipLogical', action:"flipLogicalState"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		standardTile("forceoff", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'Force End', action:"switch.off", icon:"st.secondary.off"
		}
		standardTile("huboff", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'End Hub Action', action:"huboff", icon:"st.harmony.harmony-hub-icon"
		}
		standardTile("alloff", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'All Actions', action:"alloff", icon:"st.secondary.off"
		}
		main "realState"
		details(["realState", "button", "flipLogicalState", "refresh", "forceoff", "huboff", "alloff"])
	}
}

preferences {
	input title: "Flic Button", description: "v${clientVersion()}", displayDuringSetup: true, type: "paragraph", element: "paragraph"
}

/*******************************************************************************
*	Methods                                                                    *
*******************************************************************************/

def parse(String description) {
}

def realOn() {
	log.trace 'realOn'
    
    if (device.currentValue("level") != 1) {
    	sendEvent(name: "level", value: "1")
    
		flipState()
    } else {
    	log.trace 'Device is already on'
    }
}


def realOff() {
	log.trace 'realOff'
    
    if (device.currentValue("level") != 0) {
    	sendEvent(name: "level", value: "0")
    
    	flipState()
    }	else {
    	log.trace 'Device is already off'
    }
}

def flipLogicalState() {
	if (device.currentValue("level") == 1) {
		log.trace 'flipping logical state to off'

		sendEvent(name: "level", value: "0")

	} else {
		log.trace 'flipping logical state to on'
		sendEvent(name: "level", value: "1")
	}
}
def flipState() {
    if (device.currentValue("switch") == "on") {
    	log.trace 'activity is on' 
        
        sendEvent(name: "switch", value: "off")
        log.trace parent.activity('harmony-9879117-24801557',"start")
        
    } else {
    	log.trace 'activity is off'
        sendEvent(name: "switch", value: "on")
        log.trace parent.activity(device.deviceNetworkId,"start")
    }
}


def on() {
	log.trace 'on()'
	sendEvent(name: "switch", value: "on")
    log.trace parent.activity(device.deviceNetworkId,"start")
}

def off() {
	log.trace 'off()'
	sendEvent(name: "switch", value: "off")
    log.trace parent.activity(device.deviceNetworkId,"end")
}

def huboff() {
	//sendEvent(name: "switch", value: "off")
    log.trace parent.activity(device.deviceNetworkId,"hub")
}

def alloff() {
	sendEvent(name: "switch", value: "off")
    log.trace parent.activity("all","end")
}


def refresh() {
	log.debug "Executing 'refresh'"
	log.trace parent.poll()
}

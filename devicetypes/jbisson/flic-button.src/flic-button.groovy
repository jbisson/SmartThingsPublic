/**
 *
 *  Device handler that tries to simulate an hardware flic button.
 * 
 * This device handler works really well with the expose-rest api app. https://github.com/jbisson/SmartThingsPublic/blob/master/ExposeRestApi.md
 * Combined the two together, it allows you to receive click, doubleClick and hold events from a real flic button (hardware).
 * 
 * Once the event is inside the smartthing, you could use CoRE (Community’s own Rule Engine) for defining your own rule. (ie: When I click on my flicButton do this, ect)
 * https://community.smartthings.com/t/release-candidate-core-communitys-own-rule-engine/57972
 * 
 *
 *
 *  Revision History
 *  ==============================================
 *  2016-11-13 Version 1.0.0  Initial commit
 *
 */
 
def clientVersion() {
    return "1.0.0"
}

metadata {
	definition (name: "Flic Button", namespace: "jbisson", author: "Jonathan Bisson") {	
		capability "Button"
        capability "Refresh"
        
		command "click"
		command "doubleClick"
		command "hold"
        
        command "clearSingleClickStatus"
        command "clearDoubleClickStatus"
        command "clearHoldStatus"        
        
        attribute "flicColor", "String"
        attribute "buttonNumber", "String"
	}

	tiles(scale: 2) {
        multiAttributeTile(name:"button", type:"generic", width:6, height:4) {
            tileAttribute("flicColor", key: "PRIMARY_CONTROL") {
                attributeState "white", label:'', backgroundColor: "#FFFFFF"
                attributeState "black", label:'', backgroundColor: "#000000"
                attributeState "green", label:'', backgroundColor: "#89BF47"
                attributeState "yellow", label:'', backgroundColor: "#DDD685"
                attributeState "turquise", label:'', backgroundColor: "#46A09E"
            }
            
            tileAttribute("device.status", key: "SECONDARY_CONTROL") {
                attributeState "default", label:'${currentValue}'
            }
    	}
        
         standardTile("icon", "icon") {         
			state "white", label:'', backgroundColor: "#FFFFFF", action: "dummy", icon: "https://lh5.googleusercontent.com/X3WrRB4T4UtbvYiut5T-s0Z8jRFai5g_UZFFtSoF9za_-utSGZs6FgDieZuOznb3kk4_u2L1lTqfLpQ=w1920-h911"
            state "black", label:'', backgroundColor: "#000000", action: "dummy", icon: "https://lh6.googleusercontent.com/srppoMNIi-i3-huaj4IpPcy4GR8fWAYK_EIzVpci1cJWqZyMJW45Y8jIPzLpZBemdzLD2XX3rM4VfhU=w1920-h911"
            state "green", label:'', backgroundColor: "#89BF47", action: "dummy", icon: "https://lh6.googleusercontent.com/nlLXiGuVwBOptoJgGVy7FMmKkn_SSi-qkx2_klKrZfasPM-POwWpByNca1e99biqr2JwCIha0PZj41E=w1920-h911"
            state "yellow", label:'', backgroundColor: "#DDD685", action: "dummy", icon: "https://lh6.googleusercontent.com/T9u-H9NAU37K15CNFXDjcK6D6tToMwY6bvmn7t5W1UrloOj0bEUOBYxeQSI4-mE6Pa1fCJ_IrYhyWbg=w1920-h911"
            state "turquise", label:'', backgroundColor: "#46A09E", action: "dummy", icon: "https://lh5.googleusercontent.com/-NfOKsX5N3lokv6mGubkEqR20XxyfcS-bjDmk2BzX6qNmh4fL5eGoTRnDBRC79boXb5yBjZhnJOH6fQ=w1920-h911"
         }
        
        standardTile("singleClick", "singleClick", width: 2, height: 2) {
            state "default", label:"", action:"click",  icon:"https://lh3.googleusercontent.com/YXvvcp3IsC6161nSnUNP4QERJ77E5UuvvIS_KaHGAjpqDqCWI7L1Wk1lVofimaZ6xqA0vHfx=w1920-h985", nextState:"withBackground"
            state "withBackground", label:"", icon:"https://lh3.googleusercontent.com/YXvvcp3IsC6161nSnUNP4QERJ77E5UuvvIS_KaHGAjpqDqCWI7L1Wk1lVofimaZ6xqA0vHfx=w1920-h985", backgroundColor: "#FF0000", nextState:"default"
        }
        
        standardTile("doubleClick", "doubleClick", width: 2, height: 2) {
            state "default", label:"", action:"doubleClick",  icon:"https://lh6.googleusercontent.com/ah7kRsgb-61Dg8rjrcFWOBTsnuZHDWWYem6nPw36JqjJssj4Jo7wdFVZy8VNs92pwSORvxu5=w1920-h985", nextState:"withBackground"
            state "withBackground", label:"", icon:"https://lh6.googleusercontent.com/ah7kRsgb-61Dg8rjrcFWOBTsnuZHDWWYem6nPw36JqjJssj4Jo7wdFVZy8VNs92pwSORvxu5=w1920-h985",  backgroundColor: "#FF0000", nextState:"default"
        }
        
        standardTile("hold", "hold", width: 2, height: 2) {
            state "default", label:"", action:"hold",  icon:"https://lh5.googleusercontent.com/_0aiNWLsAyf3diTowSFOAStEdk__AQ0WNJfXxfFDT4VSOZ4g3hcJ39Mda-5c_c5_efOE3pt8=w1920-h985", nextState:"withBackground"
            state "withBackground", label:"", icon:"https://lh5.googleusercontent.com/_0aiNWLsAyf3diTowSFOAStEdk__AQ0WNJfXxfFDT4VSOZ4g3hcJ39Mda-5c_c5_efOE3pt8=w1920-h985",  backgroundColor: "#FF0000", nextState:"default"                        
        }
        
        standardTile("singleClickStatus", "singleClickStatus", decoration: "flat", width: 2, height: 1) {
            state "default", label:'Last click event \n ${currentValue}', action:"clearSingleClickStatus"
        }
        
        standardTile("doubleClickStatus", "doubleClickStatus", decoration: "flat", width: 2, height: 1) {
            state "default", label:'Last double click event \n ${currentValue}', action:"clearDoubleClickStatus"
        }
        
        valueTile("holdStatus", "holdStatus", decoration: "flat", inactiveLabel: false, width: 2, height: 1) {
            state "default", label:'Last hold event\n ${currentValue}', action:"clearHoldStatus"
        }
        
		main(["icon"])
		details(["button", "singleClick", "doubleClick", "hold", "singleClickStatus", "doubleClickStatus", "holdStatus"])
	}
}   

preferences {
	input title: "Flic Button", description: "v${clientVersion()}", displayDuringSetup: true, type: "paragraph", element: "paragraph"
    input name: "isLogLevelTrace", type: "bool", title: "Show trace log level ?\n", defaultValue: "false"
    input name: "isLogLevelDebug", type: "bool", title: "Show debug log level ?\n", defaultValue: "true"
	
    input name: "colorEnum", type: "enum", title: "Color of the flic button\n", options: ["black", "white", "turquise", "green", "yellow"], required: true
    input name: "buttonNumberPref", type: "decimal", title: "Identitfy the button number (if you have more than one with the same color) to differentiate them.\n", defaultValue: "0"
}

/*******************************************************************************
*	Methods                                                                    *
*******************************************************************************/

/**
 *  updated - Called when the preferences of the device type are changed
 */
def updated() {
	logDebug "updated() $colorEnum"    
    sendEvent(name: "flicColor", value: "$colorEnum", isStateChange: true)
    sendEvent(name: "icon", value: "$colorEnum", displayed: false, isStateChange: true)    
    sendEvent(name: "buttonNumber", value: "$buttonNumberPref", isStateChange: true)
    
    sendEvent(name: "status", value: "---", displayed: false)
    sendEvent(name: "singleClickStatus", displayed: false, value: "")
    sendEvent(name: "doubleClickStatus", displayed: false, value: "")
    sendEvent(name: "holdStatus", displayed: false, value: "")
}

def click() {
	logDebug "click() $colorEnum"	
    
    def currentDateTime = new Date().format('yyyy-M-d hh:mm:ss', location.timeZone)
    sendEvent(name: "button", value: "clicked", data: [buttonNumber: 1], descriptionText: "$device.displayName was clicked", isStateChange: true)        
    sendEvent(name: "status", value: "Last event: 'click' at ${currentDateTime}", displayed: false)
    sendEvent(name: "singleClickStatus", value: currentDateTime, displayed: false)
    
    runIn(1, clearBackground)
}

def doubleClick() {
	logDebug "doubleClick()"
    
	def currentDateTime = new Date().format('yyyy-M-d hh:mm:ss', location.timeZone)	
    sendEvent(name: "button", value: "doubleClicked", data: [buttonNumber: 1], descriptionText: "$device.displayName was double clicked", isStateChange: true)    
    sendEvent(name: "status", value: "Last event: 'double click' at ${currentDateTime}", displayed: false)
    sendEvent(name: "doubleClickStatus", value: currentDateTime, displayed: false)
    
    runIn(1, clearBackgroundAndReflash)
}

def hold() {
	logDebug "hold()"
    
    def currentDateTime = new Date().format('yyyy-M-d hh:mm:ss', location.timeZone)
    sendEvent(name: "button", value: "held", data: [buttonNumber: 1], descriptionText: "$device.displayName was held", isStateChange: true)    
    sendEvent(name: "status", value: "Last event 'hold' at ${currentDateTime}", displayed: false)
    sendEvent(name: "holdStatus", value: currentDateTime, displayed: false)    
    
     runIn(4, clearBackground)
}

def clearBackgroundAndReflash() {
	clearBackground()
    
    runIn(1, flashDoubleClick)
}

def flashDoubleClick() {
	sendEvent(name: "doubleClick", value: "withBackground", displayed: false, isStateChange: true)
	runIn(1, clearBackground)
}

def clearBackground() {
	logTrace "clearBackground()"
    
    sendEvent(name: "singleClick", value: "default", displayed: false, isStateChange: true)
    sendEvent(name: "doubleClick", value: "default", displayed: false, isStateChange: true)
    sendEvent(name: "hold", value: "default", displayed: false, isStateChange: true)
}

def clearSingleClickStatus() {
	sendEvent(name: "singleClickStatus", displayed: false, value: "", isStateChange: true)
}

def clearDoubleClickStatus() {
	sendEvent(name: "doubleClickStatus", displayed: false, value: "", isStateChange: true)
}

def clearHoldStatus() {
	sendEvent(name: "holdStatus", displayed: false, value: "", isStateChange: true)
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
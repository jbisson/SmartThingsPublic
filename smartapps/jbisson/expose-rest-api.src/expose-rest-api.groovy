/**
 *
 *  Expose REST API App allows you to expose a public end-point URL, allowing you to trigger an event within the Smartthing ecosystem.
 *  It support any device handler that has notification or button capability. 
 * 
 *  For example, let's say you want to "catch" an event for a device that is not yet integrated with smartthing but has the ability to do an http request, you could expose an end-point with this
 *  app, send the request from your device and this app will propragate the event in the smartthing system. 
 *  
 *  Flic Button support: This app has been optimized to work best with flic button. You can see a step by step tutorial on how to set it up here: https://community.smartthings.com/t/flic-button/62951
 * 
 * 
 *  You will need to enable oAuth support for this app to work. A step by step instruction to do so can be found here:
 *  https://community.smartthings.com/t/flic-button/62951
 *
 *  Button support (works well with flic button handler): 
 *  Usage (REST API on how to invoked it):
 *     https://graph.api.smartthings.com/api/smartapps/installations/<installationId>/button/<btnColor>/<btnNumber>/<action>?access_token=<your_access_token>
 *     <installationId>: InstallationId of your smartApp (use https://graph.api.smartthings.com/api/smartapps/endpoints?access_token=bcea5b37-1868-4418-a131-3c82a2e9eb90 to find it out)
 *     <your_access_token> access token used for authentication
 *     <btnColor>  : Color of your button (if using the flic handler integration options are: black, white, turquise, green, yellow)
 *     <btnNumber> : Button number identification used if you have more than one button with the same color 
 *     <action>    : Action name - command name - that will be involked. (if using the flic handler integration options are: click, doubleClick, hold)
 * 
 * Examples: 
 *  https://graph.api.smartthings.com/api/smartapps/installations/bbb9dc65-7002-4d2a-9eba-d2d301320639/button/turquise/0/click?access_token=54cc5a76-53ac-5497-96ff-4846fbc02a11
 *  https://graph.api.smartthings.com/api/smartapps/installations/bbb9dc65-7002-4d2a-9eba-d2d301320639/button/black/0/doubleClick?access_token=54cc5a76-53ac-5497-96ff-4846fbc02a11
 * 
 * Notification support
 * Usage (REST API on how to invoked it):
 *     https://graph.api.smartthings.com/api/smartapps/installations/<smartAppId>/sendNotification?access_token=<your_access_token>
 *     payload: msg=<message_you_wish_to_send>
 *
 *
 *  Copyright 2016 jbisson
 *
 *
 *  Revision History
 *  ==============================================
 *  2016-11-22 Version 1.0.1  No functional code change - documentation change.
 *  2016-08-21 Version 1.0.0  Initial commit
 *
 */

def version() {
    return "1.0.1 - 2016-11-22"
}

definition(
    name: "Expose REST API",
    namespace: "jbisson",
    author: "Jonathan Bisson",
    description: "Expose REST API App allows you to trigger events to a button as well as the ability to send notification directly from a REST API.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true) {
    appSetting "oAuthClientId"
}

preferences {
	section("About") {
    	input title: "Expose REST API Version", description: "v${version()}", displayDuringSetup: true, type: "paragraph", element: "paragraph", required: false
	}
    section("Logging") {
        input name: "isLogLevelTrace", type: "bool", title: "Show trace log level ?\n", defaultValue: "false"
    	input name: "isLogLevelDebug", type: "bool", title: "Show debug log level ?\n", defaultValue: "true"
	}
	section("Allow external service to control these things...") {
		 input "notification", "capability.notification", required: false, multiple: true, title: "Notifications..."
         input "button", "capability.button", required: false, multiple: true, title: "Buttons..."
	}
    section("Other") {
    }
}

mappings {
  path("/notification") {
    action: [
      POST: "sendNotification"
    ]
  }
  path("/button/:btnColor/:btnNumber/:action") {
    action: [
      PUT: "updateButton"
    ]
  }
}

/*******************************************************************************
*	Methods                                                                    *
*******************************************************************************/

void updateButton() {
	logDebug "Received updateButtons() event..."
    logTrace "Params $params"
    
    def btnColor = params.btnColor
    def btnNumber = params.btnNumber
    def action = params.action
    
    def found = false
    button.each {
        logDebug "Current value: " + it.currentValue("flicColor")
        
        if (it.currentValue("flicColor") == btnColor && it.currentValue("buttonNumber") == btnNumber) {
        	found = true
            if (action == "click") {        
                it.click()        
            } else if (action == "doubleClick") {
                it.doubleClick()
            } else if (action == "hold") {
                it.hold()
            } else {
            	log.error "Action $action is not valid, ignoring..."
                httpError(400, "Action $action is not valid, ignoring...")
            }
        }
    }
    
    if (!found) {
    	log.error "Couln't find a flic button with color=$btnColor and buttonNumber=$btnNumber"
        httpError(400, "Couln't find a flic button with color=$btnColor and buttonNumber=$btnNumber")
    }
}

def eventHandler(evt) {
    log.debug "event created at: ${evt.date}"
}

def sendNotification() {
	log.debug "received: $params.msg"
	    
	if (notification) {
    	notification.deviceNotification(params.msg)    
	} else {
    	log.error "no device to send it to."
    }
}

def installed() {
	logDebug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	logDebug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {

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
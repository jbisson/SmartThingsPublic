/**
 *
 *  Device handler used for the flic button
 * 
 *  Copyright 2016 jbisson
 *
 *
 *  Revision History
 *  ==============================================
 *  2016-08-21 Version 1.0.0  Initial commit
 *
 */
 
def clientVersion() {
    return "1.0.0"
}

metadata {
	definition (name: "Button Capability", namespace: "capabilities", author: "SmartThings") {
		capability "Button"
	}

	tiles {
		standardTile("button", "device.button", width: 2, height: 2) {
			state "default", label: "", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
		}
		main "button"
		details "button"
	}
}



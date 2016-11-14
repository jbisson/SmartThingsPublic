## Expose Rest Api

Expose REST API App allows you to expose a public end-point URL, allowing you to trigger an event within the Smartthing ecosystem.
   It support any device handler that has notification or button capability. 
  
   For example, let's say you want to "catch" an event for a device that is not yet integrated with smartthing but has the ability to do an http request, you could expose an end-point with this
   app, send the request from your device and this app will propragate the event in the smartthing system. 
   
   Flic Button support: This app has been optimized to work best with flic button. You can see a step by step tutorial on how to set it up here: https://community.smartthings.com/t/flic-button/62951
  
  
   You will need to enable oAuth support for this app to work. 
   Once installed, go to https://graph.api.smartthings.com/ide/apps and click edit properties. In this screen
   - Creating a REST SmartApp Endpoint https://community.smartthings.com/t/tutorial-creating-a-rest-smartapp-endpoint/4331
 
   Button support (works well with flic button handler): 
   Usage (REST API on how to invoked it):
   ```
	https://graph.api.smartthings.com/api/smartapps/installations/<smartAppId>/button/<btnColor>/<btnNumber>/<action>?access_token=<your_access_token>  
      <smartAppId>: App id of your smart app
      <your_access_token> access token used for authentication
      <btnColor>  : Color of your button (if using the flic handler integration options are: black, white, turquise, green, yellow)
      <btnNumber> : Button number identification used if you have more than one button with the same color 
      <action>    : Action name - command name - that will be involked. (if using the flic handler integration options are: click, doubleClick, hold)
	```	
  
  Examples: 
   https://graph.api.smartthings.com/api/smartapps/installations/bbb9dc65-7002-4d2a-9eba-d2d301320639/button/turquise/0/click?access_token=54cc5a76-53ac-5497-96ff-4846fbc02a11
   https://graph.api.smartthings.com/api/smartapps/installations/bbb9dc65-7002-4d2a-9eba-d2d301320639/button/black/0/doubleClick?access_token=54cc5a76-53ac-5497-96ff-4846fbc02a11
  
  Notification support
  Usage (REST API on how to invoked it):
      `https://graph.api.smartthings.com/api/smartapps/installations/<smartAppId>/sendNotification?access_token=<your_access_token>`
      payload: msg=<message_you_wish_to_send>
 
## How to install
Get the code here: https://github.com/jbisson/SmartThingsPublic/blob/master/smartapps/jbisson/expose-rest-api.src/expose-rest-api.groovy

Follow this for more information https://community.smartthings.com/t/faq-an-overview-of-using-custom-code-in-smartthings/16772

## Support
https://community.smartthings.com/t/flic-button/62951

---

*If you like any of these projects and wish to support me, please consider supporting their further
development by making a donation via PayPal.*

[![PayPal](https://www.paypalobjects.com/en_US/i/btn/btn_donate_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=LNDQQW7HQPN98)

---


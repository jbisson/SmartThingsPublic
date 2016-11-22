## Flic button

Device handler that tries to simulate an hardware flic button. Here's what it can do:

- Click, doubleClick and hold event
- For each click, doubleClick and hold event, record the last time the action was perforrmed
- Choose between black, white, turquise, green and yellow color
- Support multiple buttons of the same color to differenciate them
 
 This device handler works really well with the expose-rest api app. https://github.com/jbisson/SmartThingsPublic/blob/master/ExposeRestApi.md
 Combined the two together, it allows you to receive click, doubleClick and hold events from a real flic button (hardware).  
 
 Once the event is inside the smartthing, you could use CoRE (Community’s own Rule Engine) for defining your own rule. (ie: When I click on my flicButton do this, ect)
 https://community.smartthings.com/t/release-candidate-core-communitys-own-rule-engine/57972
 
 The mapping between action and button numbers are as follow, this will be very usefl if using CoRE smartApps:  
     Click event:         Button#1 -> pushed  
     Double Click event:  Button#2 -> pushed  
     Hold event           Button#3 -> pushed  
 
 
## How to install
Get the code here: https://github.com/jbisson/SmartThingsPublic/blob/master/devicetypes/jbisson/flic-button.src/flic-button.groovy

Follow this for more information https://community.smartthings.com/t/faq-an-overview-of-using-custom-code-in-smartthings/16772

## Support
https://community.smartthings.com/t/flic-button/62951

---

*If you like any of these projects and wish to support me, please consider supporting their further
development by making a donation via PayPal.*

[![PayPal](https://www.paypalobjects.com/en_US/i/btn/btn_donate_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=LNDQQW7HQPN98)

---


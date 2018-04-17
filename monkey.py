# Imports the monkeyrunner modules used by this program
from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice

# Connects to the current device, returning a MonkeyDevice object
device = MonkeyRunner.waitForConnection()

# Installs the Android package. Notice that this method returns a boolean, so you can test
# to see if the installation worked.
device.installPackage('C:\\Users\\CianFDoherty\\AndroidStudioProjects\\GoogleMapsAPIDemo\\app\\build\\outputs\\apk\debug\\app-debug.apk')

# sets a variable with the package's internal name
package = 'com.example.cianfdoherty.googlemapsapidemo'

# sets a variable with the name of an Activity in the package
activity = 'com.example.cianfdoherty.googlemapsapidemo.LoginActivity'

# sets the name of the component to start
runComponent = package + '/' + activity

# Runs the component
device.startActivity(component=runComponent)

# Sleep here for correct screenshot
MonkeyRunner.Sleep(5);


# Presses the Menu button
device.press('KEYCODE_MENU', MonkeyDevice.DOWN_AND_UP)

# Takes a screenshot
result = device.takeSnapshot()

# Writes the screenshot to a file
result.writeToFile('C:\\Users\\CianFDoherty\\Desktop\\Uni\\FYP\\Auto_test\\shot1.png','png')
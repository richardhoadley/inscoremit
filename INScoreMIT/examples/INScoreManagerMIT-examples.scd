
// INScoreManagerMIT examples

// manage INScore on local or remote computers on local network

// Example using local computer (this computer)
// port is default INScore recieving
~netAddr = NetAddr.new("localhost", 7000);

// OR
// Example using remote computer on local network
~netAddr = NetAddr.new("192.168.178.xx", 7000);

// First ensure INScore is open (for local machines)
"Applications/INScoreViewer.app".openOS

// make INScore manager
// wait as needed
~helper = INScoreManagerMIT.new(~netAddr);

// learn your actual IP address for _local_ machine
// nil for remote machines
// unless you've provided as myIP arg to class instance
~helper.myIP // see Post Window

~helper.hostIP // will match actual ~netAddr (same as myIp on local machine)

// version of INScore
~helper.version

// where is INScore?
~helper.locale // local or remote

~helper.latency // can be shorter on remote machines

~helper.showLog // INScore log window

~helper.clearLog

// confirm you're running minimum INScore version
~helper.require(1.34); // OK

~helper.require(3.33); // deliberate error

~helper.stats

// Scene management

~helper.allScenes // by name

~helper.numScenes

// count items within a scene
~helper.sceneCount(\scene)

// make a scene
// will confirm once scene has been made
// foregrounds by default
~helper.makeScene(\erik, foreground: true);


// delete default scene
//(this will close INScore if only scene open)
~helper.deleteScene(\scene);

// INScoreManagerMIT creates OSCdefs to listen for INSCore msgs
// see all of them
INScoreManagerMIT.all // how many listeners?

// in some circumstances listeners can accumulate
// if makeScene is used and scene already exists
// or deleteScene is called and scene does not exist
// in this case, or to 'start over'
// free INScore listeners for current class instance
~helper.free

INScoreManagerMIT.all // how many listeners?

// deliberate error: do not assign class instance var
INScoreManagerMIT.new;

INScoreManagerMIT.all // how many listeners?

// examine all SuerCollider's active OSCdefs
OSCdef.all;

// INScore OSCdefs can be cleared for all class instances
INScoreManagerMIT.freeAll // delete OSCdefs

// reassign as needed
~myIP = NetAddr.new("localhost", 7000);
~helper = INScoreManagerMIT.new(~myIP);

// quit INScore
~helper.quitINScore // also frees listeners as required

// if INScore is not open
// INScoreManagerMIT still will keep listening
i = INScoreManagerMIT.new;

// in which case:
i.quit




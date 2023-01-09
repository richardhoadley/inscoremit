

// simplest example using local machine only.

~scene = INScoreSceneMIT.new; // defaults to scene \scene

// is it a miniView?
~scene.miniView // not yet

// test fullscreen
(
r{
	~scene.osaActivate;
	~scene.fullScreen;
	3.wait;
	~scene.endFullScreen;
}.play
)

// if the default scene is a miniView
// it is not fullscreened
// and any resizing of the window is halved

~scene.windowSize;

~scene.windowSize_(1.1, 1.1);

~scene.miniView_(true)

~scene.miniView

~scene.windowSize;

~scene.windowSize_(1.0, 1.0);

~scene.windowSize; // still shown as 1.0

// default scenes if miniView will not fullScreen
(
r{
	~scene.osaActivate;
	~scene.fullScreen;
	3.wait;
	~scene.endFullScreen;
}.play
)


/////////////////////////////////////////
// Example using remote and local machine to display same scene
// local machine is miniView

~scene = INScoreSceneMIT.new; // default scene is \scene

// create connection with remote
~remIP = "192.168.178.64" // change as needed
~remAddr = NetAddr.new(~remIP, 7000);

~scene.addINScoreView(~remAddr, \remote)

// NB local view of scene is named \default
// remote here is named \remote
// these are class instances of INScoreViewMIT
~scene.connections

~scene.osaActivate; // wake up local view as needed (Mac only)
~scene.note(\a) // should display on local and remote machine

~scene.miniView_(true);

~scene.windowSize;

// default view will be size 0.75
~scene.windowSize_(1.5, 1.5);

~scene.showMouse // true by default

// hide mouse for fullScreening on remote machine
~scene.showMouse_(false);

// only non-default views will fullscreen
(
r{
	~scene.osaActivate;
	~scene.note("b a c");
	~scene.fullScreen;
	4.wait;
	~scene.endFullScreen;
}.play
)

/*
INScoreSceneMIT
a convenience class to link the INScore augmented score software with the SuperCollider Audio Programming Environment.

Dependency: INScoreViewMIT

INScore: https://inscore.grame.fr
SuperCollider: https://supercollider.github.io
Richard Hoadley: http://rhoadley.net

Version compiled for The SuperCollider Book, 2nd Edn, MIT Press

NB This version of the class accommodates only a small subsets of the OSC commands already present in INScore. The class therefore provides a template for others to create their own commands/implementations,

https://inscoredoc.grame.fr/refs/1-introduction/

*/


INScoreSceneMIT {
	classvar <>showMouse = true;
	var <scene, <netAddr, <baseString="/ITL/", <connections, <guido;
	var <default, <windowHeight = 1.0, <windowWidth = 1.0, <windowXPos=0;
	var <absoluteXY=0, <windowYPos=0, <frameless =0;

	*new { | addr= "localhost", scene=\scene, port=7000|
		^super.new.init(addr, scene, port);
	}

	// SETTING UP

	init {arg argAddr, argScene, argPort;
		scene = argScene;
		netAddr = NetAddr.new(argAddr, argPort);
		connections = Dictionary.new;
		this.addINScoreView(netAddr, \default);
		default = connections[\default];
		this.addGuido;
	}

	addINScoreView {|netAddr, name, verbose=true, miniView= false|
		if(name.isNil){
			name = this.makeNetAddrName(netAddr)
		};
		// arg model, netAddr, verbose = true, miniView = false;
		connections.add(name -> INScoreViewMIT.new(this, netAddr, verbose, miniView));
		format("INScoreView created connection %", name.asCompileString).postln;
		^connections[name];
	}

	makeNetAddrName {|netAddr|
		^netAddr.ip.replace(".").asSymbol
	}

	addGuido {
		var guidoCheck = Class.allClasses.collect({|i|
			(i.name.asString).compare("SimpleGuidoMIT")
		}).includes(0);
		if(guidoCheck){
			guido = SimpleGuidoMIT.new;
		}{
			"SimpleGuidoMIT class is not installed, INScoreMIT guido var is nil".warn;
		};
		^this
	}

	addFwdSlash {|symbol|
		symbol = symbol.asString;
		if(symbol[0] != $/){symbol = "/" ++ symbol};
		^symbol;
	}

	makeOSCPath {|component, componentNum|
		component = this.addFwdSlash(component);
		^baseString ++ scene ++ component ++ componentNum.asString;
	}

	// OPTIONS

	verbose {
		^default.verbose
	}

	verbose_ {|bool|
		^default.verbose_(bool)
	}

	miniView {
		^default.miniView
	}

	miniView_ {|bool|
		^default.miniView_(bool)
	}

	showMouse {^INScoreSceneMIT.showMouse}

	showMouse_{|bool = true|
		var showStatus= if(bool){\show}{\hide};
		INScoreSceneMIT.showMouse = bool;
		this.changed(\msg, ["/ITL", \mouse, showStatus]);
		^this
	}

	//////////////////////////////////////////////////////////////////////
	// UTILITIES

	osaActivate {
		if(thisProcess.platform.name == \osx){
			"osascript -e 'tell application \"INScoreViewer\"' -e 'activate' -e 'end tell'".unixCmd;
		}{
			"Open Script Architecture (OSA) only applies to Apple macOS".postln;
		};
		^this
	}

	//////////////////////////////////////////////////////////////////////
	// WINDOW CONTROL

	absoluteXY_ {|int|
		var msg = baseString ++ scene;
		absoluteXY = int;
		this.changed(\msg, [msg, \absolutexy, int]);
		^this
	}

	window { arg width, height, x, y;
		var msg = baseString ++ scene;
		this.changed(\msg, [msg, \new]);
		if(width.notNil){this.windowWidth_(width)};
		if(height.notNil){this.windowHeight_(height)};
		if(x.notNil){this.windowXPos_(x)};
		if(y.notNil){this.windowYPos_(y)};
		^this
	}

	makeWin { arg width, height, x, y;
		^this.window(width, height, x, y);
	}

	// display scene window in foreground of all other windows in the system windows manager
	foreground {
		var msg = baseString ++ scene;
		this.changed(\msg, [msg, \foreground])
		^this
	}

	delete {
		var msg = baseString ++ scene;
		this.changed(\msg, [msg, \del]);
		^this
	}

	close {
		this.delete;
		^this
	}

	windowDelete {
		this.delete;
		^this
	}

	frameless_ { arg bool=false;
		// if it's 0 it's not frameless, if it's non-zero it is
		var msg = baseString ++ scene;
		frameless = bool;
		this.changed(\msg, [msg, \frameless, frameless.asInteger])
		^this
	}

	fullScreen { arg bool=true;
		var msg = baseString ++ scene;
		// if it's 0 it's not fullscreen, if it's non-zero it is
		this.changed(\msg, [msg, \fullscreen, bool.asInteger]);
		^this
	}

	endFullScreen {
		^this.fullScreen(false);
	}

	windowColor { arg color=[0, 0, 0, 255];
		var msg = baseString ++ scene;
		// allow for named colors or hex (as symbol)
		// as per https://www.w3schools.com/colors/colors_groups.asp
		if(color.isArray.not){color = color.bubble};
		this.changed(\msg, [msg, \color] ++ color);
		^this
	}

	// switch the scene window to opaque or transparent mode.
	// When in transparent mode, the scene alpha channel controls
	// the window opacity (from completely opaque to completely transparent).
	// In opaque mode, the scene alpha channel controls the background brush only.
	// Default value 0 is transparent.
	windowOpacity { arg opacity=0;
		var msg = baseString ++ scene;
		this.changed(\msg, [msg, \windowOpacity, opacity]);
		^this
	}

	windowWidth_ { arg width = 1.0;
		var msg = baseString ++ scene;
		windowWidth = width;
		this.changed(\msg, [msg, \width, width]);
		^this
	}

	windowHeight_ { arg height= 1.0;
		var msg = baseString ++ scene;
		windowHeight = height;
		this.changed(\msg, [msg, \height, height]);
		^this
	}

	windowSize {
		^[windowWidth, windowHeight]
	}

	windowSize_ {arg width=1.0, height=1.0;
		this.windowWidth_(width);
		this.windowHeight_(height);
		^this
	}

	windowXPos_ { arg x=0;
		var msg = baseString ++ scene;
		windowXPos = x;
		this.changed(\winPos, [msg, \x, x]);
		^this
	}

	windowYPos_ { arg y=0;
		var msg = baseString ++ scene;
		windowYPos = y;
		this.changed(\winPos, [msg, \y, y]);
		^this
	}

	windowXYPos_ { arg x=0, y=0;
		var msg = baseString ++ scene;
		this.windowXPos_(x);
		this.windowYPos_(y);
		^this
	}

	windowXYPos {
		^[windowXPos, windowYPos]
	}

	windowPos {
		^this.windowXYPos
	}

	windowPos_ { arg x=0, y=0;
		this.windowXYPos(x, y);
		^this
	}

	//////////////////////////////////////////////////////////////////////
	// SCENE / COMPONENT CONTROL

	clear {
		var msg = baseString ++ scene ++ "/*";
		this.changed(\msg, [msg, \del]);
		^this
	}

	clearAll {
		^this.clear(scene)
	}

	// delete individual components
	deleteComponent {arg component="/score", componentNum=0;
		var msg;
		msg = this.makeOSCPath(component, componentNum);
		this.changed(\msg, [msg, \del]);
		^this
	}

	// clears the scene (i.e. delete all components) and resets the scene
	// to its default state (position, size and color).
	reset {
		var msg = baseString ++ scene;
		this.changed(\msg, [msg, \reset]);
		^this
	}

	//////////////////////////////////////////////////////////////////////
	// GUIDO

	// accepted by the components types gmn | gmnstream | gmnf
	pageFormat {arg widthVal=1.0, heightVal=1.0, componentNum=0;
		var msg, component = "/gmnf";
		msg = this.makeOSCPath(component, componentNum);
		this.changed(\msg, [msg, \pageFormat, widthVal, heightVal ]);
		^this
	}

	gmn {arg guido="[a]", componentNum=0;
		var msg, component = "/score";
		msg = this.makeOSCPath(component, componentNum);
		guido = guido.asString;
		this.changed(\msg, [msg, \set, \gmn, guido ]);
		^this
	}

	note {arg pitch="a", componentNum=0;
		this.gmn("[" + pitch + "]", componentNum);
		^this
	}

	gmnf {arg filePath, componentNum=0;
		var msg, component = "/gmnf";
		msg = this.makeOSCPath(component, componentNum);
		this.changed(\msg, [msg, \set, \gmnf, filePath]);
		^this
	}

	// use with write (below)
	gmnStream {arg stream="", componentNum=0;
		var msg, component = "/score";
		msg = this.makeOSCPath(component, componentNum);
		// check for (opening) bracket
		if(stream.includes($[).not, {stream = "[" + stream;});
		this.changed(\msg, [msg, \set, \gmnstream, stream]);
		^this
	}

	gmnStreamWrite {arg stream="", componentNum=0;
		var msg, component = "/score";
		msg = this.makeOSCPath(component, componentNum);
		this.changed(\msg, [msg, \write, stream]);
		^this
	}

	gmnStreamClear {arg componentNum=0;
		var msg, component = "/score";
		msg = this.makeOSCPath(component, componentNum);
		this.changed(\msg, [msg, \clear]);
		^this
	}

	gmnStreamClose {arg stream="", componentNum=0;
		var msg, component = "/score";
		msg = this.makeOSCPath(component, componentNum);
		if(stream.includes($]).not, {stream = stream + "]"});
		this.changed(\msg, [msg, \write, stream]);
		^this
	}

	// staves doesn't include any formatting
	// to get proper staves { [ stave1 ], [ staveX ] } is required
	staves {arg notn="[e1/8. \\slur(f/16 e/8) d c h0 a/4 ], [ c1/4 d e/2 ], [ a0/4 h \\slur(c1/8 h0) a/4 ]", componentNum=0;
		notn = "{" + notn + "}";
		^this.gmn(notn, componentNum);
	}

	// accepted by the components types gmn | gmnstream | gmnf
	// also see rows
	columns {arg val=1, componentNum=0;
		var msg, component = "/score";
		msg = this.makeOSCPath(component, componentNum);
		this.changed(\msg, [msg, \columns, val]);
		^this
	}

	happyBirthday {arg componentNum=0;
		var happy, msg, component = "/score";
		msg = this.makeOSCPath(component, componentNum);
		happy = "[\\meter<\"3/4\"> empty/2 c/8. c/16 d/4 c f e/2 c/8. c/16 d/4 c g f/2 c/8. c/16 c2/4 a1 f e d/1 b&/8. b&/16 a/4 f g f/2]";
		this.changed(\msg, [msg, \set, \gmn, happy]);
		^this
	}

	happybirthday {arg componentNum=0;
		^this.happyBirthday(componentNum)
	}

	//////////////////////////////////////////////////////////////////////
	// OBJECT CREATION

	ellipse {arg width=0.1, height=0.1, x, y, z, angle, component="ellipse", componentNum=0;
		var msg, bundle = [];
		msg = this.makeOSCPath(component, componentNum);
		bundle = bundle.add([msg, \set, \ellipse, width, height]);
		if(x.notNil){bundle = bundle.add([msg, \x, x])};
		if(y.notNil){bundle = bundle.add([msg, \y, y])};
		if(angle.notNil){bundle = bundle.add([msg, \angle, angle])};
		if(z.notNil){bundle = bundle.add([msg, \z, z])};
		this.changed(\bundle, bundle);
		^this
	}

	// NB unicode text here must be formatted as "&#332;" or "&#x0251;" for hex
	// color is in hex:
	// [155, 255, 0, 255] = #9BFF00FF (semi-transparent pink); alpha, red, green, blue
	// #FF000000 is black, fully opaque
	htmlFull {arg fontfamily="Helvetica", fontsize="14pt", fontstyle="normal", fontweight="normal", opacity=0.1, color, text="default", componentNum=0;
		var msg, fullText, component = "/html";

		msg = this.makeOSCPath(component, componentNum);
		fullText = "<span style=\"font-family: " + fontfamily + "; font-size: " + fontsize +  "; font-style: " + fontstyle +  "; font-weight: " + fontweight + "; opacity:" + opacity + "; color:" + color + "; \">" + text + "</span>";
		this.changed(\msg, [msg, \set, \html, fullText]);
		^this
	}

	line {arg x=0.5, y= 0.5, angle, widthVal, componentNum=0;
		var bundle, msg, component = "/line";
		msg = this.makeOSCPath(component, componentNum);
		bundle = [[msg, \set, \line, \xy, x, y]];
		if(angle.notNil, {bundle = bundle.add([msg, \angle, angle])});
		if(widthVal.notNil, {bundle = bundle.add([msg, \penWidth, widthVal])});
		this.changed(\bundle, bundle);
		^this
	}

	// polygon: a polygon specified by a sequence of points,
	// each point being defined by its (x,y) coordinates.
	// The coordinates are expressed in the scene coordinate space,
	// but only the relative position of the points is taken into account
	// (i.e a polygon A = { (0,0) ; (1,1) ; (0,1) }
	// is equivalent to a polygon B = { (1,1) ; (2,2) ; (1,2) }).
	// default argumnets create a square
	polygon {arg polyPointsArray=[1.0, 0.0, 0.5, 0.0, 0.5, 0.5, 1.0, 0.5], componentNum=0;
		var msg, pointsSize, sendMsg, component = "/poly";
		msg = this.makeOSCPath(component, componentNum);
		pointsSize = polyPointsArray.size;
		sendMsg = [msg, \set, \polygon];
		case
		{ pointsSize <6 } {
			"a minimum of three points (each of two coordinates) is required to make a polygon".error;
			^this
		}
		{ pointsSize > 36 } {
			"polygon maximum number of supported points exceeded".error;
			^this
		};
		// check for odd number of points
		polyPointsArray = polyPointsArray.drop(pointsSize.odd.binaryValue);
		sendMsg = sendMsg ++ polyPointsArray;
		this.changed(\msg, sendMsg);
		^this
	}

	// synonym for polygon
	poly {arg polyPointsArray=[1.0, 0.0, 0.5, 0.0, 0.5, 0.5, 1.0, 0.5], componentNum=0;
		^this.polygon(polyPointsArray, componentNum)
	}

	// https://inscoredoc.grame.fr/refs/6-setsect/#6-setsect-rect
	rect {arg xSize=0.1, ySize=0.1, x, y, z, angle, componentNum=0;
		var msg, bundle, component = "/rect";
		msg = this.makeOSCPath(component, componentNum);
		bundle = [[msg, \set, \rect, xSize, ySize]];
		if(angle.notNil, {bundle = bundle.add([msg, \angle, angle])});
		if(x.notNil, {bundle = bundle.add([msg, \x, x])});
		if(y.notNil, {bundle = bundle.add([msg, \y, y])});
		if(z.notNil, {bundle = bundle.add([msg, \z, z])});
		this.changed(\bundle, bundle);
		^this
	}

	//////////////////////////////////////////////////////////////////////
	// OBJECT TRANSFORMATION

	absPos {arg x, y, componentNum=0, component="/score";
		var msg, bundle = [];
		msg = this.makeOSCPath(component, componentNum);
		if(x.notNil){bundle = bundle.add([msg, \x, x])};
		if(y.notNil){bundle = bundle.add([msg, \y, y])};
		this.changed(\bundle, bundle);
		^this
	}

	// see also dalpha
	alpha {arg alpha=255, componentNum=0, component="/score";
		var msg;
		msg = this.makeOSCPath(component, componentNum);
		this.changed(\msg, [msg, \alpha, alpha]);
		^this
	}

	color { arg color=[255, 255, 255, 255], componentNum=0, component="/score";
		var msg;
		// allow for named colors or hex (as symbol)
		// as per https://www.w3schools.com/colors/colors_groups.asp
		if(color.isArray.not){color = color.bubble};
		msg = this.makeOSCPath(component, componentNum);
		this.changed(\msg, [msg, \color]++ color);
		^this
	}

	// also see rotate
	drotate {arg dx, dy, dz, componentNum=0, component="/score";
		var msg, bundle = [];
		msg = this.makeOSCPath(component, componentNum);
		if(dx.notNil, {bundle = bundle.add([msg, \drotatex, dx])});
		if(dy.notNil, {bundle = bundle.add([msg, \drotatey, dy])});
		if(dz.notNil, {bundle = bundle.add([msg, \drotatez, dz])});
		this.changed(\bundle, bundle);
		^this
	}

	move {arg x, y, z, componentNum=0, component="/score";
		var msg, bundle = [];
		msg = this.makeOSCPath(component, componentNum);
		if(x.notNil){bundle = bundle.add([msg, \x, x])};
		if(y.notNil){bundle = bundle.add([msg, \y, y])};
		if(z.notNil){bundle = bundle.add([msg, \z, z])};
		this.changed(\bundle, bundle);
		^this
	}

	// see also xorigin and yorigin
	origin {arg x, y, componentNum=0, component="/score";
		var bundle, msg;
		msg = this.makeOSCPath(component, componentNum);
		if(x.notNil, {bundle = bundle.add([msg, \xorigin, x])});
		if(y.notNil, {bundle = bundle.add([msg, \yorigin, y])});
		this.changed(\bundle, bundle);
		^this
	}

	// see "https://inscoredoc.grame.fr/refs/3-common/#pen-control"
	// conponent e.g. rect needs to already exist
	// pen styles: solid, dash, dot, dashDot, dashDotDot
	// penColor is an RGBA Array, default is opaque black [0,0,0,255]
	pen {arg component="/rect", penWidth = 1.0, penColor, penAlpha, penStyle, componentNum=0;
		var msg, bundle = [];
		msg = this.makeOSCPath(component, componentNum);
		bundle = bundle.add([msg, \penWidth, penWidth]);
		if(penColor.notNil, {
			bundle = bundle.add([msg, \penColor, penColor[0], penColor[1], penColor[2], penColor[3]])
		});
		if(penAlpha.notNil, {bundle = bundle.add([msg, \penAlpha, penAlpha])});
		if(penStyle.notNil, {bundle = bundle.add([msg, \penStyle, penStyle])});
		this.changed(\bundle, bundle);
		^this
	}

	// also see move
	// NB these are currently relative positions
	position {arg dx, dy, componentNum=0, component="/score";
		var msg, bundle = [];
		msg = this.makeOSCPath(component, componentNum);
		if(dx.notNil, {bundle = bundle.add([msg, \dx, dx])});
		if(dy.notNil, {bundle = bundle.add([msg, \dy, dy])});
		this.changed(\bundle, bundle);
		^this
	}

	// synonym for position
	pos {arg dx, dy, componentNum=0, component="/score";
		^this.position(dx, dy, componentNum, component)
	}

	// "https://inscoredoc.grame.fr/refs/3-common/#3-common-rotate"
	// NB angle and rotatez are equivalent
	rotate {arg x=0.0, y=0.0, z, componentNum=0, component="/score";
		var msg, bundle;
		msg = this.makeOSCPath(component, componentNum);
		bundle = [ [msg, \rotatex, x ], [msg, \rotatey, y ]];
		if(z.notNil, {bundle = bundle.add([msg, \rotatez, z])});
		this.changed(\bundle, bundle);
		^this
	}

	rotateX {arg x=0.0, componentNum=0, component="/score";
		var msg;
		msg = this.makeOSCPath(component, componentNum);
		this.changed(\msg, [msg, \rotatex, x ]);
		^this
	}

	rotateY {arg y=0.0, componentNum=0, component="/score";
		var msg;
		msg = this.makeOSCPath(component, componentNum);
		this.changed(\msg, [msg, \rotatey, y ]);
		^this
	}

	rotateZ {arg z=0.0, componentNum=0, component="/score";
		var msg;
		msg = this.makeOSCPath(component, componentNum);
		this.changed(\msg, [msg, \rotatez, z ]);
		^this
	}

	scale {arg scale=6.0, componentNum=0, component="/score";
		var msg;
		msg = this.makeOSCPath(component, componentNum);
		this.changed(\msg, [msg, \scale, scale]);
		^this;
	}

	// width and height messages are accepted by the following components:
	// rect | ellipse | graph | fastgraph | grid | pianoroll | pianorollf.
	// also see height

	width {arg val=0, componentNum=0, component;
		var msg;
		msg = this.makeOSCPath(component, componentNum);
		this.changed(\msg, [msg, \width, val]);
		^this
	}

	// see also move
	x { arg x=0.0, componentNum=0, component="/score";
		var msg;
		msg = this.makeOSCPath(component, componentNum);
		this.changed(\msg, [msg, \x, x ]);
		^this
	}

	// also see origin
	xorigin {arg x=0.0, componentNum=0, component="/score";
		var msg;
		msg = this.makeOSCPath(component, componentNum);
		this.changed(\msg, [msg, \xorigin, x]);
		^this
	}

	// see also move
	y { arg y=0.0, componentNum=0, component="/score";
		var msg;
		msg = this.makeOSCPath(component, componentNum);
		this.changed(\msg, [msg, \y, y ]);
		^this
	}

	// also see origin
	yorigin {arg y=0.0, componentNum=0, component="/score";
		var msg;
		msg = this.makeOSCPath(component, componentNum);
		this.changed(\msg, [msg, \yorigin, y]);
		^this
	}


}




/*

INScoreManagerMIT

For use with INScoreSceneMIT

Version compiled for The SuperCollider Book, 2nd Edn, MIT Press

Richard Hoadley: http://rhoadley.net
Tom Hall: www.ludions.com

*/

// use for basic interaction with INScore app from SC
INScoreManagerMIT {

	classvar minVersion = 1.34;

	var <>inScoreNetAddr, traceFuncAdded = false, <errorListener;
	var <port, <netAddr, traceFunc, <errorPort, <inScoreErrorDef;
	var msgTimeSent=0, <latencyListenerName, adminCondition;
	var <locale, latencyReport = false, <scenesDict, <>myIP;
	var <recievePort, <hostIP, <oscDefIDs, <version;
	var quit, adminTask, <makeListeners, <>basePath = "/ITL";

	*all {
		^OSCdef.all.keys.select{|i| i.asString.contains("inScore")}.asArray
	}

	*freeAll {
		var allKeys = this.all;
		if(allKeys.notEmpty){
			allKeys.do{|i| OSCdef(i).free };
			"All INScoreListener OSCdefs freed".postln;
		};
		^this
	}

	*new { arg netAddr, makeListeners = true, myIP;
		^super.new.init(netAddr, makeListeners, myIP)
	}

	init {arg argNetAddr, argMakeListeners, argMyIP;
		netAddr = argNetAddr ?? NetAddr.new("localhost", 7000);
		locale = this.findLocale(netAddr);
		makeListeners = argMakeListeners;
		oscDefIDs = [];
		scenesDict = Bag.new(50);
		quit = false;
		if(makeListeners){
			this.networkAdmin; // will set inScoreNetAddr
		};
		myIP = argMyIP; // needed for 'watch' 'newElement' msgs
		^this
	}

	quit {
		quit = true;
		adminCondition.unhang;
		adminTask.stop;
		this.removeTraceFunc;
		this.free;
	}

	networkAdmin {
		var oscErrorAddr;
		"Running INScore networkAdmin...".postln;
		port = netAddr.port;
		recievePort = port + 1;
		// open ports to receive messages fron INScore
		thisProcess.openUDPPort(recievePort);
		errorPort = port + 2;
		thisProcess.openUDPPort(errorPort);
		traceFunc = {|msg, time, replyAddr, recvPort|
			// first check coming from INScore
			// 2nd checks if msg is from 'hello'
			// msg.postln;
			if(recvPort==recievePort and:{msg[3]==recievePort}){
				inScoreNetAddr = replyAddr;
				hostIP = msg[1].asString;
				if(locale=="local"){myIP = hostIP};
				adminCondition.unhang;
			}{
				"SC waiting for INScore msg...".postln;
			};
			if(recvPort==errorPort){
				msg.postln;
			}
		};
		adminCondition = Condition(false);
		adminTask = Task({ loop{ "...".postln; 0.5.wait}}).play;
		this.getINScoreAddr(port);
		// request a response from INScore to trigger traceFunc
		this.hello;
		fork {
			adminCondition.hang;
			this.removeTraceFunc;
			// update OSC error msg with beginning "/" to comply with OSC spec
			// otherwise can't be listened for by an OSCdef
			// "/ITL*" so can catch with newMatching default OSCdef below
			if(quit.not){
				oscErrorAddr = '/Error';
				netAddr.sendMsg("/ITL", 'errorAddress', oscErrorAddr);
				this.addVersionListener; // also adds usual listener once version determined
				this.addErrorListener(oscErrorAddr);
			};
		};
		^this
	}

	findLocale {|addr|
		^if(addr.isLocal){"local"}{"remote"};
	}

	quitINScore {
		this.free;
		this.sendMsg("/ITL", \quit);
		^this
	}

	free {|verbose = true|
		this.removeAllListeners;
		if(verbose){
			"INScore OSCdefs removed".postln;
		};
		^this
	}

	disconnect {
		^this.free
	}

	// all OSCdefs need to have unique names
	makeListenerName {|nameBase|
		var name;
		if(nameBase.isNil){nameBase = "inScore"};
		name = (nameBase ++ UniqueID.next).asSymbol;
		^name
	}

	// onshot OSCdef to determine INScore version
	// once recieved it also creates INScore application level listener
	// and stops admin "...".postln task
	addVersionListener {|path = "/ITL", nameBase="INScoreVersion"|
		var oSCdef, name;
		name = this.makeListenerName(nameBase);
		oscDefIDs = oscDefIDs.add(name);
		if(inScoreNetAddr.notNil){
			oSCdef = OSCdef(name, {|msg, time, addr, recvPort|
				version = msg[2].asFloat; // instance var
				adminTask.stop;
				format("INScore NetAddr received on % computer", locale).postln;
				format("IP Address: %, port: %", hostIP, inScoreNetAddr.port).postln;
				//"get using instance var inScoreNetAddr".postln;
				if(version <minVersion){
					format("INScore version >=% required for INScoreSceneMIT class", minVersion).warn
				};
				this.addListener; // usual listener
				format("OSCdef listeners made").postln;
			}, path, inScoreNetAddr
			).oneShot;
			this.getVersion;
			^this
		}{
			^"inScoreNetAddr is nil: set or run networkAdmin method to get".error;
		};
	}

	// INScore application level listener
	// created once addVersionListener has been triggered
	addListener {|path = "/ITL", nameBase, verbose = true, oneShot = false|
		var oSCdef, name;
		name = this.makeListenerName(nameBase);
		oscDefIDs = oscDefIDs.add(name);
		if(inScoreNetAddr.notNil){
			oSCdef = OSCdef(name, {|msg, time, addr, recvPort|
				if(verbose){
					format("INScore msg on % computer %",
						locale, hostIP
					).postln;
					msg.postln;
				};
			}, path, inScoreNetAddr
			);
			if(oneShot){oSCdef = oSCdef.oneShot};
			//format("OSCdef listener made").postln;
			^this
		}{
			^"inScoreNetAddr is nil: set or run networkAdmin method to get".error;
		};
	}

	addErrorListener {|path|
		var oSCdef, name;
		name = this.makeListenerName("inScoreError");
		oscDefIDs = oscDefIDs.add(name);
		if(inScoreNetAddr.notNil and:{errorListener.isNil}){
			oSCdef = OSCdef(name, {|msg, time, addr, recvPort|
				format("INScore error msg on % computer %",
					locale, hostIP
				).postln;
				msg.postln;
			}, path, inScoreNetAddr
			);
			errorListener = true;
			//format("OSCdef Error listener made", name).postln;
			^this
		}{
			"inScoreNetAddr is nil or errorListener already made".error;
			"Set inScoreNetAddr or run networkAdmin method to get inScoreNetAddr".postln;
			^this
		};
	}


	// TODO, consider if need to implement:
	//this.sendMsg("/ITL", \scenes, \a, \b, \c) // make


	makeScene {|scene = \scene, addListener = true, foreground = true|
		var path, osc;
		scene = scene.asString;
		path = "/ITL/" ++ scene;
		osc = this.makeListenerName("/inScoreSceneNew");
		this.watchNewElement("/ITL", osc); // watch for new scene only
		this.makeNewElementOSCdef(osc, path, clearWatch:true); //
		// allow time for scene watcher to be made
		// before making new scene
		r{
			0.05.wait;
			this.sendMsg(path, \new);
			if(foreground){
				0.05.wait; // wait for INScore to first make scene
				this.foreground(scene);
			};
			// scene listener receives e.g. 'count' msgs
			if(addListener){
				if(scenesDict.includes(scene).not){
					this.addSceneListener(scene);
				}{
					format("New scene listener '%' already exists", scene).postln;
				}
			}
		}.play;
		^this
	}

	//watchNewElement {|path = "/ITL", clearWatch = true, rtnIP|
	watchNewElement {|path = "/ITL", osc, rtnIP|
		var ip = this.determineIP(rtnIP);
		ip = ip ++ ":7002" ++ osc;
		["watchNewElement ip:", ip].postln;
		this.sendMsg(path, 'watch+', 'newElement', ip, '$name', '$scene');
		^this
	}

	// used with watchNewElement
	// respond msg made for scene creation
	makeNewElementOSCdef {|osc, path, clearWatch = true|
		var scene, object;
		OSCdef(osc, {
			arg msg, time, addr, recvPort;
			object = msg[1].asString;
			scene = msg[2].asString;
			if(object==scene){
				format("INScore scene '%' made on % machine %",
					scene, locale, addr.ip
				).postln;
			}{
				format("INScore object/scene '%/%' made on % machine %",
					scene, object, locale, addr.ip
				).postln;
			};
			if(clearWatch){
				this.sendMsg(path, 'watch', 'newElement'); // clear
			};
		}, osc).oneShot;
		^this;
	}


	removeAllListeners {|name|
		if(oscDefIDs.notEmpty){
			oscDefIDs.do{|i|
				OSCdef(i).free;
			};
			oscDefIDs = [];
			scenesDict = Bag.new(50);
		};
		^this
	}

	getINScoreAddr {|port|
		if(traceFuncAdded.not){
			thisProcess.addOSCRecvFunc(traceFunc);
			traceFuncAdded = true;
		};
		^this
	}

	removeTraceFunc {
		thisProcess.removeOSCRecvFunc(traceFunc);
		traceFuncAdded = false;
		^this
	}

	addSceneListener { arg scene = \scene, verbose = true;
		var name, path;
		scene = scene.asString;
		path = basePath ++ "/" ++ scene;
		if(scenesDict.includes(scene).not){
			scenesDict = scenesDict.add(scene);
			name = ("inScore"++scene);
			if(verbose){
				format("INScore scene listener '%' made", scene).postln;
			};
			this.addListener(path, name);
		}{
			if(verbose){
				format("scene listener '%' already made", scene).error;
			}
		};
		^this
	}

	addLatencyListener {|path = "/ITL"|
		var latency, name= \inScoreLatency;
		if(latencyListenerName.notNil.not){
			latencyListenerName = this.makeListenerName(name);
			oscDefIDs = oscDefIDs.add(latencyListenerName);
			OSCdef(latencyListenerName, {|msg, time, addr, recvPort|
				latency = (time - msgTimeSent).round(0.001);
				if(latencyReport){
					format("Time sent: %, time received: %",
						msgTimeSent.round(0.0001), time.round(0.0001)
					).postln;
					format("INScore latency: % seconds", latency).postln;
					latencyReport = false;
				};
			}, path, inScoreNetAddr
			);
		}{
			"latency Listener already Added".error
		}
		^this
	}

	removeLatencyListener {
		OSCdef(latencyListenerName).free;
		oscDefIDs.remove(latencyListenerName);
		latencyListenerName = nil;
	}

	latency {
		if(latencyListenerName.isNil){
			this.addLatencyListener;
		};
		latencyReport = true;
		msgTimeSent = thisThread.seconds;
		this.hello;
		^this
	}

	addFwdSlash {|symbol|
		symbol = symbol.asString;
		if(symbol[0] != $/){symbol = "/" ++ symbol};
		^symbol;
	}


	determineIP {|ip|
		if(ip.isNil){
			ip = if(myIP.isNil){
				"255.255.255.255" // attempt broadcast
			}{
				myIP
			}
		};
		^ip
	}

	findSceneListener {|sceneName|
		var ids, tmpFind;
		sceneName = sceneName.asString;
		tmpFind = oscDefIDs.collect{|i, j| [i.asString.find(sceneName), j]};
		tmpFind = tmpFind.reject{|i| i[0].isNil};
		ids = tmpFind.collect{|i| oscDefIDs[i[1]]};
		^ids
	}

	deleteScene {|scene, rtnIP|
		var path, osc, listenerName, listenerIndex;
		path = "/ITL/"++scene.asString;
		osc = this.makeListenerName("/inScoreSceneDel");
		this.watchDelScene(path, rtnIP, osc);
		this.makeDelSceneWatcher(osc, path);
		// allow time for watcher to be made
		r{
			0.05.wait;
			this.sendMsg(path, 'del');
			listenerName = this.findSceneListener(scene)[0].asSymbol;
			if(listenerName.notNil){
				// free scene listener
				OSCdef(listenerName).free;
			};
			// remove from list of listeners
			listenerIndex = oscDefIDs.indexOf(listenerName);
			if(listenerIndex.notNil){
				oscDefIDs.removeAt(listenerIndex);
			};
			scenesDict.remove(scene.asString);
		}.play;
		^this
	}

	watchDelScene {|path, rtnIP, osc|
		var ip;
		ip = this.determineIP(rtnIP);
		ip = ip ++ ":7002" ++ osc;
		this.sendMsg(path, 'watch', 'del', ip, '$scene');
		^this
	}


	makeDelSceneWatcher {|osc, path|
		var scene;
		OSCdef(osc, {
			arg msg, time, addr, recvPort;
			scene = msg[1].asString;
			format("INScore scene '%' deleted on % machine %",
				scene, locale, addr.ip
			).postln;
		}, osc).oneShot;
		^this;
	}

	sendMsg {|path = "/ITL"... msg|
		netAddr.sendMsg(path, *msg);
		^this
	}

	sendBundle {|time=nil...msgs|
		netAddr.sendBundle(time, *msgs);
		^this
	}

	getWatch {|path = "/ITL"|
		path = this.addFwdSlash(path);
		if(path.contains("/ITL").not){
			path = "/ITL"++path;
		};
		this.get(path, \watch);
		^this;
	}

	showLog {|bool = true|
		this.sendMsg("/ITL/log", \show, bool.asInteger);
		^this
	}

	clearLog {
		this.sendMsg("/ITL/log", \clear);
		^this
	}


	foregroundLog {
		this.sendMsg("/ITL/log", \foreground);
		^this
	}


	foreground {|scene = \scene|
		var path = "/ITL/" ++ scene.asString;
		this.sendMsg(path, \foreground);
		^this
	}


	hello {|path = "/ITL"|
		this.sendMsg(path, \hello)
		^this;
	}

	require {|min = 1.34, path = "/ITL"|
		if(version.notNil){
			case{version > min}{
				format("This version % is compatable with %", version, min).postln
			}
			{version < min}{
				format("This version % is NOT compatable with %", version, min).error
			}
			{version == min}{
				format("This version matches the minimum required %", min).postln
			}
		}{
			this.sendMsg(path, \require, min);
		};
		^this
	}

	listenerExists {|scene = \scene|
		^scenesDict.includes(scene.asString)
	}

	makeSceneListenerAsNeeded {|scene = \scene|
		scene = scene.asString;
		if(this.listenerExists(scene).not){
			this.addSceneListener(scene, verbose: false);
		};
		^this
	}

	// /ITL/scene get count;
	sceneCount {|scene = "scene"|
		this.makeSceneListenerAsNeeded(scene);
		this.get("/ITL/" ++ scene, \count);
		^this
	}

	// /ITL/scene get rcount;
	sceneRCount {|scene = "scene"|
		this.makeSceneListenerAsNeeded(scene);
		this.get("/ITL/" ++ scene, \rcount);
		^this
	}

	// /ITL/scene get swidth;
	sceneSWidth {|scene = "scene"|
		this.makeSceneListenerAsNeeded(scene);
		this.get("/ITL/" ++ scene, \swidth);
		^this
	}

	// /ITL/scene get sheight;
	sceneSHeight {|scene = "scene"|
		this.makeSceneListenerAsNeeded(scene);
		this.get("/ITL/" ++ scene, \sheight);
		^this
	}

	// get whatever object status required
	get {|path = "/ITL", msg|
		this.sendMsg(path, \get, msg)
		^this
	}

	getVersion {|path = "/ITL"|
		this.get(path, \version);
		^this
	}

	getIP {|path = "/ITL"|
		this.get(path, \IP);
		^this
	}

	allScenes {
		^this.get("/ITL", \scenes);
	}

	numScenes {
		^this.get("/ITL", \scount);
	}


	stats {|path = "/ITL/stats"|
		this.makeSceneListenerAsNeeded(\stats);
		this.sendMsg(path, \get);
		^this
	}

	guidoVersion {|path = "/ITL"|
		this.get(path, "guido-version");
		^this
	}

	musicXMLVersion {|path = "/ITL"|
		this.get(path, "musicxml-version");
		^this
	}
}











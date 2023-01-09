
/*

INScoreViewMIT
Dependency: INScoreViewMIT

Version compiled for The SuperCollider Book, 2nd Edn, MIT Press

Richard Hoadley: http://rhoadley.net
Tom Hall: www.ludions.com


*/

// requires instance of INScoreSceneMIT for model
INScoreViewMIT {

	var <scene, <model, <netAddr, <>verbose, <locale;
	var <>miniView, oscBundle;

	*new { arg model, netAddr, verbose = true, miniView = false;
		^super.new.init(model, netAddr, verbose, miniView);
	}

	init {arg argModel, argNetAddr, argVerbose, argMiniView;
		model = argModel; // model is INScoreSceneMIT
		model.addDependant(this);
		netAddr = argNetAddr;
		verbose = argVerbose;
		miniView = argMiniView;
		scene = model.scene;
		locale = this.findLocale;
		^this
	}

	isLocal {
		netAddr.postln;
		^netAddr.isLocal
	}

	findLocale {|addr|
		^if(this.isLocal){"local"}{"remote"};
	}

	inform { |msg, isBundle=false|
		var msgType = if(isBundle){"bundle"}{"msg"};
		format("Scene % % sent to % machine %:",
			scene.asCompileString, msgType, locale, netAddr.ip
		).postln;
		msg.postln;
		^this
	}

	dispatchMsg {|msgArr|
		netAddr.sendMsg(*msgArr);
		^this
	}

	dispatchBundle {|msgArr, closeBundle|
		oscBundle = oscBundle.add(msgArr);
		if(closeBundle){
			netAddr.sendBundle(nil, *oscBundle);
			//["Bundle sent:", oscBundle].postln;
		};
		^this
	}


	update { |obj, what, val|
		var oscMsg, closeBundle, bundleSize, partOfBundle = false;
		//[obj, what, val].postln;
		// bundles or singletons?
		partOfBundle = what==\bundle;
		// arr of msgs
		if(partOfBundle){
			oscBundle = []; // start new bundle
			bundleSize = val.size;
			val.do{|i, j|
				closeBundle = j == (bundleSize-1);
				//[j, closeBundle].postln;
				oscMsg = this.filterMsg(i);
				if(oscMsg.notNil){
				this.dispatchBundle(oscMsg, closeBundle);
			}
			};
			if(verbose){this.inform(val, isBundle:true)};
		}{
			oscMsg = this.filterMsg(val);
			if(oscMsg.notNil){
				this.dispatchMsg(oscMsg)
			};
			if(verbose){this.inform(val, isBundle:false)};
		};
	}

	// basic msg filtering and verbosity msg posting
	filterMsg {|val|
		var oscMsg, post = true;
		// show or hide the mouse pointer in all scenes
		if(val[0]=="/ITL" and:{val[1]==\mouse}){
			oscMsg = val;
		}{
			if(miniView.not){
				oscMsg = val;
			}{
				case {val[1]==\fullscreen and:{val[2]==1}}{
					post = false;
					"FullScreen ignored as scene is miniView".postln;
				}
				{val[1]==\frameless and:{val[2]==1}}{
					post = false;
					"Frameless ignored as scene is miniView".postln;
				}
				{val[1]==\width or:{val[1]==\height}}{ // Window size
					val = [val[0], val[1], val[2]/2];
					"(Window size is scaled as miniView)".postln;
					oscMsg = val;
				}
				{true}{
					oscMsg = val;
				};
			}
		};
		// if(oscMsg.notNil){
		// 	this.dispatch(oscMsg, partOfBundle)
		// };
		/*if(verbose and:{post}){
			this.inform(val);
		};*/
		post = true;
		^oscMsg
	}
}









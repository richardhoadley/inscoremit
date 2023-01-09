/*
Code compiled for The SuperCollider Book, 2nd Edn, MIT Press

26 Notations and Score-making

Tom Hall, Newton Armstrong, Richard Hoadley

CODE LISTING

*/


/////////////////////////////////////////////////////////////////////////////////

// 26.3 INScore

// Class code at https://github.com/richardhoadley/inscoremit

// **26.3.2 INScore and SuperCollider**

// depending on installation path location
"Applications/INScoreViewer.app".openOS

// example INScore text file contents
/ITL/scene/score0 set gmn "[ c ]"; // not SuperCollider code

// assumes INScoreViewer listening for OSC on port 7000
~addr = NetAddr.new("localhost", 7000);
~addr = NetAddr.new("10.0.1.49", 7000);
~addr.sendMsg("/ITL/scene/score0", \set, \gmn, "[ d ]");

// assumes INScoreViewer listening for OSC on hostname "127.0.0.1", port 7000
~scene= INScoreSceneMIT.new;
~scene= INScoreSceneMIT.new("10.0.1.49", 7000);

~scene.note(pitch: \c);

// synchronize with a remote machine
~scene.addINScoreView(NetAddr.new("192.168.178.64", 7000)) // needs correct remote IP address

// FOLLOWING INScoreManagerMIT EXAMPLE OMITTED FROM CHAPTER DUE TO SPACE CONSTRINTS
~helper = INScoreManagerMIT.new(~addr); // use ~addr from example above
~helper.showLog // display INScore log window
~helper.makeScene(\joni, foreground: true);
~helper.allScenes // list all scenes by name
~helper.deleteScene(\joni);
~helper.free // frees the class instance OSCdefs

// using INScoreSceneMIT
j = INScoreSceneMIT.new("localhost", \joni).window
j.foreground;
j.note(pitch: "b&1/2 a", componentNum: 0);
j.close; // close window


// make chords manually (_ for rest), two staves
~scene= INScoreSceneMIT.new; // new class instance as needed
~scene.staves("[{a/4, b, c} {b3,e2, g&} _/8 {c#2/4., e0}], [{g/1, f}]" );

// helper string Guido string manipulation class
g = ~scene.guido; // otherwise: g = SimpleGuidoMIT.new;
~scene.note(pitch: g.clef(\f) + g.meter(3, 8) +g.key(-2) + "c0/8. b-1");



// Figure 26.6: Dynamic CPWN using a function to generate an algorithmic chord sequence.

~scene= INScoreSceneMIT.new; // new class instance as needed
~scene= INScoreSceneMIT.new("10.0.1.49"); // new class instance as needed

(
~randomChordsFn = {
	var chordArr, chord3, noteFN, chosenDur, melodicArr;
	var guidoOutput = "", guidoChord = [], g = ~scene.guido;
	noteFN = {rrand(30, 90)};
	chordArr = [[50, 54, 57, 59], [34, 56, 78], 3.collect({noteFN.value})];
	chord3 = [chordArr[1], chordArr[1]].choose;
	melodicArr = [chordArr[0], chordArr[0]+[2, -2].choose, chord3, chordArr[2]];
	melodicArr.do({|chord|
		chosenDur = ["/16", "/4", "/2."].choose;
		chord = chord.collect({|note| g.midiNoteMap(note, "mixed") ++ chosenDur});
		chord = [g.makeChord(chord), "_"++chosenDur].wchoose([0.8, 0.2]);
		guidoOutput = guidoOutput + chord
	});
	guidoOutput;
};

~scene.osaActivate;
~scene.verbose_(false);
Routine.new({
	6.do({|i|
		~scene.note(pitch: ~randomChordsFn.value);
		~scene.htmlFull(fontsize: "40pt", text: (i+1) + "/ 6");
		~scene.move(-0.4, -0.6, component:"html");
		2.5.wait
	});
	~scene.clear; // clear exising scene
}).play;
)


// Figure 26.7: Dynamic score with audio.
~scene= INScoreSceneMIT.new // new class instance as needed
(
var acc, glyph, glyphRed, string, oldString;
s.waitForBoot({
	SynthDef(\percSine, {|midi = 69, mul = 0.2, out=0|
		var sig, env = Env.perc(releaseTime:0.4);
		sig = SinOsc.ar(midi.midicps, mul: mul)
		* EnvGen.kr(env, doneAction: Done.freeSelf);
		Out.ar(out, [sig, sig])
	}).add;
	m = [60, 62, -63, 65, 67, 69, 71, 72, -70, -68, 67, 65, -63, 62, 60];
	g = ~scene.guido;
	string = g.stemsOff;
	oldString = string.copy;
	~scene.osaActivate;
	r = Routine.new({
		1.wait;
		m.do({|i, j|
			Synth(\percSine, [\midi, i.abs]);
			acc = if(i.isNegative){"flats"}{""};
			glyph = g.midiNoteMap(i.abs, acc);
			glyphRed = g.noteFormat(color: \red) + glyph;
			// start a new string of pitches
			if(j==8){
				string = g.stemsOff + g.midiNoteMap(m[j-1].abs, acc);
				oldString = string.copy;
			};
			string = (oldString + glyphRed).postln;
			oldString = oldString + glyph;
			~scene.note(pitch: string);
			(60/94).wait
		});
		0.5.wait;
		~scene.note(pitch: oldString);
		1.wait;
		~scene.note(pitch: g.emptyStaff);
	}).play;
})
)


// scale, move or delete the existing GUIDO score0 scene object
~scene= INScoreSceneMIT.new // new class instance as needed
~scene.note(~scene.guido.emptyStaff); // from previous example
~scene.move(y: -0.8).scale(0.5);
~scene.deleteComponent(component: "score", componentNum: 0);

// create a polygon
// ITL/scene/polygon0 set poly [0.7, 0.05, 0.1, 0.1, 0.1, 0.0];
~scene.poly([0.7, 0.05, 0.1, 0.1, 0.1, 0.0]);
~scene.color(\orange, component: "poly"); // https://en.wikipedia.org/wiki/Web_colors

// place the centre of the polygon at the center
// or either horizontal edge of the scene window:
~scene.move(x: [-1, 0, 1].choose, component: "poly");

// FOLLOWING EXAMPLE OMITTED FROM CHAPTER DUE TO SPACE CONSTRINTS
// change the screen ratio, then fullscreen
// move poly across screen, fading in/out alpha
// uses absolute position (relative possible with dx or dalpha)

~scene= INScoreSceneMIT.new; // new class instance as needed
(
var screenRatio = 1.78, dur = 5, fRate=60, iter;
var pos, wait, posSpec, alpha, alphaSpec;
posSpec =[screenRatio.neg, screenRatio].asSpec;
alphaSpec = [0, 255].asSpec;
iter = dur * fRate;
~scene.osaActivate;
~scene.poly([0.7, 0.05, 0.1, 0.1, 0.1, 0.0]);
Routine.new({
	~scene.windowHeight("scene", 1.0);
	0.25.wait;
	~scene.windowWidth("scene", screenRatio);
	~scene.fullScreen(true);
	1.25.wait;
	iter.reverseDo{|i|
		alpha = alphaSpec.map(i/iter);
		alpha = (alpha*2).round.asInteger.fold(0, 255);
		~scene.scale((i/iter*2).fold(0, 1), component: "poly");
		~scene.alpha(alpha, component: "poly");
		pos = posSpec.map(i/(iter-1));
		~scene.move(x: pos, component: "poly");
		wait = fRate.reciprocal;
		wait.wait;
	};
	1.wait;
	~scene.endFullScreen;
	~scene.alpha(255, component: "poly");
}).play
)


// Code for image in Figure 26.8: INScore scene scaling, object and dynamic CPWN animation


~scene= INScoreSceneMIT.new; // assumes INScoreViewer listening for OSC on port 7000
~scene.verbose_(false);

(
var scorePos, scoreMovt, screenRatio = 1.78, guido = ~scene.guido;
~scene.osaActivate;
~scene.clear; // clear exising scene
Routine.new({
	~scene.windowHeight(1.0);
	0.25.wait;
	~scene.windowWidth("scene", screenRatio);
	~scene.fullScreen;
	1.25.wait;
	6.do({|i|
		var waitTime = rrand(3, 5), randIter = rrand(4, 7), numScores = randIter + 2;
		var newMel, melodyHeader = Array.newClear(numScores, {""}), melodyRH = melodyHeader.copy;
		var starPos = [rrand(0.6, 0.8)* screenRatio.neg, rrand(-0.5, -0.7)]; // star lines pos

		~scene.htmlFull(text: (i+1) + "/ 6", fontsize: "48pt");
		~scene.move(-1.4, -0.85, component: "html"); // counter
		scorePos = Array.fill(numScores, {[rand2(screenRatio), rand2(1.0)]});
		scoreMovt = Array.fill(numScores, {|i| {rrand(0.075, 4.0)}.dup * 0.001 * scorePos[i].sign.neg});
		numScores.do({|i|
			melodyHeader[i] = guido.meter(rrand(2, 5), [2, 4, 8].choose) + guido.key(rrand(-4, 4));
			melodyRH[i] = "";

			~scene.note(pitch: melodyHeader[i] ++ melodyRH[i], componentNum: i);
			~scene.origin(x: -1.0, y: 1.0, componentNum: i); // scores expand to the right
			~scene.move(x: scorePos[i][0], y: scorePos[i][1], componentNum: i);
			~scene.scale(scale: rrand(0.2, 1.0), componentNum: i);
			~scene.color(color: 255.rand!3++[255], componentNum: i);
			Routine.new({
				4.do({
					newMel = guido.note(*[["b&", "a", "c", "b"], ["", "1"], [2, 4, 8]].collect{|i| i.choose});
					// newMel.postln;
					melodyRH[i] = melodyRH[i] ++ newMel;
					~scene.note(pitch: melodyHeader[i] ++ melodyRH[i], componentNum: i);
					[0.25, 0.33, 0.5, 0.6].choose.wait
				})
			}).play;
			Routine.new({
				((waitTime/0.01).floor).do({
					~scene.pos(dx: scoreMovt[i][0], dy: scoreMovt[i][1], componentNum: i);
					0.01.wait
				})
			}).play
		});

		(randIter*4).do({|i|
			~scene.rect(xSize: rrand(0.01, 0.3), ySize: rrand(0.01, 0.1), angle: 0, componentNum: i); // rect
			~scene.move(x: rand2(screenRatio), y: rand2(1.0), componentNum: i, component: "rect");
			~scene.color(color: [200, 200, rrand(200, 255), 255.rand], componentNum: i, component: "rect");
			Routine.new({200.do({~scene.drotate(8.rand, componentNum: i, component: "rect"); 0.04.wait})}).play;

			~scene.line(x: rand2(0.4), y: rand2(0.4), angle: 360.rand, widthVal: 0.5, componentNum: i); // line
			~scene.move(x: starPos[0], y: starPos[1], componentNum: i, component: "line");
			~scene.color(color: [0, 0, 0, 100], componentNum: i, component: "line");
			Routine.new({200.do({
				~scene.drotate(dx: rrand(1, 6), dy: rrand(1, 6), dz: rrand(1, 6), componentNum: i, component: "line");
				0.04.wait
			});
			}).play;




			~scene.ellipse(width: rrand(0.01, 0.08), angle: 360.rand, componentNum: i); // ellipse
			~scene.move(x: rand2(screenRatio), y: rand2(1.0), componentNum: i, component: "ellipse");
			~scene.color(color: [rrand(200, 255), 0, 0, 255 ], componentNum: i, component: "ellipse");
			Routine.new({10.rand.do({
				~scene.move(x: rand2(1.0), y: rand2(1.0), componentNum: i, component: "ellipse");
				~scene.scale(scale: rrand(0.3, 0.8), componentNum: i, component: "ellipse");
				[0.15, 0.25, 0.3].choose.wait});
			}).play
		});
		waitTime.wait;
		numScores.do({|i| ~scene.deleteComponent(component: "score", componentNum: i)});
		// (randIter*4).do({|i| ~scene.deleteComponent( component: "line", componentNum: i)});
	});
	~scene.endFullScreen;
	~scene.windowWidth(1);
}).play;
)




//// CHAPTER 26 CODE LISTING - INScore - END

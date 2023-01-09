

/*

// SimpleGuidoMIT

For use with INScoreSceneMIT

Version compiled for The SuperCollider Book, 2nd Edn, MIT Press

Richard Hoadley: http://rhoadley.net
Tom Hall: www.ludions.com

*/

SimpleGuidoMIT {
	var <>durFromFloatArr, <>gr8thanVal;

	*new {
		^super.new.init
	}

	init {
		// see durToNotation
		durFromFloatArr = [
			[0.0, "/16"],
			[0.01, "/16"],
			[0.03, "/16"],
			[0.06, "/16"],
			[0.1, "/8"],
			[0.14, "/8"],
			[0.15, "/8."],
			[0.23, "/4"],
			[0.32, "/4."],
			[0.64, "/2"],
			[0.82, "/2."],
			[1.0, "/1"]
		];
		gr8thanVal = "/1."; // kludge
		^this;
	}

	guidoTag {|name, contents, quotes = true|
		var string;
		string = "\\" ++ name.asString ++"<";
		if(quotes){string = string ++ "\""};
		string = string ++ contents;
		if(quotes){string = string ++ "\""};
		string = string ++ ">"
		^string
	}

	meter {|beats = 4, base = 4|
		var contents;
		if(beats.isString or: {beats.isKindOf(Symbol)}){
			contents = beats // e.g. C
		}{
			contents = beats.asString;
		};
		contents = contents ++ "/" ++ base.asString;
		^this.guidoTag(\meter, contents)
	}

	rest { ^"_" }

	dot { ^"." }

	// octaves can default to nothing
	oct {|int=""|
		^int.asString;
	}

	dur {|int=4, dot=false|
		var string;
		if(int.isFloat){int = int.asInteger};
		string =int.asString;
		if(string[0] != $/){string = "/"++string};
		if(dot){string = string ++ this.dot};
		^string
	}

	note {|pitchName = \a, oct="", dur=4|
		pitchName = pitchName.asString;
		oct = this.oct(oct);
		dur = this.dur(dur);
		^pitchName++oct++dur
	}

	// with spaces between string items
	scatList {|strArr|
		^"".scatList(strArr)
	}

	// no spaces between string items
	catList {|strArr|
		^"".catList(strArr)
	}

	// adds commas between string items
	ccatList {|strArr|
		^"".ccatList(strArr)
	}

	clef {|type = \g|
		^this.guidoTag(\clef, type.asString)
	}

	space {|int = 1, unit = "hs"| // halfspace of current staff, or cm, pt, pica, in
		var contents = int.asString ++ unit;
		^this.guidoTag(\space, contents, false)
	}

	// value class is Integer, Symbol or String
	// e.g., 2, -3, "e&", \c
	key {|value=0|
		var bool = value.isInteger.not;
		^this.guidoTag(\key, value, bool)
	}

	// clef val can be "none"
	emptyStaff {|clef=\g, dur=4|
		var string, oct;
		clef = clef.asString;
		oct = if(clef=="f"){"0"}{"1"}; // avoid ledger lines
		string = this.clef(clef);
		string = string + this.noteFormat(style: \empty);
		string = string + "\\stemsOff a";
		string = string ++oct ++"/"++dur.asString;
		^string
	}

	stemsOff { ^"\\stemsOff" }

	// consider also dx	unit displacement on the horizontal axis
	noteFormat {|color, style, size|
		var string = "\\noteFormat<";
		if(color.notNil){
			string = string ++"color=\""++color.asString++"\"";
		};
		if(style.notNil){
			if(color.notNil){string = string ++", "};
			string = string ++"style=\""++style.asString++"\"";
		};
		if(size.notNil){
			if(color.notNil or:{style.notNil}){string = string ++", "};
			string = string ++"size=" ++size.asString;
		};
		string = string ++">";
		^string
	}

	collectPitches {|pitchesArr, chord = false|
		var string = "";
		if(chord){string = string +"{"};
		pitchesArr.collect{|i, j|
			if(chord and:{j>0}){string = string ++","};
			string = string + i.asString
		};
		if(chord){string = string +"} "};
		^string
	}

	makeMelody {|pitchesArr|
		^this.collectPitches(pitchesArr, false)
	}

	melody {|pitchesArr|
		^this.makeMelody(pitchesArr)
	}

	chord {|pitchesArr|
		^this.makeChord(pitchesArr, true)
	}

	makeChord {|pitchesArr|
		^this.collectPitches(pitchesArr, true)
	}

	midiMap  {|noteIn=69, enharm="sharps"|
		^this.midiNoteMap(noteIn, enharm)
	}


	// close copy of 'guidoNoteMap' in INScore
	midiNoteMap { |noteIn=69, enharm="sharps"|
		var note, octave, accidental, normPitch, asciiEdit, enharmonic;

		note = noteIn-48; // find octave
		note = note.round; // make sure it's rounded properly
		octave = (note/12).asInteger; // find octave
		note = note % 12; // find basic note

		// Ascribe the letter name to the particular notes,
		// so the numbers need to be translated into ascii values
		normPitch = note;
		if(normPitch > 4) { normPitch = 1 } { normPitch = 0 };
		note = note + normPitch;
		accidental = note;
		note = (note/2).trunc;
		note = note.asInteger;
		if(note > 4) { asciiEdit = 92 } { asciiEdit = 99 };
		note = note + asciiEdit;

		// accidentals
		enharm = enharm.asString;
		accidental = accidental % 2;
		if(accidental == 1){ accidental = "#" } { accidental = "" };
		enharmonic = (note.asInteger).asAscii ++ accidental.asString;

		case {enharm=="sharps"}{} // sharp by default
		{enharm=="flats"}{
			if(enharmonic == "a#"){enharmonic = "b&"};
			if(enharmonic == "c#"){enharmonic = "d&"};
			if(enharmonic == "g#"){enharmonic = "a&"};
			if(enharmonic == "d#"){enharmonic = "e&"};
		}
		{enharm=="mixed"}{
			if(enharmonic == "a#"){enharmonic = "b&"};
			if(enharmonic == "d#"){enharmonic = "e&"};
		};

		enharmonic = enharmonic ++ octave.asString;
		^enharmonic;
	}

	// DURATIONS
	durFromFloat {|duration=0, maxVal=1, noTriplets= false|
		^this.durToNotation(duration, maxVal, noTriplets)
	}

	// Matches INScore name
	// set durFromFloatArr if need other mappings
	durToNotation { |duration=0, maxVal=1, noTriplets= false|
		var floatSpec, arrFlop, durStr;
		duration = 0.max(duration); // 0 lowest
		if(duration>maxVal){
			durStr = gr8thanVal // class instance var
		}{
			if(maxVal>1){
				floatSpec = [0, maxVal].asSpec;
				duration = floatSpec.unmap(duration).round(0.01);
			};
			arrFlop = durFromFloatArr.flop; // class instance var
			durStr = arrFlop[1][arrFlop[0].lastIndexForWhich{|x| x <= duration }];
		};
		if(noTriplets and:{durStr.last==$.}){
			durStr = durStr.drop(-1);
		};
		^durStr
	}

}









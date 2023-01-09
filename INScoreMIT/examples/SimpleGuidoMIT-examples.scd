// SimpleGuidoMIT

// Example usage
g = SimpleGuidoMIT.new

g.guidoTag(\some, \thing)

g.guidoTag(\another, "1unit", false)

g.clef(\f)

g.clef(\none)

g.meter(3, 16)

g.meter("3+2+3", 16)

g.space(2, \cm)

g.key(2)

g.key("e&")

g.emptyStaff

g.emptyStaff(\f, 2)

// |color, style, size|
g.noteFormat(\red)

g.collectPitches([\a, "b&", \c])

g.collectPitches([\a, "b&", \c], true)

g.noteFormat(style: "[diamond]")

g.noteFormat("red", size: 1.5)

// |noteIn, enharm="sharps"|
g.midiNoteMap(69) // a1

g.midiNoteMap(70) // a#1

g.midiNoteMap(70, \flats) // b&1

g.midiNoteMap(78, \mixed) // f#2

g.midiNoteMap(75, \mixed) // e&2

g.durFromFloatArr.do{|i| i.postln};''

g.gr8thanVal

g.durToNotation(0.2, 1, true)

g.durToNotation(0.2, 1, false)

g.durToNotation(5, 5.5)

g.durToNotation(5.5, 5.5)

g.durToNotation(5.7, 5.5)

/////////////////////









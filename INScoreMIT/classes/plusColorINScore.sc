
/*

Color.red.asArray255

Color.grey.asArray255

c = Color.fromArray255([128, 128, 128, 255])

c.alpha

*/


+ Color {

	asArray255 {
		^(this.asArray*255).round.asInteger
	}

	*fromArray255 {|array|
		^this.new(*(array/255).round(0.01));
	}
}
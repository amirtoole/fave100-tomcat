package org.musicbrainz.search.analysis;

import org.apache.lucene.analysis.charfilter.NormalizeCharMap;

public class NumericWordMappingHelper {

	public static void addToMap(final NormalizeCharMap.Builder charConvertMap)
	{
		charConvertMap.add("0", "zero");
		charConvertMap.add("1", "one");
		charConvertMap.add("2", "two");
		charConvertMap.add("3", "three");
		charConvertMap.add("4", "four");
		charConvertMap.add("5", "five");
		charConvertMap.add("6", "six");
		charConvertMap.add("7", "seven");
		charConvertMap.add("8", "eight");
		charConvertMap.add("9", "nine");
		charConvertMap.add("10", "ten");
		charConvertMap.add("11", "eleven");
		charConvertMap.add("12", "twelve");
		charConvertMap.add("13", "thirteen");
		charConvertMap.add("14", "fourteen");
		charConvertMap.add("15", "fifteen");
		charConvertMap.add("16", "sixteen");
		charConvertMap.add("17", "seventeen");
		charConvertMap.add("18", "eighteen");
		charConvertMap.add("19", "nineteen");
		charConvertMap.add("20", "twenty");
		charConvertMap.add("30", "thirty");
		charConvertMap.add("40", "forty");
		charConvertMap.add("50", "fifty");
		charConvertMap.add("60", "sixty");
		charConvertMap.add("70", "seventy");
		charConvertMap.add("80", "eighty");
		charConvertMap.add("90", "ninety");

		charConvertMap.add("1st", "first");
		charConvertMap.add("2nd", "second");
		charConvertMap.add("3rd", "third");
		charConvertMap.add("4th", "fourth");
		charConvertMap.add("5th", "fifth");
		charConvertMap.add("6th", "sixth");
		charConvertMap.add("7th", "seventh");
		charConvertMap.add("8th", "eigth");
		charConvertMap.add("9th", "ninth");
		charConvertMap.add("10th", "tenth");

	}
}

package org.musicbrainz.search.analysis;

import org.apache.lucene.analysis.charfilter.NormalizeCharMap;

public class NumericWordMappingHelper {

	public static void addToMap(final NormalizeCharMap.Builder charConvertMap)
	{
		charConvertMap.add("zero", "0");
		charConvertMap.add("one", "1");
		charConvertMap.add("two", "2");
		charConvertMap.add("three", "3");
		charConvertMap.add("four", "4");
		charConvertMap.add("five", "5");
		charConvertMap.add("six", "6");
		charConvertMap.add("seven", "7");
		charConvertMap.add("eight", "8");
		charConvertMap.add("nine", "9");
		charConvertMap.add("ten", "10");
		charConvertMap.add("eleven", "11");
		charConvertMap.add("twelve", "12");
		charConvertMap.add("thirteen", "13");
		charConvertMap.add("fourteen", "14");
		charConvertMap.add("fifteen", "15");
		charConvertMap.add("sixteen", "16");
		charConvertMap.add("seventeen", "17");
		charConvertMap.add("eighteen", "18");
		charConvertMap.add("nineteen", "19");
		charConvertMap.add("twenty", "20");
		charConvertMap.add("thirty", "30");
		charConvertMap.add("forty", "40");
		charConvertMap.add("fifty", "50");
		charConvertMap.add("sixty", "60");
		charConvertMap.add("seventy", "70");
		charConvertMap.add("eighty", "80");
		charConvertMap.add("ninety", "90");

		charConvertMap.add("first", "1st");
		charConvertMap.add("second", "2nd");
		charConvertMap.add("third", "3rd");
		charConvertMap.add("fourth", "4th");
		charConvertMap.add("fifth", "5th");
		charConvertMap.add("sixth", "6th");
		charConvertMap.add("seventh", "7th");
		charConvertMap.add("eigth", "90");
		charConvertMap.add("ninth", "9th");
		charConvertMap.add("tenth", "10th");

	}

}

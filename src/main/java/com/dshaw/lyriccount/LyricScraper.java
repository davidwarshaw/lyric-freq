package com.dshaw.lyriccount;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

public class LyricScraper {

    final static Pattern tokenizer = Pattern.compile("\\w+");
    
    public static Pair<String, String> lyrics(String link) {
        try {
            // Get the lyrics
            final Document doc = Jsoup.connect(link).get();
            
            // Get the title
            final String safeTitle = Jsoup.clean(doc.title().replaceAll(" Lyrics \\| MetroLyrics", ""), Whitelist.basic());
            
            // Get the Lyrics
            final Elements rawLyrics = doc.select("#lyrics-body-text");
            final String safeLyrics = Jsoup.clean(rawLyrics.toString(), Whitelist.basic());
            
            return Pair.of(safeTitle, safeLyrics);
            
        } catch (IOException e) {
            // There's been some error, so return null
            return null;
        }
    }
    
    public static Map<String, Integer> freq(String safeLyrics) {
        Map<String, Integer> freq = new HashMap<>();
        
        Matcher matcher = tokenizer.matcher(safeLyrics);
        
        // Create a map of the frequency
        while (matcher.find()) {
            String word = matcher.group().toLowerCase();
            freq.put(word, freq.getOrDefault(word, 0) + 1);
        }
        
        // Sort the map by descending value
        Map<String, Integer> sortedMap = freq.entrySet().stream()
        		.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
        		.collect(Collectors.toMap(Entry::getKey, Entry::getValue,	(e1, e2) -> e1, LinkedHashMap::new));

        return sortedMap;
    }
    
}

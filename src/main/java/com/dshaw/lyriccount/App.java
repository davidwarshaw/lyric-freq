package com.dshaw.lyriccount;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import spark.template.*;
import spark.ModelAndView;
import static spark.Spark.*;

public class App {
    
    static HashMap<String, String> valueMap = initMap();
    
    static final long coolDownSeconds = 1;
    static LocalDateTime lastRequestDateTime = LocalDateTime.now();
    
    static final String DEFAULT_LINK = "http://www.metrolyrics.com/watch-me-whip-nae-nae-lyrics-silento.html";
    static final String ERROR_LINK = "http://www.metrolyrics.com/never-gonna-give-you-up-lyrics-rick-astley.html";
    
    static final String BUTTON_TEXT = "Freq Me!";
    
    private static HashMap<String, String> initMap() {
        HashMap<String, String> map = new HashMap<>();

        map.put("title", "What's the frequency, Kenneth?");
        map.put("buttontext", BUTTON_TEXT);
        
        try {
            map.put("header", Resources.toString(Resources.getResource("html/header.html"), Charsets.UTF_8));
            map.put("footer", Resources.toString(Resources.getResource("html/footer.html"), Charsets.UTF_8));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return map;
    }
    
    public static void main(String[] args) {
        
        // Set the public directory for the app
        staticFileLocation("/public");
        
        // Set the port for heroku
        port(getHerokuAssignedPort());

        // Routes
        //
        // Index
        //
        get("/", (request, response) -> {
            // Preload the link
            valueMap.put("linkPreload", DEFAULT_LINK);
            return new ModelAndView(valueMap, "index.hbs");
        }, new HandlebarsTemplateEngine());

        // Song
        //
        get("/freq", (request, response) -> {
            
            // If we're inside the cooldown time, redirect to error
            if (LocalDateTime.now().minusSeconds(coolDownSeconds).isBefore(lastRequestDateTime)) {
                response.redirect("/error");
            } else {
                lastRequestDateTime = LocalDateTime.now();
            }
            
            // Follow the link and get the lyrics
            final String link = request.queryParams("link");
            final Pair<String, String> lyricsPair = LyricScraper.lyrics(link);
            final String lyricsTitle = lyricsPair.getLeft();
            final String lyrics = lyricsPair.getRight();
            
            // If there was a problem with the lyrics, redirect to error
            if (lyrics == null) {
                response.redirect("/error");
            }
            
            // Get the lyrics frequency
            final Map<String, Integer> freq = LyricScraper.freq(lyrics);

            // Turn to frequency into a table
            String freqRows = "";
            for (Entry<String, Integer> entry : freq.entrySet()) {
            	freqRows += "<tr><td>" + entry.getKey() + "</td><td>" + entry.getValue() + "</td></tr>";
            }

            final String freqCount = new Integer(freq.size()).toString();
            
            // Load the model map
            valueMap.put("lyricsTitle", lyricsTitle);
            valueMap.put("lyrics", lyrics);
            valueMap.put("freqCount", freqCount);
            valueMap.put("freqRows", freqRows);
            
            // Preload the link
            valueMap.put("linkPreload", link);
            
            return new ModelAndView(valueMap, "freq.hbs");
        }, new HandlebarsTemplateEngine());
        
        // Error
        //
        get("/error", (request, response) -> {
            // Preload the link
            valueMap.put("linkPreload", DEFAULT_LINK);
            return new ModelAndView(valueMap, "error.hbs");
        }, new HandlebarsTemplateEngine());
        
        // Something not found
        //
//        get("/*", (request, response) -> {
//            if(!request.pathInfo().startsWith("/assets")){
//                response.redirect("/error");
//                return null;
//            }         
//            return null;
//        });

    }
    
    // From Spark Heroku tutorial
    // https://sparktutorials.github.io/2015/08/24/spark-heroku.html
    static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 4567; //return default port if heroku-port isn't set (i.e. on localhost)
    }

}

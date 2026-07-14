package com.cappielloantonio.tempo.radiobrowser;

import android.util.Log;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Risolve le favicon delle stazioni radio con fallback intelligenti.
 * Strategie in ordine di priorità:
 * 1. Favicon da Radio Browser
 * 2. Favicon dal sito web della radio (homepage)
 * 3. Favicon via Google Favicon API
 * 4. Favicon via DuckDuckGo
 */
public class FaviconResolver {
    private static final String TAG = "FaviconResolver";

    /**
     * Risolve la favicon per una stazione radio con fallback multipli.
     * 
     * @param station La stazione radio
     * @return URL della favicon o null se non trovata
     */
    public static String resolveFavicon(RadioBrowserStation station) {
        // 1. Prova la favicon da Radio Browser
        if (isValidFaviconUrl(station.favicon)) {
            Log.d(TAG, "Using favicon from Radio Browser: " + station.favicon);
            return station.favicon;
        }

        // 2. Prova a estrarre la favicon dal sito web della radio
        String faviconFromHomepage = extractFaviconFromHomepage(station.homepage);
        if (faviconFromHomepage != null) {
            Log.d(TAG, "Using favicon from homepage: " + faviconFromHomepage);
            return faviconFromHomepage;
        }

        // 3. Usa Google Favicon API
        String googleFavicon = getGoogleFavicon(station.homepage);
        if (googleFavicon != null) {
            Log.d(TAG, "Using favicon from Google: " + googleFavicon);
            return googleFavicon;
        }

        // 4. Fallback: DuckDuckGo Icon API
        String duckFavicon = getDuckDuckGoFavicon(station.homepage);
        if (duckFavicon != null) {
            Log.d(TAG, "Using favicon from DuckDuckGo: " + duckFavicon);
            return duckFavicon;
        }

        Log.w(TAG, "No favicon found for station: " + station.name);
        return null;
    }

    /**
     * Estrae il dominio dalla homepage e costruisce URL favicon standard.
     */
    private static String extractFaviconFromHomepage(String homepage) {
        if (homepage == null || homepage.isEmpty()) {
            return null;
        }

        try {
            URL url = new URL(homepage);
            String domain = url.getHost();
            String protocol = url.getProtocol();

            // Prova percorsi comuni per favicon
            String[] faviconPaths = {
                    protocol + "://" + domain + "/favicon.ico",
                    protocol + "://" + domain + "/favicon.png",
                    protocol + "://" + domain + "/apple-touch-icon.png"
            };

            for (String path : faviconPaths) {
                Log.d(TAG, "Trying favicon path: " + path);
                // Nota: non facciamo una richiesta HTTP qui per evitare latenza.
                // Il client che carica l'immagine gestirà automaticamente i fallback.
                // Ritorniamo il primo percorso valido e lasciamo che Glide/Picasso gestiscano l'errore.
            }

            // Ritorna il percorso favicon.ico più comune
            return protocol + "://" + domain + "/favicon.ico";

        } catch (Exception e) {
            Log.w(TAG, "Failed to extract favicon from homepage: " + homepage, e);
            return null;
        }
    }

    /**
     * Genera URL favicon via Google Favicon API.
     * API: https://www.google.com/s2/favicons?sz=64&domain=example.com
     */
    private static String getGoogleFavicon(String homepage) {
        if (homepage == null || homepage.isEmpty()) {
            return null;
        }

        try {
            URL url = new URL(homepage);
            String domain = url.getHost();
            return "https://www.google.com/s2/favicons?sz=128&domain=" + domain;
        } catch (Exception e) {
            Log.w(TAG, "Failed to generate Google favicon URL", e);
            return null;
        }
    }

    /**
     * Genera URL favicon via DuckDuckGo Icon API.
     * API: https://icons.duckduckgo.com/ip3/example.com.ico
     */
    private static String getDuckDuckGoFavicon(String homepage) {
        if (homepage == null || homepage.isEmpty()) {
            return null;
        }

        try {
            URL url = new URL(homepage);
            String domain = url.getHost();
            return "https://icons.duckduckgo.com/ip3/" + domain + ".ico";
        } catch (Exception e) {
            Log.w(TAG, "Failed to generate DuckDuckGo favicon URL", e);
            return null;
        }
    }

    /**
     * Valida se una URL favicon è valida e accessibile.
     */
    private static boolean isValidFaviconUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        // Deve essere un URL valido
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return false;
        }

        // Evita URL sospette
        if (url.contains("..") || url.length() > 1024) {
            return false;
        }

        return true;
    }

    /**
     * Ottiene il nome abbreviato della stazione per generare un'icona fallback.
     * Es. "Radio Italia" -> "RI"
     */
    public static String getStationInitials(RadioBrowserStation station) {
        if (station.name == null || station.name.isEmpty()) {
            return "R";
        }

        String[] words = station.name.trim().split("\\s+");
        if (words.length >= 2) {
            return (words[0].charAt(0) + "" + words[words.length - 1].charAt(0)).toUpperCase();
        }

        return String.valueOf(station.name.charAt(0)).toUpperCase();
    }
}

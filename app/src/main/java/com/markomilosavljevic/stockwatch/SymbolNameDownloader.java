package com.markomilosavljevic.stockwatch;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class SymbolNameDownloader implements Runnable {

    private static final String TAG = "SymbolNameDownloader";
    private static final String SYMBOLS_URL = "https://api.iextrading.com/1.0/ref-data/symbols";
    public static HashMap<String, String> symbolNameMap = new HashMap<>();

    @Override
    public void run() {
        Uri dataUri = Uri.parse(SYMBOLS_URL);
        String urlToUse = dataUri.toString();

        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "run: HTTP ResponseCode NOT OK: " + conn.getResponseCode());
                return;
            }

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

        } catch (Exception e) {
            return;
        }

        process(sb.toString());

    }

    private void process(String s) {
        try {
            JSONArray jObjMain = new JSONArray(s);

            for (int i = 0; i < jObjMain.length(); i++) {
                JSONObject jStock = (JSONObject) jObjMain.get(i);

                String symbol = jStock.getString("symbol");
                String name = jStock.getString("name");

                symbolNameMap.put(symbol, name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static ArrayList<String> findMatches(String str) {
        String strToMatch = str.toLowerCase().trim();
        HashSet<String> matchSet = new HashSet<>();

        for (String sym : symbolNameMap.keySet()) {
            if (sym.toLowerCase().trim().contains(strToMatch)) {
                matchSet.add(sym + " - " + symbolNameMap.get(sym));
            }
            String name = symbolNameMap.get(sym);
            if (name != null &&
                    name.toLowerCase().trim().contains(strToMatch)) {
                matchSet.add(sym + " - " + name);
            }
        }

        ArrayList<String> results = new ArrayList<>(matchSet);
        Collections.sort(results);

        return results;
    }

}

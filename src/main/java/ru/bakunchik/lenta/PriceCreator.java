package ru.bakunchik.lenta;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public class PriceCreator {

    final Gson gson;

    public PriceCreator () {
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd-HH-mm-ss").create();
    }

    public Price createByJsonString (String jsonString) {

        return gson.fromJson(jsonString, Price.class);
    }

    public List<Price> createByJsonStrings (String [] jsonStrings) {
        ArrayList<Price> prices = new ArrayList<Price>();
        for(String jsonString : jsonStrings) {
            prices.add(createByJsonString(jsonString));
        }
        return prices;
    }
}

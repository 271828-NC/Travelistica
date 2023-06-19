package com.example.travelistica;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataParser {
    //Hash table based implementation of the Map interface.
    // This implementation provides all of the optional map operations
    //  and permits null values and the null key
    //in this map we store the details of one place
    private HashMap<String, String> getPlace(JSONObject googlePlaceJson)
    {
        HashMap<String, String> googlePlaceMap = new HashMap<>();
        String placeName = "--NA--";
        String vicinity= "--NA--";
        String latitude= "";
        String longitude="";
        String reference="";

        Log.d("DataParser","jsonobject ="+googlePlaceJson.toString());


        try {
            if (!googlePlaceJson.isNull("name")) {
                //if the object is not null we record the name
                placeName = googlePlaceJson.getString("name");
            }
            if (!googlePlaceJson.isNull("vicinity")) {
                //if the object is not null we record the vicinity
                vicinity = googlePlaceJson.getString("vicinity");
            }
            //record latitude
            latitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
           //record longitude
            longitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");

            reference = googlePlaceJson.getString("reference");
            //add the record to the hash map
            googlePlaceMap.put("place_name", placeName);
            googlePlaceMap.put("vicinity", vicinity);
            googlePlaceMap.put("lat", latitude);
            googlePlaceMap.put("lng", longitude);
            googlePlaceMap.put("reference", reference);


        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return googlePlaceMap;//return the hash map created

    }
    //list of hash maps in order to hold all the places data
    private List<HashMap<String, String>> getPlaces(JSONArray jsonArray)
    {
        int count = jsonArray.length();
        List<HashMap<String, String>> placelist = new ArrayList<>();//list of hash maps
        HashMap<String, String> placeMap = null;

        for(int i = 0; i<count;i++)
        {
            try {
                //get a place
                placeMap = getPlace((JSONObject) jsonArray.get(i));
                //add it to the list
                placelist.add(placeMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return placelist;//return the list of hash maps
    }

    public List<HashMap<String, String>> parse(String jsonData)
    {
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        Log.d("json data", jsonData);

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //the jsonArray will be sent to be recorded using the methods
        //written above
        return getPlaces(jsonArray);
    }
}



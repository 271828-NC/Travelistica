package com.example.travelistica;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

class GetNearbyPlacesData extends AsyncTask<Object, String, String> {

        private String googlePlacesData;
        private GoogleMap mMap;
        String url;

        @Override
        protected String doInBackground(Object... objects){
            mMap = (GoogleMap)objects[0];
            url = (String)objects[1];

            DownloadURL downloadURL = new DownloadURL();//New object of the class DownloadURL
            try {
                googlePlacesData = downloadURL.readUrl(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return googlePlacesData;
        }

        @Override
        protected void onPostExecute(String s){
            Log.d("showNearbyPlaces", "onPostExecute ");

            List<HashMap<String, String>> nearbyPlaceList;//create list of hash maps
            DataParser parser = new DataParser();//create parser object
            nearbyPlaceList = parser.parse(s);//convert the string
            Log.d("nearbyplacesdata","called parse method");
            showNearbyPlaces(nearbyPlaceList);//call the method that prints all locations
        }

        private void showNearbyPlaces(List<HashMap<String, String>> nearbyPlaceList)
        {   //this method will show all the places in the list
            //we will create marker options and add them to the map
            for(int i = 0; i < nearbyPlaceList.size(); i++)
            {
                MarkerOptions markerOptions = new MarkerOptions();
                HashMap<String, String> googlePlace = nearbyPlaceList.get(i);

                String placeName = googlePlace.get("place_name");//name of the place
                Log.d("showNearbyPlaces", "showNearbyPlaces: placeName - " + placeName);
                String vicinity = googlePlace.get("vicinity");//vicinity of the place
                double lat = Double.parseDouble( googlePlace.get("lat"));//latitude of the place
                double lng = Double.parseDouble( googlePlace.get("lng"));//longitude of the place

                LatLng latLng = new LatLng( lat, lng);
                markerOptions.position(latLng);//position of the marker
                markerOptions.title(placeName + " : "+ vicinity);//title of the marker
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

                mMap.addMarker(markerOptions);//add marker to map
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));//move camera to new position
                mMap.animateCamera(CameraUpdateFactory.zoomTo(mMap.getCameraPosition().zoom));//set the zoom level of the camera
            }
        }
}

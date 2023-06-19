package com.example.travelistica;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
/*
this class will receive data from url using http url connection
data returned from web will be in JSON format and we get this data
using http url connection

 */
public class DownloadURL {
    public String readUrl(String myUrl) throws IOException
    {
        String data = "";
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(myUrl);//create the url
            urlConnection=(HttpURLConnection) url.openConnection();//open connection
            urlConnection.connect();

            inputStream = urlConnection.getInputStream();//read data from url
            //read text from a character-input stream
            //buffering characters so as to provide for the efficient reading of characters
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer sb = new StringBuffer();

            String line = "";
            while((line = br.readLine()) != null)//while reading from buffer
            {
                sb.append(line);
            }

            data = sb.toString();//convert the string buffer to data string
            br.close();//close the buffer

        } catch (MalformedURLException e) {
            e.printStackTrace();//catching exceptions
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {//lines that need to be executed even in case of exception
            inputStream.close();
            urlConnection.disconnect();
        }
        Log.d("DownloadURL","Returning data= "+data);

        return data;
    }
}


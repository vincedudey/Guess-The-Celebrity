package com.example.guessthecelebrity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    ArrayList<String>imageLinks = new ArrayList<String>();
    ArrayList<String>celebrityNames = new ArrayList<String>();
    ArrayList<String>selectedCelebrities = new ArrayList<String>();
    int [] answers = new int[4];
    String answer;

    public void downloadImage(String url){
        ImageDownloader task = new ImageDownloader();
        Bitmap myImage;

        try{
            myImage = task.execute(url).get();
            imageView.setImageBitmap(myImage);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void guessCelebrity(View view){
        Button pressedButton = (Button)view;
        String guessedName = pressedButton.getText().toString();

        if(guessedName.equals(answer)){
            Toast.makeText(this, "Correct!", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(this, "Wrong, the answer was " + answer, Toast.LENGTH_LONG).show();
        }

        new CountDownTimer(1500, 1000){
            public void onTick(long millisecondsUntilDone){

            }

            public void onFinish(){
                updateState();
            }
        }.start();

    }

    public void updateState(){
        imageView = findViewById(R.id.imageView);
        Random random = new Random();
        int rand = random.nextInt(imageLinks.size());
        String celebrityName = celebrityNames.get(rand);
        String imageLink = imageLinks.get(rand);
        downloadImage(imageLink);
        answers[0] = rand;
        answer = celebrityName;
        selectedCelebrities.add(celebrityName);

        rand = random.nextInt(imageLinks.size());
        String celebrityTwo = celebrityNames.get(rand);
        while(selectedCelebrities.contains(celebrityTwo)){
            rand = random.nextInt(imageLinks.size());
            celebrityTwo = celebrityNames.get(rand);
        }
        answers[1] = rand;
        selectedCelebrities.add(celebrityTwo);

        rand = random.nextInt(imageLinks.size());
        String celebrityThree = celebrityNames.get(rand);
        while(selectedCelebrities.contains(celebrityThree)){
            rand = random.nextInt(imageLinks.size());
            celebrityThree = celebrityNames.get(rand);
        }
        answers[2] = rand;
        selectedCelebrities.add(celebrityThree);

        rand = random.nextInt(imageLinks.size());
        String celebrityFour = celebrityNames.get(rand);
        while(selectedCelebrities.contains(celebrityFour)){
            rand = random.nextInt(imageLinks.size());
            celebrityFour = celebrityNames.get(rand);
        }
        answers[3] = rand;
        shuffleCelebrityAnswers();

        Button b1 = findViewById(R.id.button1);
        b1.setText(celebrityNames.get(answers[0]));

        Button b2 = findViewById(R.id.button2);
        b2.setText(celebrityNames.get(answers[1]));

        Button b3 = findViewById(R.id.button3);
        b3.setText(celebrityNames.get(answers[2]));

        Button b4 = findViewById(R.id.button4);
        b4.setText(celebrityNames.get(answers[3]));
    }

    public void shuffleCelebrityAnswers(){
        Random random = new Random();
        for(int i=0; i<answers.length; ++i){
            int rand = random.nextInt(answers.length);
            int temp = answers[i];
            answers[i] = answers[rand];
            answers[rand] = temp;
        }
    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {
            try{
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.connect();
                InputStream in = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(in);
                return myBitmap;
            }
            catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try{
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection)url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while(data != -1){
                    char current = (char)data;
                    result+= current;
                    data = reader.read();
                }

                return result;
            }
            catch(Exception e){
                e.printStackTrace();
                return "Failed";
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DownloadTask task = new DownloadTask();
        String result = null;
        Pattern p1 = Pattern.compile("img src=\"(.*?)\" alt");
        Pattern p2 = Pattern.compile(" alt=\"(.*?)\"/>");

        try{
            result = task.execute("http://web.archive.org/web/20190426074814/http://www.posh24.se/kandisar").get();
        }
        catch(Exception e){
            e.printStackTrace();
        }

        //Log.i("Result", result);
        //System.out.println(result);

        //format of image: http://web.archive.org/web/20190426074814im_/http://cdn.posh24.se/images/:profile/1c1523163a2ff3db6086a44b4d295d6af
        //format of name: alt="Taylor Swift"/>

        Matcher m1 = p1.matcher(result);
        Matcher m2 = p2.matcher(result);



        while(m1.find()){
            if(m1.group(1).substring(0,4).equals("http")){
                imageLinks.add(m1.group(1));
            }
                //Log.i("Image", m1.group(1));
                //System.out.println(m1.group(1));
        }

        while(m2.find()){
            //System.out.println(m2.group(1));
            celebrityNames.add(m2.group(1));
        }

        updateState();
    }
}

package com.example.noddingdetection;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.StrictMath.abs;

public class MainActivity extends AppCompatActivity {
    Context context;
    File path;
    FaceDetector fd1;
    FaceDetector fd2;
    FaceDetector.Face[] faces1;
    FaceDetector.Face[] faces2;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    Bitmap bitmap1;
    Bitmap bitmap2;
    String currentPhotoPath;
    String photoPath1;
    String photoPath2;
    PointF point1;
    PointF point2;
    boolean pictureBool;
    boolean photoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button pictures = (Button) findViewById(R.id.Pictures);
        final TextView testText =(TextView)findViewById(R.id.test);
        final TextView badText = (TextView)findViewById(R.id.BadText);

        pictures.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(pictureBool == false){
                    dispatchTakePictureIntent();
                    pictureBool = true;
                    pictures.setText("Check Nodding!");
                    badText.setText("Press Check Nodding Button!");
                    testText.setText("");
                    return;
                }
                pictures.setText("Take Pictures!");
                pictureBool = false;

                bitmap1 = setPic(photoPath1, 1);
                fd1 = new FaceDetector(bitmap1.getWidth(), bitmap1.getHeight(), 2);
                faces1 =  new FaceDetector.Face[2];
                int z = fd1.findFaces(bitmap1,faces1);
                point1 = new PointF();
                point1.set(0,0);
                if(z != 0){
                    faces1[0].getMidPoint(point1);
                }
                else{
                    badText.setText("Picture one not recognized as a face. Make sure the camera takes can see both eyes!");
                    return;
                }
                bitmap2 = setPic(photoPath2,2);
                fd2 = new FaceDetector(bitmap2.getWidth(), bitmap2.getHeight(), 2);
                faces2 =  new FaceDetector.Face[2];
                int y = fd2.findFaces(bitmap2,faces2);
                point2 = new PointF();
                point2.set(0,0);
                if(y != 0){
                    faces2[0].getMidPoint(point2);
                }
                else{
                    badText.setText("Picture two not recognized as a face. Make sure the picture can see both eyes!");
                    return;
                }
                if(((abs((point1.x + point2.x))/2) >= point1.x +100) && abs(point1.y + point2.y)/2 >= point1.y+75){
                    badText.setText("Your nodding is ambiguous. Please retake photos.");
                }
                else if(((abs((point1.x + point2.x))/2) >= point1.x +100)){
                    badText.setText("Nodding NO");
                }
                else if(abs(point1.y + point2.y)/2 >= point1.y+75){
                    badText.setText("Nodding YES");
                }
                else{
                    badText.setText("Neither nodding Yes nor NO");
                }



            }
        });



    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        }
        takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        if(photoPath == false){
            photoPath1 = currentPhotoPath;
            photoPath = true;
        }
        else{
            photoPath2 = currentPhotoPath;
            photoPath = false;
        }
        return image;
    }
    private Bitmap setPic(String imagePhotoPath, int i) {
        final ImageView pictureFace1 = (ImageView) findViewById(R.id.PictureFace1);
        final ImageView pictureFace2 = (ImageView) findViewById(R.id.PictureFace2);
        // Get the dimensions of the View
        int targetW = 3600;
        int targetH = 1800;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inPreferredConfig=Bitmap.Config.RGB_565;
        bmOptions.inJustDecodeBounds = true;
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePhotoPath, bmOptions); //decodes and applies bmOptions
        Matrix matrix = new Matrix();
        matrix.postRotate(270);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 3600, 1800, true);

        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth()
                , scaledBitmap.getHeight(), matrix, true); //Rotates according to Matrix
        if(i == 1){
            pictureFace1.setImageBitmap(rotatedBitmap);
        }
        else{
            pictureFace2.setImageBitmap(rotatedBitmap);
        }

        return rotatedBitmap;
    }
}

package com.example.valutop.face_detection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int RQS_LOADIMAGE = 1;
    ImageView myImage;
    Button takePhoto;
    Button detect;
    Button load;
    TextView er;
    Bitmap myBitmap;
    private FaceOverlayView mFaceOverlayView;
    final float smilingAcceptProbability = 0.3f;
    float smilingProbability = 0, eulerZ = 0, eulerY = 0, height = 0;
    int id = 0;
    boolean areYouSmiling = false;

    //protected Bitmap b = new Bitmap();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myImage = (ImageView) findViewById(R.id.image);
        er = (TextView) findViewById(R.id.statusView);


        takePhoto = (Button) findViewById(R.id.takePhoto);
        detect = (Button) findViewById(R.id.detect);
        load = (Button) findViewById(R.id.LoadBtn);

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(i,20);

            }
        });


        //autocam
        // Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //startActivityForResult(i,20);
        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, RQS_LOADIMAGE);

            }
        });

        detect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myBitmap == null) {
                    Toast.makeText(MainActivity.this, "Load some PICK",
                            Toast.LENGTH_LONG).show();
                } else {

                    detectFace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RQS_LOADIMAGE
                && resultCode == RESULT_OK
                ) {
            if (myBitmap != null) {
                myBitmap.recycle();
            }
            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                myBitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                myImage.setImageBitmap(myBitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(requestCode == 20 && resultCode == RESULT_OK && data !=null){
            myBitmap= (Bitmap) data.getExtras().get("data");
            myImage.setImageBitmap(myBitmap);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void detectFace() {
        //Rectangle layout configuration
        Paint myRectPaint = new Paint();
        myRectPaint.setStrokeWidth(5);
        myRectPaint.setColor(Color.GREEN);
        myRectPaint.setStyle(Paint.Style.STROKE);

        //Landmark layout configuration
        Paint landmarksPaint = new Paint();
        landmarksPaint.setStrokeWidth(4);
        landmarksPaint.setColor(Color.RED);
        landmarksPaint.setStyle(Paint.Style.STROKE);

        //Smile layout configuration
        Paint smilingPaint = new Paint();
        smilingPaint.setStrokeWidth(4);
        smilingPaint.setColor(Color.YELLOW);
        smilingPaint.setStyle(Paint.Style.STROKE);

        Bitmap tempBitmap = Bitmap.createBitmap(myBitmap.getWidth(), myBitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas tempCanvas = new Canvas(tempBitmap);
        tempCanvas.drawBitmap(myBitmap, 0, 0, null);


        FaceDetector faceDetector =

                new FaceDetector.Builder(getApplicationContext())
                        .setTrackingEnabled(false)
                        .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                        .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                        .build();


        Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
        SparseArray<Face> faces = faceDetector.detect(frame);


        for (int i=0; i<faces.size();i++){
            Face thisFace=faces.valueAt(i);
            float x1 = thisFace.getPosition().x;
            float y1 = thisFace.getPosition().y;
            float x2 = x1 + thisFace.getWidth();
            float y2 = y1 + thisFace.getHeight();

            tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2), 2, 2, myRectPaint);

            List<Landmark> landmarks = thisFace.getLandmarks();
            for(int l=0; l<landmarks.size(); l++){
                PointF pos = landmarks.get(l).getPosition();
               // tempCanvas.drawPoint(pos.x, pos.y, landmarksPaint);
              tempCanvas.drawCircle(pos.x,pos.y,15,landmarksPaint);
            }
            //for(int i=0;)
            eulerZ = thisFace.getEulerZ();
            eulerY = thisFace.getEulerY();
            height = thisFace.getHeight();
            smilingProbability = Float.parseFloat(new DecimalFormat("#0.00").format(thisFace.getIsSmilingProbability()));
            id = thisFace.getId();
            if(smilingProbability>smilingAcceptProbability){
                tempCanvas.drawOval(new RectF(x1,y1,x2,y2),smilingPaint);
               // areYouSmiling=true;
            }
        }
        // Needs tp work  on multiple face ids and value with frame
        er.setText("Smile probability: "+smilingProbability+"/1, Rotation angle(Y&Z): "+eulerY+"&"+eulerZ+", Face height: "+height);
        myImage.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
//        if(areYouSmiling){
//                Toast.makeText(MainActivity.this,
//                    "Oh god.. you look so cute.. ..",
//                    Toast.LENGTH_LONG).show();
//                er.setText(smilingAcceptProbability+"");
//        }else{
//            Toast.makeText(MainActivity.this,
//                    "sometimes you need to smile",
//                    Toast.LENGTH_LONG).show();
//        }
    }
}







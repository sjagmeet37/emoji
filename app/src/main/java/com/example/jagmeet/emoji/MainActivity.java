package com.example.jagmeet.emoji;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    Button bt_go;
    TextView emoji;
    RelativeLayout rl;
    FloatingActionButton save,share,close;
    ImageView photo;
    Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bt_go = findViewById(R.id.btn);
        emoji = findViewById(R.id.emojitext);
        save = findViewById(R.id.save);
        share = findViewById(R.id.share);
        close = findViewById(R.id.cross);
        photo = findViewById(R.id.img);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(),"emoji_image");
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
        imageUri = Uri.fromFile(photo);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
           bt_go.setVisibility(View.GONE);
           emoji.setVisibility(View.GONE);
           save.setVisibility(View.VISIBLE);
           share.setVisibility(View.VISIBLE);
           close.setVisibility(View.VISIBLE);
           photo.setVisibility(View.VISIBLE);

           Bundle extras = data.getExtras();
        try {

        Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUri);
            photo.setImageBitmap(imageBitmap);
            detectFace(imageBitmap);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void camera(View view) {
        dispatchTakePictureIntent();
    }

    public void close(View view) {
        bt_go.setVisibility(View.VISIBLE);
        emoji.setVisibility(View.VISIBLE);
        save.setVisibility(View.INVISIBLE);
        share.setVisibility(View.INVISIBLE);
        close.setVisibility(View.INVISIBLE);
        photo.setVisibility(View.INVISIBLE);
    }

    public void detectFace(Bitmap image)
    {
        FaceDetector detector = new FaceDetector.Builder(MainActivity.this)
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();

        Frame frame = new Frame.Builder().setBitmap(image).build();

        SparseArray<Face> faces = detector.detect(frame);

        Toast.makeText(MainActivity.this,"number of faces"+faces.size(),Toast.LENGTH_LONG).show();

        Paint rect_paint = new Paint();
        rect_paint.setStrokeWidth(10);
        rect_paint.setColor(Color.BLACK);
        rect_paint.setStyle(Paint.Style.STROKE);

        Bitmap temporarybitmap = Bitmap.createBitmap(image.getWidth(),image.getHeight(),Bitmap.Config.RGB_565);

        Canvas canvas = new Canvas(temporarybitmap);
        canvas.drawBitmap(image,0,0,null);

        for (int i = 0; i < faces.size(); i++) {
            Face face = faces.valueAt(i);

            float left = face.getPosition().x;
            float top = face.getPosition().y;
            float right = left + face.getWidth();
            float bottom = top + face.getHeight();
            float cornerRadius = 2.0f;

            RectF rectF = new RectF(left, top, right, bottom);
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, rect_paint);


            for (Landmark landmark : face.getLandmarks()) {
                int cx = (int) (landmark.getPosition().x);
                int cy = (int) (landmark.getPosition().y);
                float radius = 10.0f;

                String type = String.valueOf(landmark.getType());
                rect_paint.setTextSize(50);
                canvas.drawText(type,cx,cy,rect_paint);


                Bitmap left_eye = BitmapFactory.decodeResource(getResources(), R.drawable.eyeball_clipartspooky);
                Bitmap right_eye = BitmapFactory.decodeResource(getResources(), R.drawable.eyeball_clipartspooky);

                canvas.drawCircle(cx, cy, radius, rect_paint);

                if (landmark.getType() == 4) {
                    System.out.println("hello here");

                    canvas.drawBitmap(left_eye, cx - 70, cy - 50, null);
                }

                if (landmark.getType() == 10) {
                    canvas.drawBitmap(right_eye, cx - 70, cy - 70, null);
                }
            }
        }

        photo.setImageDrawable(new BitmapDrawable(getResources(),temporarybitmap));
        detector.release();
    }


}

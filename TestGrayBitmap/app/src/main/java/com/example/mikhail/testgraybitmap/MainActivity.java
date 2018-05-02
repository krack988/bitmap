package com.example.mikhail.testgraybitmap;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private Bitmap bitmapIn;
    private Bitmap bitmapInTwo;
    private Bitmap bitmapThree;
    private Bitmap bitmapOut;
    private ImageView imageView;
    private int count = 786432;
    private Interpreter tflite;
    private String path = "lite_model_second.tflite";
    private float[] outputArray = new float[100];
    private float[] inputData = new float[count];
    private TextView textView;
    private int[] pixels ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imgTest);
        textView = findViewById(R.id.textResult);

        int crop = (1024-768)/2;
//        bitmapIn = Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.testimg),crop,0,224,224);
        //from res
        bitmapIn = cropBitmap(convertToGrayScale(BitmapFactory.decodeResource(getResources(), R.drawable.testimg)), 768, 1024);
        //from assets
        bitmapInTwo = bitmapFromAssets(this,"testimg.jpg");
//        imageView.setImageBitmap(bitmapIn);
        //corp bitmap
        bitmapThree = cropBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.testimg),224,224);

        imageView.setImageBitmap(bitmapIn);

        /** tflite test code*/
        try {
            tflite = new Interpreter(loadModelFile(MainActivity.this));
        }catch (IOException e){
            e.printStackTrace();
        }

        findViewById(R.id.btnStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                tflite.run(inputData, outputArray);
//                textView.setText(outputArray.toString());
//                pixels = new int[bitmapThree.getWidth() * bitmapThree.getHeight()];
//                bitmapThree.getPixels(pixels, 0, bitmapThree.getWidth(), 0, 0, bitmapThree.getWidth(), bitmapThree.getHeight());
////                for (int i:pixels)
//                Log.i("test", "output pixels: " + Arrays.toString(pixels) + 0.45);
                readFile();
            }
        });



    }

    private Bitmap convertToGrayScale(Bitmap bitmap){
        int width;
        int height;
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        bitmapOut = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapOut);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter colorMatrixColorFilter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(colorMatrixColorFilter);
        canvas.drawBitmap(bitmap,0, 0, paint);
        return bitmapOut;
    }

    private Bitmap bitmapFromAssets(Context context,String path){
        InputStream inputStream;
        Bitmap bitmap = null;
        try{
            inputStream = getAssets().open(path);
            bitmap = BitmapFactory.decodeStream(inputStream);

        }catch (IOException e){
            e.printStackTrace();
        }
        return bitmap;

    }

    public Bitmap cropBitmap(Bitmap bitmap, int width, int height){
        Bitmap resBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Matrix matrix = getMatrix(bitmap);
        Canvas canvas = new Canvas(resBitmap);
        canvas.drawBitmap(bitmap, matrix, null);
        return resBitmap;
    }

    public Matrix getMatrix(Bitmap bitmap){
        Matrix matrix = getTransformMatrix(bitmap.getWidth(), bitmap.getHeight(), 224,244,0, true);
        Matrix cropToFrameMatrix = new Matrix();
        matrix.invert(cropToFrameMatrix);
        return matrix;

    }

    public Matrix getTransformMatrix(final int srcWidth,
                                     final int srcHeight,
                                     final int dstWidth,
                                     final int dstHeight,
                                     final int applyRotation,
                                     final boolean maintainAspectRatio) {
        final Matrix matrix = new Matrix();

        if (applyRotation != 0) {
            if (applyRotation % 90 != 0) {
//                LOGGER.w("Rotation of %d % 90 != 0", applyRotation);
            }

            // Translate so center of image is at origin.
            matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f);

            // Rotate around origin.
            matrix.postRotate(applyRotation);
        }

        // Account for the already applied rotation, if any, and then determine how
        // much scaling is needed for each axis.
        final boolean transpose = (Math.abs(applyRotation) + 90) % 180 == 0;

        final int inWidth = transpose ? srcHeight : srcWidth;
        final int inHeight = transpose ? srcWidth : srcHeight;

        // Apply scaling if necessary.
        if (inWidth != dstWidth || inHeight != dstHeight) {
            final float scaleFactorX = dstWidth / (float) inWidth;
            final float scaleFactorY = dstHeight / (float) inHeight;

            if (maintainAspectRatio) {
                // Scale by minimum factor so that dst is filled completely while
                // maintaining the aspect ratio. Some image may fall off the edge.
                final float scaleFactor = Math.max(scaleFactorX, scaleFactorY);
                matrix.postScale(scaleFactor, scaleFactor);
            } else {
                // Scale exactly to fill dst from src.
                matrix.postScale(scaleFactorX, scaleFactorY);
            }
        }

        if (applyRotation != 0) {
            // Translate back from origin centered reference to destination frame.
            matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f);
        }

        return matrix;
    }

    public void readFile() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    openFileInput(Environment.getExternalStorageDirectory().toString()+"/inputImgText.txt")));
            String str = "";
            while ((str = br.readLine()) != null) {
                Log.d("test", str);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("test", "not found");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("test", "io excaption");
        }
    }


    /** Memory-map the model file in Assets. */
    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(path);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


}

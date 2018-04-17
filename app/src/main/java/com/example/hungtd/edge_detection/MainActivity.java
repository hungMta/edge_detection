package com.example.hungtd.edge_detection;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import org.opencv.utils.Converters;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "OPEN_CV";

    private ImageView mImgInput;
    private ImageView mImgOutputFour, mImgTwo, mImgOutputThree;
    private Context mContext;
    private Button btnCapture, btnPickImage;
    int REQUEST_CODE = 99;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS: {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "OpenCV Loaded Success!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.i(TAG, "OpenCV loaded successfully");


                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mImgInput = (ImageView) findViewById(R.id.img_input);
        mImgOutputFour = (ImageView) findViewById(R.id.img_output_four);
        mImgOutputThree = (ImageView) findViewById(R.id.img_output_three);
        mImgTwo = (ImageView) findViewById(R.id.img_two);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }

        mImgTwo.setOnClickListener(this);
        mImgInput.setOnClickListener(this);

//        mImgInput.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(intent, 1000);
//            }
//        });
//
//        btnCapture.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(intent, 1000);
//            }
//        });
//
//        btnPickImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(Intent.ACTION_PICK);
//                intent.setType("image/*");
//                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1500);
//            }
//        });
//
//        mImgTwo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });


        List<Integer> list1 = new ArrayList<>();
        list1.add(1);
        list1.add(5);
        list1.add(2);
        list1.add(3);

        List<Integer> list2 = new ArrayList<>();
        list2.add(3);
        list2.add(10);
        list2.add(7);
        list2.add(6);
        list2.add(4);
        list2.add(21);
        list2.add(8);
//
//        sort(list1, 0, list1.size() - 1);
//        sort(list2, 0, list2.size() - 1);
        List<Integer> list3 = new ArrayList<>();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_input:
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 1000);
                break;
            case R.id.img_two:
//                Intent intent2 = new Intent(Intent.ACTION_PICK);
//                intent2.setType("image/*");
//                startActivityForResult(Intent.createChooser(intent2, "Select Picture"), 1500);
                startActivityForResult(ImagePicker.getPickImageIntent(this, ImagePicker.CHOOSE.LIBRARY), 1500);
                break;
        }
    }


    private void ex() {
        Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
        Log.d(TAG, "mat.dump() = " + mat.dump());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            Bitmap image = (Bitmap) data.getExtras().get("data");

            mImgInput.setImageBitmap(image);
            mImgOutputFour.setImageBitmap(detectEdge(image));
        } else if (requestCode == 1500 && resultCode == RESULT_OK) {
            if (data == null) {

                return;
            }
            Uri imageUri = data.getData();
//                Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            Bitmap image = ImagePicker.getImageResized(this, imageUri);
            mImgInput.setImageBitmap(image);
            mImgOutputFour.setImageBitmap(detectEdge(image));
        }

    }

    private Bitmap detectEdge(Bitmap bitmap) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        //smooth image
        Mat matBlur = new Mat();
//        Imgproc.blur(mat, matBlur, new Size(3, 3));
        Mat edges = new Mat(mat.size(), CvType.CV_8UC1);
        Imgproc.cvtColor(mat, edges, Imgproc.COLOR_RGB2GRAY);

        Bitmap result02 = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(edges, result02);
        mImgTwo.setImageBitmap(result02);


//        Mat edges2 = new Mat(mat.size(), CvType.CV_8UC1);
//        Imgproc.cvtColor(mat, edges2, Imgproc.COLOR_RGB2GRAY);
//        Bitmap result4 = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(drawlinne(edges2), result4);
//        mImgInput.setImageBitmap(result4);
        Imgproc.equalizeHist(edges, edges);
//        Imgproc.Canny(edges, edges, 30, 200);
        Bitmap result003 = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(edges, result003);
        mImgOutputThree.setImageBitmap(result003);
        // detect corner

//        MatOfPoint mmm = new MatOfPoint();
//        Imgproc.goodFeaturesToTrack(edges, mmm, 10, 0.01, 50);
//        for (Point p: mmm.toList()){
//            Imgproc.circle(mat,p,2,new Scalar(0,255,0),5);
//        }
        // detect corner ^.^


//        Bitmap result03 = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(mat, result03);
//        if (1 == 1) {
//        mImgOutputThree.setImageBitmap(result03);
//            return result03;
//        }

//
        /// test houghline
        Mat line = new Mat();
        Imgproc.HoughLinesP(edges, line, 1, Math.PI / 180,
                50, 50, 10);

        for (int i = 0; i < line.cols(); i++) {
            double[] val = line.get(0, i);
            Imgproc.line(mat, new Point(val[0], val[1]), new Point(val[2], val[3]), new Scalar(0, 0, 255), 2);
        }


        Bitmap result12 = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, result12);

        if (1 == 1)
            return result12;
        //// test houghline ^.^

        Bitmap resultMD = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(edges, resultMD);
        mImgTwo.setImageBitmap(resultMD);

        List<MatOfPoint> contours2 = new ArrayList<MatOfPoint>();
        Mat hierachy2 = new Mat();
        Imgproc.findContours(edges, contours2, hierachy2, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);


        sort(contours2, 0, contours2.size() - 1);

        Mat re3 = new Mat();
        Utils.bitmapToMat(bitmap, re3);


        for (int idx = contours2.size() - 1; idx > 0; idx--) {
            List<MatOfPoint> mm = new ArrayList<>();
            mm.add(contours2.get(idx));
            Imgproc.drawContours(re3, mm, -1, new Scalar(0, 255, 0), 1);
        }


        Bitmap result3 = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(re3, result3);
        mImgOutputThree.setImageBitmap(result3);


//        MatOfPoint2f result2f2 = new MatOfPoint2f(contours2.get(i).toArray());
//        Point[] rotpoint2 = new Point[4];
//        RotatedRect rotrect2 = Imgproc.(result2f2);
//        rotrect2.points(rotpoint2);


        int i = 0;
        for (int idx = contours2.size() - 1; idx > 0; idx--) {
            MatOfPoint2f src = new MatOfPoint2f(contours2.get(idx).toArray());
            double prei = Imgproc.arcLength(src, true);
            MatOfPoint2f des = new MatOfPoint2f();
            Imgproc.approxPolyDP(src, des, 0.02 * prei, true);
            if (des.toArray().length == 4) {
                i = idx;
                break;
            }
        }
        Log.d(TAG, "INDEX = " + i);

        Mat re = new Mat();
        Utils.bitmapToMat(bitmap, re);

//        Imgproc.line(re, rotpoint2[0], rotpoint2[1], new Scalar(0, 255, 0), 10);
//        Imgproc.line(re, rotpoint2[1], rotpoint2[2], new Scalar(255, 255, 0), 10);
//        Imgproc.line(re, rotpoint2[2], rotpoint2[3], new Scalar(0, 255, 255), 10);
//        Imgproc.line(re, rotpoint2[3], rotpoint2[0], new Scalar(255, 0, 255), 10);

        List<MatOfPoint> mm = new ArrayList<>();
        mm.add(contours2.get(i));
        Imgproc.drawContours(re, mm, -1, new Scalar(0, 255, 0), 1);

        Bitmap result4 = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(re, result4);

        if (1 == 1)
            return result4;
        //afsf
//        Imgproc.Canny(edges, edges, 80, 100);
//        Imgproc.bilateralFilter(edges,edges,1,1,1);
//        Imgproc.threshold(edges,edges,1,Imgproc.THRESH_BINARY,1);
//        Imgproc.threshold(edges, edges, 130, 190, Imgproc.THRESH_BINARY);
//        Imgproc.adaptiveThreshold(edges,edges,255,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY,11,2);

        /////////////////////////////////////////////////
        Mat matThreshold = edges.clone();

        Imgproc.threshold(edges, matThreshold, 130, 190, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

        Mat elemente = Imgproc.getStructuringElement(Imgproc.MORPH_ERODE, new Size(2, 2));
        Imgproc.erode(matThreshold, matThreshold, elemente, new Point(-1, -1), 2);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierachy = new Mat();
        Imgproc.findContours(matThreshold, contours, hierachy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        double maxArea = -1;
        int maxAreaIdx = -1;
        for (int idx = 0; idx < contours.size(); idx++) {
            double contourarea = Imgproc.contourArea(contours.get(idx));
            if (contourarea > maxArea) {
                maxArea = contourarea;
                maxAreaIdx = idx;
            }
        }

        MatOfPoint2f boundrect = new MatOfPoint2f(contours.get(maxAreaIdx).toArray());
        Point[] rotpoint = new Point[4];
        RotatedRect rotrect = Imgproc.minAreaRect(boundrect);
        rotrect.points(rotpoint);

        int hei = edges.height();
        int wid = edges.width();

        // draw rect
        Mat des = new Mat();
        Utils.bitmapToMat(bitmap, des);
        Imgproc.line(des, rotpoint[0], rotpoint[1], new Scalar(0, 255, 0), 10);
        Imgproc.line(des, rotpoint[1], rotpoint[2], new Scalar(255, 255, 0), 10);
        Imgproc.line(des, rotpoint[2], rotpoint[3], new Scalar(0, 255, 255), 10);
        Imgproc.line(des, rotpoint[3], rotpoint[0], new Scalar(255, 0, 255), 10);

        Bitmap result2 = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(des, result2);

        if (1 == 1)
            return result2;

        List<Point> mbegin = new ArrayList<Point>();
        if (rotpoint[0].x < (wid / 2)) {
            mbegin.add(rotpoint[1]);
            mbegin.add(rotpoint[0]);
            mbegin.add(rotpoint[3]);
            mbegin.add(rotpoint[2]);
        } else {
            mbegin.add(rotpoint[2]);
            mbegin.add(rotpoint[1]);
            mbegin.add(rotpoint[0]);
            mbegin.add(rotpoint[3]);
        }
        Mat beginM = Converters.vector_Point2f_to_Mat(mbegin);
        Mat output = new Mat(wid, hei, CvType.CV_8U);
        Point pOut1 = new Point(0, 0);
        Point pOut2 = new Point(0, hei);
        Point pOut3 = new Point(wid, hei);
        Point pOut4 = new Point(wid, 0);
        List<Point> mend = new ArrayList<Point>();
        mend.add(pOut1);
        mend.add(pOut2);
        mend.add(pOut3);
        mend.add(pOut4);
        Mat endM = Converters.vector_Point2f_to_Mat(mend);
        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(beginM, endM);
        Imgproc.warpPerspective(mat, output, perspectiveTransform, new Size(wid, hei), Imgproc.INTER_NEAREST, 1, new Scalar(255, 255, 255));

//        draw contours
//        Mat mat1 = new Mat(edges.rows(),edges.cols(),CvType.CV_8UC3);
//        contours.get(maxAreaIdx).toList();
//        List<MatOfPoint> list = new ArrayList<>();
//        list.add(contours.get(maxAreaIdx));
//        Imgproc.drawContours(mat1, list, -1, new Scalar(0,255,0), 2);

        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(output, result);

        return result;
    }

    public Bitmap getThumbnail(Uri uri) throws FileNotFoundException, IOException {
        InputStream input = this.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();

        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1)) {
            return null;
        }

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

//        double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
//        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither = true; //optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//
        input = this.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio) {
        int k = Integer.highestOneBit((int) Math.floor(ratio));
        if (k == 0) return 1;
        else return k;
    }


    private Mat drawlinne(Mat mat) {


        Mat matThreshold = mat.clone();

        Imgproc.threshold(mat, matThreshold, 130, 190, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

        Mat elemente = Imgproc.getStructuringElement(Imgproc.MORPH_ERODE, new Size(2, 2));
        Imgproc.erode(matThreshold, matThreshold, elemente, new Point(-1, -1), 2);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierachy = new Mat();
        Imgproc.findContours(matThreshold, contours, hierachy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        double maxArea = -1;
        int maxAreaIdx = -1;
        for (int idx = 0; idx < contours.size(); idx++) {
            double contourarea = Imgproc.contourArea(contours.get(idx));
            if (contourarea > maxArea) {
                maxArea = contourarea;
                maxAreaIdx = idx;
            }
        }

        MatOfPoint2f boundrect = new MatOfPoint2f(contours.get(maxAreaIdx).toArray());
        Point[] rotpoint = new Point[4];
        RotatedRect rotrect = Imgproc.minAreaRect(boundrect);
        rotrect.points(rotpoint);

        int hei = mat.height();
        int wid = mat.width();

        // draw rect
//        Mat des = new Mat();
//        Utils.bitmapToMat(bitmap, des);
        Imgproc.line(mat, rotpoint[0], rotpoint[1], new Scalar(0, 255, 0), 10);
        Imgproc.line(mat, rotpoint[1], rotpoint[2], new Scalar(255, 255, 0), 10);
        Imgproc.line(mat, rotpoint[2], rotpoint[3], new Scalar(0, 255, 255), 10);
        Imgproc.line(mat, rotpoint[3], rotpoint[0], new Scalar(255, 0, 255), 10);

        return mat;
    }

    private void quickSort(List<Integer> list, int low, int high) {
        int i = low, j = high;
        int pivot = list.get((low + high) / 2);
        // Divide into two lists
        while (i <= j) {
            while (list.get(i) < pivot) {
                i++;
            }
            while (list.get(j) > pivot) {
                j--;
            }
            if (i <= j) {
                exchange(list, i, j);
                i++;
                j--;
            }
        }
        // Recursion
        if (low < j)
            quickSort(list, low, j);
        if (i < high)
            quickSort(list, i, high);
    }

    private void exchange(List<Integer> list, int i, int j) {
        int temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }

    public static void sort(List<MatOfPoint> list, int from, int to) {
        if (from < to) {
            int pivot = from;
            int left = from + 1;
            int right = to;
            double pivotValue = Imgproc.contourArea(list.get(pivot));
            while (left <= right) {
                // left <= to -> limit protection
                while (left <= to && pivotValue >= Imgproc.contourArea(list.get(left))) {
                    left++;
                }
                // right > from -> limit protection
                while (right > from && pivotValue < Imgproc.contourArea(list.get(right))) {
                    right--;
                }
                if (left < right) {
                    Collections.swap(list, left, right);
                }
            }
            Collections.swap(list, pivot, left - 1);
            sort(list, from, right - 1); // <-- pivot was wrong!
            sort(list, right + 1, to);   // <-- pivot was wrong!
        }
    }


}



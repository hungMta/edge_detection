package com.example.hungtd.edge_detection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Chọn ảnh
 * Created by tungts on 26-Jun-17.
 */
public class ImagePicker {

    private static final int DEFAULT_MIN_WIDTH_QUALITY = 512;        // min pixels
    private static final String TAG = "ImagePicker";
    public static final String TEMP_IMAGE_NAME = "tempImage.png";

    private static int minWidthQuality = DEFAULT_MIN_WIDTH_QUALITY;

    private static Uri uriImage;

    public static Intent getPickImageIntent(Context context, CHOOSE chooseCamera) {
        Intent chooserIntent = null;

        List<Intent> intentList = new ArrayList<>();

        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotoIntent.putExtra("return-data", true);
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile(context)));
        if (chooseCamera == CHOOSE.CAMERA) {
//            intentList = addIntentsToList(context, intentList, takePhotoIntent);
            /* Chụp ảnh luôn không cần chọn phần mềm chụp ảnh */
            return takePhotoIntent;
        } else if (chooseCamera == CHOOSE.LIBRARY) {
            intentList = addIntentsToList(context, intentList, pickIntent);
        } else if (chooseCamera == CHOOSE.ALL) {
            intentList = addIntentsToList(context, intentList, pickIntent);
            intentList = addIntentsToList(context, intentList, takePhotoIntent);
        }

        if (intentList.size() > 0) {
            chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1),
                    "Choose Image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
        }

        return chooserIntent;
    }

    private static List<Intent> addIntentsToList(Context context, List<Intent> list,
                                                 Intent intent) {
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            list.add(targetedIntent);
            Log.e(TAG, "Intent ImagePicker: " + intent.getAction() + " package: " + packageName);
        }
        return list;
    }

    public static Bitmap getImageFromResult(Context context, int resultCode,
                                            Intent imageReturnedIntent) {
        uriImage = null;
        Bitmap bm = null;
        File imageFile = getTempFile(context);
        if (resultCode == Activity.RESULT_OK) {
            boolean isCamera = (imageReturnedIntent == null
                    || imageReturnedIntent.getData() == null
                    || imageReturnedIntent.getData().toString().contains(imageFile.toString()));
            if (isCamera) {     /* CAMERA */
                try {
                    uriImage = Uri.fromFile(imageFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {            /* ALBUM */
                uriImage = imageReturnedIntent.getData();
            }
            Log.e(TAG, "uriImage: " + uriImage);

            try {
                bm = getImageResized(context, uriImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
            int rotation = getRotation(context, uriImage, isCamera);
            bm = rotate(bm, rotation);
            /* Xoay file ảnh đã chụp */
//            saveBitmapToFile(bm, getTempFile(context));
        }

//        imageFile.delete();
        return bm;
    }

    /* Lưu bitmap vào file */
    private static void storeImage(Bitmap image, File pictureFile) {
        if (pictureFile == null) {
            Log.e(TAG, "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    /* Luu hinh anh bitmap */
    public static void saveBitmapToFile(Bitmap bm, File file) {
        if (bm == null) return;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



//    public static long getByteImage(Bitmap bitmap) {
//        if (bitmap == null) return 0;
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//        byte[] imageInByte = stream.toByteArray();
//        Log.e("getByteImage: ", imageInByte.length + " -------------- ");
//        return imageInByte.length;
//    }

    /* Lấy Uri */
    public static Uri getUri(Context context, int resultCode, Intent
            imageReturnedIntent) {
        getImageFromResult(context, resultCode, imageReturnedIntent);
        return uriImage;
    }

    /* Lấy file ảnh */
    private static File getTempFile(Context context) {
        File imageFile = new File(context.getExternalCacheDir(), TEMP_IMAGE_NAME);
        imageFile.getParentFile().mkdirs();
        return imageFile;

    }

    /* Xóa ảnh */
    public static void deleteFile(Context context) {
        File imageFile = getTempFile(context);
        if (imageFile != null) {
            imageFile.delete();
        }
    }

    private static Bitmap decodeBitmap(Context context, Uri theUri) {
        AssetFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = context.getContentResolver().openAssetFileDescriptor(theUri, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Bitmap actuallyUsableBitmap = null;
        if (fileDescriptor != null) {
            actuallyUsableBitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor());
        }

        return actuallyUsableBitmap;
    }

    /**
     * Resize to avoid using too much memory loading big images (e.g.: 2560*1920)
     **/
    public static Bitmap getImageResized(Context context, Uri selectedImage) {
        Bitmap bm = null;
        bm = decodeBitmap(context,selectedImage);
        if (bm == null) {
            return null;
        }
        Log.e(TAG, "getImageResizedAfter: " + bm.getByteCount());

        // recreate the new bitmap
//        return getImageResized(bm);
        return bm;
    }

    /* Chỉnh chiều rộng, cao theo kích thước yêu cầu */
    public static Bitmap getImageResized(Bitmap bm) {

        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) DEFAULT_MIN_WIDTH_QUALITY) / width;
        float scaleHeight = ((float) DEFAULT_MIN_WIDTH_QUALITY) / height;
        Matrix matrix = new Matrix();
        // resize the bitmap
        if (width < height) {
            matrix.postScale(scaleWidth, scaleWidth);
        } else {
            matrix.postScale(scaleHeight, scaleHeight);
        }

        // recreate the new bitmap
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
    }


    private static int getRotation(Context context, Uri imageUri, boolean isCamera) {
        int rotation;
        if (isCamera) {
            rotation = getRotationFromCamera(context, imageUri);
        } else {
            rotation = getRotationFromGallery(context, imageUri);
        }
        Log.e(TAG, "Image rotation: " + rotation);
        return rotation;
    }

    private static int getRotationFromCamera(Context context, Uri imageFile) {
        int rotate = 0;
        try {
            context.getContentResolver().notifyChange(imageFile, null);
            ExifInterface exif = new ExifInterface(imageFile.getPath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public static int getRotationFromGallery(Context context, Uri imageUri) {
        int result = 0;
        String[] columns = {MediaStore.Images.Media.ORIENTATION};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(imageUri, columns, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int orientationColumnIndex = cursor.getColumnIndex(columns[0]);
                result = cursor.getInt(orientationColumnIndex);
            }
        } catch (Exception e) {
            //Do nothing
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }//End of try-catch block
        return result;
    }

    public static Bitmap rotate(Bitmap bm, int rotation) {
        if (bm == null) return null;
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        }
        return bm;
    }

    private static int MinWH(int a, int b) {
        if (a > b) return b;
        else return a;
    }

    public enum CHOOSE {

        CAMERA(1), LIBRARY(2), ALL(3);
        int id;

        CHOOSE(int id) {
            this.id = id;
        }
    }

}
package com.developersmarket.aspect_photo_edit;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.github.dhaval2404.colorpicker.ColorPickerDialog;
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog;
import com.github.dhaval2404.colorpicker.listener.ColorListener;
import com.github.dhaval2404.colorpicker.model.ColorShape;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private ImageView id_imageview;
    private ImageView id_aspect_ratio;
    private ImageView id_download;
    private ImageView id_share;
    private ImageView id_color_picker;
    private ImageView id_transparent;
    private String selectedAspectRatioOption = "";
    private String selected_color="#ffffff";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        id_imageview = findViewById(R.id.id_imageview);
        id_aspect_ratio = findViewById(R.id.id_aspect_ratio);
        id_download = findViewById(R.id.id_download);
        id_share = findViewById(R.id.id_share);
        id_color_picker = findViewById(R.id.id_color_picker);
        id_transparent = findViewById(R.id.id_transparent);

        String imageurl = "https://images.unsplash.com/photo-1683997941376-d5bbecbcdae7?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=687&q=80";
        Glide.with(this).load(imageurl).into(id_imageview);

        id_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadImageInternal();
            }
        });

        id_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareImageInternal();
            }
        });
        String[] colorArray = new String[]{
                "#ffffff",
                "#f6e58d",
                "#ffbe76",
                "#ff7979",
                "#badc58",
                "#dff9fb",
                "#7ed6df",
                "#e056fd",
                "#686de0",
                "#30336b",
                "#95afc0",
                "#ff0000", // Additional colors
                "#00ff00",
                "#0000ff",
                "#ffff00",
                "#00ffff",
                "#ff00ff",
                "#800000",
                "#008000",
                "#000080",
                "#808000",
                "#800080",
                "#008080",
        };

        id_transparent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected_color="#00000000";
                setBackgroundColor(selected_color);
            }
        });
        id_color_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new MaterialColorPickerDialog
                        .Builder(MainActivity.this)

                        .setColors(colorArray)

                        .setColorListener(new ColorListener() {
                            @Override
                            public void onColorSelected(int i, @NonNull String s) {
                                selected_color=s;
                                setBackgroundColor(selected_color);
                            }
                        })
    .show();
            }
        });

        checkPermissions();

        id_aspect_ratio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetForAspectRatio();
            }
        });
    }

    private void downloadImageInternal() {
        Bitmap bitmap = getBitmapFromView(id_imageview);
        String fileName = generateFileName(selectedAspectRatioOption);

        saveBitmapToFile(bitmap, fileName);
    }
    private void shareImageInternal() {
        Bitmap bitmap = getBitmapFromView(id_imageview);
        String fileName = generateFileName(selectedAspectRatioOption);

        File imageFile = saveBitmapToFileForShare(bitmap, fileName);
        if (imageFile != null) {
            shareImage(imageFile);
        }
    }


    private void shareImage(File imageFile) {
        Uri imageUri = FileProvider.getUriForFile(
                this,
                getApplicationContext().getPackageName() + ".fileprovider",  // replace with your file provider authority
                imageFile);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/jpeg");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share images..."));
    }



    private Bitmap getBitmapFromView(ImageView view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Set the canvas background color
        canvas.drawColor(Color.parseColor(selected_color));

        view.draw(canvas);
        return bitmap;
    }

    private String generateFileName(String aspectRatioOption) {
        String randomName = "image_" + System.currentTimeMillis() + "_";
        return aspectRatioOption + randomName + ".png";
    }
    private File saveBitmapToFileForShare(Bitmap bitmap, String fileName) {
        // Create the AspectRatioApp directory inside the Download directory if it doesn't exist
        File appDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "AspectRatioApp");
        if (!appDirectory.exists()) {
            appDirectory.mkdir();
        }

        // Save the Bitmap to a file inside the AspectRatioApp directory
        File imageFile = new File(appDirectory, fileName);
        try {
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            // Trigger media scan for the saved image file
            MediaScannerConnection.scanFile(this, new String[]{imageFile.getAbsolutePath()}, null, null);
            return imageFile;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void saveBitmapToFile(Bitmap bitmap, String fileName) {
        // Create the AspectRatioApp directory inside the Download directory if it doesn't exist
        File appDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "AspectRatioApp");
        if (!appDirectory.exists()) {
            appDirectory.mkdir();
        }

        // Save the Bitmap to a file inside the AspectRatioApp directory
        File imageFile = new File(appDirectory, fileName);
        try {
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            // Trigger media scan for the saved image file
            MediaScannerConnection.scanFile(this, new String[]{imageFile.getAbsolutePath()}, null, null);
            onImageDownloaded(true);
        } catch (IOException e) {
            e.printStackTrace();
            onImageDownloaded(false);
        }
    }

    private void onImageDownloaded(final boolean success) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (success) {
                    Toast.makeText(MainActivity.this, "Image downloaded successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to download image", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkPermissions() {
        PermissionX.init(this).permissions(Manifest.permission.MANAGE_EXTERNAL_STORAGE).onExplainRequestReason((scope, deniedList) -> {
            scope.showRequestReasonDialog(deniedList, "Core fundamental are based on these permissions", "OK", "Cancel");
        }).onForwardToSettings((scope, deniedList) -> {
            scope.showForwardToSettingsDialog(deniedList, "You need to allow necessary permissions in Settings manually", "OK", "Cancel");
        }).request(new RequestCallback() {
            @Override
            public void onResult(boolean allGranted, List<String> grantedList, List<String> deniedList) {
                if (!allGranted) {
                    Toast.makeText(MainActivity.this, "These permissions are denied: " + deniedList, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showBottomSheetForAspectRatio() {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet, null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();

        setupBottomSheetOptions(bottomSheetDialog, bottomSheetView);
    }

    private void setupBottomSheetOptions(BottomSheetDialog bottomSheetDialog, View bottomSheetView) {
        TextView option_facebook_1_1 = bottomSheetView.findViewById(R.id.option_facebook_1_1);
        TextView option_facebook_4_5 = bottomSheetView.findViewById(R.id.option_facebook_4_5);
        TextView option_instagram_story_9_16 = bottomSheetView.findViewById(R.id.option_instagram_story_9_16);
        TextView option_instagram_1_1 = bottomSheetView.findViewById(R.id.option_instagram_1_1);
        TextView option_twitter_16_9 = bottomSheetView.findViewById(R.id.option_twitter_16_9);

        option_facebook_1_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAspectRatio(1080, 1080, "facebook_1_1");
                bottomSheetDialog.dismiss();
            }
        });

        option_facebook_4_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAspectRatio(1200, 1500, "facebook_4_5");
                bottomSheetDialog.dismiss();
            }
        });

        option_instagram_story_9_16.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAspectRatio(1080, 1920, "instagram_story_9_16");
                bottomSheetDialog.dismiss();
            }
        });

        option_twitter_16_9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAspectRatio(1200, 675, "twitter_16_9");
                bottomSheetDialog.dismiss();
            }
        });

        option_instagram_1_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAspectRatio(1080, 1080, "instagram_1_1");
                bottomSheetDialog.dismiss();
            }
        });
    }
    private void setBackgroundColor(String color) {
        id_imageview.setBackgroundColor(Color.parseColor(color));
    }

    private void setAspectRatio(int width, int height, String option) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) id_imageview.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        id_imageview.setLayoutParams(layoutParams);
//        id_imageview.setBackgroundResource(R.color.white);
        setBackgroundColor(selected_color); // Set the selected color as the canvas background
        selectedAspectRatioOption = option;
    }
}

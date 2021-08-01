package com.example.imageeditor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import static android.net.Uri.fromFile;

public class MainEdit extends AppCompatActivity {
    public ImageView imageView;
    private final int CODE_IMG = 1;
    private final String CROPPED_IMAGE = UUID.randomUUID().toString();
    Uri imageUri;
    BottomNavigationView bottomnavigationmain;
    private ImageButton crop, flip, rotate, btnSave;
    ImageButton  info;
    Bitmap bitmap;
    boolean xFl = true;
    boolean yFl =true;
    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_edit);
        /*info = (ImageButton) findViewById(R.id.info);*/



        init();

        if (getIntent().getExtras() != null) {
            imageUri = Uri.parse(getIntent().getStringExtra("data"));
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                int nh = (int) (bitmap.getHeight() * (1024.0 / bitmap.getWidth()));
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 1024, nh, true);
                imageView.setImageBitmap(scaled);
                File file =new File( imageUri.getPath());

                int file_size = Integer.parseInt(String.valueOf(file.length()/1024));

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Name :  "+file.getName());
                stringBuilder.append("\n");
                stringBuilder.append("Path :  "+file.getAbsolutePath());
                stringBuilder.append("\n");
                textView.setText( stringBuilder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
        crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCrop(imageUri);
            }
        });

        flip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    imageView.setImageBitmap(createFlippedBitmap(bitmap,xFl,yFl));
                    xFl = !xFl;
                    yFl = !yFl;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Matrix matrix = new Matrix();
                imageView.setScaleType(ImageView.ScaleType.MATRIX);   //required
                matrix.postRotate((float) angle, pivotX, pivotY);*/

                imageView.setRotation(imageView.getRotation() + 90);
            }
        });

      /*  info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivity(i);
                // ImageButton info = ()
            }
        });*/

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveToInternalStorage(bitmap);
            }
        });

        /*imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent().setAction(Intent.ACTION_GET_CONTENT).setType("image/*"), CODE_IMG);
            }
        });*/
    }

    private void init() {
        this.imageView = findViewById(R.id.imageview);
        crop = (ImageButton) findViewById(R.id.btn_crop);
        flip = (ImageButton) findViewById(R.id.btn_flip);
        rotate = (ImageButton) findViewById(R.id.btn_rotate);
        btnSave = (ImageButton)findViewById(R.id.btn_save);
        textView =findViewById(R.id.image_data);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_IMG && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                startCrop(imageUri);
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            Uri imageUriResultCrop = UCrop.getOutput(data);
            if (imageUriResultCrop != null) {
                imageUri = imageUriResultCrop;
                imageView.setImageURI(imageUri);

            }
        }
    }

    private void startCrop(@NonNull Uri uri) {
        String destinationFileName = UUID.randomUUID().toString();
        destinationFileName += ".jpg";
        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destinationFileName)));
        uCrop.withAspectRatio(1, 1);
       /* uCrop.withAspectRatio(3,4);
        uCrop.useSourceImageAspectRatio();
        uCrop.withAspectRatio(2,3);
        uCrop.withAspectRatio(16,9);*/
        uCrop.withMaxResultSize(450, 450);
        uCrop.withOptions(getCropOptions());
        uCrop.start(MainEdit.this);
    }

    private UCrop.Options getCropOptions() {
        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(70);
        //Compress Type
       /* options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
*/
        //UI
        options.setHideBottomControls(false);
        options.setFreeStyleCropEnabled(true);
        //colors
        options.setStatusBarColor(getResources().getColor(R.color.design_default_color_primary_dark));
        options.setToolbarColor(getResources().getColor(R.color.design_default_color_primary));
        options.setToolbarTitle("Crop");
        return options;
    }

    public static Bitmap createFlippedBitmap(Bitmap source, boolean xFlip, boolean yFlip) {

        Matrix matrix = new Matrix();
        matrix.postScale(xFlip ? -1 : 1, yFlip ? -1 : 1, source.getWidth() / 2f, source.getHeight() / 2f);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }


    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            Toast.makeText(this, "Saved to"+directory.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

}

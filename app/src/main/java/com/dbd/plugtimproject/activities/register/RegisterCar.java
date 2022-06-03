package com.dbd.plugtimproject.activities.register;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dbd.plugtimproject.R;
import com.dbd.plugtimproject.activities.AddStation;
import com.dbd.plugtimproject.ml.ModelUnquant;
import com.dbd.plugtimproject.models.Car;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class RegisterCar extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RegisterCar";

    private TextView regCarSkipBtn;
    private Button regCarFinishBtn;

    private EditText regCarYear;
    private Spinner regCarCompany, regCarModel, regCarColor;

    private DatabaseReference mDatabase;

    private Button addCarImageBtn;
    private ImageView carImage;

    private Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    ArrayAdapter<String> companyAdapter, modelAdapter, colorAdapter;

    int imageSize = 224;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_car);

        regCarSkipBtn = findViewById(R.id.regCarSkipBtn);
        regCarSkipBtn.setOnClickListener(this);
        regCarFinishBtn = findViewById(R.id.regCarFinishBtn);
        regCarFinishBtn.setOnClickListener(this);

        regCarCompany = findViewById(R.id.regCarCompany);
        regCarModel = findViewById(R.id.regCarModel);
        regCarColor = findViewById(R.id.regCarColor);
        regCarYear = findViewById(R.id.regCarYear);

        // add car image
        addCarImageBtn = findViewById(R.id.addCarImageBtn);
        addCarImageBtn.setOnClickListener(this);
        carImage = findViewById(R.id.carImage);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        initializeSpinners();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.regCarSkipBtn:
                startActivity(new Intent(getApplicationContext(), Login.class));
                break;
            case R.id.regCarFinishBtn:
                Intent intent = getIntent();
                String email = intent.getStringExtra("email");
                if (registerCar(email)) {
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    if (imageUri != null) {
                        uploadPicture(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    }
                }
                break;
            case R.id.addCarImageBtn:
                addCarImage();
                break;
        }
    }

    private boolean registerCar(String email) {
        String company = regCarCompany.getSelectedItem().toString();
        String model = regCarModel.getSelectedItem().toString();
        String color = regCarColor.getSelectedItem().toString();
        String year = regCarYear.getText().toString();

        if (year.isEmpty()) {
            regCarYear.setError("Year is required");
            regCarYear.requestFocus();
            return false;
        } else if (Integer.parseInt(year) < 1885 || Integer.parseInt(year) > Calendar.getInstance().get(Calendar.YEAR)) {
            regCarYear.setError("Please enter a valid year");
            regCarYear.requestFocus();
            return false;
        }

        mDatabase = FirebaseDatabase.getInstance("https://plugtimproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

        Car car = new Car(company, model, color, Integer.parseInt(year), email);
        mDatabase.child("cars").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(car).addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                Toast.makeText(RegisterCar.this, "Car has been added successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RegisterCar.this, "Failed to add your car. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });

        return true;
    }

    private void addCarImage() {
        // intent to open gallery from phone
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                int dimension = Math.min(bitmap.getWidth(), bitmap.getHeight());
                Bitmap image = ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension);

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);

                if (checkImage(image)) {
                    carImage.setImageURI(imageUri);
                } else {
                    Toast.makeText(RegisterCar.this, "It doesn't look like a car. Please take another picture from the front of the car", Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void uploadPicture(String uuid) {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Uploading image");
        pd.show();

        StorageReference stationReference = storageReference.child("images/cars/" + uuid);
        stationReference.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {


                    @Override
                    public void onSuccess(ListResult listResult) {


                        stationReference.putFile(imageUri)
                                .addOnSuccessListener(taskSnapshot -> pd.dismiss())
                                .addOnFailureListener(e -> {
                                    pd.dismiss();
                                    Toast.makeText(RegisterCar.this, "Couldn't load image", Toast.LENGTH_SHORT).show();
                                })
                                .addOnProgressListener(snapshot -> {
                                    double progress = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                                    pd.setMessage("Progress: " + (int) progress + "%");
                                });
                    }
                });
    }

    private void initializeSpinners() {
        // company spinner
        List<String> companyList = Arrays.asList(getResources().getStringArray(R.array.company));
        companyAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, companyList);
        regCarCompany.setAdapter(companyAdapter);

        // model spinner
        regCarCompany.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        modelAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.audi)));
                        break;
                    case 1:
                        modelAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.bmw)));
                        break;
                    case 2:
                        modelAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.citroen)));
                        break;
                    case 3:
                        modelAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.dacia)));
                        break;
                    case 4:
                        modelAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.ds)));
                        break;
                    case 5:
                        modelAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.fiat)));
                        break;
                    case 6:
                        modelAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.ford)));
                        break;
                    case 7:
                        modelAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.honda)));
                        break;
                    case 8:
                        modelAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.hyundai)));
                        break;
                    case 9:
                        modelAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.jaguar)));
                        break;
                    case 10:
                        modelAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.kia)));
                        break;
                    case 11:
                        modelAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.lexus)));
                        break;
                    case 12:
                        modelAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.mazda)));
                        break;
                    case 13:
                        modelAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.mercedes_benz)));
                        break;
                    case 14:
                        modelAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.mini)));
                        break;
                    case 15:
                        modelAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.nissan)));
                        break;
                    case 16:
                        modelAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.opel)));
                        break;
                    case 17:
                        modelAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.peugeot)));
                        break;
                    case 18:
                        modelAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.porsche)));
                        break;
                    case 19:
                        modelAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.renault)));
                        break;
                    case 20:
                        modelAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.skoda)));
                        break;
                    case 21:
                        modelAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.smart)));
                        break;
                    case 22:
                        modelAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.ssangyong)));
                        break;
                    case 23:
                        modelAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.tesla)));
                        break;
                    case 24:
                        modelAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.vw)));
                        break;
                    case 25:
                        modelAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.volvo)));
                        break;
                }
                regCarModel.setAdapter(modelAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // color spinner
        List<String> colors = Arrays.asList(getResources().getStringArray(R.array.colors_en));
        colorAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, colors);
        regCarColor.setAdapter(colorAdapter);
    }

    private boolean checkImage(Bitmap image) {
        try {
            ModelUnquant model = ModelUnquant.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValues[pixel++];
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            ModelUnquant.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            Log.i(TAG, "Electric Station " + confidences[0]);
            Log.i(TAG, "Gas Pump " + confidences[1]);
            Log.i(TAG, "Renault Zoe " + confidences[2]);
            Log.i(TAG, "Dacia Spring " + confidences[3]);

            if (confidences[3] > confidences[2]) {
                regCarCompany.setSelection(3);
                regCarModel.setSelection(0);
                model.close();
                return true;
            } else {
                regCarCompany.setSelection(19);
                regCarModel.setSelection(1);
                model.close();
                return false;
            }

            // Releases model resources if no longer used.
        } catch (IOException e) {
            return false;
        }

    }
}
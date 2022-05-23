package com.dbd.plugtimproject.activities.register;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dbd.plugtimproject.R;
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

import java.util.Calendar;

public class RegisterCar extends AppCompatActivity implements View.OnClickListener {

    private TextView regCarSkipBtn;
    private Button regCarFinishBtn;

    private EditText regCarCompany, regCarModel, regCarColor, regCarYear;

    private DatabaseReference mDatabase;

    private Button addCarImageBtn;
    private ImageView carImage;

    private Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;


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
                    uploadPicture(FirebaseAuth.getInstance().getCurrentUser().getUid());
                }
                break;
            case R.id.addCarImageBtn:
                addCarImage();
                break;
        }
    }

    private boolean registerCar(String email) {
        String company = regCarCompany.getText().toString();
        String model = regCarModel.getText().toString();
        String color = regCarColor.getText().toString();
        String year = regCarYear.getText().toString();

        if (company.isEmpty()) {
            regCarCompany.setError("Company name is required");
            regCarCompany.requestFocus();
            return false;
        } else if (model.isEmpty()) {
            regCarModel.setError("Model name is required");
            regCarModel.requestFocus();
            return false;
        } else if (color.isEmpty()) {
            regCarColor.setError("Color is required");
            regCarColor.requestFocus();
            return false;
        } else if (year.isEmpty()) {
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
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            carImage.setImageURI(imageUri);
        }
    }

    private void uploadPicture(String uuid) {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Uploading image");
        pd.show();

        StorageReference stationReference = storageReference.child("images/car/" + uuid);
        stationReference.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {


                    @Override
                    public void onSuccess(ListResult listResult) {


                        stationReference.putFile(imageUri)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                                        pd.dismiss();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(RegisterCar.this, "Couldn't load image", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                        double progress = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                                        pd.setMessage("Progress: " + (int) progress + "%");
                                    }
                                });
                    }
                });
    }
}
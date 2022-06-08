package com.dbd.plugtimproject.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dbd.plugtimproject.R;
import com.dbd.plugtimproject.activities.CarInfoActivity;
import com.dbd.plugtimproject.activities.ProfileInfoActivity;
import com.dbd.plugtimproject.activities.register.ForgotPasswordActivity;
import com.dbd.plugtimproject.activities.register.LoginActivity;
import com.dbd.plugtimproject.models.Car;
import com.dbd.plugtimproject.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ProfileFragment";

    private CircleImageView profileImageView;
    private TextView profileInfo, carInfo;

    private DatabaseReference mDatabase;
    private StorageReference storageReference;
    private Uri imageUri;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance("https://plugtimproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            getInfo(FirebaseAuth.getInstance().getCurrentUser().getUid());
            getProfileImage(FirebaseAuth.getInstance().getCurrentUser().getUid());
        } else {
            Log.e(TAG, String.format("Couldn't get profile information for user %s", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()));
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImageView = view.findViewById(R.id.profile_image);
        profileImageView.setOnClickListener(this);

        profileInfo = view.findViewById(R.id.profile_name);
        carInfo = view.findViewById(R.id.profile_car);

        Button editProfileBtn = view.findViewById(R.id.edit_profile_btn);
        editProfileBtn.setOnClickListener(this);
        Button editCarBtn = view.findViewById(R.id.edit_car_btn);
        editCarBtn.setOnClickListener(this);
        Button resetPasswordBtn = view.findViewById(R.id.reset_password_btn);
        resetPasswordBtn.setOnClickListener(this);
        Button signoutBtn = view.findViewById(R.id.signout_btn);
        signoutBtn.setOnClickListener(this);

        return view;
    }

    @SuppressLint("SetTextI18n")
    private void getInfo(String userId) {
        mDatabase.child("users").child(userId).get()
                .addOnCompleteListener(task -> {
                    User user = task.getResult().getValue(User.class);

                    if (user != null) {
                        String firstName = user.getFirstName();
                        String lastName = user.getLastName();
                        profileInfo.setText(firstName + " " + lastName);
                    }
                })
                .addOnCompleteListener(dataSnapshot -> {

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), getString(R.string.user_not_found), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, String.format("Couldn't get profile information for user %s", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()));
                });


        mDatabase.child("cars").child(userId).get()
                .addOnCompleteListener(task -> {
                    Car car = task.getResult().getValue(Car.class);

                    if (car != null) {
                        String carCompany = car.getCompany();
                        String carModel = car.getModel();
                        carInfo.setText(carCompany + " " + carModel);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), getString(R.string.user_not_found), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, String.format("Couldn't get car information for user %s", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()));
                });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void getProfileImage(String userId) {
        StorageReference pathReference = storageReference.child("images/cars/" + userId);

        long MAXBYTES = 1024 * 1024 * 20;

        pathReference.getBytes(MAXBYTES)
                .addOnSuccessListener(bytes -> {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if (bitmap != null) {
                        profileImageView.setImageBitmap(bitmap);
                    }
                })
                .addOnFailureListener(e -> profileImageView.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_account)));

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_profile_btn:
                startActivity(new Intent(getActivity(), ProfileInfoActivity.class));
                break;
            case R.id.edit_car_btn:
                startActivity(new Intent(getActivity(), CarInfoActivity.class));
                break;
            case R.id.profile_image:
                addCarImage();
                break;
            case R.id.reset_password_btn:
                startActivity(new Intent(getActivity(), ForgotPasswordActivity.class));
                break;
            case R.id.signout_btn:
                FirebaseAuth.getInstance().signOut();
                requireActivity().finish();
                startActivity(new Intent(getActivity(), LoginActivity.class));

        }
    }

    private void addCarImage() {
        // intent to open gallery from phone
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == -1 && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }

        uploadPicture(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    private void uploadPicture(String uuid) {
        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setTitle("Uploading image");
        pd.show();

        StorageReference stationReference = storageReference.child("images/cars/" + uuid);

        if (imageUri != null) {
            stationReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> pd.dismiss())
                    .addOnFailureListener(e -> {
                        pd.dismiss();
                        Toast.makeText(getContext(), "Couldn't load image", Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(snapshot -> {
                        double progress = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        pd.setMessage("Progress: " + (int) progress + "%");
                    });
        }
        pd.dismiss();
    }
}
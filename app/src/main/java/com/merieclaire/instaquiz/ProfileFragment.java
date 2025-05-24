package com.merieclaire.instaquiz;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class ProfileFragment extends Fragment {

    private TextView profileName, profileEmail, privacyPolicy, termsService, manageAccount;
    private ImageView profileImage;
    private Switch darkModeSwitch;
    private Button logoutBtn;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    private static final int PICK_IMAGE_REQUEST = 71;
    private static final String PREFS_NAME = "theme_prefs";
    private static final String DARK_MODE_KEY = "dark_mode_enabled";

    public ProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileName = view.findViewById(R.id.profile_name);
        profileEmail = view.findViewById(R.id.profile_email);
        profileImage = view.findViewById(R.id.profile_image);
        darkModeSwitch = view.findViewById(R.id.dark_mode_switch);
        logoutBtn = view.findViewById(R.id.logout_button);
        privacyPolicy = view.findViewById(R.id.privacy_policy);
        termsService = view.findViewById(R.id.terms_service);
        manageAccount = view.findViewById(R.id.manage_account);

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReference();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            profileEmail.setText(user.getEmail());

            mStore.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("Name");
                            String username = documentSnapshot.getString("Username");

                            profileName.setText(name != null ? name : "No Name Provided");
                            profileEmail.setText(username != null ? username : "No Username Provided");

                            String imageUrl = documentSnapshot.getString("profilePicUrl");
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Glide.with(getContext()).load(imageUrl).into(profileImage);
                            }
                        }
                    }).addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed to load profile data", Toast.LENGTH_SHORT).show());
        }

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isDarkModeEnabled = prefs.getBoolean(DARK_MODE_KEY, false);
        darkModeSwitch.setChecked(isDarkModeEnabled);

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> setDarkMode(isChecked));

        profileImage.setOnClickListener(v -> openImagePicker());

        logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getActivity(), SignIn.class));
            requireActivity().finishAffinity();
        });

        privacyPolicy.setOnClickListener(v -> openUrl("https://yourdomain.com/privacy-policy"));
        termsService.setOnClickListener(v -> openUrl("https://yourdomain.com/terms-of-service"));

        return view;
    }

    private void setDarkMode(boolean isEnabled) {
        SharedPreferences.Editor editor = requireContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit();
        editor.putBoolean(DARK_MODE_KEY, isEnabled);
        editor.apply();

        AppCompatDelegate.setDefaultNightMode(
                isEnabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        new Handler().postDelayed(() -> {
            Activity activity = getActivity();
            if (activity != null && !activity.isFinishing()) {
                activity.recreate();
            }
        }, 100);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();

            // Log the URI to debug
            Log.d("ProfileFragment", "Selected Image URI: " + selectedImageUri.toString());

            if (selectedImageUri != null) {
                File file = new File(getRealPathFromURI(selectedImageUri));
                Log.d("ProfileFragment", "File path: " + file.getAbsolutePath());
            }

            // Now use Glide to show the image
            Glide.with(this).load(selectedImageUri).into(profileImage);

            // Firebase Upload
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                StorageReference profileRef = storageReference.child("profile_images/" + user.getUid() + ".jpg");
                UploadTask uploadTask = profileRef.putFile(selectedImageUri);

                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        mStore.collection("users").document(user.getUid())
                                .update("profilePicUrl", downloadUrl)
                                .addOnSuccessListener(unused ->
                                        Toast.makeText(getContext(), "Profile picture updated", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Failed to update profile picture URL", Toast.LENGTH_SHORT).show());
                    }).addOnFailureListener(e -> {
                        Log.e("ProfileFragment", "Failed to get download URL: " + e.getMessage());
                        Toast.makeText(getContext(), "Failed to get download URL", Toast.LENGTH_SHORT).show();
                    });
                }).addOnFailureListener(e -> {
                    Log.e("ProfileFragment", "Image upload failed: " + e.getMessage());
                    Toast.makeText(getContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    private String getRealPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(projection[0]);
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }
        return null;
    }

    private String getFileExtension(Uri uri) {
        String extension = "";
        String mimeType = getActivity().getContentResolver().getType(uri);
        if (mimeType != null) {
            extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        }
        return extension;
    }

    private void openUrl(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }
}

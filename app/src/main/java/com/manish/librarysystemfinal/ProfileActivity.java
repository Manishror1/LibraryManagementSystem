package com.manish.librarysystemfinal;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivProfileImage;
    private TextView tvProfileEmail, tvProfileRole, btnChangePhoto;
    private EditText etProfileName;
    private Button btnSaveChanges, btnMyReceipts;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private Uri selectedImageUri = null;
    private ProgressDialog progressDialog;

    // Modern image picker
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivProfileImage.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        loadUserData();

        btnChangePhoto.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*")
        );

        btnSaveChanges.setOnClickListener(v -> saveProfileChanges());
    }

    private void initViews() {
        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfileRole = findViewById(R.id.tvProfileRole);
        etProfileName = findViewById(R.id.etProfileName);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnMyReceipts = findViewById(R.id.btnMyReceipts);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnMyReceipts.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, MyReceiptsActivity.class))
        );

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
    }

    private void loadUserData() {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(this::bindUserData)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void bindUserData(DocumentSnapshot doc) {
        if (!doc.exists()) return;

        String name = doc.getString("name");
        String email = doc.getString("email");
        String role = doc.getString("role");
        String profilePic = doc.getString("profilePic");

        etProfileName.setText(name);
        tvProfileEmail.setText(email);
        tvProfileRole.setText(role);

        if (profilePic != null && !profilePic.isEmpty()) {
            Glide.with(this)
                    .load(profilePic)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(ivProfileImage);
        }
    }

    private void saveProfileChanges() {
        progressDialog.setMessage("Saving...");
        progressDialog.show();

        if (selectedImageUri != null) {
            uploadToCloudinary(selectedImageUri);
        } else {
            updateFirestore(null);
        }
    }

    private void uploadToCloudinary(Uri fileUri) {

        Map<String, Object> options = new HashMap<>();
        options.put("folder", "library/profile");

        MediaManager.get().upload(fileUri)
                .unsigned("library_profile")
                .options(options)
                .callback(new UploadCallback() {

                    @Override
                    public void onStart(String requestId) {
                        // Upload started
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        // Upload progress
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");

                        if (url == null) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this,
                                    "Upload succeeded but URL missing",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        updateFirestore(url);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this,
                                "Upload failed: " + error.getDescription(),
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this,
                                "Upload rescheduled",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .dispatch();
    }

    private void updateFirestore(String imageUrl) {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", etProfileName.getText().toString().trim());

        if (imageUrl != null) {
            updates.put("profilePic", imageUrl);
        }

        db.collection("users")
                .document(uid)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

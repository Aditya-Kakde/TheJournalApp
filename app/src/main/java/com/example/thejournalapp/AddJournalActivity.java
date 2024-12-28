package com.example.thejournalapp;

import static android.os.Build.ID;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapp.services.Appwrite;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.appwrite.Client;
import io.appwrite.ID;
import io.appwrite.models.File;
import io.appwrite.services.Storage;
import io.appwrite.coroutines.CoroutineCallback;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class AddJournalActivity extends AppCompatActivity {

    private Button saveButton;
    private ImageView addPhotoBtn;
    private ProgressBar progressBar;
    private EditText titleEditText;
    private EditText thoughtsEditText;
    private ImageView imageView;
    private String ENDPOINT = "https://cloud.appwrite.io/v1";
    Storage storage;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Journal");

    private String currentUserId;
    private String currentUserName;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    ActivityResultLauncher<String> mTakePhoto;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_journal);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Appwrite.INSTANCE.init(this);

        saveButton = findViewById(R.id.post_save_journal_button);
        addPhotoBtn = findViewById(R.id.postCameraButton);
        progressBar = findViewById(R.id.post_progressBar);
        titleEditText = findViewById(R.id.post_title_et);
        thoughtsEditText = findViewById(R.id.post_description_et);
        imageView = findViewById(R.id.post_imageView);

        firebaseAuth = FirebaseAuth.getInstance();

        Log.d("SaveJournal", "currentUserName: " + currentUserName);


        saveButton.setOnClickListener(v -> SaveJournal());

        mTakePhoto = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                (ActivityResultCallback<Uri>) result -> {
                    imageView.setImageURI(result);
                    imageUri = result;
                }
        );

        addPhotoBtn.setOnClickListener(v -> mTakePhoto.launch("image/*"));
    }

    public void SaveJournal() {
        String title = titleEditText.getText().toString().trim();
        String thoughts = thoughtsEditText.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(thoughts)) {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Title and Thoughts must be filled.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri == null) {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "No image selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        Appwrite.INSTANCE.saveImageOnly(
                this,
                imageUri,
                currentUserId,
                currentUserName,
                fileUrl -> {
                    if (fileUrl == null || fileUrl.isEmpty()) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(AddJournalActivity.this, "Image upload failed.", Toast.LENGTH_SHORT).show();
                        return null;
                    }

                    Map<String, Object> journal = new HashMap<>();
                    journal.put("title", title);
                    journal.put("thoughts", thoughts);
                    journal.put("imageUrl", fileUrl);
                    journal.put("userId", currentUserId);
                    journal.put("userName", currentUserName);
                    journal.put("timeAdded", new Timestamp(new Date()));

                    collectionReference.add(journal)
                            .addOnSuccessListener(documentReference -> {
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(AddJournalActivity.this, "Journal Saved!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(AddJournalActivity.this, "Error saving journal.", Toast.LENGTH_SHORT).show();
                            });

                    return null;
                },
                errorMessage -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(AddJournalActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    return null;
                }
        );

    }




    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
            currentUserName = user.getDisplayName();

            // Fallback if displayName is null
            if (currentUserName == null || currentUserName.isEmpty()) {
                currentUserName = user.getEmail() != null ? user.getEmail() : "Anonymous";
            }
        }

    }

    public interface OnSuccessCallback {
        void onSuccess(String fileUrl);
    }

    public interface OnFailureCallback {
        void onFailure(String errorMessage);
    }
}

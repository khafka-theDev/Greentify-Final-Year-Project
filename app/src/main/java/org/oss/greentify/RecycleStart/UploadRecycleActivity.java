package org.oss.greentify.RecycleStart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import org.json.JSONObject;
import org.oss.greentify.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import okhttp3.*;

public class UploadRecycleActivity extends AppCompatActivity {

    private EditText weightInput;
    private ImageView imagePreview;
    private TextView pointsText, selectedLocationText;
    private Uri imageUri;
    private String selectedLocation, materialType;
    private static final int PICK_IMAGE_REQUEST = 101;
    private static final int CAPTURE_IMAGE_REQUEST = 102;
    private static final String PREFS_NAME = "GreentifyPrefs";
    private ProgressBar uploadProgressBar;
    private static final String KEY_HAS_SEEN_UPLOAD_WALKTHROUGH = "hasSeenUploadWalkthrough";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_recycle);

        weightInput = findViewById(R.id.weightInput);
        weightInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String weightStr = s.toString().trim();
                if (!weightStr.isEmpty()) {
                    try {
                        double weight = Double.parseDouble(weightStr);
                        int points = calculatePoints(materialType, weight);
                        int greenCredits = calculateGreenCredits(weight);
                        pointsText.setText("Points: " + points + " | Green Credits: " + greenCredits);
                    } catch (NumberFormatException e) {
                        pointsText.setText("Points: 0 | Green Credits: 0");
                    }
                } else {
                    pointsText.setText("Points: 0 | Green Credits: 0");
                }
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        uploadProgressBar = findViewById(R.id.uploadProgressBar);
        imagePreview = findViewById(R.id.imagePreview);
        pointsText = findViewById(R.id.pointsText);
        selectedLocationText = findViewById(R.id.selectedLocationText);
        Button uploadBtn = findViewById(R.id.uploadImageBtn);
        Button submitBtn = findViewById(R.id.submitBtn);

        selectedLocation = getIntent().getStringExtra("location");
        materialType = getIntent().getStringExtra("type");
        selectedLocationText.setText(selectedLocation != null
                ? "Location: " + selectedLocation
                : "Location: Not selected");

        uploadBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Choose Image Source")
                    .setMessage("Do you want to upload an image from the gallery or capture a new one?")
                    .setPositiveButton("Gallery", (dialog, which) -> chooseImageFromGallery())
                    .setNegativeButton("Camera", (dialog, which) -> captureImageFromCamera())
                    .show();
        });

        submitBtn.setOnClickListener(v -> {
            String weightStr = weightInput.getText().toString().trim();
            if (weightStr.isEmpty() || imageUri == null) {
                Toast.makeText(this, "Please enter weight and select image", Toast.LENGTH_SHORT).show();
                return;
            }
            double weight = Double.parseDouble(weightStr);
            int points = calculatePoints(materialType, weight);
            int greenCredits = calculateGreenCredits(weight);
            pointsText.setText("Points: " + points + " | Green Credits: " + greenCredits);
            uploadToCloudinary(weight, points, greenCredits);
        });
    }

    private void chooseImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void captureImageFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAPTURE_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_IMAGE_REQUEST && data.getData() != null) {
                imageUri = data.getData();
                imagePreview.setImageURI(imageUri);
            }
            // ✅ FIX: Handle camera image as Bitmap, convert to Uri
            else if (requestCode == CAPTURE_IMAGE_REQUEST) {
                Bundle extras = data.getExtras();
                if (extras != null && extras.containsKey("data")) {
                    Bitmap bitmap = (Bitmap) extras.get("data");
                    imagePreview.setImageBitmap(bitmap);
                    imageUri = getImageUri(bitmap); // Convert to Uri for upload
                }
            }
        }
    }

    // ✅ Convert Bitmap to URI using MediaStore
    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "TempImage", null);
        return Uri.parse(path);
    }

    private int calculatePoints(String type, double weight) {
        switch (type.toLowerCase()) {
            case "paper": return (int)(weight * 5);
            case "plastic": return (int)(weight * 6);
            case "glass": return (int)(weight * 4);
            case "metal": return (int)(weight * 5);
            case "e-waste": return (int)(weight * 10);
            default: return 0;
        }
    }

    private int calculateGreenCredits(double weight) {
        return (int) (weight * 10);
    }

    private void uploadToCloudinary(double weight, int points, int greenCredits) {
        runOnUiThread(() -> uploadProgressBar.setVisibility(View.VISIBLE));

        new Thread(() -> {
            try {
                InputStream in = getContentResolver().openInputStream(imageUri);
                byte[] bytes = getBytes(in);

                OkHttpClient client = new OkHttpClient();
                RequestBody body = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", "image.jpg", RequestBody.create(bytes, MediaType.parse("image/jpeg")))
                        .addFormDataPart("upload_preset", "unisgned_upload")
                        .build();

                Request req = new Request.Builder()
                        .url("https://api.cloudinary.com/v1_1/diirc1eey/image/upload")
                        .post(body)
                        .build();

                Response resp = client.newCall(req).execute();
                if (resp.isSuccessful()) {
                    String respStr = resp.body().string();
                    String url = new JSONObject(respStr).getString("secure_url");

                    runOnUiThread(() -> saveToFirestore(weight, points, greenCredits, url));
                } else {
                    String error = resp.body().string();
                    runOnUiThread(() -> {
                        uploadProgressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Upload failed: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    uploadProgressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Upload error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void saveToFirestore(double weight, int points, int greenCredits, String imageUrl) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        String email = currentUser.getEmail();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DocumentSnapshot document = task.getResult();
                String username = document.getString("username");

                Map<String, Object> data = new HashMap<>();
                data.put("userId", userId);
                data.put("email", email);
                data.put("username", username);
                data.put("location", selectedLocation);
                data.put("materialType", materialType);
                data.put("imageUrl", imageUrl);
                data.put("weight", weight);
                data.put("points", points);
                data.put("greenCredits", greenCredits);
                data.put("timestamp", System.currentTimeMillis());
                data.put("dateSubmitted", new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
                data.put("timeSubmitted", new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()));
                data.put("status", "pending");

                db.collection("recycle_submissions")
                        .add(data)
                        .addOnSuccessListener(docRef -> {
                            Map<String, Object> history = new HashMap<>();
                            history.put("submissionId", docRef.getId());
                            history.put("userId", userId);
                            history.put("status", "pending");
                            history.put("note", "");
                            history.put("notified", false);
                            history.put("timestamp", FieldValue.serverTimestamp());
                            history.put("readByUserIds", new ArrayList<>());

                            db.collection("recycle_submission_history").add(history);
                            showSuccessDialog();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showSuccessDialog() {
        runOnUiThread(() -> {
            uploadProgressBar.setVisibility(View.GONE);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_success, null);
            builder.setView(dialogView).setCancelable(false);
            AlertDialog dialog = builder.create();

            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.upload);
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.stop();
                mp.release();
            });
            mediaPlayer.start();

            dialog.show();

            new android.os.Handler().postDelayed(() -> {
                dialog.dismiss();
                Intent intent = new Intent(UploadRecycleActivity.this, org.oss.greentify.Home.HistoryActivity.class);
                intent.putExtra("fromUpload", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }, 2500);
        });
    }

    private byte[] getBytes(InputStream in) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] tmp = new byte[1024];
        int len;
        while ((len = in.read(tmp)) != -1) buf.write(tmp, 0, len);
        return buf.toByteArray();
    }

    // Walkthrough steps unchanged...

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (!prefs.getBoolean(KEY_HAS_SEEN_UPLOAD_WALKTHROUGH, false)) {
            new android.os.Handler().postDelayed(() -> {
                showWalkthroughStep1();
                prefs.edit().putBoolean(KEY_HAS_SEEN_UPLOAD_WALKTHROUGH, true).apply();
            }, 800);
        }
    }

    private void showWalkthroughStep1() {
        TapTargetView.showFor(this,
                TapTarget.forView(findViewById(R.id.selectedLocationText), "Selected Location", "This is the recycling facility you've chosen from the map.")
                        .outerCircleColorInt(Color.TRANSPARENT)
                        .targetCircleColor(android.R.color.white)
                        .titleTextColor(android.R.color.white)
                        .descriptionTextColor(android.R.color.white)
                        .dimColor(android.R.color.black)
                        .drawShadow(true)
                        .tintTarget(true)
                        .transparentTarget(true)
                        .cancelable(false),
                new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        showWalkthroughStep2();
                    }
                });
    }

    private void showWalkthroughStep2() {
        TapTargetView.showFor(this,
                TapTarget.forView(findViewById(R.id.weightInput), "Enter Weight (kg)", "Input the amount of recyclable material you've collected.")
                        .outerCircleColorInt(Color.TRANSPARENT)
                        .targetCircleColor(android.R.color.white)
                        .titleTextColor(android.R.color.white)
                        .descriptionTextColor(android.R.color.white)
                        .dimColor(android.R.color.black)
                        .drawShadow(true)
                        .tintTarget(true)
                        .transparentTarget(true)
                        .cancelable(false),
                new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        showWalkthroughStep3();
                    }
                });
    }

    private void showWalkthroughStep3() {
        TapTargetView.showFor(this,
                TapTarget.forView(findViewById(R.id.uploadImageBtn), "Upload Proof Image", "Tap here to upload a photo of your recycling activity.")
                        .outerCircleColorInt(Color.TRANSPARENT)
                        .targetCircleColor(android.R.color.white)
                        .titleTextColor(android.R.color.white)
                        .descriptionTextColor(android.R.color.white)
                        .dimColor(android.R.color.black)
                        .drawShadow(true)
                        .tintTarget(true)
                        .transparentTarget(true)
                        .cancelable(false),
                new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        showWalkthroughStep4();
                    }
                });
    }

    private void showWalkthroughStep4() {
        TapTargetView.showFor(this,
                TapTarget.forView(findViewById(R.id.pointsText), "Points & Credits", "This shows the rewards you’ll earn based on the recycled weight.")
                        .outerCircleColorInt(Color.TRANSPARENT)
                        .targetCircleColor(android.R.color.white)
                        .titleTextColor(android.R.color.white)
                        .descriptionTextColor(android.R.color.white)
                        .dimColor(android.R.color.black)
                        .drawShadow(true)
                        .tintTarget(true)
                        .transparentTarget(true)
                        .cancelable(false),
                new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        showWalkthroughStep5();
                    }
                });
    }

    private void showWalkthroughStep5() {
        TapTargetView.showFor(this,
                TapTarget.forView(findViewById(R.id.submitBtn), "Submit Activity", "Once everything is filled, tap here to submit your recycling activity.")
                        .outerCircleColorInt(Color.TRANSPARENT)
                        .targetCircleColor(android.R.color.white)
                        .titleTextColor(android.R.color.white)
                        .descriptionTextColor(android.R.color.white)
                        .dimColor(android.R.color.black)
                        .drawShadow(true)
                        .tintTarget(true)
                        .transparentTarget(true)
                        .cancelable(true));
    }
}
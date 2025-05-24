package com.merieclaire.instaquiz;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private static final int FILE_REQUEST_CODE = 1;
    private Uri fileUri;

    private Button uploadFileBtn, generateBtn;
    private CheckBox mcqCheck, trueFalseCheck, fillBlankCheck, identificationCheck, enumerationCheck;
    private EditText pdfNameEditText;
    private EditText mcqCount, tfCount, fbCount, identCount, enumCount;
    private TextView fileNameTextView;

    private List<String> quizContent;

    public HomeFragment() {}

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PDFBoxResourceLoader.init(requireContext().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        uploadFileBtn = view.findViewById(R.id.uploadFileBtn);
        generateBtn = view.findViewById(R.id.generateQuizBtn);
        mcqCheck = view.findViewById(R.id.mcqCheck);
        trueFalseCheck = view.findViewById(R.id.trueFalseCheck);
        fillBlankCheck = view.findViewById(R.id.fillBlankCheck);
        identificationCheck = view.findViewById(R.id.identificationCheck);
        enumerationCheck = view.findViewById(R.id.enumerationCheck);

        mcqCount = view.findViewById(R.id.mcqCount);
        tfCount = view.findViewById(R.id.tfCount);
        fbCount = view.findViewById(R.id.fbCount);
        identCount = view.findViewById(R.id.identCount);
        enumCount = view.findViewById(R.id.enumCount);

        pdfNameEditText = view.findViewById(R.id.pdfNameEditText);
        fileNameTextView = view.findViewById(R.id.fileNameTextView);

        uploadFileBtn.setOnClickListener(v -> openFileChooser());

        generateBtn.setOnClickListener(v -> {
            if (fileUri == null) {
                Toast.makeText(getContext(), "Please upload a file first", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Integer> quizFormatCounts = new HashMap<>();

            if (mcqCheck.isChecked()) {
                int count = getIntFromEditText(mcqCount);
                if (count > 0) quizFormatCounts.put("MCQ", count);
            }

            if (trueFalseCheck.isChecked()) {
                int count = getIntFromEditText(tfCount);
                if (count > 0) quizFormatCounts.put("TF", count);
            }

            if (fillBlankCheck.isChecked()) {
                int count = getIntFromEditText(fbCount);
                if (count > 0) quizFormatCounts.put("FB", count);
            }

            if (identificationCheck.isChecked()) {
                int count = getIntFromEditText(identCount);
                if (count > 0) quizFormatCounts.put("Identification", count);
            }

            if (enumerationCheck.isChecked()) {
                int count = getIntFromEditText(enumCount);
                if (count > 0) quizFormatCounts.put("Enumeration", count);
            }

            if (quizFormatCounts.isEmpty()) {
                Toast.makeText(getContext(), "Select at least one quiz format with a valid count", Toast.LENGTH_SHORT).show();
                return;
            }

            String content = FileParser.parseFile(requireContext(), fileUri);
            if (content == null || content.trim().isEmpty()) {
                Toast.makeText(getContext(), "Failed to extract content from the file", Toast.LENGTH_SHORT).show();
                return;
            }

            String pdfName = pdfNameEditText.getText().toString().trim();
            if (pdfName.isEmpty()) pdfName = "Quiz";

            // Call Firebase function to generate quiz with NLP
            callQuizGenerationFunction(content, quizFormatCounts, pdfName);
        });
    }

    private void callQuizGenerationFunction(String content, Map<String, Integer> quizFormatCounts, String pdfName) {
        try {
            JSONObject requestJson = new JSONObject();
            requestJson.put("text", content);

            JSONObject formatCountsJson = new JSONObject();
            for (Map.Entry<String, Integer> entry : quizFormatCounts.entrySet()) {
                formatCountsJson.put(entry.getKey(), entry.getValue());
            }
            requestJson.put("quizFormatCounts", formatCountsJson);

            RequestQueue queue = Volley.newRequestQueue(requireContext());
            String url = "https://us-central1-instaquiz-f33a4.cloudfunctions.net/generateQuiz";// Replace with your actual function URL

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestJson,
                    response -> {
                        try {
                            JSONArray quizArray = response.getJSONArray("quiz");
                            quizContent = new ArrayList<>();
                            for (int i = 0; i < quizArray.length(); i++) {
                                quizContent.add(quizArray.getString(i));
                            }

                            Log.d("InstaQuiz", "Generated quiz content: " + quizContent.toString());

                            if (quizContent.isEmpty()) {
                                Toast.makeText(getContext(), "No quiz questions generated", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            PdfExporter.exportToPdf(requireContext(), quizContent, pdfName);
                            Toast.makeText(getContext(), "PDF generated successfully!", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.e("InstaQuiz", "Response parsing failed", e);
                            Toast.makeText(getContext(), "Failed to parse quiz data", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Log.e("InstaQuiz", "Quiz generation failed", error);
                        Toast.makeText(getContext(), "Quiz generation failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    });

            queue.add(request);

        } catch (Exception e) {
            Log.e("InstaQuiz", "Request construction failed", e);
            Toast.makeText(getContext(), "Failed to prepare request", Toast.LENGTH_SHORT).show();
        }
    }

    private int getIntFromEditText(EditText editText) {
        try {
            return Integer.parseInt(editText.getText().toString().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, FILE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            fileUri = data.getData();
            if (fileUri != null) {
                String fileName = getFileName(fileUri);
                fileNameTextView.setText("Selected file: " + fileName);
            }
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
                    result = cursor.getString(nameIndex);
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result != null ? result.lastIndexOf('/') : -1;
            if (cut != -1 && result != null) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}

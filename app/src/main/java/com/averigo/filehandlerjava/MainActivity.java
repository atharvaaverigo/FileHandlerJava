package com.averigo.filehandlerjava;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    Button btnDownload;
    Button btnUpload;
    RetroService instance;
    private String fileName = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        instance = RetrofitInstance.getRetroInstance().create(RetroService.class);
        btnDownload = findViewById(R.id.downloadButton);
        btnUpload = findViewById(R.id.uploadButton);

        btnUpload.setOnClickListener(v -> {
            /*String fileContent = "This is the content of my text file";
            fileName = generateFileText();
            File file = new File(getApplicationContext().getCacheDir(), fileName + ".txt");


            try {
                FileWriter writer = new FileWriter(file);
                writer.append(fileContent);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            File file = getFileFromAssets(this, "Grab_Scan_GoTIMEBOUND200SeatsLicense062624.jsl");

            RequestBody requestFile = RequestBody.create(MediaType.parse("text/plain"), file);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            Call<FileUploadResponse> call = instance.uploadFile(
                    "8a9R7mNcA3eLxuQlVLUtei5SDm7iXGKLG7FSKOEgS94UEexChOaWBDU3Kl3O",
                    filePart
            );

            call.enqueue(new retrofit2.Callback<FileUploadResponse>() {
                @Override
                public void onResponse(Call<FileUploadResponse> call, Response<FileUploadResponse> response) {
                    if (response.isSuccessful()) {
                        FileUploadResponse res = response.body();
                        Toast.makeText(getApplicationContext(), res.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<FileUploadResponse> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnDownload.setOnClickListener(v -> {
            if (!fileName.isEmpty()) {
                Call<ResponseBody> dwCall = instance.downloadFile(
                        "8a9R7mNcA3eLxuQlVLUtei5SDm7iXGKLG7FSKOEgS94UEexChOaWBDU3Kl3O",
                        fileName + ".txt"
                );

                dwCall.enqueue(new retrofit2.Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            ResponseBody res = response.body();
                            String documentsPath = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
                            String dirPath = getApplicationContext().getExternalFilesDir(null).getAbsolutePath();
                            String filePath = dirPath;
                            saveFile(res, filePath, fileName + ".txt");
                            Log.d("fileNameA", filePath + "/" + fileName + ".txt");
                        } else {
                            Toast.makeText(getApplicationContext(), "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(getApplicationContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "File name is not provided", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void saveFile(ResponseBody body, String pathWhereYouWantToSaveFile, String fileName) {
        // Return if the body is null
        if (body == null) {
            Log.e("saveFile", "ResponseBody is null");
            return;
        }

        InputStream input = null;
        try {
            input = body.byteStream();

            // Ensure the file path ends with a separator (e.g. '/')
            String fullFilePath = pathWhereYouWantToSaveFile.endsWith("/") ?
                    pathWhereYouWantToSaveFile + fileName :
                    pathWhereYouWantToSaveFile + "/" + fileName;

            File file = new File(fullFilePath);
            // Ensure the parent directories exist
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }

            FileOutputStream output = new FileOutputStream(file);
            try {
                byte[] buffer = new byte[4 * 1024]; // Buffer size
                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
                output.flush();
                Toast.makeText(getApplicationContext(), "File Downloaded Successfully", Toast.LENGTH_SHORT).show();
            } finally {
                output.close(); // Automatically close output stream with try-finally
            }

        } catch (Exception e) {
            Log.e("saveFile", "Error saving file: " + e.getMessage());
        } finally {
            try {
                if (input != null) input.close(); // Close input stream in the finally block
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder randomString = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            randomString.append(chars.charAt(random.nextInt(chars.length())));
        }
        return randomString.toString();
    }

    // Function to format the current date and time in the desired format
    private String formatDateTime() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        return formatter.format(date);
    }

    private String generateFileText() {
        String dateTime = formatDateTime();
        String randomString = generateRandomString(4);
        return "TXT_" + dateTime + randomString;
    }

    public File getFileFromAssets(Context context, String fileName) {
        File file = new File(context.getCacheDir(), fileName); // Store file in cache

        try (InputStream inputStream = context.getAssets().open(fileName);
             OutputStream outputStream = new FileOutputStream(file)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }


}
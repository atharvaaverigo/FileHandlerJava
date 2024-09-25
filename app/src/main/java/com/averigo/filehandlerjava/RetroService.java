package com.averigo.filehandlerjava;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface RetroService {

    @Multipart
    @POST("index.php/file-upload-mobile")
    Call<FileUploadResponse> uploadFile(
        @Header("Token") String token,
        @Part MultipartBody.Part file
    );

    @FormUrlEncoded
    @POST("index.php/file-download-mobile")
    Call<ResponseBody> downloadFile(
        @Header("Token") String token,
        @Field("filename") String filename
    );
}

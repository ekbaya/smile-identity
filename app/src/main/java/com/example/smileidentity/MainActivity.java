package com.example.smileidentity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.smileid.smileidui.CaptureType;
import com.smileid.smileidui.SIDCaptureManager;
import com.smileidentity.libsmileid.core.RetryOnFailurePolicy;
import com.smileidentity.libsmileid.core.SIDConfig;
import com.smileidentity.libsmileid.core.SIDNetworkRequest;
import com.smileidentity.libsmileid.core.SIDResponse;
import com.smileidentity.libsmileid.exception.SIDException;
import com.smileidentity.libsmileid.model.GeoInfos;
import com.smileidentity.libsmileid.model.PartnerParams;
import com.smileidentity.libsmileid.model.SIDMetadata;
import com.smileidentity.libsmileid.model.SIDNetData;
import com.smileidentity.libsmileid.model.SIDUserIdInfo;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements
        SIDNetworkRequest.OnCompleteListener,
        SIDNetworkRequest.OnErrorListener,
        SIDNetworkRequest.OnUpdateListener,
        SIDNetworkRequest.OnEnrolledListener{

     private Button button;
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
    };
    int SMILE_SELFIE_REQUEST_CODE = 100;
    //instantiate the class
    RetryOnFailurePolicy retryOnFailurePolicy = new RetryOnFailurePolicy();
    //instantiate the class
    GeoInfos infos = new GeoInfos();

    //Instantiation
    SIDMetadata metadata = new SIDMetadata();

    SIDConfig mConfig;
    SIDNetworkRequest mSINetworkrequest;
    String mTag;

    String AUTH_URL;
    String PARTNER_URL;
    String PARTNER_PORT;
    String LAMBDA_URL;
    String SID_PORT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTag = "Elias";

        AUTH_URL = "api/v2/#/auth_smile/";
        PARTNER_URL = "https://test-smileid.herokuapp.com/";
        PARTNER_PORT = "8080";
        LAMBDA_URL = "https://3eydmgh10d.execute-api.us-west-2.amazonaws.com/test/";
        SID_PORT = "8443";

        mSINetworkrequest = new SIDNetworkRequest(this);
        mSINetworkrequest.setOnCompleteListener(this);
        mSINetworkrequest.set0nErrorListener(this);
        mSINetworkrequest.setOnUpdateListener(this);
        mSINetworkrequest.setOnEnrolledListener(this);

        //set the number of times a retry should be attempted before giving up
        retryOnFailurePolicy.setRetryCount(10);

        //set the spacing in milliseconds between consecutive retries
        retryOnFailurePolicy.setRetryTimeout(TimeUnit.SECONDS.toMillis(15));

        //To get the user id information
        SIDUserIdInfo userIdInfo = metadata.getSidUserIdInfo();

        //To get the partner parameters
        PartnerParams params  = metadata.getPartnerParams();

        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hasPermissions(MainActivity.this, PERMISSIONS)) {
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, PERMISSION_ALL);
                }
                else {
                    new SIDCaptureManager.Builder(MainActivity.this, CaptureType.SELFIE_AND_ID_CAPTURE, SMILE_SELFIE_REQUEST_CODE).build().start();
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SIDNetData sidNetData = new SIDNetData(MainActivity.this, SIDNetData.Environment.TEST);


        SIDConfig.Builder builder = new SIDConfig.Builder(this)
                .setRetryOnfailurePolicy(getRetryOnFailurePolicy())
                .setMode(SIDConfig.Mode.ENROLL)
                .setSmileIdNetData(sidNetData)
                .setJobType(4);


        mConfig = builder.build(mTag);
        mSINetworkrequest.submit(mConfig);
        Toast.makeText(this, "Request has been submitted", Toast.LENGTH_SHORT).show();
    }

    private RetryOnFailurePolicy getRetryOnFailurePolicy() {
        RetryOnFailurePolicy retryOnFailurePolicy = new RetryOnFailurePolicy();
        retryOnFailurePolicy.setRetryCount(10);
        retryOnFailurePolicy.setRetryTimeout(TimeUnit.SECONDS.toMillis(15));
        return retryOnFailurePolicy;
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onComplete() {
        Toast.makeText(this, "Job Complete", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEnrolled(SIDResponse sidResponse) {
        Toast.makeText(this, "Response received", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(SIDException e) {
        Toast.makeText(this, "Error "+e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpdate(int i) {

    }
}


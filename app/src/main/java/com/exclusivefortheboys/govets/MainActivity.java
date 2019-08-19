package com.exclusivefortheboys.govets;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private CallbackManager callbackManager;

    private static final String EMAIL = "email";
    private static final String TAG = "MainActivity";

    private LoginButton loginButton;
    private TextView displayName, emailID;
    private ImageView displayImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        displayName = findViewById(R.id.display_name);
        emailID = findViewById(R.id.email);
        displayImage = findViewById(R.id.image_view);
        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("email", "public_profile"));

        callbackManager = CallbackManager.Factory.create();

        printKeyHash();

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                AccessToken accessToken = loginResult.getAccessToken();
                useLoginInformation(accessToken);
            }

            @Override
            public void onCancel() {
                Log.e(TAG, "onCancel: " );
            }

            @Override
            public void onError(FacebookException exception) {
                Log.e(TAG, "onError: "+exception.toString() );
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resulrCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resulrCode, data);
        super.onActivityResult(requestCode, resulrCode, data);
    }

    /**
     Creating the GraphRequest to fetch user details
     1st Param - AccessToken
     2nd Param - Callback (which will be invoked once the request is successful)
     **/
    private void useLoginInformation(final AccessToken accessToken) {

        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            //OnCompleted is invoked once the GraphRequest is successful
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    String name = object.getString("name");
                    String email = object.getString("email");
                    String image = object.getJSONObject("picture").getJSONObject("data").getString("url");
                    displayName.setText(name);
                    emailID.setText(email);

                    Log.e(TAG, "onCompleted: name:"+name );
                    Log.e(TAG, "onCompleted: email:"+email );
                    Log.e(TAG, "onCompleted: imageurl:"+image );
                    Log.e(TAG, "onCompleted: imageurl:"+accessToken.getToken() );
                    Log.e(TAG, "onCompleted: imageurl:"+accessToken.getUserId() );


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        // We set parameters to the GraphRequest using a Bundle.
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,picture.width(200)");
        request.setParameters(parameters);
        // Initiate the GraphRequest
        request.executeAsync();
    }

    private void printKeyHash(){
        // Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.exclusivefortheboys.govets",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("KeyHash:", e.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.d("KeyHash:", e.toString());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            useLoginInformation(accessToken);
        }
    }
}

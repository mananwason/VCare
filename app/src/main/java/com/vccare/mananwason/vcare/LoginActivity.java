package com.vccare.mananwason.vcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;


/**
 * Created by mananwason on 7/16/17.
 */

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    public static final String UID = "UID";
    public static final String PHN = "PHN";
    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_email_login);
        if(!getSharedPreferences(UID, MODE_PRIVATE).contains(UID)) {
            Log.d(TAG, "UID PRESENT");
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                    new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build(),
                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                    .setTheme(R.style.FullscreenTheme)
                    .build(), RC_SIGN_IN);
        }
        else {
            Intent launchNextActivity = new Intent(this, MapsActivity.class);
            launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

            startActivity(launchNextActivity);
            finish();
        }

//        View bottomSheet = findViewById(R.id.bottom_sheet);
//        Button emailLoginButton = (Button) findViewById(R.id.email_login_button);
//        Button phoneLoginButton = (Button) findViewById(R.id.phone_login_button);
//
//        emailLoginButton.setOnClickListener(this);
//        phoneLoginButton.setOnClickListener(this);
//
//        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
//        mBottomSheetBehavior.setPeekHeight(200);
//        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
//            @Override
//            public void onStateChanged(@NonNull View bottomSheet, int newState) {
//                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
//                    mBottomSheetBehavior.setPeekHeight(0);
//                }
//            }
//
//            @Override
//            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//            }
//        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in

            if (resultCode == ResultCodes.OK) {
                FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
                Log.d(TAG, currentFirebaseUser.getUid());
                SharedPreferences.Editor editor = getSharedPreferences(UID, MODE_PRIVATE).edit();
                editor.putString(UID, currentFirebaseUser.getUid());
                editor.apply();
                if (response.getProviderType().equalsIgnoreCase("phone")) {
                    Log.d(TAG, currentFirebaseUser.getPhoneNumber());
                    editor.putString(PHN, currentFirebaseUser.getPhoneNumber()).apply();
                } else {

                }
                startActivity(new Intent(this, MapsActivity.class));
                finish();
                return;
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    showSnackbar(R.string.sign_in_cancelled);
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackbar(R.string.no_internet_connection);
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackbar(R.string.unknown_error);
                    return;
                }
            }

            showSnackbar(R.string.unknown_sign_in_response);
        }

    }

    public void showSnackbar(@StringRes int errorMessageRes) {
        View rootView = findViewById(R.id.frame);
        Snackbar.make(rootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }
}

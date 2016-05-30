package sauerapps.betterbetterrx.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import icepick.Icepick;
import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.app.authentication.createAccount.CreateAccountActivity;
import sauerapps.betterbetterrx.app.authentication.login.LoginActivity;
import sauerapps.betterbetterrx.utils.Constants;


public abstract class BaseActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    protected String mProvider, mEncodedEmail;
    protected GoogleApiClient mGoogleApiClient;
    protected Firebase.AuthStateListener mAuthListener;
    protected Firebase mFirebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);

        /* Setup the Google API object to allow Google logins */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        /* Setup the Google API object to allow Google+ logins */
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(BaseActivity.this);
        /* Get mEncodedEmail and mProvider from SharedPreferences, use null as default value */
        mEncodedEmail = sp.getString(Constants.KEY_ENCODED_EMAIL, null);
        mProvider = sp.getString(Constants.KEY_PROVIDER, null);


        if (!((this instanceof LoginActivity) || (this instanceof CreateAccountActivity))) {
            mFirebaseRef = new Firebase(Constants.FIREBASE_URL);
            mAuthListener = new Firebase.AuthStateListener() {
                @Override
                public void onAuthStateChanged(AuthData authData) {
                     /* The user has been logged out */
                    if (authData == null) {
                        /* Clear out shared preferences */
                        SharedPreferences.Editor spe = sp.edit();
                        spe.putString(Constants.KEY_ENCODED_EMAIL, null);
                        spe.putString(Constants.KEY_PROVIDER, null);

                        takeUserToLoginScreenOnUnAuth();
                    }
                }
            };
            mFirebaseRef.addAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /* Cleanup the AuthStateListener */
        if (!((this instanceof LoginActivity) || (this instanceof CreateAccountActivity))) {
            mFirebaseRef.removeAuthStateListener(mAuthListener);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_base, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void initializeBackground(LinearLayout linearLayout) {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            linearLayout.setBackgroundResource(R.color.background_white);
        } else {
            linearLayout.setBackgroundResource(R.color.background_white);
        }
    }
    protected void logout() {

        /* Logout if mProvider is not null */
        if (mProvider != null) {
            mFirebaseRef.unauth();

            if (mProvider.equals(Constants.GOOGLE_PROVIDER)) {

                /* Logout from Google+ */
                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                //nothing
                            }
                        });
            }
        }
    }

    private void takeUserToLoginScreenOnUnAuth() {
        /* Move user to LoginActivity, and remove the backstack */
        Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
}
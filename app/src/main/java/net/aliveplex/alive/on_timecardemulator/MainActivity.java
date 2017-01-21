package net.aliveplex.alive.on_timecardemulator;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings.Secure;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    SharedPreferences sp;
    boolean InternetAccess = true;
    Dialog login,regis;
    EditText etUser,etPass,etUserR,etPassR;
    Button butLogin,butClear,butRegis,butClearR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        login = new Dialog(MainActivity.this);
        login.setContentView(R.layout.login_layout);
        login.setTitle("Login");
        login.setCancelable(false);
        regis = new Dialog(MainActivity.this);
        regis.setContentView(R.layout.reges_layout);
        regis.setTitle("Regiester");
        regis.setCancelable(false);
        etUser = (EditText) login.findViewById(R.id.etUser);
        etPass = (EditText) login.findViewById(R.id.etPass);
        butLogin = (Button) login.findViewById(R.id.butLog);
        butClear = (Button) login.findViewById(R.id.butClear);
        etUserR = (EditText) regis.findViewById(R.id.etUserR);
        etPassR = (EditText) regis.findViewById(R.id.etPassR);
        butRegis = (Button) regis.findViewById(R.id.butRegis);
        butClearR = (Button) regis.findViewById(R.id.butClearR);

        // get sp with default name, this default name is shared across app
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor spEdit = sp.edit();

        String androidId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
        spEdit.putString(Constant.AndroidIdSpKey, androidId);
        spEdit.apply();
        Log.d("Android Id", "Android Id is " + androidId);

        checkPermission();

        butLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //เช็คว่า ถ้าค่าว่างให้ใส่ใหม่ ถ้าไม่ว่างค่อยส่งไปยัง server
                    if(etUser.getText().toString().equals(sp.getString(Constant.UsernameSpKey,"SuperAdmin"))&&etPass.getText().toString().equals(sp.getString(Constant.PasswordSpKey,"123456")))
                    {
                        //แก้เก็บเฉพาะ รหัส นศ กับ เบอร์โทรเก็บไว้
                        login.dismiss();
                    }
                    else {
                        Toast.makeText(MainActivity.this,"UserName or password Error",Toast.LENGTH_SHORT).show();
                    }
                }
                catch(Exception e){
                    Toast.makeText(MainActivity.this,"Error "+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
        butRegis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    spEdit.putString(Constant.UsernameSpKey, etUserR.getText().toString());
                    spEdit.putString(Constant.PasswordSpKey, etPassR.getText().toString());
                    spEdit.apply();

                    if (!InternetAccess) {
                        finish();
                    }

                }
                catch(Exception e){
                    Toast.makeText(MainActivity.this,"Error "+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Debug purpose
        // sendRegisterRequest(this, "SuperAdmin", "123456", "555455545");
    }


    void checkPermission(){
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            InternetAccess = false;
            Log.d("Internet", "No internet permission");
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.INTERNET)) {
                Log.d("Rational", "Can you give me fucking permission or fucking uninstall it.");
                Log.d("Permission", "Should show request permission rationale return true");
                requestInternetPermission();
            }
            else {
                requestInternetPermission();
            }
        }
        else {
            Log.d("Internet", "Have internet permission");
        }
    }

    // Create separate method for flexibility
    private void requestInternetPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.INTERNET}, Constant.INTERNET_ACCESS_RESULT);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constant.INTERNET_ACCESS_RESULT: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! do the
                    // calendar task you need to do.
                    InternetAccess = true;
                    Toast.makeText(this, "granted permission", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(this, "nope", Toast.LENGTH_LONG).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
        }
    }

    private void sendRegisterRequest(Context context, final String username, final String password, final String androidId) {
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest sr = new StringRequest(Request.Method.POST, Constant.REGISTER_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("http post", "response is " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("http post", "error is " + error.getMessage() + " cause " + error.getCause().getMessage());
            }
        }){
            @Override
            protected Map<String,String> getParams() {
                Map<String,String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", password);
                params.put("androidId", androidId);

                return params;
            }
        };
        queue.add(sr);
    }
}

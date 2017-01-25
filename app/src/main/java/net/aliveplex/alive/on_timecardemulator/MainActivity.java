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

import com.google.common.io.CharStreams;
import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    SharedPreferences sp;
    boolean InternetAccess = true;
    Dialog login;
    EditText etUser,etPass,etUserR,etPassR;
    Button butLogin,butClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        login = new Dialog(MainActivity.this);
        login.setContentView(R.layout.login_layout);
        login.setTitle("Login");
        login.setCancelable(false);
        etUser = (EditText) login.findViewById(R.id.etUser);
        etPass = (EditText) login.findViewById(R.id.etPass);
        butLogin = (Button) login.findViewById(R.id.butLog);
        butClear = (Button) login.findViewById(R.id.butClear);
        Button testbut = (Button) findViewById(R.id.testbut);

        // get sp with default name, this default name is shared across app
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor spEdit = sp.edit();

        String androidId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
        spEdit.putString(Constant.AndroidIdSpKey, androidId);
        spEdit.apply();

        checkPermission();
        LoginCheck();

        // debug purpose only
        testbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new sendRegisterAsync().execute(new RegisterInfo("aefaefefd", "123456", "SuperAdmin"));
            }
        });

        butLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //เช็คว่า ถ้าค่าว่างให้ใส่ใหม่ ถ้าไม่ว่างค่อยส่งไปยัง server
                    if (etUser.getText().toString().equals(sp.getString(Constant.UsernameSpKey, "SuperAdmin")) && etPass.getText().toString().equals(sp.getString(Constant.PasswordSpKey, "123456"))) {
                        //แก้เก็บเฉพาะ รหัส นศ กับ เบอร์โทรเก็บไว้
                        spEdit.putString("et_pr_sta","1");
                        spEdit.apply();
                        login.dismiss();
                    } else {
                        Toast.makeText(MainActivity.this, "UserName or password Error", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Error " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
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

    private static class sendRegisterAsync extends AsyncTask<RegisterInfo, Void, String> {

        @Override
        protected String doInBackground(RegisterInfo... params) {
            if (params[0] == null) {
                throw new IllegalArgumentException("argument is null.");
            }

            if(android.os.Debug.isDebuggerConnected())
                android.os.Debug.waitForDebugger();

            RegisterInfo info = params[0];
            HttpURLConnection urlConnection = null;

            try {
                Map<String, String> requestBody = new HashMap<>();
                requestBody.put("username", info.getUsername());
                requestBody.put("password", info.getPassword());
                requestBody.put("androidId", info.getAndroidId());
                byte[] bytesBody = getQuery(requestBody).getBytes("UTF-8");

                urlConnection = (HttpURLConnection)new URL(Constant.REGISTER_URL).openConnection();
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlConnection.setFixedLengthStreamingMode(bytesBody.length);
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);

                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                out.write(bytesBody);
                out.flush();
                out.close();

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                return CharStreams.toString(new InputStreamReader(in, "UTF-8"));
            }
            catch (IllegalStateException ill) {
                Log.d("http post", "Connection already open");
                return null;
            }
            catch (IOException io) {
                io.printStackTrace();
                Log.d("http post", "Error " + io.getMessage());
                Log.d("http post", "Cause " + io.getCause());
                return null;
            }
            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String jsonString) {
            if (jsonString == null) {
                return;
            }
            Gson gson = new Gson();
            RegisterReturnStatus result = gson.fromJson(jsonString, RegisterReturnStatus.class);

            if (result == null) {
                Log.d("Register", "JSON string is null");
                return;
            }

            Log.d("Register", result.getStatus());
        }

        private String getQuery(Map<String, String> map)
        {
            String output = null;
            Uri.Builder builder = new Uri.Builder();

            if (map != null) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    builder.appendQueryParameter(entry.getKey(), entry.getValue());
                }

                output = builder.build().getEncodedQuery();
            }

            return output;
        }
    }

    private static class RegisterInfo {
        public RegisterInfo(String androidId, String password, String username) {
            this.androidId = androidId;
            this.password = password;
            this.username = username;
        }

        public String getAndroidId() {
            return androidId;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        private String username;
        private String password;
        private String androidId;
    }

    private static class RegisterReturnStatus {
        private String status;

        public RegisterReturnStatus(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }

    protected void LoginCheck() {
        super.onStart();
        if(sp.getString("et_pr_sta","0").equals("0")){
            login.show();
    }

    }
}

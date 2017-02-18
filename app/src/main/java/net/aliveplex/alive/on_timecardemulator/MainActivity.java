package net.aliveplex.alive.on_timecardemulator;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.provider.Settings.Secure;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    SharedPreferences sp;
    boolean InternetAccess = true;
    Dialog login;
    EditText etUser,etPass;
    Button butLogin,butClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        login = new Dialog(MainActivity.this);
        login.setContentView(R.layout.login_layout);
        login.setTitle("Login");
        login.setCancelable(true);
        etUser = (EditText) login.findViewById(R.id.etUser);
        etPass = (EditText) login.findViewById(R.id.etPass);
        butLogin = (Button) login.findViewById(R.id.butLog);
        butClear = (Button) login.findViewById(R.id.butClear);

        // get sp with default name, this default name is shared across app
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor spEdit = sp.edit();

        final String androidId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);

        checkPermission();
        LoginCheck();

        butLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String username = etUser.getText().toString();
                    String password = etPass.getText().toString();

                    if (!isEmpty(username) && !isEmpty(password)) {
                        SendRegisterAsync regisTask = new SendRegisterAsync(MainActivity.this);
                        regisTask.execute(new RegisterInfo(username, password, androidId));
                        login.dismiss();
                    } else {
                        Toast.makeText(MainActivity.this, "username or password empty", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Error " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        butClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    etUser.setText("");
                    etPass.setText("");
            }
        });

        login.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                finish();
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

    protected void LoginCheck() {
        if(sp.getInt(Constant.FIRSTTIMELOGIN, 0) == 0){
            login.show();
        }
    }

    private boolean isEmpty(String string) {
        return string.trim().length() == 0;
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

    private static class SendRegisterAsync extends AsyncTask<RegisterInfo, String, String> {
        private Context _context;
        private RegisterInfo _regisInfo;

        public SendRegisterAsync(Context context) {
            _context = context;
        }

        @Override
        protected String doInBackground(RegisterInfo... params) {
            if (params[0] == null) {
                throw new IllegalArgumentException("argument is null.");
            }

            if(android.os.Debug.isDebuggerConnected())
                android.os.Debug.waitForDebugger();

            RegisterInfo info = params[0];
            _regisInfo = info;
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
                publishProgress("Error: " + ill.getMessage());
                return null;
            }
            catch (IOException io) {
                publishProgress("Error: " + io.getMessage());

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
                Toast.makeText(_context, "Error: can't process result", Toast.LENGTH_SHORT).show();
                return;
            }

            if (result.getStatus().equals("register student completed")) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(_context);
                SharedPreferences.Editor spEditor = sp.edit();

                spEditor.putString(Constant.UsernameSpKey, _regisInfo.getUsername());
                spEditor.putString(Constant.AndroidIdSpKey, _regisInfo.getAndroidId());
                spEditor.putInt(Constant.FIRSTTIMELOGIN, 1);
                spEditor.apply();
                Toast.makeText(_context, "register completed", Toast.LENGTH_SHORT).show();
            }
            else if (result.getStatus().equals("username or password invalid")) {
                Toast.makeText(_context, "username or password invalid", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(_context, "student not found", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (values.length > 0) {
                Toast.makeText(_context, values[0], Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(_context, "Error when made request", Toast.LENGTH_SHORT).show();
            }
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
        public RegisterInfo(String username, String password, String androidId) {
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
}

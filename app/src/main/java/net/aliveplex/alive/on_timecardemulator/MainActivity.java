package net.aliveplex.alive.on_timecardemulator;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.provider.SyncStateContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    SharedPreferences sp;
    final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 3;
    String PNumber;
    Dialog login,regis;
    EditText etUser,etPass,etUserR,etPassR;
    Button butLogin,butClear,butRegis,butClearR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
        final SharedPreferences.Editor spEdit = sp.edit();
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

        butLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //เช็คว่า ถ้าค่าว่างให้ใส่ใหม่ ถ้าไม่ว่างค่อยส่งไปยัง server
                    if(etUser.getText().toString().equals(sp.getString("et_pr_ID","SuperAdmin"))&&etPass.getText().toString().equals(sp.getString("et_pr_Pass","123456")))
                    {
                        //แก้เก็บเฉพาะ รหัส นศ กับ เบอร์โทรเก็บไว้
                        login.dismiss();
                    }
                    else{
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
                    spEdit.putString("et_pr_ID", etUserR.getText().toString());
                    spEdit.putString("et_pr_Pass", etPassR.getText().toString());
                    spEdit.putString("et_pr_Tel", PNumber);
                    spEdit.commit();
                }
                catch(Exception e){
                    Toast.makeText(MainActivity.this,"Error "+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    void checkPermission(){
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_PHONE_STATE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            }
            else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        else {
            getPhoneNumber();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! do the
                    // calendar task you need to do.
                    getPhoneNumber();
                    Toast.makeText(this, "granted permission", Toast.LENGTH_LONG).show();

                }
                else {
                    Toast.makeText(this, "nope", Toast.LENGTH_LONG).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'switch' lines to check for other
            // permissions this app might request
        }
    }

    public String getPhoneNumber(){
        TelephonyManager tMgr = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        final String mPhoneNumber = tMgr.getLine1Number();
        return mPhoneNumber;
    }


}




package net.aliveplex.alive.on_timecardemulator;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    SharedPreferences sp;
    Dialog login,regis;
    EditText etUser,etPass,etUserR,etPassR;
    Button butLogin,butClear,butRegis,butClearR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
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
        TelephonyManager tMgr = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        final String mPhoneNumber = tMgr.getLine1Number();
        switch (checkAppStart()) {
            case NORMAL:
                login.show();
                break;
            case FIRST_TIME_VERSION:
                Toast.makeText(this, "The app has been update.", Toast.LENGTH_SHORT).show();
                login.show();
                break;
            case FIRST_TIME:
                regis.show();
                break;
            default:
                break;
        }
        butLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(etUser.getText().toString().equals(sp.getString("et_pr_ID",""))&&etPass.getText().toString().equals(sp.getString("et_pr_Pass","")))
                    {
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
                    spEdit.putString("et_pr_Tel", mPhoneNumber);
                    spEdit.commit();
                }
                catch(Exception e){
                    Toast.makeText(MainActivity.this,"Error "+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public enum AppStart {
        FIRST_TIME, FIRST_TIME_VERSION, NORMAL;
    }
    private static final String LAST_APP_VERSION = "last_app_version";
        public AppStart checkAppStart() {
            PackageInfo pInfo;
            SharedPreferences sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(this);
            AppStart appStart = AppStart.NORMAL;
            try {
                pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                int lastVersionCode = sharedPreferences
                        .getInt(LAST_APP_VERSION, -1);
                int currentVersionCode = pInfo.versionCode;
                appStart = checkAppStart(currentVersionCode, lastVersionCode);
                // Update version in preferences
                sharedPreferences.edit()
                        .putInt(LAST_APP_VERSION, currentVersionCode).commit();
            } catch (PackageManager.NameNotFoundException e) {
                Toast.makeText(this, "Unable to determine current app version from pacakge manager. Defenisvely assuming normal app start.", Toast.LENGTH_SHORT).show();
            }
            return appStart;
        }
    public AppStart checkAppStart(int currentVersionCode, int lastVersionCode) {
        if (lastVersionCode == -1) {
            return AppStart.FIRST_TIME;
        } else if (lastVersionCode < currentVersionCode) {
            return AppStart.FIRST_TIME_VERSION;
        } else if (lastVersionCode > currentVersionCode) {
            Toast.makeText(this, "Current version code (" + currentVersionCode
                    + ") is less then the one recognized on last startup ("
                    + lastVersionCode
                    + "). Defenisvely assuming normal app start.", Toast.LENGTH_SHORT).show();
            return AppStart.NORMAL;
        } else {
            return AppStart.NORMAL;
        }
    }



}


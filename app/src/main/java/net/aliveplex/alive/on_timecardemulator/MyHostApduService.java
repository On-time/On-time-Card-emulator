package net.aliveplex.alive.on_timecardemulator;

import android.content.SharedPreferences;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.preference.PreferenceManager;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Created by Aliveplex on 16/11/2559.
 */

public class MyHostApduService  extends HostApduService {
    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        byte[] selectAID = {
                (byte)0x00, // proprietary class
                (byte)0xa4, // select instruction
                (byte)0x04, // select by DF - AID
                (byte)0x00, // First or only - no FCI
                (byte)0x05, // Lc field - have 5 bytes of command data
                (byte)0xF0, 0x00, 0x00, 0x0E, (byte)0x85
        };

        if (Arrays.equals(commandApdu, selectAID)) {
            return new byte[] { (byte)0x90 };
        }
        // if passed through above, it not select command, let process it as our protocal
        try {
            String command = new String(commandApdu, "UTF-8");

            switch (command) {
                case "getid": return getIdentity();
                default: return "unknowcommand".getBytes("UTF-8");
            }
        }
        catch (UnsupportedEncodingException unsupport) {
            throw new RuntimeException(unsupport);
        }

    }

    private byte[] getIdentity() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String studentId = sp.getString(Constant.UsernameSpKey, "");
        String androidId = sp.getString(Constant.AndroidIdSpKey, "");

        try {
            if (studentId.isEmpty() || androidId.isEmpty()) {
                return "notregister".getBytes("UTF-8");
            }

            String identity = studentId.concat(",");
            identity = identity.concat(androidId);

            return identity.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException unsupport) {
            throw new RuntimeException(unsupport);
        }
    }

    @Override
    public void onDeactivated(int reason) {

    }
}

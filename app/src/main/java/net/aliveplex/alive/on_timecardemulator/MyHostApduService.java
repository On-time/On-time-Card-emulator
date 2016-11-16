package net.aliveplex.alive.on_timecardemulator;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;

/**
 * Created by Aliveplex on 16/11/2559.
 */

public class MyHostApduService  extends HostApduService {
    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        return new byte[0];
    }

    @Override
    public void onDeactivated(int reason) {

    }
}

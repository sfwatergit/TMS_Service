package org.odk.collect.android.utilities;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import org.odk.collect.android.ITriggerManagerService;

/**
 * Created with IntelliJ IDEA.
 * Author: Sid Feygin
 * Date: 4/23/13
 * Time: 9:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class TriggerManagerService extends Service {

    private static final String TAG = "TriggerManagerService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreated");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ITriggerManagerService.Stub() {
            /**
             * Implementation of the add() method
             */
            public int add(int value1, int value2) throws RemoteException {
                Log.d(TAG, String.format("TriggerManagerService.add(%d, %d)",
                        value1,
                        value2));
                return value1 + value2;
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroyed");
    }


}

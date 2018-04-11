package bronte.flashcards;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * BroadcastReceiver for resetting hourly notification alarm when device is rebooted.
 */
public class bootReceiver extends BroadcastReceiver {

    private MainActivity mainActivity;

    public bootReceiver(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) {
            return;
        }
        if (intent.getAction().equals(MainActivity.BOOT_COMPLETED)) {
            if (mainActivity.isAlarmSet) {
                mainActivity.setNotification(true);
            }
        }
    }
}

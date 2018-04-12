package bronte.flashcards;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

/**
 * A class for what to do at an alarm; i.e. the set time when a notification is due to be sent.
 * Sends a notification asking about a card.
 */
public class NotificationAlarmReceiver extends BroadcastReceiver {

    private static final String TEXT_MESSAGE = "I'm not studying! Go annoy me!";

    @Override
    public void onReceive(Context context, Intent alarmIntent) {

        // Get deck.
        byte[] deckBytes = alarmIntent.getByteArrayExtra("deck_bytes");
        Parcel deckParcel = MainActivity.ParcelableUtil.unmarshall(deckBytes);
        Deck deck = new Deck(deckParcel);

        // Pick a random card. Shuffle deck and select first card that is not a known card.
        ArrayList<Card> shuffledDeck = new ArrayList<>(deck.getCards());
        Collections.shuffle(shuffledDeck);
        int i;
        for (i = 0; i < shuffledDeck.size(); i++) {
            if (!shuffledDeck.get(i).isKnownWord()) {
                break;
            }
        }
        // If i is the size of the deck, then all of the cards are known cards, and this deck cannot
        // be used for notifications. This should never happen, but if it does, skip this
        // notification.
        if (i == shuffledDeck.size()) {
            return;
        }
        // Find index of chosen card in original deck.
        Card card = shuffledDeck.get(i);
        int cardIndex;
        for (cardIndex = 0; cardIndex < deck.size(); cardIndex++) {
            if (card.equals(deck.getCards().get(cardIndex))) {
                break;
            }
        }
        // Also skip notification if card cannot be found.
        if (cardIndex == shuffledDeck.size()) {
            return;
        }

        // Increment number of ignored notifications in shared preferences.
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                MainActivity.NOTIFICATION_PREFERENCES, Context.MODE_PRIVATE);
        int numIgnored = sharedPreferences.getInt(MainActivity.PREF_NUM_IGNORED_NOTIFS, -1);
        int ignoreThreshold = sharedPreferences.getInt(
                MainActivity.PREF_IGNORED_NOTIFS_THRESHOLD, -1);
        if (numIgnored >= 0) {
            numIgnored++;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(MainActivity.PREF_NUM_IGNORED_NOTIFS, numIgnored);
            editor.apply();
        }

        // Friend texting stuff.
        //TODO: Add warning for when 1 below threshold.
        if (numIgnored >= 0 && ignoreThreshold >= 0) {
            if (numIgnored >= ignoreThreshold) {
                //TODO: Handle not having permission.
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                        == PackageManager.PERMISSION_GRANTED) {
                    SmsManager sms = SmsManager.getDefault();
                    sms.sendTextMessage("0448827738", null, TEXT_MESSAGE, null, null);
                }

            }
        }
        Toast.makeText(context, "up to " + numIgnored, Toast.LENGTH_SHORT).show();

        // Notification stuff.
        Intent notifIntent = new Intent(context, ViewDeckActivity.class);
        notifIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notifIntent.putExtra("deck", deck);
        notifIntent.putExtra("card_index", cardIndex);
        notifIntent.putExtra("from_notif", true);
        int requestCode = new Random().nextInt();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, requestCode, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notifBuilder =
                new NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
                        .setSmallIcon(R.drawable.heart)
                        .setContentTitle("Flashcards")
                        .setContentText("What is " + card.getFront() + "?")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setVibrate(new long[] { 500, 500, 500, 500, 500 })
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

        // Send notification.
        int notifID = UUID.randomUUID().hashCode();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notifID, notifBuilder.build());
    }
}

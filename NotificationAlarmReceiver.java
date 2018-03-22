package bronte.flashcards;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.Random;
import java.util.UUID;

/**
 * A class for what to do at an alarm; i.e. the set time when a notification is due to be sent.
 * Sends a notification asking about a card.
 */
public class NotificationAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent alarmIntent) {

        // Find card to ask.
        byte[] deckBytes = alarmIntent.getByteArrayExtra("deck_bytes");
        Parcel deckParcel = MainActivity.ParcelableUtil.unmarshall(deckBytes);
        Deck deck = new Deck(deckParcel);
        // Pick a random card.
        int cardIndex = new Random().nextInt(deck.size());
        Card card = deck.getCards().get(cardIndex);

        // Notification stuff
        Intent notifIntent = new Intent(context, NotificationQuestionActivity.class);
        notifIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notifIntent.putExtra("deck", deck);
        notifIntent.putExtra("card_index", cardIndex);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notifBuilder =
                new NotificationCompat.Builder(context, "")
                        .setSmallIcon(R.drawable.heart)
                        .setContentTitle("Flashcards")
                        .setContentText("What is " + card.getFront() + "?")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

        int notifID = UUID.randomUUID().hashCode();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notifID, notifBuilder.build());
    }
}

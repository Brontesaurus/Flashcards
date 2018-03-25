package bronte.flashcards;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private ArrayList<Deck> decks;
    private Deck testDeck;
    private AlarmManager alarmManager;
    private PendingIntent pendingAlarmIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        decks = new ArrayList<>();
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        testDeck = new Deck("test deck");
        testDeck.addCard("aye", "lol", "");
        testDeck.addCard("what card?", "this card!", "");
        testDeck.addCard("what team?", "wildcats!", "get ya head in the game");
        decks.add(testDeck);

        Deck anotherDeck = new Deck("another deck");
        anotherDeck.addCard("hello", "goodbye", "");
        anotherDeck.addCard("aaa", "bbb", "");
        anotherDeck.addCard("i am typing", "many things", "");
        anotherDeck.addCard("aaaaaaaaaaaa", "bbbbbbbbbbb", "");
        decks.add(anotherDeck);

        LinearLayout mainLayout = findViewById(R.id.main_layout);

        // Loop through decks and create buttons for them, so they can be opened and viewed.
        for (final Deck deck : decks) {
            String text = "Adding deck called " + deck.getName();
            Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
            toast.show();
            final Button button = new Button(this);
            button.setText(deck.getName());
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    button.setEnabled(false);
                    openDeck(deck);
                    button.setEnabled(true);
                }
            });
            mainLayout.addView(button);
        }

        // Make the new deck button do something.
        Button newDeckButton = findViewById(R.id.new_deck_button);
        newDeckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = "This button doesn't do anything yet fam.";
                Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    /**
     * Action performed by pressing the notification settings button. Sets hourly notifications. If
     * notifications are already set, this button will remove them.
     */
    //TODO: If alarm is set, make it reset when device is rebooted.
    public void onClickNotificationSettings(View view) {
        // Ensure alarm manager was accessed properly.
        if (alarmManager == null) {
            Toast.makeText(this, "There was a problem setting notifications",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if alarm has already been scheduled, and remove it if it has.
        if (alarmManager.getNextAlarmClock() != null) {
            alarmManager.cancel(pendingAlarmIntent);
            Toast.makeText(
                    this, "Hourly notifications removed", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set alarm.
        Intent alarmIntent = new Intent(this, NotificationAlarmReceiver.class);
        byte[] deckBytes = ParcelableUtil.marshall(testDeck);
        alarmIntent.putExtra("deck_bytes", deckBytes);
        pendingAlarmIntent = PendingIntent.getBroadcast(
                this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Make time of alarm the start of the next hour.
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.HOUR_OF_DAY, 1);

        // Set alarm to repeat every hour.
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_HOUR, pendingAlarmIntent);
        Toast.makeText(this, "Hourly notifications set", Toast.LENGTH_SHORT).show();

    }

    // Helper method to trigger a notification, used for debugging. Sets a non-repeating alarm for
    // the current time in order to keep the same functionality as a naturally occurring
    // notification.
    public void onClickTriggerNotification(View view) {
        // Set alarm.
        Intent alarmIntent = new Intent(this, NotificationAlarmReceiver.class);
        byte[] deckBytes = ParcelableUtil.marshall(testDeck);
        alarmIntent.putExtra("deck_bytes", deckBytes);
        pendingAlarmIntent = PendingIntent.getBroadcast(
                this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Make time of alarm now.
        Calendar calendar = Calendar.getInstance();

        // Set alarm.
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingAlarmIntent);
        Toast.makeText(this, "Single notification set", Toast.LENGTH_SHORT).show();
    }

    // Open a deck and view it. Starts a new deck viewing activity.
    private void openDeck(Deck deck) {
        Intent intent = new Intent(this, ViewDeckActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("deck", deck);
        startActivity(intent);
    }

    /**
     * Helper method for generating decks and flashcards I want to study because I am too lazy to
     * code manual addition.
      */
    private void generateFlashcards() {

    }

    public static class ParcelableUtil {
        public static byte[] marshall(Parcelable parcelable) {
            Parcel parcel = Parcel.obtain();
            parcelable.writeToParcel(parcel, 0);
            byte[] bytes = parcel.marshall();
            parcel.recycle();
            return bytes;
        }

        public static Parcel unmarshall(byte[] bytes) {
            Parcel parcel = Parcel.obtain();
            parcel.unmarshall(bytes, 0, bytes.length);
            parcel.setDataPosition(0); // This is extremely important!
            return parcel;
        }
    }

}


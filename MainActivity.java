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
    private Button notificationSettingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        decks = new ArrayList<>();
        notificationSettingsButton = findViewById(R.id.button_notif);

        Deck testDeck = new Deck("test deck");
        testDeck.addCard("aye", "lol", "");
        testDeck.addCard("what card?", "this card!", "");
        testDeck.addCard("what team?", "wildcats!", "get ya head in the game");
        decks.add(testDeck);

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

        // Alarm stuff
        Intent alarmIntent = new Intent(this, NotificationAlarmReceiver.class);
        byte[] deckBytes = ParcelableUtil.marshall(testDeck);
        alarmIntent.putExtra("deck_bytes", deckBytes);
        PendingIntent pendingAlarmIntent =
                PendingIntent.getBroadcast(
                        this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 2);
        calendar.set(Calendar.MINUTE, 12);
        calendar.set(Calendar.SECOND, 1);

        manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingAlarmIntent);
    }

    public void onClickNotificationSettings(View view) {

    }

    // Open a deck and view it. Starts a new deck viewing activity.
    private void openDeck(Deck deck) {
        Intent intent = new Intent(this, ViewDeckActivity.class);
        intent.putExtra("deck", deck);
        startActivity(intent);
    }

    public static class ParcelableUtil {
        public static byte[] marshall(Parcelable parceable) {
            Parcel parcel = Parcel.obtain();
            parceable.writeToParcel(parcel, 0);
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


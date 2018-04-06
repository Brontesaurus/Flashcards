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
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private ArrayList<Deck> decks;
    private AlarmManager alarmManager;
    private PendingIntent pendingAlarmIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        decks = new ArrayList<>();
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        generateFlashcards();

        LinearLayout mainLayout = findViewById(R.id.main_layout);

        // Loop through decks and create buttons for them, so they can be opened and viewed.
        for (final Deck deck : decks) {
            String text = "Adding deck called " + deck.getName();
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            final Button button = new Button(this);
            button.setText(deck.getName());
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDeck(deck);
                }
            });
            mainLayout.addView(button);
        }

        //TODO: Make the new deck button do something.
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
    //TODO: Figure out why turning notifications off does not work.
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
        int deckIndex = new Random().nextInt(decks.size()); // Pick random deck to ask from.
        byte[] deckBytes = ParcelableUtil.marshall(decks.get(deckIndex));
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
        int deckIndex = new Random().nextInt(decks.size()); // Pick random deck to ask from.
        byte[] deckBytes = ParcelableUtil.marshall(decks.get(deckIndex));
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
        Deck places = new Deck("places 1");
        places.addCard("대학교", "university", "");
        places.addCard("교실", "classroom", "");
        places.addCard("강의실", "lecture theatre", "");
        places.addCard("도서관", "library", "");
        places.addCard("은행", "bank", "");
        places.addCard("서점", "book shop", "");
        places.addCard("학생 식당", "canteen", "");
        places.addCard("우체국", "post office", "");
        places.addCard("집", "home/house", "");
        places.addCard("시내", "downtown", "");
        decks.add(places);

        Deck morePlaces = new Deck("places 2");
        morePlaces.addCard("공원", "park", "");
        morePlaces.addCard("시장", "market", "");
        morePlaces.addCard("극장", "cinema", "");
        morePlaces.addCard("역", "station", "");
        morePlaces.addCard("공항", "airport", "");
        morePlaces.addCard("병원", "hospital", "");
        morePlaces.addCard("음식점", "restaurant", "");
        morePlaces.addCard("식당", "restaurant", "");
        morePlaces.addCard("백화점", "department store", "");
        morePlaces.addCard("편의점", "convenience store", "");
        morePlaces.addCard("가게", "corner shop", "");
        morePlaces.addCard("노래방", "singing room", "");
        decks.add(morePlaces);

        Deck verbs = new Deck("verbs 1");
        verbs.addCard("해요", "to do", "");
        verbs.addCard("공부해요", "to study", "");
        verbs.addCard("숙제해요", "to do homework", "");
        verbs.addCard("전화해요", "to telephone", "");
        verbs.addCard("운동해요", "to exercise", "");
        verbs.addCard("이야기해요", "to talk/chat", "");
        verbs.addCard("식사해요", "to have a meal", "");
        verbs.addCard("일해요", "to work", "");
        decks.add(verbs);

        Deck moreVerbs = new Deck("verbs 2");
        moreVerbs.addCard("놀아요", "to play", "");
        moreVerbs.addCard("가요", "to go", "");
        moreVerbs.addCard("만나요", "to meet", "");
        moreVerbs.addCard("잠자요", "to sleep", "");
        moreVerbs.addCard("와요", "to come", "");
        moreVerbs.addCard("봐요", "to see", "");
        moreVerbs.addCard("시험봐요", "to take an exam", "");
        moreVerbs.addCard("먹어요", "to eat", "");
        moreVerbs.addCard("읽어요", "to read", "");
        moreVerbs.addCard("마셔요", "to drink", "");
        moreVerbs.addCard("써요", "to write", "");
        decks.add(moreVerbs);
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
            parcel.setDataPosition(0);
            return parcel;
        }
    }

}


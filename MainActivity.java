package bronte.flashcards;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
    private PendingIntent alarmPendingIntent;
    private SharedPreferences sharedPreferences;

    private ComponentName bootReceiver;
    private PackageManager packageManager;

    boolean isAlarmSet = false;

    // Channel stuff for Android 8.0 notifications.
    static final String CHANNEL_ID = "flashcard";
    private static final String CHANNEL_NAME = "flashcard_question_channel";
    private static final int HOURLY_ALARM_REQUEST_CODE = 1001;
    static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    // Values for shared preferences.
    static final String NOTIFICATION_PREFERENCES = "notification_preferences";
    static final String PREF_NUM_IGNORED_NOTIFS = "ignored_notifs";
    // Number of notifications that can be ignored before a friend will be texted. Negative number
    // denotes friend texting has not been set.
    static final String PREF_IGNORED_NOTIFS_THRESHOLD = "notifs_threshold";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        decks = new ArrayList<>();
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        bootReceiver = new ComponentName(this, BootReceiver.class);
        packageManager = this.getPackageManager();

        // Retrieve data from shared preferences, and set if necessary.
        sharedPreferences = getSharedPreferences(NOTIFICATION_PREFERENCES, Context.MODE_PRIVATE);
        int numIgnored = sharedPreferences.getInt(PREF_NUM_IGNORED_NOTIFS, -1);
        if (numIgnored < 0) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(PREF_NUM_IGNORED_NOTIFS, 0);
            editor.apply();
        }
        int ignoreThreshold = sharedPreferences.getInt(PREF_IGNORED_NOTIFS_THRESHOLD, -1);
        if (ignoreThreshold < 0) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(PREF_IGNORED_NOTIFS_THRESHOLD, 3);
            editor.apply();
        }

        generateFlashcards();

        LinearLayout mainLayout = findViewById(R.id.main_layout);

        // Loop through decks and create buttons for them, so they can be opened and viewed.
        for (final Deck deck : decks) {
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

        // Notification channel stuff for Android 8.0 onwards.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Action performed by pressing the notification settings button. Sets hourly notifications. If
     * notifications are already set, this button will remove them.
     */
    public void onClickNotificationSettings(View view) {
        // Check if alarm has already been scheduled, and remove it if it has.
        if (alarmManager != null && isAlarmSet) {
            alarmManager.cancel(alarmPendingIntent);
            isAlarmSet = false;
            Toast.makeText(
                    this, "Hourly notifications removed", Toast.LENGTH_SHORT).show();

            // Disable alarms to be reset on reboot.
            packageManager.setComponentEnabledSetting(bootReceiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

            return;
        }

        setNotification(true);

    }

    // Helper method to trigger a notification, used for debugging. Sets a non-repeating alarm for
    // the current time in order to keep the same functionality as a naturally occurring
    // notification.
    public void onClickTriggerNotification(View view) {
        setNotification(false);
    }

    void setNotification(boolean isRepeating) {

        // Request permission to send a text to someone if permission has not already been granted.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},0);
        }

        Intent alarmIntent = new Intent(this, NotificationAlarmReceiver.class);
        int deckIndex = new Random().nextInt(decks.size()); // Pick random deck to ask from.
        byte[] deckBytes = ParcelableUtil.marshall(decks.get(deckIndex));

        int requestCode;
        if (isRepeating) {
            requestCode = HOURLY_ALARM_REQUEST_CODE;
        } else {
            // If single notification, generate a random request code so this notification does not
            // interfere with any others.
            requestCode = new Random().nextInt();
        }

        alarmIntent.putExtra("deck_bytes", deckBytes);
        alarmPendingIntent = PendingIntent.getBroadcast(
                this, requestCode, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        if (isRepeating) {
            // Make time of the first alarm the start of the next hour.
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.add(Calendar.HOUR_OF_DAY, 1);

            // Set alarm to repeat every hour.
            alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_HOUR, alarmPendingIntent);
            Toast.makeText(this, "Hourly notifications set", Toast.LENGTH_SHORT).show();
            isAlarmSet = true;

            // Enable the alarm to be reset on reboot.
            packageManager.setComponentEnabledSetting(bootReceiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        } else {
            // Set single alarm.
            alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), alarmPendingIntent);
            Toast.makeText(this, "Single notification set", Toast.LENGTH_SHORT).show();
        }
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

        Deck drinks = new Deck("drinks 1");
        drinks.addCard("물", "water", "");
        drinks.addCard("차", "tea", "");
        drinks.addCard("홍차", "black tea", "");
        drinks.addCard("녹차", "green tea", "");
        drinks.addCard("인삼차", "insam tea", "");
        drinks.addCard("커피", "coffee", "");
        drinks.addCard("콜라", "cola", "");
        drinks.addCard("레모네이드", "lemonade", "");
        drinks.addCard("주스", "juice", "");
        decks.add(drinks);

        Deck moreDrinks = new Deck("drinks 2");
        moreDrinks.addCard("우유", "milk", "");
        moreDrinks.addCard("식혜", "rice nectar", "");
        moreDrinks.addCard("수정과", "cinnamon punch", "");
        moreDrinks.addCard("음료수", "soft drink", "");
        moreDrinks.addCard("술", "alcohol", "");
        moreDrinks.addCard("맥주", "beer", "");
        moreDrinks.addCard("포도주", "wine", "");
        moreDrinks.addCard("소주", "soju", "");
        decks.add(moreDrinks);
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


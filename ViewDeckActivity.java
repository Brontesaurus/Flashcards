package bronte.flashcards;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity for viewing a deck of flashcards.
 */
//TODO: Send changed known word data back to main activity to update original copy of deck.
public class ViewDeckActivity extends AppCompatActivity {
    private Deck deck;
    private Card currentCard; // Current card being displayed to the user.
    private int cardIndex; // The index of the current card being displayed to the user.
    // Whether the flashcard is showing the front or back.
    private boolean cardDirection = CARD_FRONT;
    // Whether the explanation for the current card is currently being displayed to the user.
    private boolean showingExplanation = false;
    private TextView flashcard;
    private TextView explanation;
    private Button prevButton;
    private Button nextButton;
    private Button knownWordButton;
    private Button explanationButton;

    private static final String GREY_COLOUR = "#C0C0C0";
    private static final String BLACK_COLOUR = "#000000";
    private static final boolean CARD_FRONT = true;
    private static final boolean CARD_BACK = false;
    static final String TAG = "FLASHCARDS";

    private static final String NO_EXPL_MSG = "This card has no explanation!";
    private static final String SHOW_EXPL = "Show Explanation";
    private static final String HIDE_EXPL = "Hide Explanation";
    private static final String YES_KNOWN = "Remove from known words";
    private static final String NOT_KNOWN = "Add to known words";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_deck);
        prevButton = findViewById(R.id.button_prev);
        prevButton.setText("<");
        nextButton = findViewById(R.id.button_next);
        knownWordButton = findViewById(R.id.button_known_word);
        explanationButton = findViewById(R.id.button_expl);
        explanation = findViewById(R.id.expl_text);

        // Get deck contents from intent.
        Intent intent = getIntent();
        deck = intent.getParcelableExtra("deck");
        cardIndex = intent.getIntExtra("card_index", 0);
        boolean fromNotif = intent.getBooleanExtra("from_notif", false);

        // Put deck contents in card.
        flashcard = findViewById(R.id.card_text);
        currentCard = deck.getCards().get(cardIndex); // Get current card in deck.
        flashcard.setText(currentCard.getFront());

        // Set button functionality according to card contents and position.
        setKnownWordButtonText();
        if (cardIndex == 0) {
            disableButton(prevButton);
        } else if (cardIndex == deck.size() - 1) {
            disableButton(nextButton);
        }

        if (fromNotif) {
            resetIgnoredNotifications();
        }
    }

    /**
     * Processes the new intent when opening the activity from a notification. Changes the card
     * display to the card from the notification.
     * @param intent the intent sent from the notification.
     */
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Deck newDeck = intent.getParcelableExtra("deck");
        int newCardIndex = intent.getIntExtra("card_index", 0);

        // If new deck, reinitialise deck.
        boolean sameDeck = deck.equals(newDeck);
        if (!sameDeck) {
            deck = newDeck;
        }

        // If new card, set to new card. If the deck has changed, there will always be a new card.
        if (cardIndex != newCardIndex || !sameDeck) {
            cardIndex = newCardIndex;
        }
        // Call this even if the card is the same as before, to reset the view to the front of the
        // card.
        setCardContents();
        setKnownWordButtonText();

        // Reset previous and next buttons.
        if (cardIndex == 0) {
            disableButton(prevButton);
            enableButton(nextButton);
        } else if (cardIndex == deck.size() - 1) {
            disableButton(nextButton);
            enableButton(prevButton);
        } else {
            enableButton(prevButton);
            enableButton(nextButton);
        }

        resetIgnoredNotifications();
    }

    public void onClickFlashcard(View view) {
        cardDirection = !cardDirection; // Flip card direction.
        if (cardDirection == CARD_FRONT) {
            flashcard.setText(currentCard.getFront());
            explanationButton.setVisibility(View.GONE);
        } else { // If back of card.
            flashcard.setText(currentCard.getBack());
            if (currentCard.hasExplanation()) {
                explanationButton.setVisibility(View.VISIBLE);
            }
        }
    }

    public void onClickPrevButton(View view) {
        removeExplanation();
        if (cardIndex > 0) {
            // Set new contents of flashcard.
            cardIndex--;
            setCardContents();
            // Enable next button if it was disabled.
            if (!nextButton.isClickable()) {
                enableButton(nextButton);
            }
        }
        // Disable prev button if beginning of deck is reached.
        if (cardIndex == 0) {
            disableButton(prevButton);
        }
        // Reset known word button for new card.
        setKnownWordButtonText();
    }

    public void onClickNextButton(View view) {
        removeExplanation();
        if (cardIndex < deck.size() - 1) {
            // Set new contents of flashcard.
            cardIndex++;
            setCardContents();
            // Enable prev button if it was disabled.
            if (!prevButton.isClickable()) {
                enableButton(prevButton);
            }
        }
        // Disable next button if end of deck is reached.
        if (cardIndex == deck.size() - 1) {
            disableButton(nextButton);
        }
        // Reset known word button for new card.
        setKnownWordButtonText();
    }

    public void onClickExplanationButton(View view) {
        if (showingExplanation) {
            removeExplanation();
            explanationButton.setText(SHOW_EXPL);
        } else {
            if (currentCard.hasExplanation()) {
                explanation.setText(currentCard.getExplanation());
                showingExplanation = true;
                explanationButton.setText(HIDE_EXPL);
            } else {
                Toast.makeText(getApplicationContext(), NO_EXPL_MSG, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onClickKnownWordButton(View view) {
        String text;
        if (currentCard.isKnownWord()) {
            currentCard.setKnownWord(false);
            text = "Removed " + currentCard.getFront() + " from known words";
        } else {
            currentCard.setKnownWord(true);
            text = "Added " + currentCard.getFront() + " to known words";
        }
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        setKnownWordButtonText();
    }

    // Set the card contents when a new card is being displayed.
    private void setCardContents() {
        currentCard = deck.getCards().get(cardIndex);
        flashcard.setText(currentCard.getFront());
        cardDirection = CARD_FRONT;
        removeExplanation();
        explanationButton.setVisibility(View.GONE);
    }

    // Helper method to disable directional buttons when the displayed flashcard is at the start or
    // end of the deck.
    private void disableButton(Button button) {
        button.setClickable(false);
        button.setTextColor(Color.parseColor(GREY_COLOUR));
    }

    // Helper method to disable directional buttons when the displayed flashcard is no longer at the
    // start or end of the deck.
    private void enableButton(Button button) {
        button.setClickable(true);
        button.setTextColor(Color.parseColor(BLACK_COLOUR));
    }

    // Helper method to remove explanation text. Used when the displayed card is changed, or when
    // the user wants to hide the explanation.
    private void removeExplanation() {
        explanation.setText("");
        showingExplanation = false;
        explanationButton.setText(SHOW_EXPL);
    }

    private void setKnownWordButtonText() {
        if (currentCard.isKnownWord()) {
            knownWordButton.setText(YES_KNOWN);
        } else {
            knownWordButton.setText(NOT_KNOWN);
        }
    }

    // After a notification has been opened, reset the number of ignored notifications in shared
    // preferences.
    private void resetIgnoredNotifications() {
        SharedPreferences sharedPreferences = getSharedPreferences(
                MainActivity.NOTIFICATION_PREFERENCES, Context.MODE_PRIVATE);
        int numIgnored = sharedPreferences.getInt(MainActivity.PREF_NUM_IGNORED_NOTIFS, -1);
        if (numIgnored > 0) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(MainActivity.PREF_NUM_IGNORED_NOTIFS, 0);
            editor.apply();
        }
    }
}

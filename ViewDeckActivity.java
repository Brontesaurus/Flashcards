package bronte.flashcards;

import android.content.Intent;
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
public class ViewDeckActivity extends AppCompatActivity {
    private Deck deck;
    private Card currentCard; // Current card being displayed to the user.
    private int cardIndex = 0; // The index of the current card being displayed to the user.
    private boolean cardDirection = true; // Whether the flashcard is showing the front or back.
    // Whether the explanation for the current card is currently being displayed to the user.
    private boolean showingExplanation = false;
    private TextView flashcard;
    private TextView explanation;
    private Button prevButton;
    private Button nextButton;
    private Button explanationButton;

    private static final String GREY_COLOUR = "#C0C0C0";
    private static final String BLACK_COLOUR = "#000000";
    private static final boolean CARD_FRONT = true;
    private static final boolean CARD_BACK = false;

    private static final String NO_EXPL_MSG = "This card has no explanation!";
    private static final String SHOW_EXPL = "Show Explanation";
    private static final String HIDE_EXPL = "Hide Explanation";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_deck);
        prevButton = findViewById(R.id.button_prev);
        prevButton.setText("<");
        nextButton = findViewById(R.id.button_next);
        explanationButton = findViewById(R.id.button_expl);
        explanation = findViewById(R.id.expl_text);

        // Get deck contents from intent.
        Intent intent = getIntent();
        deck = intent.getParcelableExtra("deck");

        // Put deck contents in card.
        flashcard = findViewById(R.id.card_text);
        currentCard = deck.getCards().get(0); // Get first card in deck.
        flashcard.setText(currentCard.getFront());

        // Disable prev button since at start.
        disableButton(prevButton);
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
        // Disable prev button if beginning of deck is reached.
        if (cardIndex == deck.size() - 1) {
            disableButton(nextButton);
        }
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
}

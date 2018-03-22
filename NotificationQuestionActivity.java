package bronte.flashcards;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * An activity only activated after opening the app from a notification asking about a card.
 */
public class NotificationQuestionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // Retrieve deck and card details from intent.
        Deck deck = getIntent().getParcelableExtra("deck");
        int cardIndex = getIntent().getIntExtra("card_index", 0);

        TextView questionText = findViewById(R.id.question_text);
        String displayText = "What is " + deck.getCards().get(cardIndex).getFront() + "?";
        questionText.setText(displayText);
    }
}

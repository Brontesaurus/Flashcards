package bronte.flashcards;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ViewDeckActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_deck);

        // Get deck contents from intent.
        Intent intent = getIntent();
        Deck deck = intent.getParcelableExtra("deck");

        // Put deck contents in card.
        TextView textView = findViewById(R.id.card_text);
        textView.setText(deck.getCards().get(0).getFront());
    }
}

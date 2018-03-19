package bronte.flashcards;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList<Deck> decks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        decks = new ArrayList<>();

        Deck testDeck = new Deck("test deck");
        testDeck.addCard("aye", "lol", "");
        testDeck.addCard("what card?", "this card!", "");
        testDeck.addCard("what team?", "wildcats!", "get ya head in the game");
        decks.add(testDeck);

        LinearLayout mainLayout = findViewById(R.id.main_layout);

        //ArrayList<Button> deckButtons = new ArrayList<>();
        for (final Deck deck : decks) {
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
    }

    private void openDeck(Deck deck) {
        Intent intent = new Intent(this, ViewDeckActivity.class);
        intent.putExtra("deck", deck);
        startActivity(intent);
    }
}


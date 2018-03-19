package bronte.flashcards;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

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

    // Open a deck and view it. Starts a new deck viewing activity.
    private void openDeck(Deck deck) {
        Intent intent = new Intent(this, ViewDeckActivity.class);
        intent.putExtra("deck", deck);
        startActivity(intent);
    }
}


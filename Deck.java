package bronte.flashcards;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

//TODO: Implement warning for when all cards become known.
public class Deck implements Parcelable {
    private String name;
    private ArrayList<Card> cards;

    public Deck(String name) {
        this.name = name;
        cards = new ArrayList<>();
    }

    public Deck(Parcel parcel) {
        name = parcel.readString();
        cards = new ArrayList<>();
        parcel.readTypedList(cards, Card.CREATOR);
    }

    public String getName() {
        return name;
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public int size() {
        return cards.size();
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public void addCard(String front, String back, String explanation) {
        cards.add(new Card(front, back, explanation));
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (this == object) {
            return true;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        Deck other = (Deck) object;
        if (!name.equals(other.getName())) {
            return false;
        }
        if (cards.size() != other.size()) {
            return false;
        }
        for (int i = 0; i < cards.size(); i++) {
            if (!cards.get(i).equals(other.getCards().get(i))) {
                return false;
            }
        }
        return true;
    }

    /* Parcelable Things */

    // Required for parcelable, but is ignored.
    @Override
    public int describeContents() {
        return 0;
    }

    // Write class contents to parcel.
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeTypedList(cards);
    }

    protected static final Creator<Deck> CREATOR = new Creator<Deck>() {

        @Override
        public Deck createFromParcel(Parcel parcel) {
            return new Deck(parcel);
        }

        @Override
        public Deck[] newArray(int size) {
            return new Deck[size];
        }
    };
}

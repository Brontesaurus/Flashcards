package bronte.flashcards;

import android.os.Parcel;
import android.os.Parcelable;

public class Card implements Parcelable {
    private String front;
    private String back;
    private String explanation;

    public Card(String front, String back, String explanation){
        this.front = front;
        this.back = back;
        this.explanation = explanation;
    }

    public Card(Parcel parcel) {
        front = parcel.readString();
        back = parcel.readString();
        explanation = parcel.readString();
    }

    public void setFront(String front) {
        this.front = front;
    }

    public void setBack(String back) {
        this.back = back;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getFront() {
        return front;
    }

    public String getBack() {
        return back;
    }

    public String getExplanation() {
        return explanation;
    }

    public boolean hasExplanation() {
        return !explanation.isEmpty();
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
        Card other = (Card) object;
        if (!front.equals(other.getFront())) {
            return false;
        }
        if (!back.equals(other.getBack())) {
            return false;
        }
        return explanation.equals(other.getExplanation()) ||
                (!hasExplanation() && !other.hasExplanation());
    }

    /* Parcelable Things */

    // Required for parcelable, but is ignored.
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(front);
        parcel.writeString(back);
        parcel.writeString(explanation);
    }

    protected static final Creator<Card> CREATOR = new Creator<Card>() {

        @Override
        public Card createFromParcel(Parcel parcel) {
            return new Card(parcel);
        }

        @Override
        public Card[] newArray(int size) {
            return new Card[size];
        }
    };
}

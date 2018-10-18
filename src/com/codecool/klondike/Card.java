package com.codecool.klondike;

import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.util.*;

public class Card extends ImageView {

    private int suit;
    private int rank;
    private boolean faceDown;
    private CardSuit cardSuit;
    private CardRank cardRank;

    private Image backFace;
    private Image frontFace;
    private Pile containingPile;
    private DropShadow dropShadow;

    static Image cardBackImage;
    private static final Map<String, Image> cardFaceImages = new HashMap<>();
    public static final int WIDTH = 150;
    public static final int HEIGHT = 215;

    public Card(int suit, int rank, boolean faceDown) {
        this.suit = suit;
        this.rank = rank;
        this.faceDown = faceDown;
        this.dropShadow = new DropShadow(2, Color.gray(0, 0.75));
        backFace = cardBackImage;
        frontFace = cardFaceImages.get(getShortName());
        setImage(faceDown ? backFace : frontFace);
        setEffect(dropShadow);
    }

    public int getSuit() {
        return suit;
    }

    public int getRank() {
        return rank;
    }

    public boolean isFaceDown() {
        return faceDown;
    }

    public String getShortName() {
        return "S" + suit + "R" + rank;
    }

    public DropShadow getDropShadow() {
        return dropShadow;
    }

    public Pile getContainingPile() {
        return containingPile;
    }

    public void setContainingPile(Pile containingPile) {
        this.containingPile = containingPile;
    }

    public void moveToPile(Pile destPile) {
        this.getContainingPile().getCards().remove(this);
        destPile.addCard(this);
    }

    public void flip() {
        faceDown = !faceDown;
        setImage(faceDown ? backFace : frontFace);
    }

    @Override
    public String toString() {
        return "The " + "Rank" + rank + " of " + "Suit" + suit;
    }

    public static boolean isOppositeColor(Card card1, Card card2) {

        boolean isOppositeColor = false;
        if (card1.suit == CardSuit.HEARTS.getCardSuitNumber() || card1.suit == CardSuit.DIAMONDS.getCardSuitNumber()) {
            if (card2.suit == CardSuit.SPADES.getCardSuitNumber() || card2.suit == CardSuit.CLUBS.getCardSuitNumber()) {
                isOppositeColor = true;
            }
        }
        if (card2.suit == CardSuit.HEARTS.getCardSuitNumber() || card2.suit == CardSuit.DIAMONDS.getCardSuitNumber()) {
            if (card1.suit == CardSuit.SPADES.getCardSuitNumber() || card1.suit == CardSuit.CLUBS.getCardSuitNumber()) {
                isOppositeColor = true;
            }
        }
        return isOppositeColor;

    }

    public static boolean isSameSuit(Card card1, Card card2) {
        return card1.getSuit() == card2.getSuit();
    }

    public static List<Card> createNewDeck() {
        List<Card> result = new ArrayList<>();
        for (int suit = 1; suit < 5; suit++) {
            for (int rank = 1; rank < 14; rank++) {
                result.add(new Card(suit, rank, true));
            }
        }
        return result;
    }

    public static void loadCardImages() {
        cardBackImage = new Image("card_images/card_back.png");
        String cardName = "";
        for (CardSuit cardSuit : CardSuit.values()) {
            String cardSuitName = cardSuit.getCardSuitName();
            int cardSuitNumber = cardSuit.getCardSuitNumber();
            for (CardRank cardRank : CardRank.values()) {
                int cardRankNumber = cardRank.getCardRank();
                cardName = cardSuitName + cardRankNumber;
                String cardId = "S" + cardSuitNumber + "R" + cardRankNumber;
                String imageFileName = "card_images/" + cardName + ".png";
                cardFaceImages.put(cardId, new Image(imageFileName));
            }
        }

    }

    public enum CardSuit {
        HEARTS("hearts", 1),
        DIAMONDS("diamonds", 2),
        SPADES("spades", 3),
        CLUBS("clubs", 4);


        private final String cardSuitName;
        private final int cardSuitNumber;

        CardSuit(String cardSuitName, int cardSuitNumber) {
            this.cardSuitName = cardSuitName;
            this.cardSuitNumber = cardSuitNumber;
        }

        public String getCardSuitName() {
            return cardSuitName;
        }

        public int getCardSuitNumber() {
            return cardSuitNumber;
        }
    }

    public enum CardRank {
        ACE(1),
        TWO(2),
        THREE(3),
        FOUR(4),
        FIVE(5),
        SIX(6),
        SEVEN(7),
        EIGHT(8),
        NINE(9),
        TEN(10),
        JACK(11),
        QUEEN(12),
        KING(13);

        private final int cardRankNumber;

        CardRank(int cardRankNumber) {
            this.cardRankNumber = cardRankNumber;
        }

        public int getCardRank() {
            return this.cardRankNumber;
        }

    }

}

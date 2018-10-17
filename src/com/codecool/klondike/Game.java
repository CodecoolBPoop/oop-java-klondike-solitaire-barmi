package com.codecool.klondike;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Game extends Pane {

    private List<Card> deck = new ArrayList<>();

    private Pile stockPile;
    private Pile discardPile;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();

    private double dragStartX, dragStartY;
    private List<Card> draggedCards = FXCollections.observableArrayList();

    private static double STOCK_GAP = 1;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;


    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card card = (Card) e.getSource();
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK &&
                card == card.getContainingPile().getTopCard()) {
            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
            System.out.println("Placed " + card + " to the waste.");
        }
    };

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        if (stockPile.isEmpty()) {
            refillStockFromDiscard();
        }
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
        dragStartX = e.getSceneX();
        dragStartY = e.getSceneY();
    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
        Card card = (Card) e.getSource();

        Pile activePile = card.getContainingPile();

        List<Card> cards = activePile.getCards();


        if (activePile.getPileType() == Pile.PileType.STOCK || card.isFaceDown() ||
                (activePile.getPileType() == Pile.PileType.DISCARD && card != discardPile.getTopCard()))
            return;
        double offsetX = e.getSceneX() - dragStartX;
        double offsetY = e.getSceneY() - dragStartY;

        draggedCards.clear();
        draggedCards.add(card);

        card.getDropShadow().setRadius(20);
        card.getDropShadow().setOffsetX(10);
        card.getDropShadow().setOffsetY(10);

        card.toFront();
        card.setTranslateX(offsetX);
        card.setTranslateY(offsetY);
    };
    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        if (draggedCards.isEmpty())
            return;
        Card card = (Card) e.getSource();
        Pile fromPileOfCard = card.getContainingPile();
        Pile pile = getValidIntersectingPile(card, tableauPiles);
        Pile pile1 = getValidIntersectingPile(card, foundationPiles);


        if (pile != null) {
            card.moveToPile(pile);
            if (fromPileOfCard.getPileType() != Pile.PileType.DISCARD && !fromPileOfCard.isEmpty() &&
                    fromPileOfCard.getPileType() == pile.getPileType()) {

                if (fromPileOfCard.getTopCard().isFaceDown()) {
                    fromPileOfCard.getTopCard().flip();
                }
            }
            handleValidMove(card, pile);

        } else if (pile1 != null) {
            card.moveToPile(pile1);
            if (fromPileOfCard.getPileType() != Pile.PileType.DISCARD && !fromPileOfCard.isEmpty() &&
                    fromPileOfCard.getPileType() != pile1.getPileType()) {

                if (fromPileOfCard.getTopCard().isFaceDown()) {
                    fromPileOfCard.getTopCard().flip();
                }
            }
            handleValidMove(card, pile1);
        } else {
            draggedCards.forEach(MouseUtil::slideBack);
            draggedCards.clear();
        }
        if (isGameWon()) {
            gameIsWonMessage();
        }
    };


    public boolean isGameWon() {
        int pilesComplited = 0;
        for (Pile pile: foundationPiles) {
            if (pile.numOfCards() == 13) {
                pilesComplited++;
            }
        }
        if (pilesComplited == 4) {
            return true;
        } else {
            return false;
        }
    }

    public void gameIsWonMessage() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("");
        alert.setHeaderText("YOU WON!");
        alert.setContentText("Well done! Great job, pal! :)");

        alert.showAndWait();
    }

    public Game() {
        deck = Card.createNewDeck();
        shuffleDeck();
        initPiles();
        dealCards();
    }

    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);
    }

    public void refillStockFromDiscard() {
        Collections.reverse(discardPile.getCards());
        for (Card card : discardPile.getCards()) {
            card.flip();
            stockPile.addCard(card);
        }
        discardPile.clear();
        System.out.println("Stock refilled from discard pile.");
    }

    public boolean isMoveValid(Card card, Pile destPile) {
        Card destPileTop = destPile.getTopCard();
        if (!destPile.isEmpty() && destPile.getPileType().equals(Pile.PileType.TABLEAU)) {
            if (destPileTop.getRank() - card.getRank() == 1 && Card.isOppositeColor(card, destPileTop)) {
                return true;
            } else {
                return false;
            }
        } else if (destPile.getPileType().equals(Pile.PileType.FOUNDATION)) {
            if (destPile.isEmpty() && card.getRank() == 1) {
                return true;
            }else if (!destPile.isEmpty() && card.isSameSuit(card, destPileTop) && card.getRank()-destPileTop.getRank() == 1) {
                return true;
            } else {
                return false;
            }
        } else if (card.getRank() == 13) {
            return true;
        } else {
            return false;
        }
    }

    private Pile getValidIntersectingPile(Card card, List<Pile> piles) {
        Pile result = null;
        for (Pile pile : piles) {
            if (!pile.equals(card.getContainingPile()) &&
                    isOverPile(card, pile) &&
                    isMoveValid(card, pile))
                result = pile;
        }
        return result;
    }

    private boolean isOverPile(Card card, Pile pile) {
        if (pile.isEmpty()) {
            return card.getBoundsInParent().intersects(pile.getBoundsInParent());
        } else {
            return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());
        }
    }

    private void handleValidMove(Card card, Pile destPile) {
        String msg = null;
        if (destPile.isEmpty()) {
            if (destPile.getPileType().equals(Pile.PileType.FOUNDATION))
                msg = String.format("Placed %s to the foundation.", card);
            if (destPile.getPileType().equals(Pile.PileType.TABLEAU))
                msg = String.format("Placed %s to a new pile.", card);
        } else {
            msg = String.format("Placed %s to %s.", card, destPile.getTopCard());
        }
        System.out.println(msg);
        MouseUtil.slideToDest(draggedCards, destPile);
        draggedCards.clear();
    }


    private void initPiles() {
        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
        stockPile.setBlurredBackground();
        stockPile.setLayoutX(95);
        stockPile.setLayoutY(20);
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);

        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", STOCK_GAP);
        discardPile.setBlurredBackground();
        discardPile.setLayoutX(285);
        discardPile.setLayoutY(20);
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            foundationPile.setBlurredBackground();
            foundationPile.setLayoutX(610 + i * 180);
            foundationPile.setLayoutY(20);
            foundationPiles.add(foundationPile);
            getChildren().add(foundationPile);
        }
        for (int i = 0; i < 7; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            tableauPile.setBlurredBackground();
            tableauPile.setLayoutX(95 + i * 180);
            tableauPile.setLayoutY(275);
            tableauPiles.add(tableauPile);
            getChildren().add(tableauPile);
        }
    }

    public void dealCards() {
        Iterator<Card> deckIterator = deck.iterator();
        int i = 1;
        for (Pile pile : tableauPiles) {
            for (int j = 0; j < i; j++) {
                Card card = deckIterator.next();
                pile.addCard(card);
                addMouseEventHandlers(card);
                getChildren().add(card);
                deckIterator.remove();
                if (j == i - 1) card.flip();
            }
            i++;
        }

        deckIterator.forEachRemaining(card -> {
            stockPile.addCard(card);
            addMouseEventHandlers(card);
            getChildren().add(card);
        });

    }

    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

    public void shuffleDeck() {
        Collections.shuffle(deck);
    }
}

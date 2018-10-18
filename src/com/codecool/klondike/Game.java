package com.codecool.klondike;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

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

    private Button buttonRestart;
    private Button undoButton;

    private List<Pile> fromPiles = new ArrayList<>();
    private List<Card> movedCards = new ArrayList<>();
    private List<Integer> numOfMovedCards = new ArrayList<>();

    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card card = (Card) e.getSource();
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK &&
                card == card.getContainingPile().getTopCard()) {
            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
            System.out.println("Placed " + card + " to the waste.");
        }
        int movedCardsCount = 0;

        if(e.getClickCount() == 2 && !card.isFaceDown() && card.getContainingPile().getTopCard() == card){
            List cardList = new ArrayList();
            cardList.add(card);
            Card nextCard = card.getContainingPile().getTopCard();
            Pile cardPile = card.getContainingPile();
            if (card.getRank() == 1) {
//                for (Pile pile: foundationPiles) {
//                    if (pile.isEmpty()) {
//                        MouseUtil.slideToDest(cardList, pile);
//                        if (!cardPile.isEmpty() && cardPile.getPileType() != Pile.PileType.DISCARD && cardPile.getTopCard().isFaceDown()) {
//                            cardPile.getTopCard().flip();
//                        }
//                    }
//                }
                if (foundationPiles.get(0).isEmpty()) {
                    movedCardsCount++;
                    numOfMovedCards.add(movedCardsCount);
                    moveAdder(cardPile, card);
                    MouseUtil.slideToDest(cardList, foundationPiles.get(0));
                    if (!cardPile.isEmpty() && cardPile.getPileType() != Pile.PileType.DISCARD) {
                        cardPile.getTopCard().flip();
                    }
                } else if (foundationPiles.get(1).isEmpty()) {
                    movedCardsCount++;
                    numOfMovedCards.add(movedCardsCount);
                    moveAdder(cardPile, card);
                    MouseUtil.slideToDest(cardList, foundationPiles.get(1));
                    if (!cardPile.isEmpty() && cardPile.getPileType() != Pile.PileType.DISCARD) {
                        cardPile.getTopCard().flip();
                    }
                } else if (foundationPiles.get(2).isEmpty()) {
                    movedCardsCount++;
                    numOfMovedCards.add(movedCardsCount);
                    moveAdder(cardPile, card);
                    MouseUtil.slideToDest(cardList, foundationPiles.get(2));
                    if (!cardPile.isEmpty() && cardPile.getPileType() != Pile.PileType.DISCARD) {
                        cardPile.getTopCard().flip();
                    }
                } else if (foundationPiles.get(3).isEmpty()) {
                    movedCardsCount++;
                    numOfMovedCards.add(movedCardsCount);
                    moveAdder(cardPile, card);
                    MouseUtil.slideToDest(cardList, foundationPiles.get(3));
                    if (!cardPile.isEmpty() && cardPile.getPileType() != Pile.PileType.DISCARD) {
                        cardPile.getTopCard().flip();
                    }
                }
            }
            if (card.getRank() > 1 ) {
                for (Pile pile: foundationPiles) {
                    if (pile.getTopCard().getSuit() == card.getSuit() && card.getRank() - pile.getTopCard().getRank() == 1) {
                        movedCardsCount++;
                        numOfMovedCards.add(movedCardsCount);
                        moveAdder(cardPile, card);
                        MouseUtil.slideToDest(cardList, pile);
                        if (!cardPile.isEmpty() && cardPile.getPileType() != Pile.PileType.DISCARD && cardPile.getTopCard().isFaceDown()) {
                            cardPile.getTopCard().flip();
                        }
                    }

                }
            }
            if (isGameWon()) {
                gameIsWonMessage();
            }

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


        if (activePile.getPileType() == Pile.PileType.STOCK || card.isFaceDown() || (activePile.getPileType() == Pile.PileType.DISCARD && card != discardPile.getTopCard()))
            return;
        double offsetX = e.getSceneX() - dragStartX;
        double offsetY = e.getSceneY() - dragStartY;

        List<Card> cards = FXCollections.observableArrayList();

        if(activePile.getPileType() == Pile.PileType.TABLEAU){
            cards = activePile.getCards();
            List<Card> temp = FXCollections.observableArrayList();
            for(Card c : cards){
                if(!c.isFaceDown() && c.getRank() <= card.getRank()){
                    temp.add(c);
                }
            }
            cards = temp;
        }

        draggedCards.clear();

        if(activePile.getPileType() == Pile.PileType.TABLEAU && cards.size() > 1){
            draggedCards.addAll(cards);
        } else {
            draggedCards.add(card);
        }

        card.getDropShadow().setRadius(20);
        card.getDropShadow().setOffsetX(10);
        card.getDropShadow().setOffsetY(10);
        for(Card c : draggedCards){
            c.toFront();
            c.setTranslateX(offsetX);
            c.setTranslateY(offsetY);
        }
    };

    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        if (draggedCards.isEmpty())
            return;
        Card card = (Card) e.getSource();
        Pile fromPileOfCard = card.getContainingPile();
        Pile pile = getValidIntersectingPile(card, tableauPiles);
        Pile pile1 = getValidIntersectingPile(card, foundationPiles);
        int movedCards = 0;

        if (pile != null) {
            for(Card c : draggedCards){
                movedCards++;
                moveAdder(c.getContainingPile(), c);
                c.moveToPile(pile);
            }

            draggedCards.clear();
            handleValidMove(card, pile);

            if (fromPileOfCard.getPileType() != Pile.PileType.DISCARD && !fromPileOfCard.isEmpty() && fromPileOfCard.getPileType() == pile.getPileType()){
                if (fromPileOfCard.getTopCard().isFaceDown()) {
                    System.out.println(fromPileOfCard.getTopCard().getShortName());
                    fromPileOfCard.getTopCard().flip();
                }
            }

        } else if (pile1 != null && card == fromPileOfCard.getTopCard()) {
            moveAdder(card.getContainingPile(), card);
            movedCards++;
            card.moveToPile(pile1);
            draggedCards.clear();
            handleValidMove(card, pile1);

            if (fromPileOfCard.getPileType() != Pile.PileType.DISCARD && !fromPileOfCard.isEmpty() && fromPileOfCard.getPileType() != pile1.getPileType() ){

                if (fromPileOfCard.getTopCard().isFaceDown()) {
                    fromPileOfCard.getTopCard().flip();
                }
            }
            if (isGameWon()) {
                gameIsWonMessage();
            }
            handleValidMove(card, pile1);
        } else {
            draggedCards.forEach(MouseUtil::slideBack);
            draggedCards.clear();

        }
        numOfMovedCards.add(movedCards);
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
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Well done! Great job, pal! :) \nDo you wanna play again?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("");
        alert.setHeaderText("YOU WON!");
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            //TODO
            restart();
        }

    }

    public Game() {
        deck = Card.createNewDeck();
        shuffleDeck();
        initPiles();
        dealCards();
        addEventToRestartButton();
        addEventToUndoButton();
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

        buttonRestart = new Button("Restart");
        buttonRestart.setStyle("-fx-font: 22 arial; -fx-base: #b6e7c9;");
        buttonRestart.setVisible(true);
        buttonRestart.setLayoutX(470);
        buttonRestart.setLayoutY(20);
        getChildren().add(buttonRestart);


        undoButton = new Button("Undo");
        undoButton.setStyle("-fx-font: 22 arial; -fx-base: #b6e7c9;");
        undoButton.setVisible(true);
        undoButton.setLayoutX(470);
        undoButton.setLayoutY(70);
        getChildren().add(undoButton);


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

    public void addEventToRestartButton() {

        buttonRestart.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                System.out.println("restart button created");
                restart();
            }
        });
    }

    public void addEventToUndoButton() {

        undoButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                undo();
            }
        });
    }

  public void restart() {
      discardPile.clear();
      foundationPiles.clear();
      stockPile.clear();
      tableauPiles.clear();
      fromPiles.clear();
      movedCards.clear();
      numOfMovedCards.clear();
      getChildren().clear();


      deck = Card.createNewDeck();
      shuffleDeck();
      initPiles();
      dealCards();
      addEventToRestartButton();
      addEventToUndoButton();
  }

  public void undo(){
        System.out.println(movedCards);
        if(movedCards.size() != 0) {
            int numOfCardsMoved = numOfMovedCards.get(numOfMovedCards.size()-1);
            List<Card> temp = movedCards.subList(movedCards.size()-numOfCardsMoved, movedCards.size());
            List<Pile> pileTemp = fromPiles.subList(fromPiles.size()-numOfCardsMoved, fromPiles.size());
            System.out.println(temp);
            
            Iterator<Card> tempIterator = temp.iterator();
            Iterator<Pile> pileTempIterator = pileTemp.iterator();
            Pile fromPile = fromPiles.get(fromPiles.size() - 1);

            if(fromPile.getPileType() == Pile.PileType.TABLEAU){
                int flipCount = 0;
                for(Card c : fromPile.getCards()){
                    if(!c.isFaceDown()) flipCount++;
                }
                if(!fromPile.isEmpty() && flipCount == 1) fromPile.getTopCard().flip();
            } //else if(fromPile.getPileType() == Pile.PileType.FOUNDATION){

            //}

            while (tempIterator.hasNext()){
                Card card = tempIterator.next();
                Pile pile = pileTempIterator.next();
                card.moveToPile(fromPile);
                tempIterator.remove();
                pileTempIterator.remove();
                System.out.println("undo");
            }
            System.out.println(movedCards);
            numOfMovedCards.remove(numOfMovedCards.size()-1);

        }
  }

  private void moveAdder(Pile fromPile, Card card){
        fromPiles.add(fromPile);
        movedCards.add(card);
  }

}

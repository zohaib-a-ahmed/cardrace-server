package com.cardrace.cardrace_server.model.game;

public class Move {

    private PlayingCard card;
    private PlayingCard substitute;
    private Marble marble;
    private Marble[] targets;
    private Detail specification;

    public enum Detail {
        REVERSE, SWAP, SPLIT, ACTIVATE
    }

    public Move (PlayingCard card, Marble marble) {

        this.card = card;
        this.marble = marble;
    }

    public Move (PlayingCard card, PlayingCard substitute, Marble marble, Marble[] targets, Detail specification) {

        this.card = card;
        this.substitute = substitute;
        this.marble = marble;
        this.targets = targets;
        this.specification = specification;
    }

    public Marble getMarble() {
        return marble;
    }

    public Detail getSpecification() {
        return specification;
    }

    public Marble[] getTargets() {
        return targets;
    }

    public PlayingCard getCard() {
        return card;
    }

    public PlayingCard getSubstitute() {
        return substitute;
    }

    public void setMarble(Marble marble) {
        this.marble = marble;
    }

    public void setCard(PlayingCard card) {
        this.card = card;
    }

    public void setSpecification(Detail specification) {
        this.specification = specification;
    }

    public void setSubstitute(PlayingCard substitute) {
        this.substitute = substitute;
    }

    public void setTargets(Marble[] targets) {
        this.targets = targets;
    }
}



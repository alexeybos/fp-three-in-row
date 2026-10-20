package org.skillsmart;

import org.skillsmart.game.BoardState;
import org.skillsmart.game.Game;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        //System.out.printf("Hello and welcome!");

        BoardState bs = Game.initializeGame();
        while (true) {
            Game.draw(bs);
            bs = Game.readMove(bs);
        }
    }
}
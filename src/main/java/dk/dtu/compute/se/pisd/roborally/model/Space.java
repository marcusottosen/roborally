/*
 *  This file is part of the initial project provided for the
 *  course "Project in Software Development (02362)" held at
 *  DTU Compute at the Technical University of Denmark.
 *
 *  Copyright (C) 2019, 2020: Ekkart Kindler, ekki@dtu.dk
 *
 *  This software is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; version 2 of the License.
 *
 *  This project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this project; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package dk.dtu.compute.se.pisd.roborally.model;

import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.roborally.controller.FieldAction;
import dk.dtu.compute.se.pisd.roborally.view.SpaceView;

import java.util.List;

/**
 * Indeholder spillets felter.
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class Space extends Subject {

    public final Board board;

    public final int x;
    public final int y;

    private Player player;

    /**
     * Metode bruges til at bestemme et felt på pladen. Oprettelse af objekt.
     * @param board Sætter boarded fra metoden til det public final board i Space.java.
     * @param x Sætter x-koordinaten til feltet til den public final int x i Space.java.
     * @param y Sætter y-koordinaten til feltet til den public final int y i Space.java.
     */
    public Space(Board board, int x, int y) {
        this.board = board;
        this.x = x;
        this.y = y;
        player = null;
    }

    /**
     * Ved brug af denne metode returneres player.
     * @return returnerer player.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Metoden bruges til, at sammenligne 2 spillere, ved at sætte player lig med oldPlayer igennem en håndfuld tjek:
     * Hvis player ikke er lig med oldPlayer og ikke er null, bliver this.player sat til = player.
     * @param player Spillerens objekt.
     */
    public void setPlayer(Player player) {
        Player oldPlayer = this.player;
        if (player != oldPlayer &&
                (player == null || board == player.board)) {
            this.player = player;
            if (oldPlayer != null) {
                // this should actually not happen
                oldPlayer.setSpace(null);
            }
            if (player != null) {
                player.setSpace(this);
            }
            notifyChange();
        }
    }


    /**
     *
     * @return walls.
     */
    public List<Heading> getWalls() {
        List<Heading> walls = null; //This line only to avoid errors
        return walls;
    }

    /**
     *
     * @return type af aktion.
     */
    public List<FieldAction> getActions() {
        List<FieldAction> actions = null; //This line only to avoid errors
        return actions;
    }

    /**
     * This is a minor hack; since some views that are registered with the space
     * also need to update when some player attributes change, the player can
     * notify the space of these changes by calling this method.
     */
    void playerChanged() {
        notifyChange();
    }

}

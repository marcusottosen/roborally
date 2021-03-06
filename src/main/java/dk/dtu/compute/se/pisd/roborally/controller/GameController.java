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
package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.fileaccess.LoadBoard;
import dk.dtu.compute.se.pisd.roborally.model.*;
import dk.dtu.compute.se.pisd.roborally.model.specialFields.Laser;
import dk.dtu.compute.se.pisd.roborally.model.specialFields.Wall;
import dk.dtu.compute.se.pisd.roborally.view.LaserView;
import dk.dtu.compute.se.pisd.roborally.view.PopupView;
import javafx.scene.control.Alert;
import org.jetbrains.annotations.NotNull;

/**
 * Den primære logik af selve spillet.
 * Sørger for at rykke rundt på spillerne, skifte mellem spillerne, finde vinder, og eksekvere kortene.
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 * @author Marcus Ottosen
 * @author Victor Kongsbak
 */
public class GameController {
    final public Board board;

    /**
     * Konstruktøren til GameController. Kræver et board.
     *
     * @param board spillets plade.
     */
    public GameController(@NotNull Board board) {
        this.board = board;
    }

    /**
     * Starter programmeringsfasen, skifter til den første spiller og sætter steps til 0
     * Gør eventuelle kort visuelle for alle brugere samt giver random kort vha. generateRandomCommandCard().
     */
    public void startProgrammingPhase() {
        for (Player player : board.getPlayers()) {
            if (player.getEnergyCubesOptained().contains(EnergyCubeTypes.GETLASER)) {
                player.initiatePlayerLaser();
            }
        }

        LaserView.shootLaser();
        Laser.laserDamage();
        playerDeath(board);
        board.setPhase(Phase.PROGRAMMING);
        board.setCurrentPlayer(board.getPlayer(0));
        board.setStep(0);
        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player player = board.getPlayer(i);
            if (player != null) {
                for (int j = 0; j < Player.NO_REGISTERS; j++) {
                    CommandCardField field = player.getProgramField(j);
                    field.setCard(null);
                    field.setVisible(true);
                }
                for (int j = 0; j < Player.NO_CARDS; j++) {
                    if (player.getCardField(j).getCard() == null) { //Giver kun kort ved de brugte kort
                        CommandCardField field = player.getCardField(j);
                        field.setCard(generateRandomCommandCard());
                        field.setVisible(true);
                    }
                }
            }
        }
    }

    /**
     * Finder random kort som bliver efterspurgt fra StartProgrammingPhase().
     *
     * @return nyt random kommandokort
     */
    private CommandCard generateRandomCommandCard() {
        Command[] commands = Command.values();
        int random = (int) (Math.random() * commands.length);
        return new CommandCard(commands[random]);
    }

    /**
     * Når programmeringsfasen er færdig gør den program fields usyndlige vha. makeProgramFieldsVisible()
     * Ænder derudover fasen til ACTIVATION.
     */
    public void finishProgrammingPhase() {
        LaserView.stopLaser();
        makeProgramFieldsInvisible();
        makeProgramFieldsVisible(0);
        board.setPhase(Phase.ACTIVATION);
        board.setCurrentPlayer(board.getPlayer(0));
        board.setStep(0);
    }

    /**
     * Bliver brugt i bl.a. finishProgrammingPhase til at enten vise (1) eller skjule (0) programming fields.
     *
     * @param register int.
     */
    private void makeProgramFieldsVisible(int register) {
        if (register >= 0 && register < Player.NO_REGISTERS) {
            for (int i = 0; i < board.getPlayersNumber(); i++) {
                Player player = board.getPlayer(i);
                CommandCardField field = player.getProgramField(register);
                field.setVisible(true);
            }
        }
    }

    /**
     * Bliver brugt i bl.a. finishProgrammingPhase til at skjule programming fields.
     */
    private void makeProgramFieldsInvisible() {
        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player player = board.getPlayer(i);
            for (int j = 0; j < Player.NO_REGISTERS; j++) {
                CommandCardField field = player.getProgramField(j);
                field.setVisible(false);
            }
        }
    }

    /**
     * Bruges til knappen "execute program" og kører alle programkort igennem automatisk (udover option-kort)
     */
    public void executePrograms() {
        board.setStepMode(false);
        continuePrograms();
    }

    /**
     * Bruges til knappen "execute current register" og kører kun det næste programkort.
     */
    public void executeStep() {
        board.setStepMode(true);
        continuePrograms();
    }

    /**
     * Bruges ved executeProgram() og executeStep() og tjekker om fasen er ACTIVATION og spillet ikke er stepMode.
     */
    private void continuePrograms() {
        do {
            executeNextStep();
        } while (board.getPhase() == Phase.ACTIVATION && !board.isStepMode());
    }

    /**
     * Udfører kommandokortene, så længe der er et næste. Alle kortene bliver kørt igennem ved 1 kort pr. person af gangen.
     * Tjekker bl.a. om et kort kræver interaktion fra spilleren.
     * Sætter næste spillers tur.
     * Tjekker om der er flere spillere i runden og starter en ny runde hvis alle har fået sin tur.
     * Runden fortsætter til alle kort er spillet, eller til der kræves en interaktion fra en spiller.
     * Starter en ny programmeringsfase når alle runder er færdige.
     */
    private void executeNextStep() {
        Player currentPlayer = board.getCurrentPlayer();
        if ((board.getPhase() == Phase.ACTIVATION ||
                (board.getPhase() == Phase.PLAYER_INTERACTION
                        && board.getUserChoice() != null))
                && currentPlayer != null) {
            int step = board.getStep();
            if (step >= 0 && step < Player.NO_REGISTERS) {
                Command userChoice = board.getUserChoice();
                if (currentPlayer.getHealth() >= 1) {
                    if (userChoice != null) {
                        board.setUserChoice(null);
                        board.setPhase(Phase.ACTIVATION);
                        executeCommand(currentPlayer, userChoice);

                    } else {
                        CommandCard card = currentPlayer.getProgramField(step).getCard();
                        if (card != null) {
                            Command command = card.command;
                            //Afbryder eksekveringsløkken og giver spilleren et valg.
                            if (command.isInteractive()) {
                                board.setPhase(Phase.PLAYER_INTERACTION);
                                return;
                            }
                            executeCommand(currentPlayer, command);
                        }
                    }
                }
                isWinnerFound(currentPlayer);

                int nextPlayerNumber = board.getPlayerNumber(currentPlayer) + 1;
                if (nextPlayerNumber < board.getPlayersNumber()) {
                    board.setCurrentPlayer(board.getPlayer(nextPlayerNumber));
                } else {
                    step++;
                    if (step < Player.NO_REGISTERS) {
                        makeProgramFieldsVisible(step);
                        board.setStep(step);
                        board.setCurrentPlayer(board.getPlayer(0));
                    } else {
                        startProgrammingPhase();
                    }
                }

            } else {
                assert false;
            }
        } else {
            assert false;
        }
    }

    /**
     * Eksekverer command option - spillerens interaktion og fortsætter spillet derefter.
     *
     * @param option .
     */
    public void executeCommandOptionAndContinue(@NotNull Command option) {
        assert board.getPhase() == Phase.PLAYER_INTERACTION;
        assert board.getCurrentPlayer() != null;
        board.setUserChoice(option);
        continuePrograms();
    }

    /**
     * Overfører kortets navn til kortets funktion og udfører metoden til kortet.
     * Hvis spilleren har EXTRAMOVE energyCuben, bliver der rykket en ekstra frem.
     *
     * @param player  Spillerens objekt
     * @param command Objekt af kommandokortet.
     */
    private void executeCommand(@NotNull Player player, Command command) {
        if (player.board == board && command != null) {
            if (player.energyCubesOptained.contains(EnergyCubeTypes.EXTRAMOVE) &&
                    !((command == Command.RIGHT) || (command == Command.LEFT) || (command == Command.UTURN)
                            || (command == Command.OPTION_LEFT_RIGHT))) {
                forward1(player);
            }

            switch (command) {
                case FORWARD1:
                    this.forward1(player);
                    break;
                case FORWARD2:
                    this.forward2(player);
                    break;
                case FORWARD3:
                    this.forward3(player);
                    break;
                case RIGHT:
                    this.turnRight(player);
                    spaceActionInit(player.getSpace());
                    break;
                case LEFT:
                    this.turnLeft(player);
                    spaceActionInit(player.getSpace());
                    break;
                case UTURN:
                    this.uTurn(player);
                    spaceActionInit(player.getSpace());
                    break;
                default:
            }
        }
    }

    /**
     * Rykker spilleren en plads frem.
     * Hvis der på pladsen allerede står en spiller skubbes den spiller.
     * Hvis spilleren har et melee våben, bliver den skubbede spiller også skadet.
     * Tjekker ydermere hvorvidt der er en væg i vejen.
     * Spiller bliver rykket vha. moveToSpace metoden.
     *
     * @param player which player to move.
     */
    public void forward1(@NotNull Player player) {
        Wall wall = new Wall(board);
        if (player.board == board) {
            if ((player.getHealth() >= 1)) {
                Space space = player.getSpace();
                Heading heading = player.getHeading();

                Space target = board.getNeighbour(space, heading);
                if (target != null) {
                    try {
                        if (!wall.checkForWall(player)) {
                            if (target.getPlayer() != null) {
                                if (player.energyCubesOptained.contains(EnergyCubeTypes.MELEEWEAPON)) {
                                    target.getPlayer().takeHealth(1);
                                }
                                if (!wall.checkForWall(target.getPlayer())) {
                                    moveToSpace(player, target, heading);
                                }
                            } else {
                                moveToSpace(player, target, heading);
                            }
                        }
                    } catch (ImpossibleMoveException e) {
                        // Catching exception
                    }
                }
                spaceActionInit(player.getSpace());
            }
        }
    }

    /**
     * Rykker spillerne til deres respektive lokationer.
     *
     * @param player  spilleren der skal rykkes
     * @param space   feltet der skal rykkes til
     * @param heading retningen af spilleren
     * @throws ImpossibleMoveException hvis spilleren ikke kan rykke til feltet.
     */
    public void moveToSpace(@NotNull Player player, @NotNull Space space, @NotNull Heading heading) throws ImpossibleMoveException {
        assert board.getNeighbour(player.getSpace(), heading) == space; // make sure the move to here is possible in principle
        Player other = space.getPlayer();
        if (other != null) {
            Space target = board.getNeighbour(space, heading);
            if (target != null) {
                moveToSpace(other, target, heading);
                assert target.getPlayer() == null : target; // sikre at spilleren er fri nu.
            } else {
                throw new ImpossibleMoveException(player, space, heading);
            }
        }
        player.setSpace(space);
    }

    /**
     * Hvis spilleren ikke kan rykke til feltet bruges denne klasse til error-handling.
     */
    public static class ImpossibleMoveException extends Exception {
        private Player player;
        private Space space;
        private Heading heading;

        /**
         * @param player  Den omtalte spiller.
         * @param space   feltet spilleren ikke kunne rykke sig til.
         * @param heading retningen af spilleren.
         */
        public ImpossibleMoveException(Player player, Space space, Heading heading) {
            super("Spiller kan ikke rykke sig! - ImpossibleMoveException!");
            this.player = player;
            this.space = space;
            this.heading = heading;
        }
    }

    /**
     * Flytter spilleren frem 2 felter ved at kalde forward1 metoden 2 gange.
     *
     * @param player Spillerens objekt.
     */
    public void forward2(@NotNull Player player) {
        forward1(player);
        forward1(player);
    }

    /**
     * Flytter spilleren frem 3 felter ved at kalde forward1 metoden 3 gange.
     *
     * @param player Spillerens objekt.
     */
    public void forward3(@NotNull Player player) {
        forward1(player);
        forward1(player);
        forward1(player);
    }

    /**
     * Skifter spillerens heading til højre.
     *
     * @param player Spillerens objekt.
     */
    public void turnRight(@NotNull Player player) {
        player.setHeading(player.getHeading().next());
    }

    /**
     * Skifter spilleren heading til venstre.
     *
     * @param player Spillerens objekt.
     */
    public void turnLeft(@NotNull Player player) {
        player.setHeading(player.getHeading().prev());
    }

    /**
     * Skifter spillerens heading to gange til højre og altså vender spilleren i den modsatte retning.
     *
     * @param player Spillerens objekt.
     */
    public void uTurn(@NotNull Player player) {
        player.setHeading(player.getHeading().next());
        player.setHeading(player.getHeading().next());
    }

    /**
     * Tjekker om kortet er et move card eller ej.
     *
     * @param source CommandCardField
     * @param target CommandCardField
     * @return returnerer true/false
     */
    public boolean moveCards(@NotNull CommandCardField source, @NotNull CommandCardField target) {
        CommandCard sourceCard = source.getCard();
        CommandCard targetCard = target.getCard();
        if (sourceCard != null && targetCard == null) {
            target.setCard(sourceCard);
            source.setCard(null);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Tjekker hvorvidt en spillet har vundet.
     * Hvis en spiller har vundet, vises en ny boks som fortæller spillerne hvem der har vundet.
     * Derudover ændres fasen til FINISH som skjuler knapper og kort.
     *
     * @param player spilleren der tjekkes.
     */
    public void isWinnerFound(Player player) {
        if (player.getScore() >= board.getCheckpointAmount()) {
            board.setPhase(Phase.FINISH);
            PopupView view = new PopupView();
            view.winningWindow(player);
        }
    }

    /**
     * Tjekker hvorvidt feltet er specielt og initialiserer feltets doAction.
     *
     * @param space object af feltet
     */
    public void spaceActionInit(@NotNull Space space) {
        if (space.getActions().size() != 0) {
            FieldAction actionType = space.getActions().get(0);
            actionType.doAction(this, space);
        }
    }

    /**
     * Tjekker hvis en spiller er død (0 health) og resetter dem, så de re-spawner på deres originale spawn.
     * Resetter ydermere spillerens score, heading og energyCubes.
     *
     * @param board which board is being used
     */
    public void playerDeath(Board board) {
        for (int i = 0; i < board.getPlayersNumber(); i++) {
            int health = board.getPlayer(i).getHealth();
            Player player = board.getPlayer(i);
            if (health <= 0) {
                int x = LoadBoard.template.spawns.get(i).x;
                int y = LoadBoard.template.spawns.get(i).y;
                player.setSpace(board.getSpace(x, y));
                player.setHealth(3);
                player.setHeading(Heading.SOUTH);
                player.getEnergyCubesOptained().clear();
                player.getCheckpointsCompleted().clear();
                player.setScore(0);

                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle(null);
                alert.setHeaderText(null);
                alert.setContentText(player.getName() + " just died!");

                alert.showAndWait();
            }
        }
    }
}
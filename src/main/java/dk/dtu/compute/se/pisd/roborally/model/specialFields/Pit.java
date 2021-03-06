package dk.dtu.compute.se.pisd.roborally.model.specialFields;

import dk.dtu.compute.se.pisd.roborally.controller.FieldAction;
import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import javafx.scene.control.Alert;

/**
 * Håndterer aktionen af et pit.
 *
 * @author Marcus Ottosen
 * @author Victor Kongsbak
 */
public class Pit extends FieldAction {

    /**
     * Sætter spillerens liv til 0 og informerer spillerne.
     *
     * @param gameController the gameController of the respective game
     * @param space          the space this action should be executed for
     * @return hvorvidt doAction blev udført.
     */
    @Override
    public boolean doAction(GameController gameController, Space space) {
        try {
            space.getPlayer().setHealth(0);
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(null);
            alert.setHeaderText(null);
            alert.setContentText(space.getPlayer().getName() + " fell into the abyss!");
            alert.showAndWait();

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

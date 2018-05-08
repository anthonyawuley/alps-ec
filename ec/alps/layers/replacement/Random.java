package ec.alps.layers.replacement;

import ec.Population;
import ec.alps.layers.ALPSLayers;
import ec.alps.layers.Replacement;


/**
 * In Random replacement, when an old  individual from a lower layer is moving to a higher layer
 * with a larger age limit, a randomly selected  individual from the higher
 * layer's population  is picked for replacement.
 *
 * @author Anthony Awuley
 */
public class Random extends Replacement {

    /** */
    private static final long serialVersionUID = 1;

    public Random() {
    }

    public String toString() {
        return "Random Replacement";
    }

    @Override
    public void layerMigrations(ALPSLayers alpsLayers, Population current) {
        // TODO Auto-generated method stub

    }


}

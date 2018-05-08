package ec.alps.layers.agingscheme;

import ec.alps.layers.AgingScheme;
import ec.alps.layers.Layer;

import java.util.ArrayList;

/**
 * 2^n: 1 2 4 8 16 32 64 128
 * This values are multipled by the age-gap parameter to determine the maximum age per layer
 *
 * @author Anthony Awuley
 */
public class Exponential extends AgingScheme {

    public Exponential() {
        // TODO Auto-generated constructor stub
    }

    public String toString() {
        return this.getClass().getName() + "aging scheme selected ";
    }

    /**
     * @return 2^n
     */
    public ArrayList<Layer> agingScheme() {
        layers = new ArrayList<>();

        for (int i = 0; i < AgingScheme.alpsAgeLayers; i++) {
            Layer layer = new Layer();
            layer.setMaxAgeLayer((int) Math.pow(2, i) * AgingScheme.alpsAgeGap);
            layer.setIsActive(Boolean.FALSE);
            layer.setId(i);


            if (i == 0) {
                layer.setIsBottomLayer(Boolean.TRUE);
                layer.setGenerations(layer.getMaxAge());
            } else {
                layer.setIsBottomLayer(Boolean.FALSE);
                layer.setGenerations( //get the difference between maximum of this layer and previous layer
                        layer.getMaxAge() - layers.get(i - 1).getMaxAge());
            }
            layers.add(layer);
            //layers.set(i, layer);
        }
        /*
         * there is no age-limit to the last layer so as to be able to keep the
         * best individuals from the most promising (longest running) search
         */
        //layers.get(layers.size()-1).setMaxAgeLayer(Constants.ALPS_MAX_AGE_LAST_LAYER);

        return layers;

    }

}

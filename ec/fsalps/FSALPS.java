package ec.fsalps;

import ec.alps.Engine;
import ec.util.Parameter;

/**
 * @author Anthony Awuley
 * @version 1.0
 */
public class FSALPS {

    public static final String P_FSALPS = "fsalps";

    /**
     * return base of fsalps
     */
    public static Parameter defaultBase() {
        return Engine.base().push(P_FSALPS);
    }


}

package net.ftb.util;

import javax.swing.*;

public class SwingUtils {

    /**
    * @param minimumSize minimum size -- used to insantiate returned Spring
    * @param spr springs to get summed with the minimum size
    * @return summed spring
     */
    public static Spring springSum(int minimumSize, Spring... spr){
        Spring ret = Spring.constant(minimumSize);
        for (Spring aSpr : spr) {
            ret = ret.sum(ret, aSpr);
        }
        return ret;
    }
    /**
* @param base spring used to create spring that will get returned
* @param spr springs to get summed with the minimum size
* @return summed spring
 */
    public static Spring springSum(Spring base, Spring... spr){
        Spring ret = base;
        for (Spring aSpr : spr) {
            ret = ret.sum(ret, aSpr);
        }
        return ret;
    }

    /**
* @param base spring used to create spring that will get returned
* @param spr springs to get maxed with the minimum size
* @return maxed spring
 */
    public static Spring springMax(Spring base, Spring... spr){
        Spring ret = base;
        for (Spring aSpr : spr) {
            ret = ret.max(ret, aSpr);
        }
        return ret;
    }

}

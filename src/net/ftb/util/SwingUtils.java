package net.ftb.util;

import javax.swing.*;

public class SwingUtils {

    /*
    * @Param minimumSize minimum size -- used to insantiate returned Spring
    * @Param spr springs to get summed with the minimum size
    * @Return summed spring
     */
    public static Spring springSum(int minimumSize, Spring... spr){
        Spring ret = Spring.constant(minimumSize);
        for(int i = 0; i < spr.length; i++){
        ret = ret.sum(ret,spr[i]);}
        return ret;
    }
    /*
* @Param base spring used to create spring that will get returned
* @Param spr springs to get summed with the minimum size
* @Return summed spring
 */
    public static Spring springSum(Spring base, Spring... spr){
        Spring ret = base;
        for(int i = 0; i < spr.length; i++){
            ret = ret.sum(ret,spr[i]);}
        return ret;
    }

    /*
* @Param base spring used to create spring that will get returned
* @Param spr springs to get maxed with the minimum size
* @Return maxed spring
 */
    public static Spring springMax(Spring base, Spring... spr){
        Spring ret = base;
        for(int i = 0; i < spr.length; i++){
            ret = ret.max(ret,spr[i]);}
        return ret;
    }

}

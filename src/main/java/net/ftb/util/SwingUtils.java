/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2014, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
 * FTB Launcher is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

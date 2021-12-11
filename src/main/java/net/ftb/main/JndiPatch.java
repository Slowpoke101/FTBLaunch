/*
 * Copyright 2016 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ftb.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.LoggerContext;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Based off fabric-loader's refection fix. https://github.com/FabricMC/fabric-loader/commit/c8266049da4df1157ce0ff5dc25941928b59b7b7
 *
 * This patches log4j used by the launcher. See MCLauncher#extractLog4jPatcher for the fix applied to the game.
 */
public class JndiPatch {
    public static void patchJndi() throws Exception {
        LoggerContext context = LogManager.getContext(false);

        context.getClass().getMethod("addPropertyChangeListener", PropertyChangeListener.class).invoke(context, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("config")) {
                    try {
                        removeSubstitutionLookups();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        removeSubstitutionLookups();
    }

    private static void removeSubstitutionLookups() throws Exception {
        // strip the jndi lookup and then all over lookups from the active org.apache.logging.log4j.core.lookup.Interpolator instance's lookups map
        LoggerContext context = LogManager.getContext(false);
        Object config = context.getClass().getMethod("getConfiguration").invoke(context);
        Object substitutor = config.getClass().getMethod("getStrSubstitutor").invoke(config);
        Object varResolver = substitutor.getClass().getMethod("getVariableResolver").invoke(substitutor);
        if (varResolver == null) return;

        boolean removed = false;

        for (Field field : varResolver.getClass().getDeclaredFields()) {
            if (Map.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                @SuppressWarnings("unchecked")
                Map<String, ?> map = (Map<String, ?>) field.get(varResolver);

                if (map.remove("jndi") != null) {
                    map.clear();
                    removed = true;
                    break;
                }
            }
        }

        if (!removed) throw new RuntimeException("couldn't find JNDI lookup entry");
    }
}

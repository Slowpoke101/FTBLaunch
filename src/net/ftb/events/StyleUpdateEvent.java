package net.ftb.events;


import lombok.Getter;
import lombok.Setter;

//Event for any changes that require partial or full UI re-setting up.
public class StyleUpdateEvent implements ILauncherEvent {
    /**
     *  tab = shared tab changes ex: map/TP swap
     *  style = changes to base colors, or fonts used by the launcher that require re-setting up parts of the UI
     */
    public enum TYPE {
        TAB, STYLE
    }
    @Getter
    @Setter
    private TYPE eventType;

    @Getter
    @Setter
    private String[] eventTarget;

    /**
    * constructor for events, must have a type so that listeners know what to refresh!
    * @param type type of style update
     */
    public StyleUpdateEvent (TYPE type){
        this.eventType = type;
    }
}

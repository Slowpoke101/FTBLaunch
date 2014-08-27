package net.ftb.ui.comp;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;

public class ToggleButtonGroup
extends ButtonGroup{
    @Override
    public void setSelected(ButtonModel model, boolean selected){
        if(selected){
            super.setSelected(model, selected);
        } else{
            clearSelection();

            if(this.getSelection() == null){
                super.setSelected(model, true);
            }
        }
    }
}
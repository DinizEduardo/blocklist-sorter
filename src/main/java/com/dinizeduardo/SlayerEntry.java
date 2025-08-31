package com.dinizeduardo;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.widgets.Widget;

@Data
@AllArgsConstructor
public class SlayerEntry
{
    private String name;
    private double chance;
    private int originalY;
    private int newY;
    private Widget nameWidget;
    private Widget percentWidget;
    private Widget backgroundWidget;
    private Widget iconWidget;
    private Widget clickableWidget;
}

package com.dinizeduardo;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.api.events.WidgetLoaded;

import java.awt.*;
import java.util.*;
import java.util.List;

@Slf4j
@PluginDescriptor(name = "Slayer Block Reorder", description = "Reordena a lista de criaturas bloqueadas por chance", tags = {"slayer", "tasks", "order"})
public class SlayerReorderPlugin extends Plugin {
    private static final int BLOCK_LIST_GROUP_ID = 924;
    private static final int INITIAL_Y = 0;
    private static final int LINE_HEIGHT = 22;
    private static final int DRAWABLE_ID = 60555272;
    private static final int CLICKABLE_ID = 60555273;
    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;
    @Override
    protected void startUp() {
    }

    @Override
    protected void shutDown() {
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event) {
        if (event.getGroupId() == BLOCK_LIST_GROUP_ID) {
            clientThread.invokeLater(this::reorderList);
        }
    }

    private void reorderList() {
        Widget drawable = client.getWidget(DRAWABLE_ID);
        Widget clickable = client.getWidget(CLICKABLE_ID);

        if (clickable == null || clickable.getDynamicChildren().length == 0 || drawable == null || drawable.getDynamicChildren().length == 0) {
            return;
        }

        List<SlayerEntry> entries = this.getOrderedTask(drawable);

        Widget[] clickableChildren = clickable.getDynamicChildren();
        for (Widget w : clickableChildren) {
            String name = w.getName().replace("<col=ff9040>", "");
            entries.stream().filter(e -> e.getName().contains(name)).findFirst().ifPresent(e -> {
                e.setClickableWidget(w);
            });
        }

        this.positionList(entries);
    }

    private List<SlayerEntry> getOrderedTask(Widget widget) {
        if (widget == null || widget.getDynamicChildren().length == 0) {
            return List.of();
        }

        Widget[] children = widget.getDynamicChildren();

        List<SlayerEntry> entries = new ArrayList<>();
        for (int i = 0; i < children.length; i++) {
            Widget w = children[i];
            String text = w.getText();
            if (text == null || text.isEmpty()) continue;

            if (text.contains("%")) {
                String percentText = text.replace("%", "");
                double chance = 0;
                try {
                    chance = Double.parseDouble(percentText);
                } catch (NumberFormatException e) {
                    continue;
                }

                String name = (i > 0) ? children[i - 3].getText() : "Unknown";
                entries.add(new SlayerEntry(name, chance, w.getRelativeY(), 0, children[i - 3], w, children[i - 2], children[i - 1], null));
            }
        }

        entries.sort(Comparator.comparingDouble(SlayerEntry::getChance).reversed());

        return entries;
    }

    private void positionList(List<SlayerEntry> entries) {
        clientThread.invoke(() -> {
            int y = INITIAL_Y;
            boolean alternateFilled = true;

            for (SlayerEntry e : entries) {
                e.setNewY(y);
                for (Widget w : List.of(
                        e.getNameWidget(),
                        e.getPercentWidget(),
                        e.getIconWidget(),
                        e.getBackgroundWidget(),
                        e.getClickableWidget()
                )) {
                    if (w != null) {
                        w.setYPositionMode(0);
                        w.setOriginalY(y);
                    }
                }
                e.getBackgroundWidget().setFilled(alternateFilled);
                e.getBackgroundWidget().setOpacity(alternateFilled ? 200 : 220);
                e.getNameWidget().setTextColor(new Color(0xFF981F).getRGB());
                e.getPercentWidget().setTextColor(new Color(0xFF981F).getRGB());

                alternateFilled = !alternateFilled;
                y += LINE_HEIGHT;
            }

            int contentHeight = y;

            Widget drawable = client.getWidget(DRAWABLE_ID);
            Widget clickable = client.getWidget(CLICKABLE_ID);

            if (drawable != null) {
                drawable.getParent().setScrollHeight(contentHeight);
                drawable.getParent().revalidateScroll();
            }
            if (clickable != null) {
                clickable.getParent().setScrollHeight(contentHeight);
                clickable.getParent().revalidateScroll();
            }
        });
    }


}

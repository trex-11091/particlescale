package de.mati.particlescale.gui;

import de.mati.particlescale.ParticleScaleConfig;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI mit je einem Schieberegler pro Partikel-Typ. Die Liste ist scrollbar
 * und durchsuchbar. Änderungen werden sofort gespeichert und auf neu erzeugte
 * Partikel angewendet.
 */
public class ParticleScaleScreen extends Screen {

    private static final int ROW_HEIGHT = 28;     // Kartenhöhe inkl. Abstand
    private static final int CARD_GAP = 4;
    private static final int LIST_TOP = 70;
    private static final int SLIDER_W = 150;
    private static final int VALUE_W = 50;
    private static final int CONTENT_W = 470;
    private static final int PAD = 12;            // Innenabstand der Karten

    // Farbpalette (Lunar-inspiriert, ARGB – Alpha != 0, sonst unsichtbar!)
    private static final int COL_PANEL      = 0xF0141519;
    private static final int COL_PANEL_EDGE = 0xFF2C2E38;
    private static final int COL_HEADER     = 0xFF1B1D23;
    private static final int COL_CARD       = 0xFF1E2027;
    private static final int COL_CARD_HOVER = 0xFF272A34;
    private static final int COL_CARD_EDGE  = 0xFF31343F;
    private static final int COL_LABEL      = 0xFFF2F3F5;
    private static final int COL_NAMESPACE  = 0xFF6E7280;
    private static final int COL_TRACK      = 0xFF101116;
    private static final int COL_FILL       = 0xFF4C8DFF;
    private static final int COL_FILL_DEF   = 0xFF4A4D5A;
    private static final int COL_KNOB       = 0xFFF2F3F5;
    private static final int COL_KNOB_HOVER = 0xFFFFFFFF;
    private static final int COL_VALUE      = 0xFF7FB0FF;
    private static final int COL_VALUE_DEF  = 0xFF9A9DAA;
    private static final int COL_TITLE      = 0xFFFFFFFF;
    private static final int COL_ACCENT     = 0xFF4C8DFF;
    private static final int COL_SUBTITLE   = 0xFF8A8D99;
    private static final int COL_SCROLL_BG  = 0x33000000;
    private static final int COL_SCROLL_BAR = 0xFF4C8DFF;

    private final Screen parent;

    private final List<Identifier> allTypes = new ArrayList<>();
    private final List<Identifier> visibleTypes = new ArrayList<>();

    private TextFieldWidget searchField;
    private double scrollOffset = 0;
    private int draggingIndex = -1;

    public ParticleScaleScreen(Screen parent) {
        super(Text.translatable("particlescale.gui.title"));
        this.parent = parent;
        Registries.PARTICLE_TYPE.getIds().stream()
                .sorted((a, b) -> a.toString().compareToIgnoreCase(b.toString()))
                .forEach(allTypes::add);
    }

    private int panelTop() {
        return 24;
    }

    private int panelBottom() {
        return this.height - 36;
    }

    private int listBottom() {
        return panelBottom() - 6;
    }

    private int panelLeft() {
        return (this.width - CONTENT_W) / 2;
    }

    private int cardRight() {
        return panelLeft() + CONTENT_W - 16;
    }

    private int sliderX() {
        return cardRight() - VALUE_W - SLIDER_W - 6;
    }

    @Override
    protected void init() {
        searchField = new TextFieldWidget(this.textRenderer,
                panelLeft() + PAD, 46, CONTENT_W - PAD * 2, 18,
                Text.translatable("particlescale.gui.search"));
        searchField.setPlaceholder(Text.translatable("particlescale.gui.search"));
        searchField.setChangedListener(text -> {
            scrollOffset = 0;
            rebuildVisible();
        });
        addDrawableChild(searchField);

        int by = this.height - 30;
        addDrawableChild(ButtonWidget.builder(
                        Text.translatable("particlescale.gui.reset_all"),
                        b -> ParticleScaleConfig.resetAll())
                .dimensions(panelLeft(), by, 130, 20)
                .build());
        addDrawableChild(ButtonWidget.builder(
                        debugLabel(),
                        b -> {
                            ParticleScaleConfig.setDebugLog(!ParticleScaleConfig.isDebugLog());
                            b.setMessage(debugLabel());
                        })
                .dimensions(this.width / 2 - 90, by, 180, 20)
                .build());
        addDrawableChild(ButtonWidget.builder(
                        Text.translatable("particlescale.gui.done"),
                        b -> close())
                .dimensions(panelLeft() + CONTENT_W - 130, by, 130, 20)
                .build());

        rebuildVisible();
    }

    private Text debugLabel() {
        return Text.translatable(ParticleScaleConfig.isDebugLog()
                ? "particlescale.gui.debug_on"
                : "particlescale.gui.debug_off");
    }

    private void rebuildVisible() {
        visibleTypes.clear();
        String query = searchField == null ? "" : searchField.getText().trim().toLowerCase();
        for (Identifier id : allTypes) {
            if (query.isEmpty() || id.toString().toLowerCase().contains(query)) {
                visibleTypes.add(id);
            }
        }
        clampScroll();
    }

    private void clampScroll() {
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll()));
    }

    private double maxScroll() {
        int total = visibleTypes.size() * ROW_HEIGHT;
        int view = listBottom() - LIST_TOP;
        return Math.max(0, total - view);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Hintergrund (inkl. Blur) zeichnet die Engine bereits vor render();
        // ein eigener renderBackground-Aufruf würde doppelt blurren und crashen.
        super.render(context, mouseX, mouseY, delta);

        int pl = panelLeft();
        int top = LIST_TOP;
        int bottom = listBottom();

        // Gesamt-Panel mit Rahmen
        fillRounded(context, pl - 8, panelTop(), pl + CONTENT_W + 8, panelBottom(), COL_PANEL);
        roundedBorder(context, pl - 8, panelTop(), pl + CONTENT_W + 8, panelBottom(), COL_PANEL_EDGE);

        // Kopfzeile (Titel, Zähler, Akzentlinie)
        context.drawTextWithShadow(this.textRenderer, this.title, pl, panelTop() + 9, COL_TITLE);
        String count = visibleTypes.size() + " / " + allTypes.size();
        context.drawTextWithShadow(this.textRenderer, Text.literal(count),
                pl + CONTENT_W - this.textRenderer.getWidth(count), panelTop() + 9, COL_SUBTITLE);
        context.fill(pl, top - 4, pl + CONTENT_W, top - 3, COL_PANEL_EDGE);

        // Liste (geclippt)
        context.enableScissor(pl - 8, top, pl + CONTENT_W + 8, bottom);
        for (int i = 0; i < visibleTypes.size(); i++) {
            int rowY = (int) (top - scrollOffset + i * ROW_HEIGHT);
            if (rowY + ROW_HEIGHT < top || rowY > bottom) {
                continue;
            }
            drawRow(context, visibleTypes.get(i), rowY, mouseX, mouseY);
        }
        context.disableScissor();

        drawScrollbar(context);
    }

    private void drawRow(DrawContext context, Identifier id, int rowY, int mouseX, int mouseY) {
        int pl = panelLeft();
        int cardTop = rowY + CARD_GAP / 2;
        int cardBot = rowY + ROW_HEIGHT - CARD_GAP / 2;
        int cardMid = (cardTop + cardBot) / 2;
        int textY = cardMid - 4;
        int cardL = pl;
        int cardR = cardRight();

        float scale = ParticleScaleConfig.getScale(id);
        boolean changed = Math.abs(scale - ParticleScaleConfig.DEFAULT_SCALE) > 1.0e-4f;

        boolean hover = mouseX >= cardL && mouseX <= cardR
                && mouseY >= cardTop && mouseY < cardBot;

        // Karte
        fillRounded(context, cardL, cardTop, cardR, cardBot, hover ? COL_CARD_HOVER : COL_CARD);
        roundedBorder(context, cardL, cardTop, cardR, cardBot, hover ? COL_ACCENT : COL_CARD_EDGE);

        // Name links (Namespace gedimmt, falls nicht vanilla)
        int nameX = cardL + PAD;
        if (id.getNamespace().equals("minecraft")) {
            String name = trim(id.getPath(), sliderX() - nameX - 8);
            context.drawTextWithShadow(this.textRenderer, Text.literal(name), nameX, textY, COL_LABEL);
        } else {
            String ns = id.getNamespace() + ":";
            context.drawTextWithShadow(this.textRenderer, Text.literal(ns), nameX, textY, COL_NAMESPACE);
            int nsW = this.textRenderer.getWidth(ns);
            String name = trim(id.getPath(), sliderX() - nameX - 8 - nsW);
            context.drawTextWithShadow(this.textRenderer, Text.literal(name), nameX + nsW, textY, COL_LABEL);
        }

        // Slider-Spur
        int sx = sliderX();
        int sh = 6;
        int sy = cardMid - sh / 2;
        fillRounded(context, sx, sy, sx + SLIDER_W, sy + sh, COL_TRACK);

        // Gefüllter Teil + Griff
        float fraction = (scale - ParticleScaleConfig.MIN_SCALE)
                / (ParticleScaleConfig.MAX_SCALE - ParticleScaleConfig.MIN_SCALE);
        int knobCenter = sx + Math.round(fraction * SLIDER_W);
        if (knobCenter > sx + 1) {
            fillRounded(context, sx, sy, knobCenter, sy + sh, changed ? COL_FILL : COL_FILL_DEF);
        }
        boolean knobHover = mouseX >= sx - 4 && mouseX <= sx + SLIDER_W + 4
                && mouseY >= cardTop && mouseY < cardBot;
        fillRounded(context, knobCenter - 3, cardMid - 7, knobCenter + 3, cardMid + 7,
                knobHover ? COL_KNOB_HOVER : COL_KNOB);

        // Wert rechts
        String value = String.format("%.1fx", scale);
        context.drawTextWithShadow(this.textRenderer, Text.literal(value),
                sx + SLIDER_W + 10, textY, changed ? COL_VALUE : COL_VALUE_DEF);
    }

    private void drawScrollbar(DrawContext context) {
        double max = maxScroll();
        if (max <= 0) {
            return;
        }
        int top = LIST_TOP;
        int bottom = listBottom();
        int trackH = bottom - top;
        int barX = panelLeft() + CONTENT_W - 8;
        int total = visibleTypes.size() * ROW_HEIGHT;
        int barH = Math.max(24, (int) ((long) trackH * trackH / total));
        int barY = top + (int) ((trackH - barH) * (scrollOffset / max));
        fillRounded(context, barX, top, barX + 4, bottom, COL_SCROLL_BG);
        fillRounded(context, barX, barY, barX + 4, barY + barH, COL_SCROLL_BAR);
    }

    /** Füllt ein Rechteck mit „abgerundeten" 1px-Ecken. */
    private void fillRounded(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        if (x2 - x1 < 2 || y2 - y1 < 2) {
            context.fill(x1, y1, x2, y2, color);
            return;
        }
        context.fill(x1 + 1, y1, x2 - 1, y2, color);
        context.fill(x1, y1 + 1, x1 + 1, y2 - 1, color);
        context.fill(x2 - 1, y1 + 1, x2, y2 - 1, color);
    }

    private void roundedBorder(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        context.fill(x1 + 1, y1, x2 - 1, y1 + 1, color);
        context.fill(x1 + 1, y2 - 1, x2 - 1, y2, color);
        context.fill(x1, y1 + 1, x1 + 1, y2 - 1, color);
        context.fill(x2 - 1, y1 + 1, x2, y2 - 1, color);
    }

    private String trim(String text, int maxWidth) {
        if (this.textRenderer.getWidth(text) <= maxWidth) {
            return text;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if (this.textRenderer.getWidth(sb.toString() + text.charAt(i) + "...") > maxWidth) {
                break;
            }
            sb.append(text.charAt(i));
        }
        return sb + "...";
    }

    private int entryAt(double mx, double my) {
        if (my < LIST_TOP || my > listBottom()) {
            return -1;
        }
        int sx = sliderX();
        if (mx < sx || mx > sx + SLIDER_W) {
            return -1;
        }
        int idx = (int) ((my - LIST_TOP + scrollOffset) / ROW_HEIGHT);
        if (idx < 0 || idx >= visibleTypes.size()) {
            return -1;
        }
        return idx;
    }

    private void applySliderValue(int index, double mx) {
        if (index < 0 || index >= visibleTypes.size()) {
            return;
        }
        int sx = sliderX();
        double fraction = Math.max(0, Math.min(1, (mx - sx) / SLIDER_W));
        float scale = (float) (ParticleScaleConfig.MIN_SCALE
                + fraction * (ParticleScaleConfig.MAX_SCALE - ParticleScaleConfig.MIN_SCALE));
        // auf 0.1-Schritte runden
        scale = Math.round(scale * 10f) / 10f;
        ParticleScaleConfig.setScale(visibleTypes.get(index), scale);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (super.mouseClicked(click, doubled)) {
            return true;
        }
        int idx = entryAt(click.x(), click.y());
        if (idx >= 0 && click.button() == 0) {
            draggingIndex = idx;
            applySliderValue(idx, click.x());
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (draggingIndex >= 0) {
            applySliderValue(draggingIndex, click.x());
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        draggingIndex = -1;
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseY >= LIST_TOP && mouseY <= listBottom()) {
            scrollOffset -= verticalAmount * ROW_HEIGHT;
            clampScroll();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void close() {
        ParticleScaleConfig.save();
        this.client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}

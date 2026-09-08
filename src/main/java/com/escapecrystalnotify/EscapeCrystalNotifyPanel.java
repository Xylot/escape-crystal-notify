package com.escapecrystalnotify;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.ParseException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;

final class EscapeCrystalNotifyPanel extends PluginPanel {
    private final EscapeCrystalNotifyThresholds thresholds;
    private final Map<String, EncounterRow> rows = new LinkedHashMap<>();
    private final Map<EscapeCrystalNotifyRegionType, JLabel> headings = new EnumMap<>(EscapeCrystalNotifyRegionType.class);
    private final Map<EscapeCrystalNotifyRegionType, JPanel> sections = new EnumMap<>(EscapeCrystalNotifyRegionType.class);
    private final JPanel nearbyRows = createRowContainer();
    private final JLabel nearbyEmpty = new JLabel("No supported entrances nearby");
    private Set<EscapeCrystalNotifyRegion> nearbyBosses = Collections.emptySet();
    private final JLabel noMatches = new JLabel("No matching encounters");
    private boolean refreshing;
    private Object displayedProfile;
    private String lastQuery = "";

    EscapeCrystalNotifyPanel(EscapeCrystalNotifyThresholds thresholds, BiConsumer<Integer, JLabel> loadIcon) {
        this.thresholds = thresholds;
        addHeader();
        addSearchField();
        addNearbySection();
        addEncounterSection(EscapeCrystalNotifyRegionType.BOSSES, "Bosses", loadIcon);
        addEncounterSection(EscapeCrystalNotifyRegionType.RAIDS, "Raids", loadIcon);
        noMatches.setVisible(false);
        add(noMatches);
        refresh();
    }

    private void addHeader() {
        add(new JLabel("<html><div style='width:150px'><b>Maximum crystal settings</b><br>All values are in seconds.</div></html>"));
    }

    private void addSearchField() {
        JLabel searchLabel = new JLabel("Search bosses and raids");
        searchLabel.setBorder(new EmptyBorder(8, 0, 0, 0));
        IconTextField search = new IconTextField();
        search.setIcon(IconTextField.Icon.SEARCH);
        search.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        search.setBorder(new EmptyBorder(7, 0, 7, 0));
        searchLabel.setLabelFor(search);
        add(searchLabel);
        add(search);
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filter(search.getText()); }
            @Override public void removeUpdate(DocumentEvent e) { filter(search.getText()); }
            @Override public void changedUpdate(DocumentEvent e) { filter(search.getText()); }
        });
    }

    private void addEncounterSection(EscapeCrystalNotifyRegionType type, String title, BiConsumer<Integer, JLabel> loadIcon) {
        JLabel heading = new JLabel(title);
        heading.setBorder(new EmptyBorder(12, 0, 4, 0));
        headings.put(type, heading);
        add(heading);
        JPanel section = createRowContainer();
        sections.put(type, section);
        add(section);
        for (EscapeCrystalNotifyRegion encounter : EscapeCrystalNotifyEncounters.all()) {
            if (encounter.getRegionType() != type) continue;
            EncounterRow row = new EncounterRow(encounter, loadIcon);
            rows.put(row.key, row);
            section.add(row);
        }
    }

    private static JPanel createRowContainer() {
        JPanel container = new JPanel(new DynamicGridLayout(0, 1, 0, 3));
        container.setOpaque(false);
        return container;
    }

    private void addNearbySection() {
        JLabel heading = new JLabel("Nearby");
        heading.setBorder(new EmptyBorder(12, 0, 4, 0));
        add(heading);
        nearbyRows.setVisible(false);
        add(nearbyRows);
        nearbyEmpty.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        add(nearbyEmpty);
    }

    void setNearbyBosses(Set<EscapeCrystalNotifyRegion> nearby) {
        if (nearbyBosses.equals(nearby)) return;
        nearbyBosses = Set.copyOf(nearby);
        int nearbyIndex = 0;
        int bossIndex = 0;
        int raidIndex = 0;
        for (EncounterRow row : rows.values()) {
            boolean isNearby = nearbyBosses.contains(row.encounter);
            JPanel parent = isNearby ? nearbyRows : sections.get(row.encounter.getRegionType());
            int index = isNearby ? nearbyIndex++ : row.encounter.getRegionType() == EscapeCrystalNotifyRegionType.BOSSES
                ? bossIndex++ : raidIndex++;
            if (row.getParent() != parent) parent.add(row);
            parent.setComponentZOrder(row, index);
        }
        applyFilter();
    }

    private void filter(String text) {
        String query = text.trim().toLowerCase(Locale.ROOT);
        if (query.equals(lastQuery)) return;
        lastQuery = query;
        applyFilter();
    }

    private void applyFilter() {
        boolean bossesVisible = false;
        boolean raidsVisible = false;
        boolean nearbyVisible = false;
        for (EncounterRow row : rows.values()) {
            boolean visible = row.searchName.contains(lastQuery);
            row.setVisible(visible);
            if (visible) {
                if (nearbyBosses.contains(row.encounter)) nearbyVisible = true;
                else if (row.encounter.getRegionType() == EscapeCrystalNotifyRegionType.BOSSES) bossesVisible = true;
                else raidsVisible = true;
            }
        }
        headings.get(EscapeCrystalNotifyRegionType.BOSSES).setVisible(bossesVisible);
        headings.get(EscapeCrystalNotifyRegionType.RAIDS).setVisible(raidsVisible);
        sections.get(EscapeCrystalNotifyRegionType.BOSSES).setVisible(bossesVisible);
        sections.get(EscapeCrystalNotifyRegionType.RAIDS).setVisible(raidsVisible);
        nearbyRows.setVisible(nearbyVisible);
        nearbyEmpty.setText(nearbyBosses.isEmpty() ? "No supported entrances nearby" : "No nearby matches");
        nearbyEmpty.setVisible(!nearbyVisible);
        noMatches.setVisible(!bossesVisible && !raidsVisible && !nearbyVisible);
        revalidate();
        repaint();
    }

    private boolean ensureCurrentProfile() {
        if (displayedProfile == thresholds.profile()) return true;
        refresh();
        return false;
    }

    void refresh() {
        refresh(null);
    }

    void refresh(String key) {
        Object profile = thresholds.profile();
        boolean refreshAll = key == null || displayedProfile != profile;
        refreshing = true;
        try {
            displayedProfile = profile;
            if (refreshAll) {
                rows.values().forEach(row -> row.refresh(true));
            } else {
                EncounterRow row = rows.get(key);
                if (row != null) row.refresh(false);
            }
        } finally {
            refreshing = false;
        }
    }

    private final class EncounterRow extends JPanel {
        private final EscapeCrystalNotifyRegion encounter;
        private final String key;
        private final String searchName;
        private final JSpinner spinner;
        private final JFormattedTextField input;

        private EncounterRow(EscapeCrystalNotifyRegion encounter, BiConsumer<Integer, JLabel> loadIcon) {
            super(new BorderLayout(6, 0));
            this.encounter = encounter;
            key = EscapeCrystalNotifyThresholds.key(encounter);
            searchName = encounter.getRegionName().toLowerCase(Locale.ROOT);
            setBackground(ColorScheme.DARKER_GRAY_COLOR);
            setBorder(new EmptyBorder(8, 6, 8, 8));
            spinner = EscapeCrystalNotifyEncounters.hasEntrance(encounter) ? createSpinner() : null;
            input = spinner == null ? null : ((JSpinner.NumberEditor) spinner.getEditor()).getTextField();
            if (input != null) configureInput();
            add(createIcon(loadIcon), BorderLayout.WEST);
            add(createDetails(), BorderLayout.CENTER);
        }

        private JLabel createIcon(BiConsumer<Integer, JLabel> loadIcon) {
            JLabel icon = new JLabel();
            icon.setPreferredSize(new Dimension(36, 32));
            loadIcon.accept(EscapeCrystalNotifyEncounters.icon(encounter), icon);
            return icon;
        }

        private JPanel createDetails() {
            JPanel details = new JPanel(new BorderLayout(0, 6));
            details.setOpaque(false);
            details.add(wrappedLabel(encounter.getRegionName()), BorderLayout.NORTH);
            if (spinner != null) {
                details.add(createEditor(), BorderLayout.CENTER);
            } else {
                JLabel unavailable = wrappedLabel("Entrance warning unavailable");
                unavailable.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
                details.add(unavailable, BorderLayout.CENTER);
            }
            return details;
        }

        private JLabel wrappedLabel(String text) {
            return new JLabel("<html><div style='width:112px'>" + text + "</div></html>");
        }

        private JSpinner createSpinner() {
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(EscapeCrystalNotifyThresholds.DEFAULT_SECONDS,
                EscapeCrystalNotifyThresholds.MIN_SECONDS, Integer.MAX_VALUE, 1));
            spinner.setEditor(new JSpinner.NumberEditor(spinner, "0"));
            spinner.setPreferredSize(new Dimension(68, 24));
            return spinner;
        }

        private void configureInput() {
            input.setFormatterFactory(new DefaultFormatterFactory(new DefaultFormatter() {
                @Override public Object stringToValue(String text) throws ParseException {
                    try {
                        return EscapeCrystalNotifyThresholds.parseSeconds(text);
                    } catch (NumberFormatException e) {
                        throw new ParseException("Enter whole seconds, at least " + EscapeCrystalNotifyThresholds.MIN_SECONDS, 0);
                    }
                }
            }));
            ((DefaultFormatter) input.getFormatter()).setOverwriteMode(false);
            input.setFocusLostBehavior(JFormattedTextField.PERSIST);
            input.setToolTipText("Maximum inactivity setting in seconds (minimum: " + EscapeCrystalNotifyThresholds.MIN_SECONDS
                + ", default: " + EscapeCrystalNotifyThresholds.DEFAULT_SECONDS + ")");
            input.addActionListener(e -> commit());
            input.addFocusListener(new FocusAdapter() {
                @Override public void focusLost(FocusEvent e) { commit(); }
            });
            spinner.addChangeListener(e -> save());
        }

        private JPanel createEditor() {
            JPanel editor = new JPanel(new BorderLayout(8, 0));
            editor.setOpaque(false);
            editor.add(spinner, BorderLayout.WEST);
            JButton reset = new JButton("Reset");
            reset.setMargin(new Insets(0, 2, 0, 2));
            reset.setBorderPainted(false);
            reset.setContentAreaFilled(false);
            reset.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
            reset.setToolTipText("Reset to " + EscapeCrystalNotifyThresholds.DEFAULT_SECONDS + " seconds");
            reset.addActionListener(e -> resetToDefault());
            editor.add(reset, BorderLayout.EAST);
            return editor;
        }

        private void save() {
            if (!refreshing && ensureCurrentProfile()) {
                thresholds.set(encounter, (Integer) spinner.getValue(), displayedProfile);
            }
        }

        private void resetToDefault() {
            if (!ensureCurrentProfile()) return;
            spinner.setValue(EscapeCrystalNotifyThresholds.DEFAULT_SECONDS);
            input.setValue(spinner.getValue());
        }

        private void commit() {
            if (refreshing || !ensureCurrentProfile()) return;
            try {
                spinner.commitEdit();
            } catch (ParseException | IllegalArgumentException e) {
                input.setValue(spinner.getValue());
            }
        }

        private void refresh(boolean force) {
            if (spinner == null) return;
            int value = thresholds.get(encounter);
            if (force || !spinner.getValue().equals(value)) {
                spinner.setValue(value);
                input.setValue(value);
            }
        }
    }
}

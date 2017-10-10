package com.tylersuehr.library.data;
import android.support.annotation.VisibleForTesting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Copyright © 2017 Tyler Suehr
 *
 * Simple implementation of {@link ChipDataSource} that will aggregate chips
 * using an {@link ArrayList}.
 *
 * @author Tyler Suehr
 * @version 1.0
 */
public class ListChipDataSource implements ChipDataSource {
    /* Stores listener to receive chip selection callbacks */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    OnChipSelectedObserver chipSelectedObserver;

    /* Aggregation of observers listening to chip data changes */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    List<OnChipChangedObserver> observers;

    /* Stores all the original chips */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    List<Chip> originalChips;

    /* Stores all the filtered chips, that are not selected by the user */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    List<Chip> filteredChips;

    /* Stores all the selected chips, selected by the user */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    List<Chip> selectedChips;


    /* Construct with all empty lists */
    public ListChipDataSource() {
        this.originalChips = new ArrayList<>();
        this.filteredChips = new ArrayList<>();
        this.selectedChips = new ArrayList<>();
    }

    @Override
    public List<Chip> getSelectedChips() {
        return selectedChips;
    }

    @Override
    public List<Chip> getFilteredChips() {
        return filteredChips;
    }

    @Override
    public List<Chip> getOriginalChips() {
        return originalChips;
    }

    @Override
    public Chip getFilteredChip(int position) {
        return filteredChips.get(position);
    }

    @Override
    public Chip getSelectedChip(int position) {
        return selectedChips.get(position);
    }

    @Override
    public void setFilterableChips(List<? extends Chip> chips) {
        if (chips == null) {
            throw new NullPointerException("Chips cannot be null!");
        }

        // Instantiate our chip lists with the size of the given list
        this.originalChips = new ArrayList<>(chips.size());
        this.filteredChips = new ArrayList<>(chips.size());
        this.selectedChips = new ArrayList<>(chips.size());

        // Only copy the data from our chips into the original and filtered lists
        for (Chip chip : chips) {
            chip.setFilterable(true);
            this.originalChips.add(chip);
            this.filteredChips.add(chip);
        }

        // Sort the lists
        Collections.sort(originalChips, Chip.getComparator());
        Collections.sort(filteredChips, Chip.getComparator());
    }

    @Override
    public void takeChip(Chip chip) {
        if (chip == null) {
            throw new NullPointerException("Chip cannot be null!");
        }

        // Check if chip is filterable
        if (chip.isFilterable()) {
            // Check if chip is actually in the filtered list
            if (filteredChips.contains(chip)) {
                this.originalChips.remove(chip);
                this.filteredChips.remove(chip);
                this.selectedChips.add(chip);
                notifySelected(chip);
                notifyChanged();
            } else {
                throw new IllegalArgumentException("Chip is not in filtered chip list!");
            }
        } else {
            // Just add it to the selected list only
            this.selectedChips.add(chip);
            notifySelected(chip);
            notifyChanged();
        }
    }

    @Override
    public void takeChip(int position) {
        final Chip foundChip = filteredChips.get(position);
        if (foundChip == null) {
            throw new NullPointerException("Chip cannot be null; not found in filtered chip list!");
        }

        // Check if chip is filterable
        if (foundChip.isFilterable()) {
            // Since the child isn't null, we know it's in the filtered list
            this.originalChips.remove(foundChip);
            this.filteredChips.remove(foundChip);
            this.selectedChips.add(foundChip);
            notifySelected(foundChip);
            notifyChanged();
        } else {
            // Just add it to the selected list only
            this.selectedChips.add(foundChip);
            notifySelected(foundChip);
            notifyChanged();
        }
    }

    @Override
    public void replaceChip(Chip chip) {
        if (chip == null) {
            throw new NullPointerException("Chip cannot be null!");
        }

        // Check if chip is actually selected
        if (selectedChips.contains(chip)) {
            this.selectedChips.remove(chip);

            // Check if the chip is filterable
            if (chip.isFilterable()) {
                this.filteredChips.add(chip);
                this.originalChips.add(chip);
            }

            notifyDeselected(chip);
            notifyChanged();
        } else {
            throw new IllegalArgumentException("Chip is not in selected chip list!");
        }
    }

    @Override
    public void replaceChip(int position) {
        final Chip foundChip = selectedChips.get(position);
        if (foundChip == null) {
            throw new NullPointerException("Chip cannot be null; not found in selected chip list!");
        }

        // Since not null, we know the chip is selected
        this.selectedChips.remove(foundChip);

        // Check if the chip is filterable
        if (foundChip.isFilterable()) {
            this.filteredChips.add(foundChip);
            this.originalChips.add(foundChip);
        }

        notifyDeselected(foundChip);
        notifyChanged();
    }

    @Override
    public boolean existsInDataSource(Chip chip) {
        if (chip == null) {
            throw new NullPointerException("Chip cannot be null!");
        }
        return (originalChips.contains(chip)
                || filteredChips.contains(chip)
                || selectedChips.contains(chip));
    }

    @Override
    public void setOnChipSelectedObserver(OnChipSelectedObserver listener) {
        this.chipSelectedObserver = listener;
    }

    @Override
    public void addOnChipChangedObserver(OnChipChangedObserver observer) {
        if (observers == null) {
            this.observers = new LinkedList<>();
        }
        this.observers.add(observer);
    }

    @Override
    public void removeOnChipChangedObserver(OnChipChangedObserver observer) {
        if (observers != null) {
            this.observers.remove(observer);
        }
    }

    @Override
    public void removeAllOnChipChangedObservers() {
        if (observers != null) {
            this.observers.clear();
        }
    }

    /**
     * Notifies the observers of a change to the data source.
     */
    private void notifyChanged() {
        if (observers != null) {
            synchronized (this) {
                for (OnChipChangedObserver observer : observers) {
                    observer.onChipDataSourceChanged();
                }
            }
        }
    }

    private void notifySelected(Chip chip) {
        if (chipSelectedObserver != null) {
            synchronized (this) {
                this.chipSelectedObserver.onChipAdded(chip);
            }
        }
    }

    private void notifyDeselected(Chip chip) {
        if (chipSelectedObserver != null) {
            synchronized (this) {
                this.chipSelectedObserver.onChipRemoved(chip);
            }
        }
    }
}
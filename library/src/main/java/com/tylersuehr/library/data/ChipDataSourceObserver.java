package com.tylersuehr.library.data;
import android.support.annotation.Nullable;

/**
 * Copyright © 2017 Tyler Suehr
 *
 * Defines an observer that wants to watch for any kind of overall change to the data source.
 *
 * Note: This is intended to be used by internal components of this library.
 *
 * @author Tyler Suehr
 * @version 1.0
 */
public interface ChipDataSourceObserver {
    void onChipDataSourceChanged();
}
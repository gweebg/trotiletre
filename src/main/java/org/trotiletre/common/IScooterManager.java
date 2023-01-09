package org.trotiletre.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.trotiletre.models.utils.GenericPair;
import org.trotiletre.models.utils.Location;

import java.io.IOException;

public interface IScooterManager {

    public @NotNull String listFreeScooters(final int range, @NotNull Location lookupPosition) throws IOException, InterruptedException;
    public GenericPair<String, Location> reserveScooter(final int range, @NotNull Location local, String username) throws IOException, InterruptedException;
    public GenericPair<Double, Double> parkScooter(String reservationCode, Location newScooterLocation, String username) throws IOException, InterruptedException;

}

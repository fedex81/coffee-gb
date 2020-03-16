package eu.rekawek.coffeegb.debug;

import eu.rekawek.coffeegb.Gameboy;

/**
 * IConsole
 * <p>
 * Federico Berti
 * <p>
 * Copyright 2020
 */
public interface IConsole extends Runnable {
    void init(Gameboy gameboy);
    void tick();
}

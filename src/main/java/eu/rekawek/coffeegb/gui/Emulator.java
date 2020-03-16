package eu.rekawek.coffeegb.gui;

import eu.rekawek.coffeegb.Gameboy;
import eu.rekawek.coffeegb.GameboyOptions;
import eu.rekawek.coffeegb.controller.Controller;
import eu.rekawek.coffeegb.cpu.SpeedMode;
import eu.rekawek.coffeegb.debug.Console;
import eu.rekawek.coffeegb.debug.IConsole;
import eu.rekawek.coffeegb.gpu.Display;
import eu.rekawek.coffeegb.memory.cart.Cartridge;
import eu.rekawek.coffeegb.serial.SerialEndpoint;
import eu.rekawek.coffeegb.sound.SoundOutput;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

public class Emulator {

    private static final int SCALE = 2;

    private final GameboyOptions options;

    private final Cartridge rom;

    private final SoundOutput sound;

    private final Display display;

    private final SwingController controller;

    private final SerialEndpoint serialEndpoint;

    private final SpeedMode speedMode;

    private final Gameboy gameboy;

    private final Optional<IConsole> console;

    private JFrame mainWindow;

    public Emulator(String[] args, Properties properties, Display disp, SoundOutput soundOutput) throws IOException {
        options = parseArgs(args);
        rom = new Cartridge(options);
        speedMode = new SpeedMode();
        serialEndpoint = SerialEndpoint.NULL_ENDPOINT;
        //do not map console as it requires the jline lib
        console = Optional.empty();
        console.map(Thread::new).ifPresent(Thread::start);

        if (options.isHeadless()) {
            sound = null;
            display = null;
            controller = null;
            gameboy = new Gameboy(options, rom, Display.NULL_DISPLAY, Controller.NULL_CONTROLLER, SoundOutput.NULL_OUTPUT, serialEndpoint, console);
        } else {
            sound = soundOutput;
            controller = SwingController.createIntController(properties);
            display = disp;
            display.addKeyListener(controller);
            gameboy = new Gameboy(options, rom, display, controller, sound, serialEndpoint, console);
        }
        console.ifPresent(c -> c.init(gameboy));
    }

    public Emulator(String[] args, Properties properties) throws IOException {
        options = parseArgs(args);
        rom = new Cartridge(options);
        speedMode = new SpeedMode();
        serialEndpoint = SerialEndpoint.NULL_ENDPOINT;
        console = options.isDebug() ? Optional.of(new Console()) : Optional.empty();
        console.map(Thread::new).ifPresent(Thread::start);

        if (options.isHeadless()) {
            sound = null;
            display = null;
            controller = null;
            gameboy = new Gameboy(options, rom, Display.NULL_DISPLAY, Controller.NULL_CONTROLLER, SoundOutput.NULL_OUTPUT, serialEndpoint, console);
        } else {
            sound = new AudioSystemSoundOutput();
            controller = SwingController.createController(properties);
            display = new SwingDisplay(SCALE);
            display.addKeyListener(controller);
            gameboy = new Gameboy(options, rom, display, controller, sound, serialEndpoint, console);
        }
        console.ifPresent(c -> c.init(gameboy));
    }

    private static GameboyOptions parseArgs(String[] args) {
        if (args.length == 0) {
            GameboyOptions.printUsage(System.out);
            System.exit(0);
            return null;
        }
        try {
            return createGameboyOptions(args);
        } catch(IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println();
            GameboyOptions.printUsage(System.err);
            System.exit(1);
            return null;
        }
    }

    private static GameboyOptions createGameboyOptions(String[] args) {
        Set<String> params = new HashSet<>();
        Set<String> shortParams = new HashSet<>();
        String romPath = null;
        for (String a : args) {
            if (a.startsWith("--")) {
                params.add(a.substring(2));
            } else if (a.startsWith("-")) {
                shortParams.add(a.substring(1));
            } else {
                romPath = a;
            }
        }
        if (romPath == null) {
            throw new IllegalArgumentException("ROM path hasn't been specified");
        }
        File romFile = new File(romPath);
        if (!romFile.exists()) {
            throw new IllegalArgumentException("The ROM path doesn't exist: " + romPath);
        }
        return new GameboyOptions(romFile, params, shortParams);
    }

    public void run() throws Exception {
        if (options.isHeadless()) {
            gameboy.run();
        } else {
            System.setProperty("sun.java2d.opengl", "true");

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.invokeLater(() -> startGui());
        }
    }

    public void runOnCurrentThread() {
        gameboy.run();
    }

    public void stop(){
        gameboy.stop();
    }

    private void startGui() {
        Optional<SwingDisplay> swingDisplay = Optional.empty();
        if(display instanceof SwingDisplay){
            swingDisplay = Optional.of((SwingDisplay) display);
        }

        swingDisplay.ifPresent(d -> d.setPreferredSize(new Dimension(160 * SCALE, 144 * SCALE)));
        mainWindow = new JFrame("Coffee GB: " + rom.getTitle());
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWindow.setLocationRelativeTo(null);

        swingDisplay.ifPresent(mainWindow::setContentPane);
        mainWindow.setResizable(false);
        mainWindow.setVisible(true);
        mainWindow.pack();

        mainWindow.addKeyListener(controller);

        swingDisplay.ifPresent(d -> new Thread(d).start());
        new Thread(gameboy).start();
    }

    private void stopGui() {
        display.stop();
        gameboy.stop();
        mainWindow.dispose();
    }
}

package eu.rekawek.coffeegb.gui;

import eu.rekawek.coffeegb.LoggerFactory;
import eu.rekawek.coffeegb.controller.ButtonListener;
import eu.rekawek.coffeegb.controller.ButtonListener.Button;
import eu.rekawek.coffeegb.controller.Controller;
import org.apache.logging.log4j.Logger;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.Field;
import java.security.InvalidParameterException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class SwingController implements Controller, KeyListener {

    private static final Logger LOG = LoggerFactory.getLogger(SwingController.class);

    private ButtonListener listener;
    private Map<Integer, Button> mapping;

    public static SwingController createController(Properties properties){
        SwingController sc = new SwingController();
        EnumMap<Button, Integer> buttonToKey = sc.addDefaultMapping();
        buttonToKey = sc.parseMappings(buttonToKey, properties);
        return sc;
    }

    public static SwingController createIntController(Properties properties){
        SwingController sc = new SwingController();
        EnumMap<Button, Integer> buttonToKey = sc.addDefaultMapping();
        buttonToKey = sc.parseMappings2(buttonToKey, properties);
        return sc;
    }

    private EnumMap<Button, Integer> addDefaultMapping(){
        EnumMap<Button, Integer> buttonToKey = new EnumMap<>(Button.class);

        buttonToKey.put(Button.LEFT, KeyEvent.VK_LEFT);
        buttonToKey.put(Button.RIGHT, KeyEvent.VK_RIGHT);
        buttonToKey.put(Button.UP, KeyEvent.VK_UP);
        buttonToKey.put(Button.DOWN, KeyEvent.VK_DOWN);
        buttonToKey.put(Button.A, KeyEvent.VK_Z);
        buttonToKey.put(Button.B, KeyEvent.VK_X);
        buttonToKey.put(Button.START, KeyEvent.VK_ENTER);
        buttonToKey.put(Button.SELECT, KeyEvent.VK_BACK_SPACE);
        return buttonToKey;
    }

    private EnumMap<Button, Integer> parseMappings(EnumMap<Button, Integer> buttonToKey, Properties properties){
        for (String k : properties.stringPropertyNames()) {
            String v = properties.getProperty(k);
            if (k.startsWith("btn_") && v.startsWith("VK_")) {
                try {
                    Button button = Button.valueOf(k.substring(4).toUpperCase());
                    Field field = KeyEvent.class.getField(properties.getProperty(k));
                    if (field.getType() != int.class) {
                        continue;
                    }
                    int value = field.getInt(null);
                    buttonToKey.put(button, value);
                } catch (IllegalArgumentException | NoSuchFieldException | IllegalAccessException e) {
                    LOG.error("Can't parse button configuration", e);
                }
            }
        }
        this.mapping = buttonToKey.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        return buttonToKey;
    }

    private EnumMap<Button, Integer> parseMappings2(EnumMap<Button, Integer> buttonToKey, Properties properties){
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            if (entry.getKey() instanceof String) {
                String k = String.valueOf(entry.getKey());
                if (k.startsWith("btn_")) {
                    try {
                        int v = Integer.valueOf(entry.getValue().toString());
                        Button button = Button.valueOf(k.substring(4).toUpperCase());
                        buttonToKey.put(button, v);
                        LOG.debug("{} -> {}", k, v);
                    } catch (Exception e) {
                        LOG.debug("Ignoring button {}", k);
                    }
                }
            }
        }
        this.mapping = buttonToKey.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        return buttonToKey;
    }

    @Override
    public void setButtonListener(ButtonListener listener) {
        this.listener = listener;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (listener == null) {
            return;
        }
        Button b = getButton(e);
        if (b != null) {
            listener.onButtonPress(b);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (listener == null) {
            return;
        }
        Button b = getButton(e);
        if (b != null) {
            listener.onButtonRelease(b);
        }
    }

    private Button getButton(KeyEvent e) {
        return mapping.get(e.getKeyCode());
    }
}

package eu.rekawek.coffeegb.gpu;

import java.awt.event.KeyListener;

public interface Display {

    void putDmgPixel(int color);

    void putColorPixel(int gbcRgb);

    void requestRefresh();

    void waitForRefresh();

    void enableLcd();

    void disableLcd();

    default void addKeyListener(KeyListener listener){

    }

    default void stop(){

    }

    Display NULL_DISPLAY = new Display() {

        @Override
        public void putDmgPixel(int color) {
        }

        @Override
        public void putColorPixel(int gbcRgb) {
        }

        @Override
        public void requestRefresh() {
        }

        @Override
        public void waitForRefresh() {
        }

        @Override
        public void enableLcd() {
        }

        @Override
        public void disableLcd() {
        }

        @Override
        public void addKeyListener(KeyListener listener) {

        }
    };

}

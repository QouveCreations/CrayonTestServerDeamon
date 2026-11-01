package space.qouve;

import java.io.IOException;

public class Launcher {
    public static void main(String[] args) throws IOException {
        Main.main();
        DebugDeamon.main(args);
    }
}
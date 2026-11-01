package space.qouve;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.theming.JavaFXThemes;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import io.github.palexdev.materialfx.theming.UserAgentBuilder;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ServerHandshake;
import space.qouve.logger.DeamonLogger;
import space.qouve.models.ServerType;
import space.qouve.networking.CrayonWebSocketServer;
import space.qouve.networking.client.Client;
import space.qouve.networking.client.ClientInfo;
import space.qouve.networking.handler.Dispatcher;
import space.qouve.networking.handler.Handler;
import space.qouve.protocol.ClientProtocol;
import space.qouve.protocol.ServerProtocol;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * Debug-UI für den Daemon. Muss über {@link Launcher} gestartet werden, damit
 * der WebSocket-Server vorher hochgefahren wird.
 *
 * Das Formular wird per Reflection aus den Records in {@link ServerProtocol}
 * erzeugt. Neue Nachrichtentypen tauchen also automatisch auf.
 */
public class DebugDeamon extends Application {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final int MAX_LOG_CHARS = 200_000;
    private static final String TITLE_STYLE = "-fx-font-size: 15px; -fx-font-weight: bold;";
    private static final String MONO_STYLE = "-fx-font-family: 'monospace'; -fx-font-size: 12px;";

    /**
     * Dark-Theme. Der erste Block überschreibt die Modena-Farbvariablen und färbt damit alle
     * Standard-Controls um (ListView, TextArea, Tabs, ScrollPane, Labels). Der zweite Block
     * ist ein Best-Effort für die MaterialFX-Controls.
     */
    private static final String DARK_CSS = """
            .root {
                -fx-base: #1e1f22;
                -fx-background: #1e1f22;
                -fx-control-inner-background: #2b2d30;
                -fx-control-inner-background-alt: #26282b;
                -fx-accent: #6c8cff;
                -fx-focus-color: #6c8cff;
                -fx-faint-focus-color: #6c8cff22;
                -fx-text-base-color: #dfe1e5;
                -fx-text-background-color: #dfe1e5;
                -fx-text-inner-color: #dfe1e5;
                -fx-prompt-text-fill: #7a7e85;
                -fx-selection-bar: #3a4a8a;
                -fx-selection-bar-non-focused: #33363b;
            }
            .label { -fx-text-fill: #dfe1e5; }
            .text-area, .text-area .content { -fx-text-fill: #dfe1e5; }

            .mfx-button { -fx-background-color: #3a4a8a; -fx-text-fill: #ffffff; }
            .mfx-button:hover { -fx-background-color: #4a5cab; }
            .mfx-text-field, .mfx-combo-box { -fx-background-color: #2b2d30; -fx-text-fill: #dfe1e5; }
            .mfx-checkbox, .mfx-checkbox .label { -fx-text-fill: #dfe1e5; }
            """;
    private static final String DARK_SHEET =
            "data:text/css;base64," + Base64.getEncoder().encodeToString(DARK_CSS.getBytes(StandardCharsets.UTF_8));

    private final ObjectMapper pretty = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final ObjectMapper compact = new ObjectMapper();

    private final ObservableList<Client> clientItems = FXCollections.observableArrayList();
    private final ListView<Client> clientList = new ListView<>(clientItems);
    private final Label clientHeader = new Label("Verbundene Clients (0)");

    private final VBox formBox = new VBox(10);
    private final List<FieldEditor> editors = new ArrayList<>();
    private final TextArea preview = new TextArea();
    private final TextArea rawArea = new TextArea();
    private final TextArea logArea = new TextArea();

    private final MFXCheckbox sendToAll = new MFXCheckbox("An alle Clients senden");
    private final MFXCheckbox autoAck = new MFXCheckbox("Eingehende Nachrichten automatisch mit Ack beantworten");
    private final MFXCheckbox darkMode = new MFXCheckbox("Dark Mode");
    private Scene mainScene;

    private final Map<String, DummyClient> dummyClients = new ConcurrentHashMap<>();
    private Class<?> selectedType;

    @FunctionalInterface
    private interface ThrowingSupplier {
        Object get() throws Exception;
    }

    private record FieldEditor(Node node, ThrowingSupplier getter) {}

    // ------------------------------------------------------------------ start

    @Override
    public void start(Stage stage) {
        UserAgentBuilder.builder()
                .themes(JavaFXThemes.MODENA)
                .themes(MaterialFXStylesheets.forAssemble(true))
                .setDeploy(true)
                .setResolveAssets(true)
                .build()
                .setGlobal();

        stage.setTitle("Crayon Daemon Debug");

        if (Main.getSocketServer() == null) {
            Label hint = new Label("Der WebSocket-Server läuft nicht.\nBitte space.qouve.Launcher starten, nicht DebugDeamon direkt.");
            hint.setPadding(new Insets(30));
            stage.setScene(new Scene(new VBox(hint), 520, 160));
            stage.show();
            return;
        }

        hookServerLog();
        registerDebugHandlers();

        BorderPane root = new BorderPane();
        root.setLeft(buildClientPanel());
        root.setCenter(buildSendPanel());
        root.setBottom(buildLogPanel());

        Scene scene = new Scene(root, 1240, 820);
        mainScene = scene;
        applyTheme(darkMode.isSelected());
        stage.setScene(scene);
        stage.show();

        Timeline poll = new Timeline(new KeyFrame(Duration.seconds(1), e -> refreshClients()));
        poll.setCycleCount(Animation.INDEFINITE);
        poll.play();
        refreshClients();

        appendLog("INFO", "Debug-UI bereit. Server lauscht auf Port " + Main.getSocketServer().getPort());
    }

    @Override
    public void stop() {
        dummyClients.values().forEach(c -> {
            try {
                c.close();
            } catch (Exception ignored) {
            }
        });
        try {
            if (Main.getSocketServer() != null) Main.getSocketServer().stop(500);
        } catch (Exception ignored) {
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }

    // ------------------------------------------------------------ client panel

    private Node buildClientPanel() {
        clientHeader.setStyle(TITLE_STYLE);

        clientList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        clientList.setPrefHeight(260);
        clientList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Client c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) {
                    setText(null);
                    return;
                }
                ClientInfo i = c.info();
                setText(i.type() + "  ·  " + i.serverId() + "  ·  v" + i.version());
            }
        });

        MFXButton disconnect = new MFXButton("Auswahl trennen");
        disconnect.setOnAction(e -> {
            for (Client c : new ArrayList<>(clientList.getSelectionModel().getSelectedItems())) {
                c.close(CloseFrame.NORMAL, "Debug-UI");
            }
        });

        // Dummy-Client, damit man ohne echte Gegenstelle testen kann
        Label dummyTitle = new Label("Dummy-Client verbinden");
        dummyTitle.setStyle(TITLE_STYLE);

        MFXComboBox<ServerType> dummyType = new MFXComboBox<>(FXCollections.observableArrayList(ServerType.values()));
        dummyType.setFloatingText("Server-Typ");
        dummyType.setMaxWidth(Double.MAX_VALUE);
        dummyType.selectFirst();

        MFXTextField dummyId = new MFXTextField();
        dummyId.setFloatingText("Server-ID");
        dummyId.setText("dummy-" + shortId());
        dummyId.setMaxWidth(Double.MAX_VALUE);

        MFXTextField dummyToken = new MFXTextField();
        dummyToken.setFloatingText("Token (leer = automatisch)");
        dummyToken.setMaxWidth(Double.MAX_VALUE);

        MFXTextField dummyVersion = new MFXTextField();
        dummyVersion.setFloatingText("Version");
        dummyVersion.setText("debug-1.0");
        dummyVersion.setMaxWidth(Double.MAX_VALUE);

        MFXButton connect = new MFXButton("Verbinden");
        connect.setOnAction(e -> {
            ServerType type = dummyType.getSelectedItem();
            String id = dummyId.getText() == null ? "" : dummyId.getText().trim();
            if (type == null || id.isEmpty()) {
                appendLog("WARN", "Dummy: Typ und ID angeben");
                return;
            }
            String token = dummyToken.getText() == null ? "" : dummyToken.getText().trim();
            if (token.isEmpty()) token = autoToken(type, id);
            connectDummy(type, id, token, dummyVersion.getText());
        });

        MFXButton dropDummies = new MFXButton("Alle Dummys trennen");
        dropDummies.setOnAction(e -> {
            dummyClients.values().forEach(c -> c.close());
            dummyClients.clear();
        });

        VBox box = new VBox(10,
                clientHeader, clientList, disconnect,
                new Separator(Orientation.HORIZONTAL),
                dummyTitle, dummyType, dummyId, dummyToken, dummyVersion,
                new HBox(8, connect, dropDummies));
        box.setPadding(new Insets(14));
        box.setPrefWidth(340);
        VBox.setVgrow(clientList, Priority.NEVER);
        return box;
    }

    private String autoToken(ServerType type, String id) {
        return switch (type) {
            case TESTINSTANCE -> Main.getAuth().createTestInstanceToken(id);
            case STAGING -> Main.getConfig().stagingSecret();
            case PRODUCTIVE -> Main.getConfig().prodSecret();
            case WEBEDITOR -> Main.getConfig().webeditorSecret();
        };
    }

    private void connectDummy(ServerType type, String id, String token, String version) {
        if (dummyClients.containsKey(id)) {
            appendLog("WARN", "Dummy " + id + " existiert schon");
            return;
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Server-Type", type.name());
        headers.put("X-Server-Id", id);
        headers.put("X-Token", token);
        headers.put("X-Version", version == null ? "" : version);
        try {
            DummyClient client = new DummyClient(
                    new URI("ws://localhost:" + Main.getSocketServer().getPort()), headers, id);
            dummyClients.put(id, client);
            client.connect();
            appendLog("DUMMY", "Verbinde " + id + " als " + type + " ...");
        } catch (Exception ex) {
            appendLog("ERROR", "Dummy-Verbindung fehlgeschlagen: " + rootMessage(ex));
        }
    }

    private final class DummyClient extends WebSocketClient {
        private final String id;

        DummyClient(URI uri, Map<String, String> headers, String id) {
            super(uri, headers);
            this.id = id;
        }

        @Override
        public void onOpen(ServerHandshake handshake) {
            appendLog("DUMMY", id + " verbunden");
        }

        @Override
        public void onMessage(String message) {
            appendLog("DUMMY-IN", id + " ← " + message);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            dummyClients.remove(id, this);
            appendLog("DUMMY", id + " getrennt (" + code + (reason == null || reason.isBlank() ? "" : ", " + reason) + ")");
        }

        @Override
        public void onError(Exception ex) {
            appendLog("DUMMY", id + " Fehler: " + rootMessage(ex));
        }
    }

    private void refreshClients() {
        CrayonWebSocketServer server = Main.getSocketServer();
        if (server == null) return;
        List<Client> current = new ArrayList<>(server.getClients());
        clientHeader.setText("Verbundene Clients (" + current.size() + ")");
        if (current.equals(clientItems)) return;

        List<Client> selected = new ArrayList<>(clientList.getSelectionModel().getSelectedItems());
        clientItems.setAll(current);
        for (Client c : selected) {
            if (current.contains(c)) clientList.getSelectionModel().select(c);
        }
    }

    // ------------------------------------------------------------- send panel

    private Node buildSendPanel() {
        Label title = new Label("Nachricht vom Server senden");
        title.setStyle(TITLE_STYLE);

        MFXComboBox<Class<?>> typeBox = new MFXComboBox<>();
        typeBox.setFloatingText("Nachrichtentyp (ServerProtocol)");
        typeBox.setMaxWidth(Double.MAX_VALUE);
        typeBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Class<?> c) {
                return c == null ? "" : c.getSimpleName();
            }

            @Override
            public Class<?> fromString(String s) {
                return null;
            }
        });
        typeBox.setItems(FXCollections.<Class<?>>observableArrayList(ServerProtocol.class.getPermittedSubclasses()));
        typeBox.selectedItemProperty().addListener((obs, old, now) -> {
            if (now != null) buildForm(now);
        });

        // Tab 1: Formular + Vorschau
        formBox.setPadding(new Insets(8));
        ScrollPane formScroll = new ScrollPane(formBox);
        formScroll.setFitToWidth(true);
        HBox.setHgrow(formScroll, Priority.ALWAYS);

        preview.setEditable(false);
        preview.setStyle(MONO_STYLE);
        preview.setWrapText(true);
        Label previewTitle = new Label("JSON-Vorschau");
        VBox previewBox = new VBox(6, previewTitle, preview);
        previewBox.setPrefWidth(380);
        VBox.setVgrow(preview, Priority.ALWAYS);

        HBox formTab = new HBox(12, formScroll, previewBox);
        formTab.setPadding(new Insets(10));

        // Tab 2: freies JSON
        rawArea.setStyle(MONO_STYLE);
        rawArea.setPromptText("{\"type\":\"ack\",\"id\":\"123\"}");
        MFXButton fromForm = new MFXButton("Aus Formular übernehmen");
        fromForm.setOnAction(e -> {
            try {
                rawArea.setText(pretty.writeValueAsString(buildMessage()));
            } catch (Exception ex) {
                appendLog("WARN", "Formular ungültig: " + rootMessage(ex));
            }
        });
        MFXButton sendRaw = new MFXButton("Raw-JSON senden");
        sendRaw.setOnAction(e -> {
            try {
                send(pretty.readValue(rawArea.getText(), ServerProtocol.class));
            } catch (Exception ex) {
                appendLog("WARN", "Raw-JSON ungültig: " + rootMessage(ex));
            }
        });
        VBox rawTab = new VBox(10, rawArea, new HBox(8, fromForm, sendRaw));
        rawTab.setPadding(new Insets(10));
        VBox.setVgrow(rawArea, Priority.ALWAYS);

        TabPane tabs = new TabPane(new Tab("Formular", formTab), new Tab("Raw JSON", rawTab));
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabs, Priority.ALWAYS);

        // Aktionen
        MFXButton send = new MFXButton("Senden");
        send.setOnAction(e -> {
            try {
                send(buildMessage());
            } catch (Exception ex) {
                appendLog("WARN", "Eingabe ungültig: " + rootMessage(ex));
            }
        });
        MFXButton newId = new MFXButton("Neue ID würfeln");
        newId.setOnAction(e -> regenerateIdField());

        autoAck.setSelected(true);

        VBox box = new VBox(12, title, typeBox, tabs, sendToAll, new HBox(8, send, newId), autoAck);
        box.setPadding(new Insets(14));

        typeBox.selectFirst();
        return box;
    }

    private void buildForm(Class<?> type) {
        selectedType = type;
        editors.clear();
        formBox.getChildren().clear();
        for (RecordComponent rc : type.getRecordComponents()) {
            FieldEditor editor = createEditor(rc);
            editors.add(editor);
            formBox.getChildren().add(editor.node());
        }
        updatePreview();
    }

    private FieldEditor createEditor(RecordComponent rc) {
        Class<?> t = rc.getType();
        String name = rc.getName();

        if (t.isEnum()) {
            Object[] constants = t.getEnumConstants();
            MFXComboBox<Object> box = new MFXComboBox<>(FXCollections.<Object>observableArrayList(constants));
            box.setFloatingText(name + " (" + t.getSimpleName() + ")");
            box.setMaxWidth(Double.MAX_VALUE);
            Object preferred = constants[0];
            for (Object c : constants) {
                if (c.toString().equals("STAGING")) preferred = c;
            }
            box.selectItem(preferred);
            box.selectedItemProperty().addListener((o, a, b) -> updatePreview());
            return new FieldEditor(box, box::getSelectedItem);
        }

        MFXTextField tf = new MFXTextField();
        tf.setText(defaultValue(rc));
        tf.setFloatingText(name + " (" + typeLabel(rc) + ")");
        tf.setUserData(name);
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.textProperty().addListener((o, a, b) -> updatePreview());

        ThrowingSupplier getter;
        if (t == String.class) {
            getter = () -> {
                String s = tf.getText();
                return s == null || s.isEmpty() ? null : s;
            };
        } else if (t == int.class || t == Integer.class) {
            getter = () -> Integer.parseInt(tf.getText().trim());
        } else if (t == long.class || t == Long.class) {
            getter = () -> Long.parseLong(tf.getText().trim());
        } else if (t == double.class || t == Double.class) {
            getter = () -> Double.parseDouble(tf.getText().trim().replace(',', '.'));
        } else if (t == boolean.class || t == Boolean.class) {
            getter = () -> Boolean.parseBoolean(tf.getText().trim());
        } else {
            // Listen und verschachtelte Records als JSON
            JavaType jt = compact.getTypeFactory().constructType(rc.getGenericType());
            getter = () -> compact.readValue(tf.getText(), jt);
        }
        return new FieldEditor(tf, getter);
    }

    private ServerProtocol buildMessage() throws Exception {
        RecordComponent[] components = selectedType.getRecordComponents();
        Class<?>[] types = new Class<?>[components.length];
        Object[] args = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            types[i] = components[i].getType();
            args[i] = editors.get(i).getter().get();
        }
        try {
            return (ServerProtocol) selectedType.getDeclaredConstructor(types).newInstance(args);
        } catch (InvocationTargetException e) {
            throw e.getCause() instanceof Exception ex ? ex : e;
        }
    }

    private void updatePreview() {
        if (selectedType == null) return;
        try {
            preview.setText(pretty.writeValueAsString(buildMessage()));
        } catch (Exception e) {
            preview.setText("Eingabefehler:\n" + rootMessage(e));
        }
    }

    private void regenerateIdField() {
        RecordComponent[] components = selectedType.getRecordComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i].getName().equals("id") && editors.get(i).node() instanceof MFXTextField tf) {
                tf.setText(shortId());
            }
        }
    }

    private static String typeLabel(RecordComponent rc) {
        String s = rc.getGenericType().getTypeName();
        return s.substring(s.lastIndexOf('.') + 1).replace("java.util.List", "List");
    }

    private static String defaultValue(RecordComponent rc) {
        String record = rc.getDeclaringRecord().getSimpleName();
        return switch (rc.getName()) {
            case "id" -> shortId();
            case "userID" -> "dummy-user";
            case "repositoryID" -> "repo-1";
            case "code" -> "DUMMY_ERROR";
            case "message" -> "Dummy error message";
            case "line" -> "[12:00:00] [Server thread/INFO]: Dummy console line";
            case "lines" -> "[\"[Server thread/INFO]: Starting minecraft server\",\"[Server thread/INFO]: Done (3.2s)!\"]";
            case "errorLines" -> "[\"java.lang.NullPointerException: dummy\",\"\\tat dummy.Main.run(Main.java:42)\"]";
            case "place" -> "1";
            case "usage" -> record.equals("CpuUsage") ? "37" : "1024";
            case "max" -> record.equals("CpuUsage") ? "100" : "2048";
            case "time" -> "30";
            case "tps" -> "19.8";
            case "servers" -> "[]";
            case "daemonInformation" ->
                    "{\"version\":\"1.0-SNAPSHOT\",\"uptimeSeconds\":3600,\"runningServers\":2,\"maxServers\":10}";
            default -> {
                Class<?> t = rc.getType();
                if (t == String.class) yield "dummy";
                if (t == boolean.class) yield "false";
                if (t.isPrimitive()) yield "0";
                yield "null";
            }
        };
    }

    private static String shortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private void send(ServerProtocol msg) throws Exception {
        List<Client> targets = sendToAll.isSelected()
                ? new ArrayList<>(Main.getSocketServer().getClients())
                : new ArrayList<>(clientList.getSelectionModel().getSelectedItems());
        if (targets.isEmpty()) {
            appendLog("WARN", "Kein Client ausgewählt (oder \"An alle\" aktivieren)");
            return;
        }
        String json = compact.writeValueAsString(msg);
        for (Client c : targets) {
            c.send(msg);
            appendLog("OUT", c.info().serverId() + " → " + json);
        }
    }

    // -------------------------------------------------------------- incoming

    /**
     * Registriert für alle ClientProtocol-Nachrichten Debug-Handler, die den
     * Eingang loggen. CreateTestServerFromCommit bleibt beim echten Handler.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void registerDebugHandlers() {
        Dispatcher dispatcher = Main.getDispatcher();
        for (Class<?> c : ClientProtocol.class.getPermittedSubclasses()) {
            if (c == ClientProtocol.CreateTestServerFromCommit.class) continue;
            dispatcher.register((Class) c, (Handler) (client, msg) -> onIncoming((Client) client, (ClientProtocol) msg));
        }
    }

    private void onIncoming(Client client, ClientProtocol msg) {
        try {
            appendLog("IN", client.info().serverId() + " ← " + compact.writeValueAsString(msg));
        } catch (Exception e) {
            appendLog("IN", client.info().serverId() + " ← " + msg);
        }
        if (autoAck.isSelected()) {
            client.send(new ServerProtocol.Ack(msg.id()));
            appendLog("OUT", client.info().serverId() + " → Ack(" + msg.id() + ")");
        }
    }

    // ------------------------------------------------------------------- log

    private Node buildLogPanel() {
        Label title = new Label("Log");
        title.setStyle(TITLE_STYLE);
        MFXButton clear = new MFXButton("Leeren");
        clear.setOnAction(e -> logArea.clear());

        darkMode.setSelected(true); // Standard: dunkel
        darkMode.selectedProperty().addListener((o, a, dark) -> applyTheme(dark));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(12, title, clear, spacer, darkMode);

        logArea.setEditable(false);
        logArea.setStyle(MONO_STYLE);
        logArea.setPrefRowCount(11);

        VBox box = new VBox(6, header, logArea);
        box.setPadding(new Insets(0, 14, 14, 14));
        return box;
    }

    private void applyTheme(boolean dark) {
        if (mainScene == null) return;
        mainScene.getStylesheets().remove(DARK_SHEET);
        if (dark) mainScene.getStylesheets().add(DARK_SHEET);
        mainScene.setFill(dark ? Color.web("#1e1f22") : Color.WHITE);
    }

    private void hookServerLog() {
        DeamonLogger.get().addHandler(new java.util.logging.Handler() {
            private final SimpleFormatter formatter = new SimpleFormatter();

            @Override
            public void publish(LogRecord record) {
                appendLog("LOG", record.getLevel() + " " + formatter.formatMessage(record));
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        });
    }

    private void appendLog(String tag, String text) {
        String line = "[" + LocalTime.now().format(TIME) + "] [" + tag + "] " + text + "\n";
        if (Platform.isFxApplicationThread()) {
            doAppend(line);
        } else {
            Platform.runLater(() -> doAppend(line));
        }
    }

    private void doAppend(String line) {
        logArea.appendText(line);
        if (logArea.getLength() > MAX_LOG_CHARS) {
            logArea.deleteText(0, logArea.getLength() - MAX_LOG_CHARS / 2);
        }
    }

    private static String rootMessage(Throwable t) {
        Throwable r = t;
        while (r.getCause() != null && r.getCause() != r) r = r.getCause();
        String m = r.getMessage();
        return r.getClass().getSimpleName() + (m == null ? "" : ": " + m);
    }
}
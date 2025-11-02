package Application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

import antivirus.AntivirusScanner;
import network.NetworkScanner;
import ai.AIAnalyzer;
import software.SoftwareInfo;
import hardware.CPUInfo;
import hardware.DISKInfo;
import hardware.GPUInfo;
import hardware.RAMInfo;

import oshi.SystemInfo;

public class App extends Application {

    private TextArea outputArea;
    private TextField inputField;
    private ListView<String> chatHistoryList;
    private List<Chat> chats;
    private int chatCounter = 1;
    private boolean darkMode = true; 
    private Label headerLabel;
    private AIAnalyzer aiAnalyzer;
    
    private BorderPane root;

    private static class Chat {
        String name;
        StringBuilder messages = new StringBuilder();

        Chat(String name) { this.name = name; }
        void addMessage(String msg) { messages.append(msg).append("\n"); }
        String getMessages() { return messages.toString(); }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Randazzo AI");

        root = new BorderPane();
        root.setPadding(new Insets(10));

        // Intestazione
        headerLabel = new Label("Randazzo AI");
        headerLabel.setPadding(new Insets(10));
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        root.setTop(new StackPane(headerLabel));

        // Area output
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(true);

        // Lista chat
        chats = new ArrayList<>();
        chatHistoryList = new ListView<>();
        chatHistoryList.setPrefWidth(200);
        chatHistoryList.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.intValue() >= 0) loadChat(newVal.intValue());
        });

        // Pulsanti
        Button newChatButton = new Button("Nuova Chat");
        newChatButton.setOnAction(e -> startNewChat());
        Button themeButton = new Button("Cambia Tema");
        themeButton.setOnAction(e -> toggleTheme());

        VBox leftPanel = new VBox(10, new Label("Cronologia Chat"), chatHistoryList, newChatButton, themeButton);
        leftPanel.setPadding(new Insets(0,10,0,0));
        leftPanel.setPrefWidth(200);

        // Pannello input
        HBox inputPanel = new HBox(10);
        inputPanel.setPadding(new Insets(10,0,0,0));
        inputField = new TextField();
        inputField.setPromptText("Scrivi qui la tua domanda...");
        inputField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                processInput();
                e.consume();
            }
        });
        Button sendButton = new Button("Invia");
        sendButton.setOnAction(e -> processInput());
        inputPanel.getChildren().addAll(inputField, sendButton);
        HBox.setHgrow(inputField, Priority.ALWAYS);

        // Organizzazione layout
        root.setLeft(leftPanel);
        root.setCenter(outputArea);
        root.setBottom(inputPanel);

        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        startNewChat();
        applyTheme();
        initAIAnalyzer(); // inizializza l'AI
    }

    private void initAIAnalyzer() {
        // Inizializza SystemInfo
        SystemInfo si = new SystemInfo();

        // Hardware
        CPUInfo cpu = new CPUInfo(si);
        GPUInfo gpu = new GPUInfo(si);
        RAMInfo ram = new RAMInfo(si);
        DISKInfo disk = new DISKInfo(si);

        // Software, antivirus, rete
        SoftwareInfo software = new SoftwareInfo();
        AntivirusScanner avScanner = new AntivirusScanner();
        NetworkScanner netScanner = new NetworkScanner();

        // Corretto ordine dei parametri: cpu, disk, gpu, ram, software, antivirus, network
        aiAnalyzer = new AIAnalyzer(cpu, disk, gpu, ram, software, avScanner, netScanner);

        // Raccoglie dati iniziali
        aiAnalyzer.collectBaseData();
    }


    // Metodo che invia una domanda all'AI e gestisce le scansioni
    private void processAIQuestion(String question) {
        appendOutput("Utente: " + question);

        new Thread(() -> {
            try {
                // Esegue scansione antivirus se la domanda lo richiede
                if (question.toLowerCase().contains("antivirus")) {
                    appendOutput("Avvio scansione antivirus...");
                    aiAnalyzer.runAntivirusScan("C:\\"); // esempio percorso
                }

                // Esegue diagnostica rete se la domanda lo richiede
                if (question.toLowerCase().contains("network") || question.toLowerCase().contains("porta")) {
                    appendOutput("Avvio diagnostica rete...");
                    aiAnalyzer.runNetworkScan("192.168.1.0/24"); // esempio subnet
                }

                // L'AI elabora la risposta
                String answer = aiAnalyzer.answerQuestion(question);

                Platform.runLater(() -> appendOutput("AI: " + answer));
            } catch (Exception e) {
                Platform.runLater(() -> appendOutput("Errore AI: " + e.getMessage()));
            }
        }).start();
    }

    private void processInput() {
        String userInput = inputField.getText().trim();
        if (!userInput.isEmpty()) {
            int currentIndex = chatHistoryList.getSelectionModel().getSelectedIndex();
            if (currentIndex < 0) return;

            Chat chat = chats.get(currentIndex);
            chat.addMessage("Utente: " + userInput);

            processAIQuestion(userInput); // invia domanda all'AI

            inputField.clear();
        }
    }

    private void appendOutput(String text) { outputArea.appendText(text + "\n"); }

    private void startNewChat() {
        String chatName = "Chat " + chatCounter++;
        Chat newChat = new Chat(chatName);
        chats.add(newChat);
        chatHistoryList.getItems().add(chatName);
        chatHistoryList.getSelectionModel().selectLast();
    }

    private void loadChat(int index) {
        Chat chat = chats.get(index);
        outputArea.setText(chat.getMessages());
    }

    private void toggleTheme() {
        darkMode = !darkMode;
        applyTheme();
    }

    private void applyTheme() {
        if (darkMode) {
            root.setStyle("-fx-background-color: #2e2e2e;");
            outputArea.setStyle("-fx-control-inner-background: #1e1e1e; -fx-text-fill: #ffffff;");
            inputField.setStyle("-fx-control-inner-background: #3e3e3e; -fx-text-fill: #ffffff; -fx-prompt-text-fill: #bbbbbb;");
            chatHistoryList.setStyle("-fx-control-inner-background: #3e3e3e; -fx-text-fill: #ffffff;");
            headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-background-color: #3e3e3e; -fx-padding: 10px;");
            for (Button btn : getAllButtons()) btn.setStyle("-fx-background-color: #555555; -fx-text-fill: #ffffff;");
        } else {
            root.setStyle("-fx-background-color: #ffffff;");
            outputArea.setStyle("-fx-control-inner-background: #f9f9f9; -fx-text-fill: #000000;");
            inputField.setStyle("-fx-control-inner-background: #ffffff; -fx-text-fill: #000000; -fx-prompt-text-fill: #666666;");
            chatHistoryList.setStyle("-fx-control-inner-background: #ffffff; -fx-text-fill: #000000;");
            headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #000000; -fx-background-color: #dddddd; -fx-padding: 10px;");
            for (Button btn : getAllButtons()) btn.setStyle("-fx-background-color: #dddddd; -fx-text-fill: #000000;");
        }
    }

    private List<Button> getAllButtons() {
        List<Button> buttons = new ArrayList<>();
        root.lookupAll(".button").forEach(node -> { if (node instanceof Button b) buttons.add(b); });
        return buttons;
    }

    public static void main(String[] args) { launch(args); }
}

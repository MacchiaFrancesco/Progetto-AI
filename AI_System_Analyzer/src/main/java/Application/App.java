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

public class App extends Application {

    private TextArea outputArea;
    private TextField inputField;
    private ListView<String> chatHistoryList;
    private List<Chat> chats;
    private int chatCounter = 1;
    private boolean darkMode = false; // flag per tema
    private Label headerLabel; // intestazione

    private static class Chat {
        String name;
        StringBuilder messages = new StringBuilder();

        Chat(String name) {
            this.name = name;
        }

        void addMessage(String msg) {
            messages.append(msg).append("\n");
        }

        String getMessages() {
            return messages.toString();
        }
    }

    private BorderPane root;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("AI GUI Demo");

        // Layout principale
        root = new BorderPane();
        root.setPadding(new Insets(10));

        // Intestazione
        headerLabel = new Label("AI GUI Demo");
        headerLabel.setPadding(new Insets(10));
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        StackPane headerPane = new StackPane(headerLabel);
        root.setTop(headerPane);

        // Area di output
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(true);

        // Lista chat
        chats = new ArrayList<>();
        chatHistoryList = new ListView<>();
        chatHistoryList.setPrefWidth(200);
        chatHistoryList.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.intValue() >= 0) {
                loadChat(newVal.intValue());
            }
        });

        // Pulsante nuova chat
        Button newChatButton = new Button("Nuova Chat");
        newChatButton.setOnAction(e -> startNewChat());

        // Pulsante tema
        Button themeButton = new Button("Cambia Tema");
        themeButton.setOnAction(e -> toggleTheme());

        VBox leftPanel = new VBox(10, new Label("Cronologia Chat"), chatHistoryList, newChatButton, themeButton);
        leftPanel.setPadding(new Insets(0, 10, 0, 0));
        leftPanel.setPrefWidth(200);

        // Pannello input
        HBox inputPanel = new HBox(10);
        inputPanel.setPadding(new Insets(10, 0, 0, 0));

        inputField = new TextField();
        inputField.setPromptText("Scrivi qui il tuo comando...");
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
        applyTheme(); // applica tema iniziale
    }

    private void toggleTheme() {
        darkMode = !darkMode;
        applyTheme();
    }

    private void applyTheme() {
        if (darkMode) {
            // Dark Mode
            root.setStyle("-fx-background-color: #2e2e2e;");
            outputArea.setStyle("-fx-control-inner-background: #1e1e1e; -fx-text-fill: #ffffff;");
            inputField.setStyle("-fx-control-inner-background: #3e3e3e; -fx-text-fill: #ffffff; -fx-prompt-text-fill: #bbbbbb;");
            chatHistoryList.setStyle("-fx-control-inner-background: #3e3e3e; -fx-text-fill: #ffffff;");

            headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-background-color: #3e3e3e; -fx-padding: 10px;");

            for (Button btn : getAllButtons()) {
                btn.setStyle("-fx-background-color: #555555; -fx-text-fill: #ffffff;");
            }

        } else {
            // Light Mode
            root.setStyle("-fx-background-color: #ffffff;");
            outputArea.setStyle("-fx-control-inner-background: #f9f9f9; -fx-text-fill: #000000;");
            inputField.setStyle("-fx-control-inner-background: #ffffff; -fx-text-fill: #000000; -fx-prompt-text-fill: #666666;");
            chatHistoryList.setStyle("-fx-control-inner-background: #ffffff; -fx-text-fill: #000000;");

            headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #000000; -fx-background-color: #dddddd; -fx-padding: 10px;");

            for (Button btn : getAllButtons()) {
                btn.setStyle("-fx-background-color: #dddddd; -fx-text-fill: #000000;");
            }
        }
    }


    private List<Button> getAllButtons() {
        List<Button> buttons = new ArrayList<>();
        root.lookupAll(".button").forEach(node -> {
            if (node instanceof Button b) {
                buttons.add(b);
            }
        });
        return buttons;
    }

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

    private void processInput() {
        String userInput = inputField.getText().trim();
        if (!userInput.isEmpty()) {
            int currentIndex = chatHistoryList.getSelectionModel().getSelectedIndex();
            if (currentIndex < 0) return;

            Chat chat = chats.get(currentIndex);

            chat.addMessage("Utente: " + userInput);
            String aiResponse = simulateAIResponse(userInput);
            chat.addMessage("AI: " + aiResponse);

            outputArea.setText(chat.getMessages());
            inputField.clear();
        }
    }

    private String simulateAIResponse(String input) {
        return "Risposta simulata a: \"" + input + "\"";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
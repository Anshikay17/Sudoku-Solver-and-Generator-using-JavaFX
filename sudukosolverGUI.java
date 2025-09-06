import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.*;

public class sudukosolverGUI extends Application {
    private static final int SIZE = 9;
    private TextField[][] cells = new TextField[SIZE][SIZE];
    private ComboBox<String> difficultyBox;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Sudoku Solver and Generator");

        BorderPane root = new BorderPane();

        // Menu bar
        MenuBar menuBar = new MenuBar();
        Menu helpMenu = new Menu("Help");
        MenuItem howToPlay = new MenuItem("How to Play");
        howToPlay.setOnAction(e -> showHowToPlay());
        helpMenu.getItems().add(howToPlay);
        menuBar.getMenus().add(helpMenu);
        root.setTop(menuBar);

        // Title
        Label title = new Label("SUDOKU SOLVER");
        title.setFont(Font.font("Arial", 24));
        title.setTextFill(javafx.scene.paint.Color.web("#0066CC"));
        VBox titleBox = new VBox(title);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(10, 0, 10, 0));
        root.setTop(new VBox(menuBar, titleBox));

        // Grid
        GridPane grid = new GridPane();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                TextField cell = new TextField();
                cell.setAlignment(Pos.CENTER);
                cell.setFont(Font.font("Arial", 18));
                cell.setPrefSize(50, 50);
                cells[r][c] = cell;
                grid.add(cell, c, r);
                // Restrict input to 1 digit (1–9)
                cell.textProperty().addListener((obs, old, val) -> {
                    if (!val.matches("[1-9]?")) {
                        cell.setText("");
                    }
                });

                // Bold borders for 3x3 blocks
                cell.setBorder(new Border(new BorderStroke(
                        Color.BLACK,
                        BorderStrokeStyle.SOLID,
                        CornerRadii.EMPTY,
                        new BorderWidths(
                                (r % 3 == 0) ? 2 : 0.5,
                                ( c == SIZE-1) ? 2 : 0.5, 
                                (r == SIZE - 1) ? 2 : 0.5,
                                ((c % 3 == 0) ) ? 2 : 0.5 
                        )
                )));
            }
        }


        grid.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        grid.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        grid.setPrefSize(450, 450); 

        VBox gridWrapper = new VBox(grid);
        gridWrapper.setAlignment(Pos.CENTER);
        gridWrapper.setPadding(new Insets(20));

        root.setCenter(gridWrapper);


        //Buttons and difficulty
        Button createBtn = new Button("Create Puzzle");
        Button solveBtn = new Button("Solve");
        Button clearBtn = new Button("Clear");

        styleButton(createBtn, "#4682B4");
        styleButton(solveBtn, "#009900");
        styleButton(clearBtn, "#CC0000");

        difficultyBox = new ComboBox<>();
        difficultyBox.getItems().addAll("Easy", "Medium", "Hard");
        difficultyBox.setValue("Easy");

        createBtn.setOnAction(e -> generatePuzzle());
        solveBtn.setOnAction(e -> solveSudoku());
        clearBtn.setOnAction(e -> clearGrid());

        HBox buttonBox = new HBox(10,new Label("Difficulty:"), difficultyBox, createBtn, solveBtn, clearBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));

        root.setBottom(buttonBox);

        // Scene
        Scene scene = new Scene(root, 600, 680);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private void styleButton(Button button, String color) {
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white;");
        button.setFont(Font.font("Arial", 14));
        button.setPrefSize(120, 35);
    }

    private void clearGrid() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                cells[r][c].setText("");
                cells[r][c].setEditable(true);
                cells[r][c].setStyle("-fx-text-fill: black; -fx-background-color: white;");
            }
        }
    }

    private void generatePuzzle() {
        clearGrid();
        int[][] board = new int[SIZE][SIZE];
        generateFullSudoku(board);

        String level = difficultyBox.getValue();
        int blanks = switch (level) {
            case "Easy" -> 30;
            case "Medium" -> 40;
            default -> 50;
        };

        Random rand = new Random();
        while (blanks > 0) {
            int r = rand.nextInt(9);
            int c = rand.nextInt(9);
            if (board[r][c] != 0) {
                board[r][c] = 0;
                blanks--;
            }
        }

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (board[r][c] != 0) {
                    cells[r][c].setText(String.valueOf(board[r][c]));
                    cells[r][c].setEditable(false);
                    cells[r][c].setStyle("-fx-background-color: #DCDCDC;");
                }
            }
        }
    }

    private boolean generateFullSudoku(int[][] board) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (board[r][c] == 0) {
                    List<Integer> nums = new ArrayList<>();
                    for (int i = 1; i <= 9; i++) nums.add(i);
                    Collections.shuffle(nums);

                    for (int num : nums) {
                        if (isSafe(board, r, c, num)) {
                            board[r][c] = num;
                            if (generateFullSudoku(board)) return true;
                            board[r][c] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private void solveSudoku() {
        int[][] board = new int[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                String val = cells[r][c].getText();
                board[r][c] = (val == null || val.isEmpty()) ? 0 : Integer.parseInt(val);
            }
        }
        
        if (!isValidPuzzle(board)) {
        new Alert(Alert.AlertType.ERROR, "Invalid Sudoku puzzle: Conflicting numbers detected!").showAndWait();
        return; 
    }

        if (solve(board)) {
            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    cells[r][c].setText(String.valueOf(board[r][c]));
                    if (cells[r][c].isEditable()) {
                        cells[r][c].setStyle("-fx-text-fill: blue;");
                    }
                }
            }
        } else {
            new Alert(Alert.AlertType.ERROR, "No solution exists!").showAndWait();
        }
    }

    private boolean solve(int[][] board) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (board[r][c] == 0) {
                    for (int num = 1; num <= 9; num++) {
                        if (isSafe(board, r, c, num)) {
                            board[r][c] = num;
                            if (solve(board)) return true;
                            board[r][c] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isSafe(int[][] board, int row, int col, int num) {
        for (int x = 0; x < SIZE; x++) {
            if (board[row][x] == num || board[x][col] == num ||
                    board[row - row % 3 + x / 3][col - col % 3 + x % 3] == num) {
                return false;
            }
        }
        return true;
    }
    private boolean isValidPuzzle(int[][] board) {
    for (int r = 0; r < SIZE; r++) {
        for (int c = 0; c < SIZE; c++) {
            int val = board[r][c];
            if (val != 0) {
                board[r][c] = 0; 
                if (!isSafe(board, r, c, val)) {
                    board[r][c] = val; 
                    return false; 
                }
                board[r][c] = val;
            }
        }
    }
    return true;
}


    private void showHowToPlay() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("How to Play");
        alert.setHeaderText("🧩 HOW TO PLAY SUDOKU");
        alert.setContentText("""
                1. The grid is 9x9, divided into 3x3 blocks.
                2. Fill the empty cells with numbers 1–9.
                3. Rules:
                   - Each row must have digits 1–9 without repetition.
                   - Each column must have digits 1–9 without repetition.
                   - Each 3x3 block must have digits 1–9 without repetition.
                4. Select difficulty and click 'Create Puzzle' to start.
                5. Enter numbers manually OR click 'Solve' to see the solution.
                6. Use 'Clear' to reset the board.

                💡 Tip: Start with rows/columns that already have many numbers filled!
                """);
        alert.showAndWait();
    }
}
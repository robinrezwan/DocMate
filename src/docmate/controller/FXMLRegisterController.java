package docmate.controller;

import docmate.database.Database;
import docmate.util.AlertMessage;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FXMLRegisterController implements Initializable {

    public static final String TAG = "DOCMATE";

    private Stage primaryStage;
    private Scene loginScene;

    @FXML
    private ProgressIndicator registerProgress;

    @FXML
    private TextField doctorNameField;
    @FXML
    private HBox doctorNameError;
    @FXML
    private Text doctorNameErrorText;

    @FXML
    private ComboBox<String> doctorSexComboBox;
    @FXML
    private HBox doctorSexError;
    @FXML
    private Text doctorSexErrorText;

    @FXML
    private TextField doctorEmailField;
    @FXML
    private HBox doctorEmailError;
    @FXML
    private Text doctorEmailErrorText;

    @FXML
    private PasswordField doctorPasswordField;
    @FXML
    private HBox doctorPasswordError;
    @FXML
    private Text doctorPasswordErrorText;

    @FXML
    private PasswordField doctorPasswordConfirmField;
    @FXML
    private HBox doctorPasswordConfirmError;
    @FXML
    private Text doctorPasswordConfirmErrorText;

    @FXML
    private Button registerButton;

    @FXML
    private Hyperlink openLoginLink;

    private String doctorName;
    private String doctorSex;
    private String doctorEmail;
    private String doctorPassword;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setLoginScene(Scene loginScene) {
        this.loginScene = loginScene;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initDoctorSexComboBox();
        registerButton.setOnAction(event -> register());
        openLoginLink.setOnAction(event -> openLogin());
    }

    private void initDoctorSexComboBox() {
        doctorSexComboBox.getItems().addAll("Male", "Female", "Other");
    }

    private void register() {
        if (validateData()) {
            registerToDatabase();
        }
    }

    private void registerToDatabase() {
        registerProgress.setVisible(true);

        new Thread(new Task<Boolean>() {

            @Override
            protected Boolean call() {
                // Making database call
                Connection connection = null;
                PreparedStatement preparedStatement = null;

                try {
                    connection = Database.getInstance().getConnection();

                    if (connection != null) {
                        String query = "INSERT INTO DOCTOR (DoctorName, DoctorSex, DoctorEmail, DoctorPassword) " +
                                "VALUES (? , ?, ?, ?)";

                        preparedStatement = connection.prepareStatement(query);

                        preparedStatement.setString(1, doctorName);
                        preparedStatement.setString(2, doctorSex);
                        preparedStatement.setString(3, doctorEmail);
                        preparedStatement.setString(4, doctorPassword);

                        preparedStatement.executeUpdate();
                    } else {
                        return false;
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                    // TODO: 9/14/2020 Alert for email already used
                    return false;
                } finally {
                    Database.getInstance().close(connection, preparedStatement);
                }

                return true;
            }

            @Override
            protected void updateValue(Boolean isRegistered) {
                super.updateValue(isRegistered);

                if (isRegistered) {
                    doctorNameField.clear();
                    doctorEmailField.clear();
                    doctorPasswordField.clear();

                    registerProgress.setVisible(false);

                    AlertMessage alertMessage = new AlertMessage("Registration successful!");
                    alertMessage.showAndWait();

                    Logger.getLogger(TAG).log(Level.INFO, "Registered!");
                } else {
                    registerProgress.setVisible(false);

                    AlertMessage alertMessage = new AlertMessage("Registration failed!");
                    alertMessage.showAndWait();
                }
            }
        }).start();
    }

    private boolean validateData() {
        doctorName = doctorNameField.getText().trim();
        doctorSex = doctorSexComboBox.getValue();
        doctorEmail = doctorEmailField.getText().trim().toLowerCase();
        doctorPassword = doctorPasswordField.getText().trim();
        String doctorPasswordConfirm = doctorPasswordConfirmField.getText().trim();

        boolean isDataValid = true;

        clearError(doctorNameError, doctorNameErrorText);
        clearError(doctorEmailError, doctorEmailErrorText);
        clearError(doctorPasswordError, doctorPasswordErrorText);

        // Name validation
        if (doctorName.isEmpty()) {
            setError(doctorNameError, doctorNameErrorText, "Name can not be empty");
            isDataValid = false;
        } else if (!doctorName.matches("^[a-zA-Z .]*$")) {
            setError(doctorNameError, doctorNameErrorText,
                    "Name can only contain letters, dots and spaces");
            isDataValid = false;
        } else if (doctorName.length() < 3) {
            setError(doctorNameError, doctorNameErrorText, "Name requires at least 3 characters");
            isDataValid = false;
        }

        // Sex validation
        if (doctorSex == null) {
            setError(doctorSexError, doctorSexErrorText, "Sex can not be empty");
            isDataValid = false;
        }

        // Email validation
        if (doctorEmail.isEmpty()) {
            setError(doctorEmailError, doctorEmailErrorText, "Email can not be empty");
            isDataValid = false;
        } else if (!doctorEmail.matches("[a-zA-Z0-9+._%\\-]{1,256}" + "@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\." + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+")) {
            setError(doctorEmailError, doctorEmailErrorText, "Invalid email format");
            isDataValid = false;
        }

        // Password validation
        if (doctorPassword.isEmpty()) {
            setError(doctorPasswordError, doctorPasswordErrorText, "Password can not be empty");
            isDataValid = false;
        } else if (doctorPassword.contains(" ")) {
            setError(doctorPasswordError, doctorPasswordErrorText,
                    "Password should not contain spaces");
            isDataValid = false;
        } else if (doctorPassword.length() < 4) {
            setError(doctorPasswordError, doctorPasswordErrorText,
                    "Password requires at least 4 characters");
            isDataValid = false;
        } else if (doctorPassword.length() > 30) {
            setError(doctorPasswordError, doctorPasswordErrorText,
                    "Password should not exceed 30 characters");
            isDataValid = false;
        }

        // Password confirm validation
        if (!doctorPassword.equals(doctorPasswordConfirm)) {
            setError(doctorPasswordConfirmError, doctorPasswordConfirmErrorText,
                    "Password did not match");
            isDataValid = false;
        }

        return isDataValid;
    }

    private void clearError(HBox errorBox, Text errorText) {
        errorBox.setVisible(false);
        errorText.setText("");
    }

    private void setError(HBox errorBox, Text errorText, String errorMessage) {
        errorBox.setVisible(true);
        errorText.setText(errorMessage);
    }

    /**
     * Switches to login scene
     */
    private void openLogin() {
        primaryStage.setScene(loginScene);
    }
}

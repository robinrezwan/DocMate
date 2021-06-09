package docmate.controller;

import docmate.database.Database;
import docmate.model.Doctor;
import docmate.model.Patient;
import docmate.model.PatientMedicine;
import docmate.util.AlertMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class FXMLPrescriptionAddController implements Initializable {

    public static final String TAG = "DOCMATE";

    private Stage stage;
    private Patient patient;

    @FXML
    private ComboBox<String> patientUserIdComboBox;
    @FXML
    private Button patientSetButton;

    @FXML
    private HBox patientInfoPane;
    @FXML
    private ImageView patientImageView;
    @FXML
    private Text patientNameText;
    @FXML
    private Text patientIdText;
    @FXML
    private Text patientAgeText;
    @FXML
    private Text patientSexText;

    @FXML
    private TextArea symptomsTextArea;
    @FXML
    private TextArea observationTextArea;
    @FXML
    private TextArea testsTextArea;
    @FXML
    private TextArea diagnosisTextArea;
    @FXML
    private TextArea adviceTextArea;
    @FXML
    private TextField visitAgainTextField;

    @FXML
    private ComboBox<String> medicineDosageFormComboBox;
    @FXML
    private ComboBox<String> medicineBrandNameComboBox;
    @FXML
    private ComboBox<String> medicineStrengthComboBox;
    @FXML
    private TextField medicineDosageTextField;
    @FXML
    private TextField medicineDurationTextField;
    @FXML
    private TextField medicineAdviceTextField;

    @FXML
    private Button medicineClearButton;
    @FXML
    private Button medicineAddButton;

    @FXML
    private TableView<PatientMedicine> patientMedicineTableView;
    @FXML
    private TableColumn<PatientMedicine, ?> medicineNumberColumn;
    @FXML
    private TableColumn<PatientMedicine, ?> medicineDosageFormColumn;
    @FXML
    private TableColumn<PatientMedicine, ?> medicineBrandNameColumn;
    @FXML
    private TableColumn<PatientMedicine, ?> medicineStrengthColumn;
    @FXML
    private TableColumn<PatientMedicine, ?> medicineDosageColumn;
    @FXML
    private TableColumn<PatientMedicine, ?> medicineDurationColumn;
    @FXML
    private TableColumn<PatientMedicine, ?> medicineAdviceColumn;

    @FXML
    private Button prescriptionCancelButton;
    @FXML
    private Button prescriptionSaveButton;

    private int suggestionsObservableListLength;

    private ObservableList<PatientMedicine> patientMedicineObservableList;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
        setPatientData();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        patientMedicineObservableList = FXCollections.observableArrayList();

        patientSetButton.setOnAction(event -> getPatientFromDatabase());
        medicineClearButton.setOnAction(event -> clearMedicine());
        medicineAddButton.setOnAction(event -> addMedicine());
        prescriptionCancelButton.setOnAction(event -> cancelPrescription());
        prescriptionSaveButton.setOnAction(event -> savePrescription());

        initComboBox();
        initPatientMedicineTable();
    }

    private void getPatientFromDatabase() {

        new Thread(new Task<Patient>() {
            @Override
            protected Patient call() {
                Patient patient = null;

                // Making database call
                Connection connection = null;
                PreparedStatement preparedStatement = null;
                ResultSet resultSet;

                try {
                    connection = Database.getInstance().getConnection();

                    if (connection != null) {
                        String query = "SELECT * FROM PATIENT WHERE DoctorId = ? AND PatientUserId = ?";

                        preparedStatement = connection.prepareStatement(query);
                        preparedStatement.setInt(1, Doctor.getInstance().getId());
                        preparedStatement.setInt(2, Integer.parseInt(patientUserIdComboBox.getEditor()
                                .getText()));

                        resultSet = preparedStatement.executeQuery();

                        if (resultSet.next()) {
                            patient = new Patient();

                            patient.setId(resultSet.getInt("PatientId"));
                            patient.setUserId(resultSet.getInt("PatientUserId"));
                            patient.setName(resultSet.getString("PatientName"));
                            patient.setDateOfBirth(resultSet.getDate("PatientDOB").toLocalDate());
                            patient.setSex(resultSet.getString("PatientSex"));
                            patient.setPhone(resultSet.getString("PatientPhone"));
                            patient.setEmail(resultSet.getString("PatientEmail"));
                            patient.setAddress(resultSet.getString("PatientAddress"));
                            patient.setComments(resultSet.getString("PatientComments"));
                            patient.setRegistrationDate(resultSet.getDate("PatientRegistrationDate")
                                    .toLocalDate());
                        }
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                } finally {
                    Database.getInstance().close(connection, preparedStatement);
                }

                return patient;
            }

            @Override
            protected void updateValue(Patient patient) {
                super.updateValue(patient);

                if (patient != null) {
                    setPatient(patient);
                } else {
                    AlertMessage alertMessage = new AlertMessage("Patient not found!");
                    alertMessage.showAndWait();
                }
            }
        }).start();
    }

    private void setPatientData() {
        String patientId = String.valueOf(patient.getUserId());
        patientUserIdComboBox.setValue(patientId);
        if (!patientUserIdComboBox.getItems().contains(patientId)) {
            patientUserIdComboBox.getItems().add(patientId);
        }
        Platform.runLater(() -> patientUserIdComboBox.getEditor().positionCaret(patientId.length()));

        patientNameText.setText(patient.getName());
        patientIdText.setText("ID: " + patientId);
        patientAgeText.setText("Age: " + patient.getAge());
        patientSexText.setText("Sex: " + patient.getSex());

        patientInfoPane.setVisible(true);
    }

    private void initComboBox() {
        // Setting key event listener
        patientUserIdComboBox.setOnKeyReleased(event -> {
            if (isKeyValid(event)) {
                searchPatient();
            }
        });
        medicineDosageFormComboBox.setOnKeyReleased(event -> {
            if (isKeyValid(event)) {
                searchMedicineDosageForm();
            }
        });
        medicineBrandNameComboBox.setOnKeyReleased(event -> {
            if (isKeyValid(event)) {
                searchMedicineBrandName();
            }
        });
        medicineStrengthComboBox.setOnKeyReleased(event -> {
            if (isKeyValid(event)) {
                searchMedicineStrength();
            }
        });

        // Setting value change listener
        patientUserIdComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Platform.runLater(() -> patientUserIdComboBox.getEditor()
                        .positionCaret(patientUserIdComboBox.getEditor().getText().length()));
            }
        });
        medicineDosageFormComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Platform.runLater(() -> medicineDosageFormComboBox.getEditor()
                        .positionCaret(medicineDosageFormComboBox.getEditor().getText().length()));
            }
        });
        medicineBrandNameComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Platform.runLater(() -> medicineBrandNameComboBox.getEditor()
                        .positionCaret(medicineBrandNameComboBox.getEditor().getText().length()));
            }
        });
        medicineStrengthComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Platform.runLater(() -> medicineStrengthComboBox.getEditor()
                        .positionCaret(medicineStrengthComboBox.getEditor().getText().length()));
            }
        });

        // Setting focus listener
        patientUserIdComboBox.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                suggestionsObservableListLength = 0;
                Platform.runLater(() -> patientUserIdComboBox.getEditor()
                        .positionCaret(patientUserIdComboBox.getEditor().getText().length()));
            }
        });
        medicineDosageFormComboBox.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                suggestionsObservableListLength = 0;
                Platform.runLater(() -> medicineDosageFormComboBox.getEditor()
                        .positionCaret(medicineDosageFormComboBox.getEditor().getText().length()));
            }
        });
        medicineBrandNameComboBox.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                suggestionsObservableListLength = 0;
                Platform.runLater(() -> medicineBrandNameComboBox.getEditor()
                        .positionCaret(medicineBrandNameComboBox.getEditor().getText().length()));
            }
        });
        medicineStrengthComboBox.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                suggestionsObservableListLength = 0;
                Platform.runLater(() -> medicineStrengthComboBox.getEditor()
                        .positionCaret(medicineStrengthComboBox.getEditor().getText().length()));
            }
        });
    }

    private void initPatientMedicineTable() {
        medicineNumberColumn.setCellValueFactory(new PropertyValueFactory<>("medicineNumber"));
        medicineDosageFormColumn.setCellValueFactory(new PropertyValueFactory<>("dosageForm"));
        medicineBrandNameColumn.setCellValueFactory(new PropertyValueFactory<>("brandName"));
        medicineStrengthColumn.setCellValueFactory(new PropertyValueFactory<>("strength"));
        medicineDosageColumn.setCellValueFactory(new PropertyValueFactory<>("dosage"));
        medicineDurationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        medicineAdviceColumn.setCellValueFactory(new PropertyValueFactory<>("advice"));

        // Initializing context menu
        ContextMenu medicineContextMenu = new ContextMenu();
        medicineContextMenu.setOnHiding(event -> medicineContextMenu.getItems().clear());

        patientMedicineTableView.setRowFactory(tableView -> {
            TableRow<PatientMedicine> tableRow = new TableRow<>();
            tableRow.setOnMouseClicked(event -> {
                if (!tableRow.isEmpty() && event.getButton() == MouseButton.SECONDARY &&
                        event.getClickCount() == 1) {
                    PatientMedicine patientMedicine = tableRow.getItem();

                    // Setting context menu items
                    if (!medicineContextMenu.isShowing()) {
                        MenuItem editMenuItem = new MenuItem("Edit");
                        MenuItem removeMenuItem = new MenuItem("Remove");

                        medicineContextMenu.getItems().addAll(editMenuItem, removeMenuItem);

                        editMenuItem.setOnAction(menuEvent -> {
                            medicineContextMenu.hide();
                        });

                        removeMenuItem.setOnAction(menuEvent -> {
                            patientMedicineObservableList.remove(patientMedicine);

                            for (int index = 0; index < patientMedicineObservableList.size(); index++) {
                                patientMedicineObservableList.get(index).setMedicineNumber(index + 1);
                            }

                            medicineContextMenu.hide();
                        });

                    }

                    medicineContextMenu.show(tableRow, Side.BOTTOM, event.getX(), 0);
                }
            });

            return tableRow;
        });
    }

    private boolean isKeyValid(KeyEvent event) {
        return !event.isControlDown() && !event.isAltDown() && event.getCode() != KeyCode.ENTER &&
                event.getCode().isLetterKey() || event.getCode().isDigitKey() || event.getCode().isKeypadKey() ||
                event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.BACK_SPACE ||
                event.getCode() == KeyCode.DELETE;
    }

    private void searchPatient() {
        String searchKey = patientUserIdComboBox.getEditor().getText();

        if (searchKey.isEmpty()) {
            updateComboBox(patientUserIdComboBox, FXCollections.observableArrayList());
            return;
        }

        new Thread(new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() {
                ObservableList<String> patientIdObservableList = FXCollections.observableArrayList();

                // Making database call
                Connection connection = null;
                PreparedStatement preparedStatement = null;
                ResultSet resultSet;

                try {
                    connection = Database.getInstance().getConnection();

                    if (connection != null) {
                        String query = "SELECT PatientUserId FROM PATIENT WHERE DoctorId = ? AND PatientUserId LIKE ?";

                        preparedStatement = connection.prepareStatement(query);
                        preparedStatement.setInt(1, Doctor.getInstance().getId());
                        preparedStatement.setString(2, searchKey + "%");

                        resultSet = preparedStatement.executeQuery();

                        while (resultSet.next()) {
                            patientIdObservableList.add(String.valueOf(resultSet.getInt("PatientUserId")));
                        }
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                } finally {
                    Database.getInstance().close(connection, preparedStatement);
                }

                return patientIdObservableList;
            }

            @Override
            protected void updateValue(ObservableList<String> patientIdObservableList) {
                super.updateValue(patientIdObservableList);

                if (patientIdObservableList != null) {
                    Platform.runLater(() -> updateComboBox(patientUserIdComboBox, patientIdObservableList));
                }
            }
        }).start();
    }

    private void searchMedicineDosageForm() {
        String searchKeyDosageForm = medicineDosageFormComboBox.getEditor().getText();

        if (searchKeyDosageForm.isEmpty()) {
            updateComboBox(medicineDosageFormComboBox, FXCollections.observableArrayList());
            return;
        }

        new Thread(new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() {
                ObservableList<String> medicineDosageFormObservableList = FXCollections.observableArrayList();

                // Making database call
                Connection connection = null;
                PreparedStatement preparedStatement = null;
                ResultSet resultSet;

                try {
                    connection = Database.getInstance().getConnection();

                    if (connection != null) {
                        String query = "SELECT DISTINCT DosageForm FROM MEDICINE WHERE DosageForm LIKE ?";

                        preparedStatement = connection.prepareStatement(query);
                        preparedStatement.setString(1, searchKeyDosageForm + "%");

                        resultSet = preparedStatement.executeQuery();

                        while (resultSet.next()) {
                            medicineDosageFormObservableList.add(resultSet.getString("DosageForm"));
                        }
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                } finally {
                    Database.getInstance().close(connection, preparedStatement);
                }

                return medicineDosageFormObservableList;
            }

            @Override
            protected void updateValue(ObservableList<String> medicineDosageFormObservableList) {
                super.updateValue(medicineDosageFormObservableList);

                if (medicineDosageFormObservableList != null) {
                    Platform.runLater(() -> updateComboBox(medicineDosageFormComboBox,
                            medicineDosageFormObservableList));
                }
            }
        }).start();
    }

    private void searchMedicineBrandName() {
        String searchKeyDosageFrom = medicineDosageFormComboBox.getEditor().getText();
        String searchKeyBrandName = medicineBrandNameComboBox.getEditor().getText();

        if (searchKeyDosageFrom.isEmpty() || searchKeyBrandName.isEmpty()) {
            updateComboBox(medicineBrandNameComboBox, FXCollections.observableArrayList());
            return;
        }

        new Thread(new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() {
                ObservableList<String> medicineBrandNameObservableList = FXCollections.observableArrayList();

                // Making database call
                Connection connection = null;
                PreparedStatement preparedStatement = null;
                ResultSet resultSet;

                try {
                    connection = Database.getInstance().getConnection();

                    if (connection != null) {
                        String query = "SELECT DISTINCT M.BrandName FROM (SELECT BrandName FROM MEDICINE " +
                                "WHERE DosageForm = ? AND BrandName LIKE ?) AS M";

                        preparedStatement = connection.prepareStatement(query);
                        preparedStatement.setString(1, searchKeyDosageFrom);
                        preparedStatement.setString(2, searchKeyBrandName + "%");

                        resultSet = preparedStatement.executeQuery();

                        while (resultSet.next()) {
                            medicineBrandNameObservableList.add(resultSet.getString("BrandName"));
                        }
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                } finally {
                    Database.getInstance().close(connection, preparedStatement);
                }

                return medicineBrandNameObservableList;
            }

            @Override
            protected void updateValue(ObservableList<String> medicineBrandNameObservableList) {
                super.updateValue(medicineBrandNameObservableList);

                if (medicineBrandNameObservableList != null) {
                    updateComboBox(medicineBrandNameComboBox, medicineBrandNameObservableList);
                }
            }
        }).start();
    }

    private void searchMedicineStrength() {
        String searchKeyDosageForm = medicineDosageFormComboBox.getEditor().getText();
        String searchKeyBrandName = medicineBrandNameComboBox.getEditor().getText();
        String searchKeyStrength = medicineStrengthComboBox.getEditor().getText();

        if (searchKeyDosageForm.isEmpty() || searchKeyBrandName.isEmpty() || searchKeyStrength.isEmpty()) {
            updateComboBox(medicineStrengthComboBox, FXCollections.observableArrayList());
            return;
        }

        new Thread(new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() {
                ObservableList<String> medicineStrengthObservableList = FXCollections.observableArrayList();

                // Making database call
                Connection connection = null;
                PreparedStatement preparedStatement = null;
                ResultSet resultSet;

                try {
                    connection = Database.getInstance().getConnection();

                    if (connection != null) {
                        String query = "SELECT Strength FROM MEDICINE WHERE DosageForm = ? AND BrandName = ? " +
                                "AND Strength LIKE ?";

                        preparedStatement = connection.prepareStatement(query);
                        preparedStatement.setString(1, searchKeyDosageForm);
                        preparedStatement.setString(2, searchKeyBrandName);
                        preparedStatement.setString(3, searchKeyStrength + "%");

                        resultSet = preparedStatement.executeQuery();

                        while (resultSet.next()) {
                            medicineStrengthObservableList.add(resultSet.getString("Strength"));
                        }
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                } finally {
                    Database.getInstance().close(connection, preparedStatement);
                }

                return medicineStrengthObservableList;
            }

            @Override
            protected void updateValue(ObservableList<String> medicineStrengthObservableList) {
                super.updateValue(medicineStrengthObservableList);

                if (medicineStrengthObservableList != null) {
                    updateComboBox(medicineStrengthComboBox, medicineStrengthObservableList);
                }
            }
        }).start();
    }

    private void updateComboBox(ComboBox<String> comboBox, ObservableList<String> observableList) {
        String editorText = comboBox.getEditor().getText();
        int caret = comboBox.getEditor().getCaretPosition();

        comboBox.getItems().clear();

        comboBox.getEditor().setText(editorText);
        Platform.runLater(() -> comboBox.getEditor().positionCaret(caret));

        if (!observableList.isEmpty()) {
            if (observableList.size() != suggestionsObservableListLength) {
                comboBox.hide();
                comboBox.show();
                comboBox.hide();
            }

            comboBox.getItems().addAll(observableList);
            comboBox.show();
        } else {
            comboBox.hide();
            comboBox.getItems().addAll(observableList);
        }

        suggestionsObservableListLength = observableList.size();
    }

    private void clearMedicine() {
        medicineDosageFormComboBox.setValue(null);
        medicineDosageFormComboBox.getItems().clear();
        medicineBrandNameComboBox.setValue(null);
        medicineBrandNameComboBox.getItems().clear();
        medicineStrengthComboBox.setValue(null);
        medicineStrengthComboBox.getItems().clear();
        medicineDosageTextField.clear();
        medicineDurationTextField.clear();
        medicineAdviceTextField.clear();
    }

    private void addMedicine() {
        String medicineDosageForm = medicineDosageFormComboBox.getEditor().getText().trim();
        String medicineBrandName = medicineBrandNameComboBox.getEditor().getText().trim();
        String medicineStrength = medicineStrengthComboBox.getEditor().getText().trim();
        String medicineDosage = medicineDosageTextField.getText().trim();
        String medicineDuration = medicineDurationTextField.getText().trim();
        String medicineAdvice = medicineAdviceTextField.getText().trim();

        new Thread(new Task<Integer>() {

            @Override
            protected Integer call() {
                Integer medicineId = null;

                // Making database call
                Connection connection = null;
                PreparedStatement preparedStatement = null;
                ResultSet resultSet;

                try {
                    connection = Database.getInstance().getConnection();

                    if (connection != null) {
                        String query = "SELECT MedicineId FROM MEDICINE WHERE DosageForm = ? AND BrandName = ? " +
                                "AND Strength = ?";

                        preparedStatement = connection.prepareStatement(query);
                        preparedStatement.setString(1, medicineDosageForm);
                        preparedStatement.setString(2, medicineBrandName);
                        preparedStatement.setString(3, medicineStrength);

                        resultSet = preparedStatement.executeQuery();

                        if (resultSet.next()) {
                            medicineId = resultSet.getInt("MedicineId");
                        }
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                } finally {
                    Database.getInstance().close(connection, preparedStatement);
                }

                return medicineId;
            }

            @Override
            protected void updateValue(Integer medicineId) {
                super.updateValue(medicineId);

                if (medicineId != null) {
                    PatientMedicine patientMedicine = new PatientMedicine();

                    patientMedicine.setMedicineNumber(patientMedicineObservableList.size() + 1);
                    patientMedicine.setMedicineId(medicineId);
                    patientMedicine.setDosageForm(medicineDosageForm);
                    patientMedicine.setBrandName(medicineBrandName);
                    patientMedicine.setStrength(medicineStrength);
                    patientMedicine.setDosage(medicineDosage);
                    patientMedicine.setDuration(medicineDuration);
                    patientMedicine.setAdvice(medicineAdvice);

                    patientMedicineObservableList.add(patientMedicine);

                    setPatientMedicineTable();
                } else {
                    AlertMessage alertMessage = new AlertMessage("Medicine was not found in database!");
                    alertMessage.showAndWait();
                }
            }
        }).start();
    }

    private void setPatientMedicineTable() {
        patientMedicineTableView.setItems(patientMedicineObservableList);
        clearMedicine();
    }

    private void cancelPrescription() {
        stage.close();
    }

    private void savePrescription() {
        if (patient != null) {
            savePrescriptionToDatabase();
        } else {
            AlertMessage alertMessage = new AlertMessage("No patient is set!");
            alertMessage.showAndWait();
        }
    }

    private void savePrescriptionToDatabase() {
        String symptoms = symptomsTextArea.getText().trim();
        String observation = observationTextArea.getText().trim();
        String tests = testsTextArea.getText().trim();
        String diagnosis = diagnosisTextArea.getText().trim();
        String advice = adviceTextArea.getText().trim();
        String visitAgain = visitAgainTextField.getText().trim();

        new Thread(new Task<Integer>() {

            @Override
            protected Integer call() {
                Integer prescriptionId = null;

                // Making database call
                Connection connection = null;
                PreparedStatement preparedStatement = null;
                ResultSet resultSet;

                try {
                    connection = Database.getInstance().getConnection();

                    if (connection != null) {
                        String query = "INSERT INTO PRESCRIPTION (DoctorId, PatientId, PrescriptionDate, Symptoms, " +
                                "Observation, PrescribedTests, Diagnosis, Advice, VisitAgain)" +
                                "OUTPUT INSERTED.PrescriptionId VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                        preparedStatement = connection.prepareStatement(query);
                        preparedStatement.setInt(1, Doctor.getInstance().getId());
                        preparedStatement.setInt(2, patient.getId());
                        preparedStatement.setDate(3, Date.valueOf(LocalDate.now()));

                        if (!symptoms.isEmpty()) {
                            preparedStatement.setString(4, symptoms);
                        } else {
                            preparedStatement.setNull(4, Types.VARCHAR);
                        }
                        if (!observation.isEmpty()) {
                            preparedStatement.setString(5, observation);
                        } else {
                            preparedStatement.setNull(5, Types.VARCHAR);
                        }
                        if (!tests.isEmpty()) {
                            preparedStatement.setString(6, tests);
                        } else {
                            preparedStatement.setNull(6, Types.VARCHAR);
                        }
                        if (!diagnosis.isEmpty()) {
                            preparedStatement.setString(7, diagnosis);
                        } else {
                            preparedStatement.setNull(7, Types.VARCHAR);
                        }
                        if (!advice.isEmpty()) {
                            preparedStatement.setString(8, advice);
                        } else {
                            preparedStatement.setNull(8, Types.VARCHAR);
                        }
                        if (!visitAgain.isEmpty()) {
                            preparedStatement.setString(9, visitAgain);
                        } else {
                            preparedStatement.setNull(9, Types.VARCHAR);
                        }

                        resultSet = preparedStatement.executeQuery();

                        if (resultSet.next()) {
                            prescriptionId = resultSet.getInt("PrescriptionId");
                        }

                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                } finally {
                    Database.getInstance().close(connection, preparedStatement);
                }

                return prescriptionId;
            }

            @Override
            protected void updateValue(Integer prescriptionId) {
                super.updateValue(prescriptionId);

                if (prescriptionId != null) {
                    addPrescriptionIdToPatientMedicines(prescriptionId);
                } else {
                    AlertMessage alertMessage = new AlertMessage("Saving prescription failed!");
                    alertMessage.showAndWait();
                }
            }
        }).start();
    }

    private void addPrescriptionIdToPatientMedicines(Integer prescriptionId) {
        for (PatientMedicine patientMedicine : patientMedicineObservableList) {
            patientMedicine.setPrescriptionId(prescriptionId);
        }

        savePatientMedicines();
    }

    private void savePatientMedicines() {
        new Thread(new Task<Boolean>() {

            @Override
            protected Boolean call() {
                // Making database call
                Connection connection = null;
                PreparedStatement preparedStatement = null;

                try {
                    connection = Database.getInstance().getConnection();

                    if (connection != null) {
                        String query = "INSERT INTO PATIENT_MEDICINE (PrescriptionId, MedicineNumber, MedicineId, " +
                                "Dosage, Duration, Advice) VALUES (?, ?, ?, ?, ?, ?)";

                        preparedStatement = connection.prepareStatement(query);

                        for (PatientMedicine patientMedicine : patientMedicineObservableList) {
                            preparedStatement.setInt(1, patientMedicine.getPrescriptionId());
                            preparedStatement.setInt(2, patientMedicine.getMedicineNumber());
                            preparedStatement.setInt(3, patientMedicine.getMedicineId());

                            if (!patientMedicine.getDosage().isEmpty()) {
                                preparedStatement.setString(4, patientMedicine.getDosage());
                            } else {
                                preparedStatement.setNull(4, Types.VARCHAR);
                            }
                            if (!patientMedicine.getDuration().isEmpty()) {
                                preparedStatement.setString(5, patientMedicine.getDuration());
                            } else {
                                preparedStatement.setNull(5, Types.VARCHAR);
                            }
                            if (!patientMedicine.getAdvice().isEmpty()) {
                                preparedStatement.setString(6, patientMedicine.getAdvice());
                            } else {
                                preparedStatement.setNull(6, Types.VARCHAR);
                            }

                            preparedStatement.addBatch();
                        }

                        preparedStatement.executeBatch();
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                    return false;
                } finally {
                    Database.getInstance().close(connection, preparedStatement);
                }

                return true;
            }

            @Override
            protected void updateValue(Boolean isSaved) {
                super.updateValue(isSaved);

                AlertMessage alertMessage;

                if (isSaved) {
                    alertMessage = new AlertMessage("Prescription is saved!");
                } else {
                    alertMessage = new AlertMessage("Saving prescription failed!");
                }

                alertMessage.showAndWait();
            }
        }).start();
    }
}

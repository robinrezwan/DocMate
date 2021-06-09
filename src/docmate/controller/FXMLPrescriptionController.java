package docmate.controller;

import docmate.database.Database;
import docmate.model.Patient;
import docmate.model.Prescription;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class FXMLPrescriptionController implements Initializable {

    public static final String TAG = "DOCMATE";

    @FXML
    private TextField searchPrescriptionTextField;
    @FXML
    private Button searchPrescriptionButton;
    @FXML
    private Button addPrescriptionButton;

    @FXML
    private TableView<Prescription> prescriptionTable;
    @FXML
    private TableColumn<Prescription, ?> prescriptionIdColumn;
    @FXML
    private TableColumn<Prescription, ?> dateColumn;
    @FXML
    private TableColumn<Prescription, ?> patientIDColumn;
    @FXML
    private TableColumn<Prescription, ?> patientNameColumn;
    @FXML
    private TableColumn<Prescription, ?> patientAgeColumn;
    @FXML
    private TableColumn<Prescription, ?> patientSexColumn;
    @FXML
    private TableColumn<Prescription, ?> symptomsColumn;
    @FXML
    private TableColumn<Prescription, ?> diagnosisColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addPrescriptionButton.setOnAction(event -> openPrescriptionAdd());
        searchPrescriptionButton.setOnAction(event -> searchPrescription());
        searchPrescriptionTextField.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                searchPrescription();
            }
        });

        getPrescriptions();
    }

    private void getPrescriptions() {

        new Thread(new Task<ObservableList<Prescription>>() {

            @Override
            protected ObservableList<Prescription> call() {
                ObservableList<Prescription> prescriptionObservableList = FXCollections.observableArrayList();
                Prescription prescription;
                Patient patient;

                // Making database call
                Connection connection = null;
                PreparedStatement preparedStatement = null;
                ResultSet resultSet = null;

                try {
                    connection = Database.getInstance().getConnection();

                    if (connection != null) {
                        String query = "SELECT " +
                                "PR.PrescriptionId, PR.PrescriptionDate, PT.PatientId, PT.PatientUserId, " +
                                "PT.PatientName, PT.PatientDOB, PT.PatientSex, " +
                                "ISNULL(PR.Symptoms, 'None') AS Symptoms, " +
                                "ISNULL(PR.Observation, 'None') AS Observation, " +
                                "ISNULL(PR.Diagnosis, 'None') AS Diagnosis, " +
                                "ISNULL(PR.Advice, 'None') AS Advice, " +
                                "ISNULL(PR.VisitAgain, 'None') AS VisitAgain, " +
                                "ISNULL(PR.PrescribedTests,'None') AS PrescribedTest " +
                                "FROM PRESCRIPTION PR LEFT JOIN PATIENT PT ON PT.PatientId = PR.PatientId";

                        preparedStatement = connection.prepareStatement(query);
                        resultSet = preparedStatement.executeQuery();

                        // Creating Prescription object with retrieved data
                        while (resultSet.next()) {
                            prescription = new Prescription();
                            patient = new Patient();

                            prescription.setPrescriptionId(resultSet.getInt("PrescriptionId"));
                            prescription.setDate(resultSet.getDate("PrescriptionDate").toLocalDate());
                            prescription.setPatientId(resultSet.getInt("PatientId"));
                            prescription.setPatientUserId(resultSet.getInt("PatientUserId"));
                            prescription.setPatientName(resultSet.getString("PatientName"));
                            patient.setDateOfBirth(resultSet.getDate("PatientDOB").toLocalDate());
                            prescription.setPatientAge(patient.getAge());
                            prescription.setPatientSex(resultSet.getString("PatientSex"));
                            prescription.setSymptoms(resultSet.getString("Symptoms"));
                            prescription.setObservation(resultSet.getString("Observation"));
                            prescription.setTests(resultSet.getString("PrescribedTest"));
                            prescription.setDiagnosis(resultSet.getString("Diagnosis"));
                            prescription.setAdvice(resultSet.getString("Advice"));
                            prescription.setVisitAgain(resultSet.getString("VisitAgain"));

                            prescriptionObservableList.add(prescription);
                        }
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                } finally {
                    Database.getInstance().close(connection, preparedStatement, resultSet);
                }

                return prescriptionObservableList;
            }

            @Override
            protected void updateValue(ObservableList<Prescription> prescriptionObservableList) {
                super.updateValue(prescriptionObservableList);

                if (prescriptionObservableList != null) {
                    setPrescriptionTable(prescriptionObservableList);
                }
            }
        }).start();
    }

    private void searchPrescription() {
        String searchKey = searchPrescriptionTextField.getText().trim().toLowerCase();

        if (searchKey.isEmpty()) {
            getPrescriptions();
            return;
        }

        new Thread(new Task<ObservableList<Prescription>>() {

            @Override
            protected ObservableList<Prescription> call() {
                ObservableList<Prescription> prescriptionObservableList = FXCollections.observableArrayList();
                Prescription prescription;
                Patient patient;

                // Making database call
                Connection connection = null;
                PreparedStatement preparedStatement = null;
                ResultSet resultSet = null;

                try {
                    connection = Database.getInstance().getConnection();

                    if (connection != null) {
                        String query = "SELECT " +
                                "PR.PrescriptionId, PR.PrescriptionDate, PT.PatientId, PT.PatientUserId, " +
                                "PT.PatientName, PT.PatientDOB, PT.PatientSex, " +
                                "ISNULL(PR.Symptoms, 'None') AS Symptoms, " +
                                "ISNULL(PR.Observation, 'None') AS Observation, " +
                                "ISNULL(PR.Diagnosis, 'None') AS Diagnosis, " +
                                "ISNULL(PR.Advice, 'None') AS Advice, " +
                                "ISNULL(PR.VisitAgain, 'None') AS VisitAgain, " +
                                "ISNULL(PR.PrescribedTests,'None') AS PrescribedTest " +
                                "FROM PRESCRIPTION PR LEFT JOIN PATIENT PT ON PT.PatientId = PR.PatientId " +
                                "WHERE PT.PatientUserId LIKE ? OR PT.PatientName LIKE ?";

                        preparedStatement = connection.prepareStatement(query);
                        preparedStatement.setString(1, searchKey + "%");
                        preparedStatement.setString(2, searchKey + "%");

                        resultSet = preparedStatement.executeQuery();

                        // Creating Prescription object with retrieved data
                        while (resultSet.next()) {
                            prescription = new Prescription();
                            patient = new Patient();

                            prescription.setPrescriptionId(resultSet.getInt("PrescriptionId"));
                            prescription.setDate(resultSet.getDate("PrescriptionDate").toLocalDate());
                            prescription.setPatientId(resultSet.getInt("PatientId"));
                            prescription.setPatientUserId(resultSet.getInt("PatientUserId"));
                            prescription.setPatientName(resultSet.getString("PatientName"));
                            patient.setDateOfBirth(resultSet.getDate("PatientDOB").toLocalDate());
                            prescription.setPatientAge(patient.getAge());
                            prescription.setPatientSex(resultSet.getString("PatientSex"));
                            prescription.setSymptoms(resultSet.getString("Symptoms"));
                            prescription.setObservation(resultSet.getString("Observation"));
                            prescription.setTests(resultSet.getString("PrescribedTest"));
                            prescription.setDiagnosis(resultSet.getString("Diagnosis"));
                            prescription.setAdvice(resultSet.getString("Advice"));
                            prescription.setVisitAgain(resultSet.getString("VisitAgain"));

                            prescriptionObservableList.add(prescription);
                        }
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                } finally {
                    Database.getInstance().close(connection, preparedStatement, resultSet);
                }

                return prescriptionObservableList;
            }

            @Override
            protected void updateValue(ObservableList<Prescription> prescriptionObservableList) {
                super.updateValue(prescriptionObservableList);

                if (prescriptionObservableList != null) {
                    setPrescriptionTable(prescriptionObservableList);
                }
            }
        }).start();
    }

    private void setPrescriptionTable(ObservableList<Prescription> prescriptionObservableList) {
        prescriptionIdColumn.setCellValueFactory(new PropertyValueFactory<>("prescriptionId"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        patientIDColumn.setCellValueFactory(new PropertyValueFactory<>("patientUserId"));
        patientNameColumn.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        patientAgeColumn.setCellValueFactory(new PropertyValueFactory<>("patientAge"));
        patientSexColumn.setCellValueFactory(new PropertyValueFactory<>("patientSex"));
        symptomsColumn.setCellValueFactory(new PropertyValueFactory<>("symptoms"));
        diagnosisColumn.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));

        prescriptionTable.setItems(prescriptionObservableList);

        prescriptionTable.setRowFactory(tableView -> {
            TableRow<Prescription> tableRow = new TableRow<>();

            tableRow.setOnMouseClicked(event -> {
                if (!tableRow.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    Prescription prescription = tableRow.getItem();
                    // Opening prescription view
                    openPrescriptionView(prescription);
                }
            });

            return tableRow;
        });
    }

    private void openPrescriptionView(Prescription prescription) {
        try {
            Stage stage = new Stage();

            stage.getIcons().add(new Image("docmate/res/docmate_logo_96px.png"));
            stage.setTitle("DocMate");

            // Loading prescription view scene
            FXMLLoader prescriptionViewFXMLLoader =
                    new FXMLLoader(getClass().getResource("../view/FXMLPrescriptionView.fxml"));
            Parent prescriptionViewParent = prescriptionViewFXMLLoader.load();
            Scene prescriptionViewScene = new Scene(prescriptionViewParent);

            // Injecting stage and data into prescription view controller
            FXMLPrescriptionViewController fxmlPrescriptionViewController =
                    prescriptionViewFXMLLoader.getController();
            fxmlPrescriptionViewController.setStage(stage);
            fxmlPrescriptionViewController.setPrescription(prescription);

            stage.setScene(prescriptionViewScene);
            stage.show();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void openPrescriptionAdd() {
        try {
            Stage stage = new Stage();
            stage.getIcons().add(new Image("docmate/res/docmate_logo_96px.png"));
            stage.setTitle("DocMate");

            // Loading add prescription scene
            FXMLLoader prescriptionNewFXMLLoader = new FXMLLoader(getClass()
                    .getResource("../view/FXMLPrescriptionAdd.fxml"));
            Parent prescriptionNewRoot = prescriptionNewFXMLLoader.load();
            Scene prescriptionNewScene = new Scene(prescriptionNewRoot);

            // Injecting stage and patient object into prescription new controller
            FXMLPrescriptionAddController prescriptionAddController = prescriptionNewFXMLLoader.getController();
            prescriptionAddController.setStage(stage);

            stage.setScene(prescriptionNewScene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}

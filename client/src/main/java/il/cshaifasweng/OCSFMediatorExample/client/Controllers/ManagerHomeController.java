package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.App;
import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.CustomMessage;
import il.cshaifasweng.OCSFMediatorExample.entities.ExtraTime;
import il.cshaifasweng.OCSFMediatorExample.entities.Principal;
import il.cshaifasweng.OCSFMediatorExample.server.Events.*;
import il.cshaifasweng.OCSFMediatorExample.client.App;
import il.cshaifasweng.OCSFMediatorExample.entities.ScheduledTest;
import javafx.application.Platform;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import javax.swing.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ManagerHomeController {

    @FXML
    private Button allStudentsBN;

    @FXML
    private Button homeBN;

    @FXML
    private Label idLabel;
    @FXML
    private Label helloLabel;

    private String id;

    private Principal manager;

    public ManagerHomeController(){
        EventBus.getDefault().register(this);
    }

    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }
    public void setId(String id){this.id = id;}



    @FXML
    @Subscribe
    public void onUserHomeEvent(UserHomeEvent event){
        setId((String) event.getUserID().get(0));
        manager = (Principal) event.getUserID().get(1);
        initializeIfIdNotNull();
    }

    @FXML
    void initialize(){
        App.getStage().setOnCloseRequest(event -> {
            ArrayList<String> info = new ArrayList<>();
            info.add(id);
            info.add("manager");
            try {
                SimpleClient.getClient().sendToServer(new CustomMessage("#logout", info));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Perform logout");
            cleanup();
            javafx.application.Platform.exit();
        });

    }

    private void initializeIfIdNotNull() {
        Platform.runLater(()->{
            if (id != null) {
                idLabel.setText("ID: " + this.id);
            }
            if (manager != null) {
                helloLabel.setText("Hello Manager " + manager.getFirst_name() + " " + manager.getLast_name());
            }
        });
    }
    @FXML
    void handleGoHomeButtonClick(ActionEvent event) {

    }
    @FXML
    void handleGoToAllStudentsButtonClick(ActionEvent event) throws IOException {
        cleanup();
        App.switchScreen("allStudents");
        Platform.runLater(()->{
            EventBus.getDefault().post(new MoveManagerIdEvent(id));
            try {
                SimpleClient.getClient().sendToServer(new CustomMessage("#showAllStudents",""));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    @FXML
    public void goToQuestions(ActionEvent event) throws IOException {
        cleanup();
        App.switchScreen("showAllQuestions");
        Platform.runLater(()->{
            EventBus.getDefault().post(new MoveManagerIdEvent(id));
        });
    }
    @FXML
    public void goToExamForms(ActionEvent event) throws IOException {
        cleanup();
        App.switchScreen("showExamForms");
        Platform.runLater(()->{
            EventBus.getDefault().post(new MoveManagerIdEvent(id));
        });
    }
    @FXML
    public void goToScheduledTests(ActionEvent event) throws IOException {
        cleanup();
        App.switchScreen("showScheduleTest");
        Platform.runLater(()->{
            EventBus.getDefault().post(new MoveManagerIdEvent(id));
            try {
                SimpleClient.getClient().sendToServer(new CustomMessage("#showScheduleTest",""));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    @FXML
    public void goToStatistics(ActionEvent event) throws IOException {
        cleanup();
        App.switchScreen("showStatistics");
        Platform.runLater(()->{
            EventBus.getDefault().post(new MoveManagerIdEvent(id));
        });
    }

    @Subscribe
    public void onTimeLeftEvent(TimeLeftEvent event){
        try {
            SimpleClient.getClient().sendToServer(new CustomMessage("#getExtraTimeRequests",""));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public synchronized void onExtraTimeRequestEvent(extraTimeRequestEvent event){
        List<ExtraTime> extraTimeRequestEventList = event.getData();
        if (!extraTimeRequestEventList.isEmpty()){
            for (ExtraTime extraTime : extraTimeRequestEventList) {
                if (isScheduledTestActive(extraTime.getScheduledTest())) {
                    handleExtraTimeRequest(extraTime);
                }
            }
        }
    }

    public synchronized boolean isScheduledTestActive(ScheduledTest scheduledTest) {
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        // Get the start date and time of the scheduled test
        LocalDate testStartDate = scheduledTest.getDate();
        LocalTime testStartTime = scheduledTest.getTime();

        // Calculate the end date and time of the scheduled test
        LocalDateTime testStartDateTime = LocalDateTime.of(testStartDate, testStartTime);
        LocalDateTime testEndDateTime = testStartDateTime.plusMinutes(scheduledTest.getTimeLimit());

        // Check if the current date and time is within the scheduled test's time range
        return (currentDate.isEqual(testStartDate) && currentTime.isAfter(testStartTime)) ||
                (currentDate.isAfter(testStartDate) && currentDate.isBefore(testEndDateTime.toLocalDate())) ||
                (currentDate.isEqual(testEndDateTime.toLocalDate()) && currentTime.isBefore(testEndDateTime.toLocalTime()));
    }

    public synchronized void handleExtraTimeRequest(ExtraTime extraTime){
            String explanation = extraTime.getExplanation();
            int extraMinutes = extraTime.getExtraTime();
            String teacherName = extraTime.getTeacherName();
            String subCourse = extraTime.getSubCourse();
            ScheduledTest myScheduledTest = extraTime.getScheduledTest();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Extra time request");
                alert.setContentText("The teacher " +teacherName + " has requested " + extraMinutes + " extra minutes to an exam in "
                        + subCourse  + " from the reason: " + '"'+ explanation + '"' + ". Select if you want to approve this request.");
                alert.setResizable(true);
                alert.getDialogPane().setPrefSize(480, 320);

                Optional<ButtonType> result = alert.showAndWait();

                List<Object> data = new ArrayList<>();
                data.add(myScheduledTest);
                if (result.isPresent() && result.get() == ButtonType.OK) {
                        int x = myScheduledTest.getTimeLimit();
                        myScheduledTest.setTimeLimit(x+extraMinutes);
                        Platform.runLater(()->{
                            try {
                                SimpleClient.getClient().sendToServer(new CustomMessage("#updateScheduleTest", myScheduledTest));
                                data.add(1, true);
                                data.add(2, extraMinutes);
                                data.add(3, myScheduledTest.getId());
                                SimpleClient.getClient().sendToServer(new CustomMessage("#extraTimeResponse", data));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                else {
                    try{
                        data.add(1, false);
                        data.add(2,  0);
                        data.add(3, myScheduledTest.getId());
                        SimpleClient.getClient().sendToServer(new CustomMessage("#extraTimeResponse", data));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
    }

    public void handleLogoutButtonClick(ActionEvent actionEvent) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("LOGOUT");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to logout?");

        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");

        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            ArrayList<String> info = new ArrayList<>();
            info.add(id);
            info.add("manager");
            SimpleClient.getClient().sendToServer(new CustomMessage("#logout", info));
            System.out.println("Perform logout");
            cleanup();
            javafx.application.Platform.exit();
        } else {
            alert.close();
        }
    }

    public void handleBackButtonClick(ActionEvent actionEvent) {
    }
}



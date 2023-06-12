package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.App;
import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.*;
import il.cshaifasweng.OCSFMediatorExample.server.Events.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentExecuteExamController {


    @FXML
    private GridPane StudentsGR;
    @FXML
    private Button homeBN;
    @FXML
    private Label text_Id;
    @FXML
    private Label timeLeftText;
    @FXML
    private Button submitButton;
    @FXML
    private List<ToggleGroup> toggleGroups = new ArrayList<>();
    @FXML
    private ToggleGroup toggleGroup;
    @FXML
    private ListView<Question_Answer> questionsListView;

    private String id;
    private ScheduledTest scheduledTest;
    private StudentTest studentTest;
    private List<Question_Score> questionScoreList;
    private Student student;
    private List<Question_Answer> questionAnswers ;
    private long timeLeft;
//    private List<TextField> q_notes;



    public StudentExecuteExamController() {
        EventBus.getDefault().register(this);

    }

    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onSelectedStudentEvent(SelectedStudentEvent event){
        student =event.getStudent();
        id = student.getId();
        questionAnswers= new ArrayList<>();
        Platform.runLater(() -> {
            text_Id.setText(text_Id.getText() + student.getFirst_name() + " " + student.getLast_name());
        });
        studentTest = new StudentTest();
        studentTest.setStudent(student);
        student.getStudentTests().add(studentTest);
    }

    @Subscribe
    public void onSelectedTestEvent(SelectedTestEvent event) throws IOException {
        scheduledTest = event.getSelectedTestEvent();
        scheduledTest.setActiveStudents(scheduledTest.getActiveStudents()+1);
        //TODO handle active students to be 0 after schedule test ends
        //TODO handle if student gets out and comes back again
        SimpleClient.getClient().sendToServer(new CustomMessage("#updateScheduleTest",scheduledTest));
        questionScoreList = scheduledTest.getExamForm().getQuestionScores();

        for (Question_Score questionScore : questionScoreList) {
            Question_Answer questionAnswer = new Question_Answer();
            questionAnswer.setStudentTest(studentTest);
            questionAnswer.setQuestionScore(questionScore);
            questionAnswer.setAnswer(-1); // Initialize with no answer selected
            questionScore.getQuestionAnswers().add(questionAnswer);

            questionAnswers.add(questionAnswer);
        }

        ObservableList<Question_Answer> questionAnswerObservableList = FXCollections.observableArrayList(questionAnswers);
        questionsListView.setItems(questionAnswerObservableList);

        questionsListView.setCellFactory(param -> new ListCell<Question_Answer>() {
            @Override
            protected void updateItem(Question_Answer questionAnswer, boolean empty) {
                super.updateItem(questionAnswer, empty);

                if (empty || questionAnswer == null) {
                    setText(null);
                    setGraphic(null);
                } else {

                    Question_Score qs = questionAnswer.getQuestionScore();
                    Question q = qs.getQuestion();
                    String questionText = q.getText();
                    String answer0 = q.getAnswer0();
                    String answer1 = q.getAnswer1();
                    String answer2 = q.getAnswer2();
                    String answer3 = q.getAnswer3();

                    VBox vbox = new VBox();
                    vbox.setSpacing(10);

                    Label questionLabel = new Label("Question:      " + questionText);
                    vbox.getChildren().add(questionLabel);

                    toggleGroup = new ToggleGroup();

                    RadioButton answer1RadioButton = new RadioButton("1.    " + answer0);
                    answer1RadioButton.setToggleGroup(toggleGroup);
                    vbox.getChildren().add(answer1RadioButton);

                    RadioButton answer2RadioButton = new RadioButton("2.     " + answer1);
                    answer2RadioButton.setToggleGroup(toggleGroup);
                    vbox.getChildren().add(answer2RadioButton);

                    RadioButton answer3RadioButton = new RadioButton("3.     " + answer2);
                    answer3RadioButton.setToggleGroup(toggleGroup);
                    vbox.getChildren().add(answer3RadioButton);

                    RadioButton answer4RadioButton = new RadioButton("4.     " + answer3);
                    answer4RadioButton.setToggleGroup(toggleGroup);
                    vbox.getChildren().add(answer4RadioButton);

                    Label noteStudentLabel = new Label("teacher's note: " + qs.getStudent_note());
                    vbox.getChildren().add(noteStudentLabel);

                    Label scoreLabel = new Label("Points: " + questionAnswer.getQuestionScore().getScore());
                    vbox.getChildren().add(scoreLabel);

                    Label note = new Label("note: ");
                    vbox.getChildren().add(note);
                    TextField noteText = new TextField();
                    vbox.getChildren().add(noteText);

                    noteText.textProperty().addListener((observable, oldValue, newValue) -> {
                        // Save the entered note to the Question_Answer object
                        questionAnswer.setNote(newValue);
                    });

                    setGraphic(vbox);
                    toggleGroups.add(toggleGroup);
//                    q_notes.add(noteText);

                    //

                    // Listen for changes in the selected toggle
                    toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
                        RadioButton selectedRadioButton = (RadioButton) newValue;
                        if (selectedRadioButton != null) {
                            int answerIndex = Integer.parseInt(selectedRadioButton.getText().split("\\.")[0]) - 1;
                            questionAnswer.setAnswer(answerIndex); // Update the answer index in the Question_Answer object
                        } else {
                        }
                    });



                    //
                }
            }
        });
    }

    @FXML
    public void submitTestBtn(ActionEvent event) throws IOException {
        //TODO validation checks
        endTest();
    }

@Subscribe
    public void onShowSuccessEvent(ShowSuccessEvent event) throws IOException {
        System.out.println("good");
        cleanup();
        App.switchScreen("studentHome");
        Platform.runLater(()->{
            EventBus.getDefault().post(new MoveIdToNextPageEvent(id));
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setContentText("Exam Submitted Successfully");
            alert.setHeaderText(null);
            alert.show();
        });
    }

    @Subscribe
    public void onManagerExtraTimeEvent(ManagerExtraTimeEvent event) {
        Platform.runLater(() -> {
            List<Object> objectList = event.getData();
            if (objectList.get(3).equals(scheduledTest.getId())){
                if ((int)objectList.get(2) != 0){
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Information");
                    alert.setHeaderText(null);
                    alert.setContentText("The teacher added " + objectList.get(2) + " minutes to the test!");
                    alert.show();
                }
            }
        });
    }

@Subscribe
    public void onTimerStartEvent(TimerStartEvent event){ // not necessary
        if(event.getScheduledTest().getId().equals(scheduledTest.getId()))
        {
            System.out.println(" on schedule test "+ scheduledTest.getId() + " timer started ");
        }
    }

@Subscribe
    public void onTimeLeftEvent(TimeLeftEvent event){
        List<Object> scheduleTestId_timeLeft = event.getScheduleTestId_timeLeft();
        timeLeft = (long)scheduleTestId_timeLeft.get(1);
        String scheduleTestId = (String) scheduleTestId_timeLeft.get(0);

        Platform.runLater(() -> {
            if(scheduleTestId.equals(scheduledTest.getId())) {
                timeLeftText.setText(Long.toString(timeLeft));
            }
        });
}

@Subscribe
    public void onTimerFinishedEvent(TimerFinishedEvent event) throws IOException {
        if(event.getScheduledTest().getId().equals(scheduledTest.getId()))
        {
            System.out.println(" on schedule test "+ scheduledTest.getId() + " timer FINISHED ");
            studentTest.setOnTime(false);
            endTest();
        }
    }

    public void endTest() throws IOException {
        studentTest.setScheduledTest(scheduledTest);
        studentTest.setQuestionAnswers(questionAnswers);
        studentTest.setTimeToComplete(scheduledTest.getExamForm().getTimeLimit()-timeLeft);
        int sum =0;

        // student test is ready
        //TODO subtract 1 to scheduled test active students executing test and add 1 to submissions
        scheduledTest.setSubmissions(scheduledTest.getSubmissions()+1);
        scheduledTest.setActiveStudents(scheduledTest.getActiveStudents()-1);


        for(Question_Answer questionAnswer:questionAnswers){
            int points = questionAnswer.getQuestionScore().getScore();
            int indexAnsStudent = questionAnswer.getAnswer();
            int indexCorrect = questionAnswer.getQuestionScore().getQuestion().getIndexAnswer();
            if(indexAnsStudent == indexCorrect){
                sum+=points;
            }
        }
        studentTest.setGrade(sum);
        List<Object> student_studentTest_questionAnswers = new ArrayList<>();
        student_studentTest_questionAnswers.add(student);
        student_studentTest_questionAnswers.add(studentTest);
        for(Question_Answer questionAnswer:questionAnswers){
            student_studentTest_questionAnswers.add(questionAnswer);
        }
        SimpleClient.getClient().sendToServer(new CustomMessage("#updateScheduleTest",scheduledTest));
        SimpleClient.getClient().sendToServer(new CustomMessage("#saveQuestionAnswers",student_studentTest_questionAnswers));
    }

    public void handleLogoutButtonClick(ActionEvent actionEvent) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("LOGOUT");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to exit test and logout ?");

        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");

        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            ArrayList<String> info = new ArrayList<>();
            info.add(id);
            info.add("student");
            SimpleClient.getClient().sendToServer(new CustomMessage("#logout", info));
            System.out.println("Perform logout");
            cleanup();
            javafx.application.Platform.exit();
        } else {
            alert.close();
        }
    }

    public void handleBackButtonClick(ActionEvent actionEvent) { Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("EXIT");
        alert.setHeaderText(null);
        alert.setContentText("Cant go back, please submit test");
        ButtonType yesButton = new ButtonType("return");
        alert.getButtonTypes().setAll(yesButton);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yesButton){
            alert.close();
        }
    }

    public void handleGoHomeButtonClick(ActionEvent actionEvent) {
        handleBackButtonClick(null);
    }
}



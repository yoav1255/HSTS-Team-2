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

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.FileOutputStream;
import java.util.Objects;

public class StudentExecuteExamLOCALController {


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



    public StudentExecuteExamLOCALController() {
        EventBus.getDefault().register(this);

    }

    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onSelectedStudentEvent(SelectedStudentEvent event){
        student =event.getStudent();
        questionAnswers= new ArrayList<>();
        Platform.runLater(() -> {
            text_Id.setText(text_Id.getText() + student.getFirst_name() + " " + student.getLast_name());
        });
        studentTest = new StudentTest();
        studentTest.setStudent(student);
        student.getStudentTests().add(studentTest);
    }

    @Subscribe
    public void onSelectedTestEvent(SelectedTestEvent event) {
        scheduledTest = event.getSelectedTestEvent();
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
                            System.out.println("No answer selected for question: " + questionAnswer.getQuestionScore().getQuestion().getText());
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
        //endTest();
        System.out.println("good");
        cleanup();
        App.switchScreen("studentHome");
        JOptionPane.showMessageDialog(null, "Exam Submitted Successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        Platform.runLater(()->{
            EventBus.getDefault().post(new MoveIdToNextPageEvent(student.getId()));
        });
    }

    @Subscribe
    public void onShowSuccessEvent(ShowSuccessEvent event) throws IOException {
        System.out.println("good");
        cleanup();
        App.switchScreen("studentHome");
        JOptionPane.showMessageDialog(null, "Exam Submitted Successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        Platform.runLater(()->{
            EventBus.getDefault().post(new MoveIdToNextPageEvent(student.getId()));
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

        if(scheduleTestId.equals(scheduledTest.getId())) {
            Platform.runLater(() -> {
                timeLeftText.setText(Long.toString(timeLeft));
            });
        }
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
        //TODO update the student checked and schedule test

        // student test is ready

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

        SimpleClient.getClient().sendToServer(new CustomMessage("#saveQuestionAnswers",student_studentTest_questionAnswers));
    }

    public void handleDownloadButton(ActionEvent actionEvent) {
        XWPFDocument document = new XWPFDocument();
        XWPFParagraph titleParagraph = document.createParagraph();
        titleParagraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setFontFamily("Arial");
        titleRun.setFontSize(15);
        titleRun.setBold(true);
        titleRun.setText(scheduledTest.getSubjectName() + ", " + scheduledTest.getCourseName());
        titleRun.setText(", " + scheduledTest.getTeacherName());
        //titleRun.setText("Local Test Format");
        titleRun.addBreak();
        titleRun.setText("Exam Form Code : " + scheduledTest.getExamFormCode());
        titleRun.setText(", For student " + student.getFirst_name() + " " + student.getLast_name());
        titleRun.addBreak();
        int counter = 1;

        for (Question_Score questionScore : questionScoreList) {
            Question_Answer questionAnswer = new Question_Answer();
            questionAnswer.setStudentTest(studentTest);
            questionAnswer.setQuestionScore(questionScore);
            questionAnswer.setAnswer(-1); // Initialize with no answer selected
            questionScore.getQuestionAnswers().add(questionAnswer);
            questionAnswers.add(questionAnswer);
            Question_Score qs = questionAnswer.getQuestionScore();
            Question q = qs.getQuestion();
            String questionText = q.getText();
            String answer0 = q.getAnswer0();
            String answer1 = q.getAnswer1();
            String answer2 = q.getAnswer2();
            String answer3 = q.getAnswer3();

            XWPFParagraph questionParagraph = document.createParagraph();
            XWPFRun questionRun = questionParagraph.createRun();
            questionRun.setFontFamily("Arial");
            questionRun.setBold(true);
            String score = String.valueOf(questionAnswer.getQuestionScore().getScore());
            questionRun.setText(counter + ".  " + questionText + " (" + score + ").");
            questionRun.addBreak();
            XWPFRun questionRun2 = questionParagraph.createRun();
            questionRun2.setFontFamily("Arial");
            questionRun2.setBold(false);
            questionRun2.setText("  1.  " + answer0);
            questionRun2.addBreak();
            questionRun2.setText("  2.  " + answer1);
            questionRun2.addBreak();
            questionRun2.setText("  3.  " + answer2);
            questionRun2.addBreak();
            questionRun2.setText("  4.  " + answer3);
            if(!Objects.equals(qs.getStudent_note(), "")){
                questionRun2.addBreak();
                questionRun2.setText("Teacher note : " + qs.getStudent_note());
            }
            questionRun2.addBreak();
            questionRun2.addBreak();
            counter++;
        }

        try {
            String filename = "test" + scheduledTest.getExamFormCode() + "for" + student.getId() + ".docx";
            FileOutputStream output = new FileOutputStream(filename);
            document.write(output);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


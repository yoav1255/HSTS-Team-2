package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.App;
import il.cshaifasweng.OCSFMediatorExample.entities.*;
import il.cshaifasweng.OCSFMediatorExample.server.Events.MoveIdToNextPageEvent;
import il.cshaifasweng.OCSFMediatorExample.server.Events.ShowUpdateStudentEvent;
import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.server.Events.MoveObjectToNextPageEvent;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.List;


public class ShowStudentTest {
    private StudentTest studentTest;
    private ScheduledTest scheduledTest;
    private List<Question_Answer> questionAnswerList;
    private String studentId;
    private Student student;

    @FXML
    private Button homeBN;

    @FXML
    private Label oldGrade;

    @FXML
    private Label statusLB;

    @FXML
    private Label test_course;

    @FXML
    private Label test_id;

    @FXML
    private Label timeToComplete;

    @FXML
    private TextArea txtChange;

    @FXML
    private ListView<Question_Answer> questionsListView;

    public ShowStudentTest() {
        EventBus.getDefault().register(this);
    }

    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onMoveIdToNextPageEvent(MoveIdToNextPageEvent event) {
        studentId = event.getId();
    }
    @Subscribe
    public void onMoveObjectToNextPageEvent(MoveObjectToNextPageEvent event){ student = (Student) event.getObject(); }

    @Subscribe
    public void onShowStudentTestEvent(ShowUpdateStudentEvent event) {
        try {
            studentTest = event.getStudentTest();
            Platform.runLater(() -> {
                statusLB.setText(statusLB.getText() + studentTest.getExamFormCode());
                test_id.setText(String.valueOf(studentTest.getExamFormCode()));
                test_course.setText(String.valueOf(studentTest.getCourseName()));
                oldGrade.setText(String.valueOf(studentTest.getGrade()));
                timeToComplete.setText(String.valueOf(studentTest.getTimeToComplete()));
                txtChange.setText(studentTest.getChange_explanation());
                txtChange.setDisable(true);
            });
            questionAnswerList = studentTest.getQuestionAnswers();
            scheduledTest = studentTest.getScheduledTest();
            System.out.println("ans q1 : " + questionAnswerList.get(0).getAnswer());
            Platform.runLater(() -> {
                setTable();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTable() {
        try {
            ObservableList<Question_Answer> questions1 = FXCollections.observableArrayList(questionAnswerList);
            questionsListView.setItems(questions1);
            questionsListView.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Question_Answer questionAnswer, boolean empty) {
                    super.updateItem(questionAnswer, empty);
                    if (empty || questionAnswer == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        Question_Score questionScore = questionAnswer.getQuestionScore();
                        Question question = questionScore.getQuestion();
                        VBox vbox = new VBox();
                        Label questionText = new Label(question.getText());
                        vbox.getChildren().add(questionText);

                        // Add the answers as separate labels in the VBox

                        Label answerLabel0 = new Label("1.      " + question.getAnswer0());
                        System.out.println("qa is : " + questionAnswer.getAnswer());
                        if (questionAnswer.getAnswer() == 0)
                            answerLabel0.setStyle("-fx-font-weight: bold; -fx-background-color: red;");
                        if (question.getIndexAnswer() == 0) {
                            answerLabel0.setStyle("-fx-font-weight: bold; -fx-background-color: green;");
                        }
                        vbox.getChildren().add(answerLabel0);

                        Label answerLabel1 = new Label("2.      " + question.getAnswer1());
                        if (questionAnswer.getAnswer() == 1)
                            answerLabel1.setStyle("-fx-font-weight: bold; -fx-background-color: red;");
                        if (question.getIndexAnswer() == 1) {
                            answerLabel1.setStyle("-fx-font-weight: bold; -fx-background-color: green;");
                        }
                        vbox.getChildren().add(answerLabel1);

                        Label answerLabel2 = new Label("3.      " + question.getAnswer2());
                        if (questionAnswer.getAnswer() == 2)
                            answerLabel2.setStyle("-fx-font-weight: bold; -fx-background-color: red;");
                        if (question.getIndexAnswer() == 2) {
                            answerLabel2.setStyle("-fx-font-weight: bold; -fx-background-color: green;");
                        }
                        vbox.getChildren().add(answerLabel2);

                        Label answerLabel3 = new Label("4.      " + question.getAnswer3());
                        if (questionAnswer.getAnswer() == 3)
                            answerLabel3.setStyle("-fx-font-weight: bold; -fx-background-color: red;");
                        if (question.getIndexAnswer() == 3) {
                            answerLabel3.setStyle("-fx-font-weight: bold; -fx-background-color: green;");
                        }
                        vbox.getChildren().add(answerLabel3);

                        Label score = new Label("( " + questionScore.getScore() + " points )");
                        vbox.getChildren().add(score);

                        if (questionAnswer.getNote() != "") {
                            Label studentNotes = new Label("Student Note: " + questionAnswer.getNote());
                            vbox.getChildren().add(studentNotes);
                        }
                        setGraphic(vbox);


                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goBackButton() throws IOException {
        try {
            cleanup();
            App.switchScreen("showOneStudent");

            Platform.runLater(()->{
                try {
                    EventBus.getDefault().post(new MoveIdToNextPageEvent(student.getId()));
                    EventBus.getDefault().post(new MoveObjectToNextPageEvent(student));
                    SimpleClient.getClient().sendToServer(new CustomMessage("#getStudentTests", student));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleGoHomeButtonClick(ActionEvent event) throws IOException {
        cleanup();
        App.switchScreen("studentHome");
        Platform.runLater(() -> {
            try {
                SimpleClient.getClient().sendToServer(new CustomMessage("#studentHome", studentId));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}



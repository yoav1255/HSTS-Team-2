package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.App;
import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.CustomMessage;
import il.cshaifasweng.OCSFMediatorExample.server.Events.UserHomeEvent;
import il.cshaifasweng.OCSFMediatorExample.server.Events.loginEvent;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.javatuples.Triplet;

import java.io.IOException;
import java.util.ArrayList;

public class loginController {

    @FXML
    private Label error_msg;

    //@FXML
    //private TextArea error_msgg;

    @FXML
    private Button loginBN;

    @FXML
    private TextField user_id;

    @FXML
    private PasswordField user_password;
    private static int instances = 0;

    public loginController(){
        EventBus.getDefault().register(this);
        instances++;
    }
    public void cleanup() {
        EventBus.getDefault().unregister(this);
        instances--;
    }

    private String user_type;
    private void setUserType(String user_type){this.user_type = user_type;}


    @FXML void initialize(){
        error_msg.setVisible(false);
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    @FXML
    public void onloginEvent(loginEvent event) throws IOException {
        setUserType(event.getUserType());

        switch (user_type) {
            case ("wrong"):
                error_msg.setVisible(true);
                break;
            case ("student"):
                cleanup();
                App.switchScreen("studentHome");
                Platform.runLater(()->{
                    try {
                        SimpleClient.getClient().sendToServer(new CustomMessage("#studentHome", user_id.getText()));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });
                break;
            case ("teacher"):
                cleanup();
                App.switchScreen("teacherHome");
                Platform.runLater(()->{
                    try{
                        SimpleClient.getClient().sendToServer(new CustomMessage("#teacherHome", user_id.getText()));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });
                break;
            case ("manager"):
                cleanup();
                App.switchScreen("managerHome");
                Platform.runLater(()->{
                    try {
                        SimpleClient.getClient().sendToServer(new CustomMessage("#managerHome", user_id.getText()));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });
                break;
        }
    }

    @FXML
    void loginButton(ActionEvent event) {
        try{
            String id = user_id.getText();
            String pass = user_password.getText();
            ArrayList<String> loginData = new ArrayList<>();
            loginData.add(0,id);
            loginData.add(1,pass);
            SimpleClient.getClient().sendToServer(new CustomMessage("#login", loginData));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.CustomMessage;
import il.cshaifasweng.OCSFMediatorExample.entities.ExamForm;
import il.cshaifasweng.OCSFMediatorExample.server.Events.DeleteClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.Events.NewClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.Events.TerminateAllClientsEvent;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

public class ServerController {

    @FXML
    private TableView<CustomMessage> clients_table_view;
    @FXML
    private TableColumn<CustomMessage, String> address;
    @FXML
    private TableColumn<CustomMessage, String> host;
    @FXML
    private TableColumn<CustomMessage, String> port;
    @FXML
    private Label connected;

    private ObservableList<ConnectionToClient> clientObservableList;


    public ServerController() {
        EventBus.getDefault().register(this);
        clientObservableList = FXCollections.observableArrayList();
    }

    @Subscribe
    public void onNewClientEvent(NewClientEvent event){
        try {
            ConnectionToClient client = event.getClient();
            clientObservableList.add(client);
            Platform.runLater(()->{
                connected.setText((Integer.toString(clientObservableList.size())));
                clients_table_view.refresh();
            });
            setTable();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onDeleteClientEvent(DeleteClientEvent event){
        try {
            ConnectionToClient client = event.getClient();
            removeClient(client);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void removeClient(ConnectionToClient client){
        clientObservableList.remove(client);
        Platform.runLater(()->{
            connected.setText((Integer.toString(clientObservableList.size())));
            clients_table_view.refresh();
        });
        setTable();
    }


    public void setTable() {
        try {

//			clientObservableList = FXCollections.observableList(clients);
//			clients_table_view.setItems(clientObservableList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
@FXML
    public void disconnectClients(ActionEvent event) throws IOException {
        EventBus.getDefault().post(new TerminateAllClientsEvent());
        clientObservableList.clear();

        Platform.runLater(()->{
            connected.setText((Integer.toString(clientObservableList.size())));
            clients_table_view.refresh();
        });
    }
}


package view;

import java.io.IOException;

import java.net.URL;
import java.util.ResourceBundle;

import moneyrain.MoneyRain;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.Tietokanta;
import model.User;


public class HomepageViewController implements Initializable {
	@FXML Label nameLabel;
	@FXML Text kolikotLabel;
	@FXML Text krediititLabel;
	@FXML Button logoutButton;
	@FXML Button toStoreButton;
	@FXML Button toUserInfoButton;
	@FXML Button toArcadeBlackjack1;
	@FXML Button toMoneyRain;
	
	private void init() {
		nameLabel.setText(User.getUsername());
		kolikotLabel.setText("Kolikot: " + User.getCoins());
		krediititLabel.setText("Krediitit: " + User.getCredits());
	}
	
	public void toArcadeBlackjack1(ActionEvent e) {
		try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApplication.class.getResource("ArcadeBlackjack1View.fxml"));
            AnchorPane blackjackView = (AnchorPane) loader.load();
            Scene blackjackScene = new Scene(blackjackView);
			Stage window = (Stage) toArcadeBlackjack1.getScene().getWindow();
			window.setScene(blackjackScene);
        } catch (IOException iOE) {
            iOE.printStackTrace();
        }
	}
	
	public void toMoneyRain(ActionEvent e) {
		try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApplication.class.getResource("MoneyRainMenuView.fxml"));
            AnchorPane moneyrainmenuView = (AnchorPane) loader.load();
            Scene moneyrainmenuScene = new Scene(moneyrainmenuView);
			Stage window = new Stage();
			window.setOnCloseRequest(evt -> {
				if(MoneyRain.getTl() != null) {
					MoneyRain.getTl().stop();//Pysäyttää pelin timelinen jos suljetaan ikkuna kesken pelin.
				}
			});
			window.setScene(moneyrainmenuScene);
			window.setResizable(false);
			window.show();
        } catch (IOException iOE) {
            iOE.printStackTrace();
        }
	}
	
	public void toUserInfo(ActionEvent e) {
		try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApplication.class.getResource("UserInfoView.fxml"));
            AnchorPane userInfoView = (AnchorPane) loader.load();
            Scene userInfoScene = new Scene(userInfoView);
			Stage window = (Stage) toUserInfoButton.getScene().getWindow();
			window.setScene(userInfoScene);
        } catch (IOException iOE) {
            iOE.printStackTrace();
        }
	}
	
	public void toStore(ActionEvent e) {
		try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApplication.class.getResource("StoreView.fxml"));
            BorderPane storeView = (BorderPane) loader.load();
            Scene storeScene = new Scene(storeView);
			Stage window = (Stage) toStoreButton.getScene().getWindow();
			window.setScene(storeScene);
        } catch (IOException iOE) {
            iOE.printStackTrace();
        }
	}
	
	public void logout(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApplication.class.getResource("LoginView.fxml"));
            BorderPane loginView = (BorderPane) loader.load();
            Scene loginScene = new Scene(loginView);
			Stage window = (Stage) toStoreButton.getScene().getWindow();
			Tietokanta.logout();
			window.setScene(loginScene);
        } catch (IOException iOE) {
            iOE.printStackTrace();
        }
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		init();
	}
}

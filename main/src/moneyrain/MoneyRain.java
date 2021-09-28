package moneyrain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class MoneyRain extends Canvas {
	
	private Stage stage;
	
	private Timeline tl;
	private int timeInMillis;
	private static final int height = 605;
	private static final int width = 720;
	private static final int PLAYER_WIDTH = 85;
	private static final int PLAYER_HEIGHT = 130;
	private static final int playableAreaLeft = 100;
	private static final int playableAreaRight = 600;
	private double playerXPos = width / 2;
	private double playerYPos = height - PLAYER_HEIGHT;
	private Image bg;
	private Image bgfullcar;
	private Image deathscreen;
	private Image player;
	private Image bill;
	private Image click;
	private Image heart1;
	private Image heart2;
	private Image heart3;
	private Image poison;
	private Image megis;
	private boolean gameStarted = false;
	private int points;
	private int collectedCash;
	private int health = 3;
	private List<Item> items = new ArrayList<>();
	
	public MoneyRain(Stage stage) {
		try {
			start(stage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void start(Stage stage) throws Exception{
		this.stage = stage;
		this.stage.setTitle("MoneyRain");
		bg = new Image(getClass().getResourceAsStream("bg.jpg"));
		bgfullcar = new Image(getClass().getResourceAsStream("bgfullcar.jpg"));
		heart1 = new Image(getClass().getResourceAsStream("heart1.png"));
		heart2 = new Image(getClass().getResourceAsStream("heart2.png"));
		heart3 = new Image(getClass().getResourceAsStream("heart3.png"));
		bill = new Image(getClass().getResourceAsStream("bill.png"));
		poison = new Image(getClass().getResourceAsStream("poison.png"));
		megis = new Image(getClass().getResourceAsStream("megis.png"));
		Canvas canvas = new Canvas(width, height);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		tl = new Timeline(new KeyFrame(Duration.millis(10), e -> run(gc)));
		tl.setCycleCount(Timeline.INDEFINITE);
		
		
		/**
		 * Pelaajan controlleri
		 */
		canvas.setOnMouseMoved(e -> {
			
			//Pelaajan kuvaa vaihdetaan riippuen siitä liiktaanko vasemmalle tai oikealle
			if(e.getX() - (PLAYER_WIDTH/2) < playerXPos)
				player = new Image(getClass().getResourceAsStream("playerLeft.png"));
			if(e.getX() - (PLAYER_WIDTH/2) > playerXPos)
				player = new Image(getClass().getResourceAsStream("playerRight.png"));
			
			//Asetetaan pelaaja seuraamaan hiirtä ja rajoitukset kuinka pikälle pelaaja voi siirtyä ruudulla
			//PLAYER_WIDTH/2, jotta hiiri olisi pelaaja-kuvan keskellä, eikä pelaaja-kuvan vasemmalla
			if(e.getX() < playableAreaLeft)
				playerXPos = playableAreaLeft - (PLAYER_WIDTH/2);
			else if(e.getX() > playableAreaRight)
				playerXPos = playableAreaRight - (PLAYER_WIDTH/2);
			else
				playerXPos = e.getX() - (PLAYER_WIDTH/2);
			
		});
		
		/*
		 * Hiiren klikkauksesta tapahtuvat asiat
		 */
		canvas.setOnMouseClicked(e -> {
			if(!gameStarted) {
				gameStarted = true;
			}
			else {
				if(playerXPos < 80 && collectedCash >= 1) {
					switch(collectedCash) { //Annetaan pisteitä sen mukaan kuinka paljon rahaa on kädessä
						case 1:
							points+=1;
							break;
						case 2:
							points+=2;
							break;
						case 3:
							points+=4;
							break;
						case 4:
							points+=7;
							break;
						case 5:
							points+=10;
							break;
					}
					collectedCash--; //Rahat tyhjennetään kädestä autoon
				}
			}
		});
		
		this.stage.setScene(new Scene(new StackPane(canvas)));
		this.stage.show();
		tl.play();
	}
	
	private void run(GraphicsContext gc) {
		if(collectedCash == 5) 
			gc.drawImage(bgfullcar, 0, 0);
		else
			gc.drawImage(bg, 0, 0);
		gc.setFont(Font.font(25));
		gc.setTextAlign(TextAlignment.LEFT);
		
		//Mitä tapahtuu pelin aikana
		if(gameStarted) {
			timeInMillis += 10; //Laskuri jolla seurataan pelissä käytettyä aikaa
			gc.setFill(Color.WHITE);
			gc.drawImage(player, playerXPos, playerYPos, PLAYER_WIDTH, PLAYER_HEIGHT);
			gc.fillText("Pisteet: " +  points + "    Rahat kädessä: " + collectedCash + "/5", 10, 30);
			
			if(health == 3)
				gc.drawImage(heart3, 630, 10);
			else if(health == 2)
				gc.drawImage(heart2, 630, 10);
			else
				gc.drawImage(heart1, 630, 10);

			//Tehdään asioita kerran per 1s
			if(timeInMillis % 1000 == 0) {
				createBill(); //Luodaan seteli
			}
			
			//Tehdään asioita kerran per 2s
			if(timeInMillis % 2000 == 0) {
				createPoison(); //Luodaan myrkkypullo
			}
			
			//Tehdään asioita kerran per 20s
			if(timeInMillis % 20000 == 0) {
				createMegis(); //Luodaan megis
			}
			
			items.forEach(item -> {
				
				//Jos item osuu pelaajaan / Box collider
				if(item.getYPos() > playerYPos - item.getHeight() &&
					(item.getXPos() >= playerXPos && item.getXPos() <= PLAYER_WIDTH + playerXPos ||
						item.getWidth() + item.getXPos() >= playerXPos && item.getWidth() + item.getXPos() <= PLAYER_WIDTH + playerXPos)) {
						item.isCollected();
						if(collectedCash < 5 && !item.isDangerous() && !item.isGivesHp()) //Jos item on seteli
							collectedCash++;
						if(item.isDangerous()) { //Jos item on vaarallinen
							health--;
							System.out.println("Health: " + health);
							if(health <= 0) {
								deathscreen = new Image(getClass().getResourceAsStream("deathscreen.png"));
								gc.drawImage(deathscreen, 0, 0);
								tl.stop();
							}
						}
						if(item.isGivesHp() && health <= 2) //Jos item antaa elämän
							health++;
				}
				item.fall(); //Laitetaan item tippumaan
				gc.drawImage(item.getImg(), item.getXPos(), item.getYPos(), item.getWidth(), item.getHeight()); //Tulostetaan item
			});
			
			//Poistetaan tippuva item
			items.removeIf(item -> 
				//Jos item tippuu ruudulta pois
				item.getYPos() > height ||
				
				//Jos item osuu pelaajaan kummalta puolelta tahansa
				item.getYPos() > playerYPos - item.getHeight() && (item.getXPos() >= playerXPos && item.getXPos() <= PLAYER_WIDTH + playerXPos && item.checkCollected() || 
				item.getWidth() + item.getXPos() >= playerXPos && item.getWidth() + item.getXPos() <= PLAYER_WIDTH + playerXPos && item.checkCollected())
			);

		}
		//Ennen pelin alkamista suoritettavat rivit
		else {
			click = new Image(getClass().getResourceAsStream("click.png"));
			gc.drawImage(click, 0, 0);
		}
	}
	
	/*
	 * Luodaan Bill olioita
	 */
	private void createBill() {
		int xPos = new Random().nextInt((playableAreaRight - 69) - playableAreaLeft + 1) + playableAreaLeft;
		Item item = new Item(bill, 69, 33, xPos, 0, false, false);
		items.add(item);
	}
	/*
	 * Luodaan Poison olioita
	 */
	private void createPoison() {
		int xPos = new Random().nextInt((playableAreaRight - 69) - playableAreaLeft + 1) + playableAreaLeft;
		Item item = new Item(poison, 24, 48, xPos, 0, true, false);
		items.add(item);
	}
	
	/*
	 * Luodaan Megis olioita
	 */
	private void createMegis() {
		int xPos = new Random().nextInt((playableAreaRight - 69) - playableAreaLeft + 1) + playableAreaLeft;
		Item item = new Item(megis, 25, 57, xPos, 0, false, true);
		items.add(item);
	}
	
}

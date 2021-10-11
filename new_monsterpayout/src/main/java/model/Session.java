package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import javafx.scene.image.Image;

public class Session {
	/*
	 * Tänne luokkaan tallennetaan "istunnon" tiedot, kuten tilaushistoria
	 */
	private static Order[] orders;
	
	private static Image[] avatarImages;
	
	public Session(Order[] orders) {
		Session.orders = orders;
	}
	
	public static void setOrders(Order[] orders) {
		Session.orders = orders;
	}
	
	public static Order[] getOrders() {
		return orders;
	}
	
	public static Image getAvatar(int index) {
		return avatarImages[index];
	}
	
	public static void loadAvatarImages() {
		//File path = new File("./src/main/resources/avatars");
		File path = new File("./src/main/resources/avatars");
		System.out.println(path);
		File[] allAvatarFiles = path.listFiles();
		/*
		 * Lajitellaan sijainnin tiedostot aakkos järjestykseen
		 */
		if(allAvatarFiles!=null) {
			Arrays.sort(allAvatarFiles);
			for(File file : allAvatarFiles) {
			    System.out.println(file);
			}
			System.out.println("Löytyi "+allAvatarFiles.length+" kuvaa");
			
			avatarImages = new Image[allAvatarFiles.length];
			
			for(int i = 0; i < allAvatarFiles.length; i++) {
				try {
					avatarImages[i] = new Image(new FileInputStream(allAvatarFiles[i]));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

}

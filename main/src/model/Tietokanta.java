package model;

import java.nio.charset.StandardCharsets;
import java.sql.*;

import com.google.common.hash.Hashing;

public class Tietokanta {
	
	final static String URL = "jdbc:mariadb://10.114.32.22:3306/kasino";
	final static String USERNAME = "remote";
	final static String PASSWORD = "remote";
	
	private static boolean loggedIn = false;
	
	public static void logout() {
		User.logout();
		loggedIn = false;
	}
	
	public static boolean login(String username, String password) {
		
		try {
			Connection con;
			con = DriverManager.getConnection(
					URL + "?user=" + USERNAME + "&password=" + PASSWORD);
			
			Statement stmt = con.createStatement();
			
			String passwordHash = Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString();
			
			//SQL syöttökutsu, tehdään Kayttaja tauluun uusi rivi
			String query = "INSERT INTO Kayttaja (Kayttajanimi, Salasana, Tilinumero, Sahkoposti, Firstname, Lastname) "
					+ "values ('Testikäyttäjä', SHA2('"+password+"',256), 'FI20 40 8950 1253 1250 20', 'testi@testi.fi', 'Mikko', 'Suomalainen')";
			
			stmt.executeQuery(query);
			
			//Tehdään SQL haku kutsu ja haetaan Testikäyttäjä/käyttäjät
			query = "SELECT KayttajaID, Kayttajanimi, Sahkoposti, Tilinumero, TiliID, Firstname, Lastname "
					+ "FROM Kayttaja WHERE Kayttajanimi = '"+ username +"' AND Salasana = '"+ passwordHash +"'";
			
			ResultSet rs = stmt.executeQuery(query);
			
			if(rs.next())
				if(rs.getString("Kayttajanimi") != null) {
					int tId = rs.getInt("KayttajaID");
					String tUsername = rs.getString("Kayttajanimi");
					String tFirstname = rs.getString("Firstname");
					String tLastname = rs.getString("Lastname");
					String tEmail = rs.getString("Sahkoposti");
					int tTiliId = rs.getInt("TiliID");
					query = "SELECT KolikkoSaldo, KrediittiSaldo FROM Tili "
							+ "WHERE TiliID = " + tTiliId;
					rs = stmt.executeQuery(query);
					if(rs.next()) {
						double tCredits = rs.getDouble("KrediittiSaldo");
						int tCoins = rs.getInt("KolikkoSaldo");
						User.setUserData(tId, tUsername, password,tFirstname, tLastname, tEmail, tTiliId, tCoins, tCredits);
						return loggedIn = true;
					}
					
				}
			
		} catch (SQLException e) {
			do {
				System.err.println("Viesti: "+e.getMessage());
				System.err.println("Virhekoodi: "+e.getErrorCode());
				System.err.println("SQL-tilakoodi: "+e.getSQLState());
			} while (e.getNextException() != null);
		}
		
		return loggedIn = false;
	}
	
	public static Product[] getProducts() {
		try {
			Connection con;
			con = DriverManager.getConnection(
					URL + "?user=" + USERNAME + "&password=" + PASSWORD);
			
			Statement stmt = con.createStatement();
			
			
			//SQL syöttökutsu, tehdään Kayttaja tauluun uusi rivi
			String query = "SELECT * FROM Tuote";
			
			stmt.executeQuery(query);
			
			ResultSet rs = stmt.executeQuery(query);
			int size = 0;
			if (rs.last()) {
			  size = rs.getRow();
			  rs.beforeFirst();
			  Product[] products = new Product[size];
			  while(rs.next()) {
				  System.out.println(rs.getRow()+"/"+size);
				  System.out.println("Tuotenumero: "+rs.getInt("Tuotenumero"));
				  System.out.println("Kuvaus: "+rs.getString("Kuvaus"));
				  System.out.println("Hinta: "+rs.getDouble("Hinta"));
				  System.out.println("Krediittien määrä: "+rs.getDouble("KrediittienMaara"));
				  System.out.println("Alennuskerroin: "+rs.getDouble("Alennuskerroin"));
				  products[rs.getRow()-1] = new Product(
						  rs.getInt("Tuotenumero"),
						  rs.getString("Kuvaus"),
						  rs.getDouble("Hinta"),
						  rs.getDouble("KrediittienMaara"),
						  rs.getDouble("Alennuskerroin"));
				}
			  return products;
			}
			
			
		} catch (SQLException e) {
			do {
				System.err.println("Viesti: "+e.getMessage());
				System.err.println("Virhekoodi: "+e.getErrorCode());
				System.err.println("SQL-tilakoodi: "+e.getSQLState());
			} while (e.getNextException() != null);
		}
		return null;
	}
	
	public static boolean register(String username, String password, String email, String firstname, String lastname) {
		/*
		 * Jos rekisteröinti onnistuu palauttaa metodi boolean arvon true, muuten false
		 */
		
		try {
			Connection con;
			con = DriverManager.getConnection(
					URL + "?user=" + USERNAME + "&password=" + PASSWORD);
			
			Statement stmt = con.createStatement();
			
			/*
			 * Jos käyttäjänimi on vapaa siirrytään if-lausekkeen sisään.
			 */
			if(checkUsername(username)) {
				/*
				 * Ennen uuden käyttäjän luomista luodaan uusi tili, 
				 * joka linkitetään uuteen käyttäjään.
				 */
				String query = "INSERT INTO Tili (KolikkoSaldo, KrediittiSaldo) "
						+ "VALUES (0,0)";
				/*
				 * Tehdään uuden tilin SQL-kutsu if lauseen sisällä. Jos 
				 * uuden tilin luonti onnistuu palauttaa executeUpdate uusien 
				 * rivien määrän. Jos luonti epäonnistuu uusia rivejä on 0
				 */
				
				if(stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS) > 0) {
					ResultSet rs = stmt.getGeneratedKeys();
					if(rs.next()) {
						int tiliID = rs.getInt(1);
						System.out.println(tiliID);
						query = "INSERT INTO Kayttaja (Kayttajanimi, Salasana, TiliID, Sahkoposti, Firstname, Lastname) "
								+ "values ('"+username+"', SHA2('"+password+"',256),'"+ tiliID +"', '"+email+"', '"+firstname+"', '"+lastname+"')";
						/*
						 * Ylempää if-lauseen perjaatetta jatkaen lähetetään käyttäjän luonnin SQL-kutsu 
						 * uuden if-lauseen sisällä ja jos käyttäjän luonti onnistuu palauttaa 
						 * executeUpdate metodi 0 suuremman luvun
						 */
						if(stmt.executeUpdate(query) > 0)
							return true;
					}
				}
			}
			
		} catch (SQLException e) {
			do {
				System.err.println("Viesti: "+e.getMessage());
				System.err.println("Virhekoodi: "+e.getErrorCode());
				System.err.println("SQL-tilakoodi: "+e.getSQLState());
			} while (e.getNextException() != null);
		}
		
		return false;
	}
	
	public static boolean checkUsername(String username) {
		/*
		 * Metodi palauttaa boolean arvon false jos käyttäjänimi on jo käytössä.
		 * True tarkoittaa ettei käyttäjänimellä ole vielä tehty käyttäjää.
		 */
		try {
			Connection con;
			con = DriverManager.getConnection(
					URL + "?user=" + USERNAME + "&password=" + PASSWORD);
			
			Statement stmt = con.createStatement();
			
			
			//SQL syöttökutsu, tehdään Kayttaja tauluun uusi rivi
			String query = "SELECT * FROM Kayttaja WHERE Kayttajanimi LIKE '"+username+"' LIMIT 1";
			
			stmt.executeQuery(query);
			
			ResultSet rs = stmt.executeQuery(query);
			
			if(rs.next())
				return false;
			
		} catch (SQLException e) {
			do {
				System.err.println("Viesti: "+e.getMessage());
				System.err.println("Virhekoodi: "+e.getErrorCode());
				System.err.println("SQL-tilakoodi: "+e.getSQLState());
			} while (e.getNextException() != null);
		}
		return true;
	}
	
	public static int decreaseKolikkoBalance(int amount) {
		
		/*
		 * Metodi ottaa parametreina Kayttaja-luokan joka sisältää 
		 * käyttäjän tiedot, jolloin voidaan varmistaa uudestaan, että 
		 * käyttäjällä on oikeat kirjautumis tunnukset ennen tiliin 
		 * käsiksi pääsyä.
		 * 
		 * Jos tililtä onnistutaan vähentämään pyydetyn määrän saldoa palauttaa 
		 * metodi tämän saldon määrän. Muuten 0.
		 */
		
		/*
		 * Jos vähennettävä saldo on 0 tai pienempi niin 
		 * lopetetaan metodin suorittaminen tähän ja palautetaan arvo 0
		 */
		if(amount <= 0)
			return 0;
		
		if(Tietokanta.isLogged() && User.getUsername() != null && User.getPassword() != null) {
			try {
				Connection con;
				con = DriverManager.getConnection(
						URL + "?user=" + USERNAME + "&password=" + PASSWORD);
				
				Statement stmt = con.createStatement();
				
				//Tehdään SQL haku kutsu ja haetaan Testikäyttäjä/käyttäjät
				String query = "SELECT TiliID "
						+ "FROM Kayttaja WHERE Kayttajanimi = '"+ User.getUsername() +"' AND Salasana = SHA2('"+ User.getPassword() +"',256)";

				ResultSet rs = stmt.executeQuery(query);
				
				/*
				 * Jos löytyy seuraava tulosjoukko on tietokannasta löytynyt käyttäjä
				 */
				if(rs.next()) {
					int tiliID = rs.getInt("TiliID");
					query = "SELECT KolikkoSaldo FROM Tili "
							+ "WHERE TiliID = "+tiliID;
					rs = stmt.executeQuery(query);
					/*
					 * Jos löytyy seuraava tulosjoukko löytyy tietokannasta käyttäjän tili
					 */
					if(rs.next()) {
						int saldo = rs.getInt("KolikkoSaldo");
						/*
						 * Verrataan käyttäjän tilin saldoa vähennettävään määrään. 
						 * Jos käyttäjän saldo riittää niin vähennetään tietokannasta 
						 * amount-muuttujan verran kolikko saldoa
						 */
						if(saldo >= amount) {
							query = "UPDATE Tili "
									+ "SET KolikkoSaldo = KolikkoSaldo - "+amount
											+ " WHERE TiliID = "+tiliID;
							int updatedRows = stmt.executeUpdate(query);
							query = "SELECT KolikkoSaldo FROM Tili "
									+ "WHERE TiliID = "+tiliID;
							rs = stmt.executeQuery(query);
							if(rs.next())
								User.setCoins(rs.getInt("KolikkoSaldo"));
							
							/*
							 * Jos SQL-kutsu muokkasi vähintään 1-riviä, 
							 * niin saldon vähennys kutsu on onnistunut.
							 */
							if(updatedRows > 0)
								return amount;
						}
					}
				}
				
			} catch (SQLException e) {
				do {
					System.err.println("Viesti: "+e.getMessage());
					System.err.println("Virhekoodi: "+e.getErrorCode());
					System.err.println("SQL-tilakoodi: "+e.getSQLState());
				} while (e.getNextException() != null);
			}
		}
		return 0;
	}
	
	public static int addCredits(double amount) {
		
		
		if(Tietokanta.isLogged() && User.getUsername() != null && User.getPassword() != null) {
			try {
				Connection con = DriverManager.getConnection(
						URL + "?user=" + USERNAME + "&password=" + PASSWORD);
				
				Statement stmt = con.createStatement();
				
				String query = "SELECT TiliID "
						+ "FROM Kayttaja WHERE Kayttajanimi = '"+ User.getUsername() +"' AND Salasana = SHA2('"+ User.getPassword() +"',256)";

				ResultSet rs = stmt.executeQuery(query);
				
				/*
				 * Jos löytyy seuraava tulosjoukko on tietokannasta löytynyt käyttäjä
				 */
				if(rs.next()) {
					int tiliID = rs.getInt("TiliID");
					query = "SELECT KrediittiSaldo FROM Tili "
							+ "WHERE TiliID = "+tiliID;
					rs = stmt.executeQuery(query);
					/*
					 * Jos löytyy seuraava tulosjoukko löytyy tietokannasta käyttäjän tili
					 */
					if(rs.next()) {
						double saldo = rs.getDouble("KrediittiSaldo");
						
						// Lisätään saldoa
						query = "UPDATE Tili "
								+ "SET KrediittiSaldo = KrediittiSaldo + "+amount
										+ " WHERE TiliID = "+tiliID;
						query = "SELECT KrediittiSaldo FROM Tili "
								+ "WHERE TiliID = "+tiliID;
						rs = stmt.executeQuery(query);
						if(rs.next())
							User.setCredits(rs.getDouble("KrediittiSaldo"));
						int updatedRows = stmt.executeUpdate(query);
						return updatedRows;
							
					}
				}
				
			} catch (SQLException e) {
				do {
					System.err.println("Viesti: "+e.getMessage());
					System.err.println("Virhekoodi: "+e.getErrorCode());
					System.err.println("SQL-tilakoodi: "+e.getSQLState());
				} while (e.getNextException() != null);
			}
		}
		return 0;
		
	}
	
	public static boolean createProduct(String description, double price, double creditAmount, double saleMultiplier) {
		if(Tietokanta.isLogged() && User.getUsername() != null && User.getPassword() != null) {
			try {
				Connection con = DriverManager.getConnection(
						URL + "?user=" + USERNAME + "&password=" + PASSWORD);
				
				Statement stmt = con.createStatement();
				
				String query = "SELECT Status "
						+ "FROM Kayttaja WHERE Kayttajanimi = '"+ User.getUsername() +"' AND Salasana = SHA2('"+ User.getPassword() +"',256)";

				ResultSet rs = stmt.executeQuery(query);
				
				/*
				 * Jos löytyy seuraava tulosjoukko on tietokannasta löytynyt käyttäjä statuksella 1 (admin)
				 */
				if(rs.next()) {
					if(rs.getInt("Status")==1) {
						query = "INSERT INTO Tuote (Kuvaus, Hinta, KrediittienMaara, Alennuskerroin)"
								+ "values('"+description+"',"+price+","+creditAmount+","+saleMultiplier+")";
						rs = stmt.executeQuery(query);
						/*
						 * Jos löytyy seuraava tulosjoukko on tietokantaan lisätty onnistuneesti
						 */
						if(rs.next()) {
							return true;
								
						}
					}
					
				}
				
			} catch (SQLException e) {
				do {
					System.err.println("Viesti: "+e.getMessage());
					System.err.println("Virhekoodi: "+e.getErrorCode());
					System.err.println("SQL-tilakoodi: "+e.getSQLState());
				} while (e.getNextException() != null);
			}
		}
		return false;
	}
	
	public static boolean editProduct(int productNumber, String description, double price, double creditAmount, double saleMultiplier) {
		if(Tietokanta.isLogged() && User.getUsername() != null && User.getPassword() != null) {
			try {
				Connection con = DriverManager.getConnection(
						URL + "?user=" + USERNAME + "&password=" + PASSWORD);
				
				Statement stmt = con.createStatement();
				
				String query = "SELECT Status "
						+ "FROM Kayttaja WHERE Kayttajanimi = '"+ User.getUsername() +"' AND Salasana = SHA2('"+ User.getPassword() +"',256)";

				ResultSet rs = stmt.executeQuery(query);
				
				/*
				 * Jos löytyy seuraava tulosjoukko on tietokannasta löytynyt käyttäjä statuksella 1 (admin)
				 */
				if(rs.next()) {
					if(rs.getInt("Status")==1) {
						query = "UPDATE Tuote SET "
								+ "Kuvaus = '"+description+"', "
								+ "Hinta = "+price+", "
								+ "KrediittienMaara = "+creditAmount+", "
								+ "Alennuskerroin = "+saleMultiplier+" "
								+ "WHERE Tuotenumero = "+productNumber;
						rs = stmt.executeQuery(query);

						/*
						 * Jos löytyy seuraava tulosjoukko on tietokantaan päivitetty tuote onnistuneesti
						 */
						if(rs.next()) {
							return true;
								
						}
					}
				}
				
			} catch (SQLException e) {
				do {
					System.err.println("Viesti: "+e.getMessage());
					System.err.println("Virhekoodi: "+e.getErrorCode());
					System.err.println("SQL-tilakoodi: "+e.getSQLState());
				} while (e.getNextException() != null);
			}
		}
		return false;
	}
	
	public static boolean deleteProduct(int productNumber) {
		if(Tietokanta.isLogged() && User.getUsername() != null && User.getPassword() != null) {
			try {
				Connection con = DriverManager.getConnection(
						URL + "?user=" + USERNAME + "&password=" + PASSWORD);
				
				Statement stmt = con.createStatement();
				
				String query = "SELECT Status "
						+ "FROM Kayttaja WHERE Kayttajanimi = '"+ User.getUsername() +"' AND Salasana = SHA2('"+ User.getPassword() +"',256)";

				ResultSet rs = stmt.executeQuery(query);
				
				/*
				 * Jos löytyy seuraava tulosjoukko on tietokannasta löytynyt käyttäjä statuksella 1 (admin)
				 */
				if(rs.next()) {
					if(rs.getInt("Status")==1) {
						query = "DELETE FROM Tuote WHERE Tuotenumero = "+productNumber;
						rs = stmt.executeQuery(query);
						/*
						 * Jos löytyy seuraava tulosjoukko on valittu tuote poistettu tietokannasta
						 */
						if(rs.next()) {
							return true;
						}
					}
					
				}
				
			} catch (SQLException e) {
				do {
					System.err.println("Viesti: "+e.getMessage());
					System.err.println("Virhekoodi: "+e.getErrorCode());
					System.err.println("SQL-tilakoodi: "+e.getSQLState());
				} while (e.getNextException() != null);
			}
		}
		return false;
	}
	
	public static boolean isLogged() {
		/*
		 * Tarkistetaan onko käyttäjä kirjautunut sisään
		 */
		return loggedIn;
	}
	
}

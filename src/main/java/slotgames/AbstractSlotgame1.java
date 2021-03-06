/*
 * @author Eljas Hirvelä
 */

package slotgames;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.scene.image.Image;
import model.Database;
import model.User;

/**
 * Abstrakti luokka slotti peleille.
 * Käynnistäessä uutta pyöräytystä kutsutaan ensiki
 * insertBet metodia ja jos se palauttaa arvon 'true'
 * kutsutaan spin metodia, joka palauttaa symboolien kuvat.
 * Tämän jälkeen haetaan voiton suuruus metodilla 'checkLines'
 * @author eljashirvela
 *
 */
public abstract class AbstractSlotgame1 {

	private double bet = 0;
	private final int bonus_symbols = 3;
	private final int scatter_symbols = 3;
	private final int[] scatter_freespins = {5,10,15};
	protected final String resourcePath = "./src/main/resources/slot_icons/";
	/*
	 * Symboolien määrä. Tässä otetaan sama määrä kuin Veikkauksen
	 * Kulta-Jaska 2 pelissä ja matkitaan symboolejen arvoa
	 *
	 * 0 = bonus
	 * 1 = scatter/freespin (3x=5, 4x=10, 5x = 15)
	 * 2 = wild
	 * 3 =
	 * 4 =
	 * 5 =
	 * 6 =
	 * 7 =
	 * 8 =
	 * 9 =
	 */
	protected SlotSymbol[] symbols;

	/*
	 * Metodilla ladataan symboolien kuvakkeet
	 */
	abstract void loadSymbols();

	/*
	 * Metodilla luodaan symboolit (asetetaan arvot ja kuvat kohdilleen)
	 */
	abstract void createSymbols();

	/*
	 * Pelin constructori hakee tähän muuttujaan parhaimman symboolin
	 * mahdollisia 5-wildin putkia varten
	 */
	private SlotSymbol highestPayer = null;

	/*
	 * Tehdään rivit.
	 * 5 riviä, kussakin rivissä 3 symboolia
	 */
	private SlotSymbol[][] rows = new SlotSymbol[3][5];

	/*
	 * Voittolinjat tähän 2-uloitteeseen tauluun.
	 */
	private int[][] win_lines = {
			{0,0,0,0,0},
			{1,1,1,1,1},
			{2,2,2,2,2},
			{0,0,1,0,0},
			{0,1,0,1,0},
			{0,1,1,1,0},
			{0,1,2,1,0},
			{1,0,0,0,1},
			{1,0,1,0,1},
			{1,1,0,1,1},
			{1,1,2,1,1},
			{1,2,1,2,1},
			{1,2,2,2,1},
			{2,2,1,2,2},
			{2,1,2,1,2},
			{2,1,1,1,2},
			{2,1,0,1,2}
	};

	private List<SlotSymbol> reel_of_symbols = new ArrayList<>();

	public AbstractSlotgame1() {
		try{
			/*
			 * Ladataan symboolit tiedostoista
			 */
			loadSymbols();
			/*
			 * Tehdään symboolit pelille
			 */
			createSymbols();
			}catch(Exception e) {
			fatalError();
		}
		/*
		 * Jokaista määritettyä symboolia asetetaan
		 * reel_of_symbols listaan niin monta kertaa, kuin
		 * symboolin probability luku on (määrittyy SlotSymbol-olion
		 * "setMultipliers" luokassa switch-casessa.
		 */
		for(SlotSymbol s : symbols) {
			for(int i = 0; i < s.getProbability(); i++)
				reel_of_symbols.add(s);
		}

		/*
		 * Haetaan suurin symbooli
		 */
		highestPayer = symbols[0];
		for(SlotSymbol s : symbols) {
			if(s.getMaxMultiplier()>highestPayer.getMaxMultiplier())
				highestPayer = s;
		}
	}

	/*
	 * Ennen spin metodin kutsua, pitäisi katsoa riittääkö
	 * pelaajalla krediitit pelaamiseen
	 */
	public boolean insertBet(double bet) {
		if(User.getCredits() < bet) {
			bet = 0;
			return false;
		}
		this.bet = bet;
		return true;
	}

	public Image[] spin() {
		Database.decreaseCreditBalance(bet);
		int imageCount = rows.length * rows[0].length;
		Image[] symbolImages = new Image[imageCount];
		Random rand = new Random();
		/*
		 * Syötetään kaksiuloitteeseen rows tauluun
		 * satunnaisesti "reel_of_symbols" listasta symbooleja
		 */
		for(SlotSymbol[] row : rows) {
			for(int i = 0; i < row.length; i++) {
				row[i] = reel_of_symbols.get(rand.nextInt(reel_of_symbols.size()));

			}
		}

		/*
		 * Haetaan kuvat tauluun joka palautetaan
		 * Kirjoitetaan näytölle testin vuoksi
		 */
		for(int i = 0, x = 0; i < rows.length; i++) {
			String s = "";
			for(int y = 0; y < rows[i].length; y++) {
				/*
				 * ÄLÄ POISTA TÄTÄ
				 */
				symbolImages[x]=rows[i][y].getNewImage();
				x++;

				/*
				 * DEBUGGAUSTA VARTEN
				 * SAA POISTAA
				 */
				s+=rows[i][y].getName()+"-";
			}
			System.out.println(s);
		}
		return symbolImages;
	}

	public double checkLines() {
		double multiply_bet = 0.0;
		int freespins = 0;
		boolean launch_bonus = false;

		/*
		 * Lasketaan Scatter ja Bonukset
		 */
		int bonus = 0;
		int scatter = 0;
		for(SlotSymbol[] row : rows) {
			for(int i = 0; i < rows.length; i++) {
				if(row[i].isBonus())
					bonus++;
				if(row[i].isScatter())
					scatter++;
			}
		}

		/*
		 * Laukastaanko bonus
		 */
		if(bonus>=bonus_symbols)
			launch_bonus = true;

		if(scatter>=scatter_symbols) {
			int freespins_index = 0;
			if(scatter>scatter_symbols) {
				int surplus = scatter-scatter_symbols;
				if(surplus>=scatter_freespins.length)
					freespins_index = scatter_freespins.length-1;
			}
			freespins = scatter_freespins[freespins_index];
		}

		/*
		 * Lasketaan voittorivit
		 */
		for(int[] line : win_lines) {
			/*
			 * Mikä symbooli on kyseessä
			 */
			SlotSymbol symbol = null;

			/*
			 * Kuinka monta samaa symboolia
			 * vierekkäin
			 */
			int connected = 0;

			/*
			 * Jos rivin ensimmäinen symbooli on
			 * wild, määritä symbooli vasta, kun
			 * rivillä tulee vastaan normaali symbooli.
			 * Wild-symbooli alusta loppuun = kovin symbooli
			 * (käy symbooli lista läpi ja valitse se, millä
			 * kovin kerroin)
			 */
			boolean first_is_wild = false;

			/*
			 * Skippaa bonus ja scatter symboolit.
			 * Laske näiden summa toisella tavalla
			 */
			for(int i = 1; i < rows[0].length; i++) {

				/*
				 * Poistutaan for silmukasta jos ensimmäinen symbooli on bonus tai scatter
				 */
				if(rows[line[i-1]][i-1].isBonus()||rows[line[i-1]][i-1].isScatter())
					break;

				/*
				 * Katsotaan onko ensimmäinen symbooli wild-symbooli ja
				 * asetetaan muuttujat sen mukaan.
				 */
				if(rows[line[i-1]][i-1].isWild()) {
					first_is_wild = true;
				} else {
					symbol=rows[line[i-1]][i-1];
				}

				if(symbol!=null) {
					if(symbol==rows[line[i]][i] || rows[line[i]][i].isWild()) {
						/*
						 * ilmoitetaan uudesta yhteydestä
						 */
						connected++;

						/*
						 * Jos ensimmäinen symbooli oli wild tai boolean on pysyny TRUE
						 * muodossa, koska seuraavat symboolit ovat myös olleet wildejä,
						 * yritetään selvittää saadaanko riville "perus" symbooli
						 */
						if(first_is_wild) {
							if(!rows[line[i]][i].isWild()) {
								symbol=rows[line[i]][i];
								first_is_wild = false;
							}
						}
					} else {
						break;
					}
				} else if (first_is_wild) {
					/*
					 * ilmoitetaan uudesta yhteydestä
					 */
					connected++;

					/*
					 * Jos ensimmäinen symbooli oli wild tai boolean on pysyny TRUE
					 * muodossa, koska seuraavat symboolit ovat myös olleet wildejä,
					 * yritetään selvittää saadaanko riville "perus" symbooli
					 */
					if(!rows[line[i]][i].isWild()) {
						symbol=rows[line[i]][i];
						first_is_wild = false;
					}
				} else {
					break;
				}
			}

			/*
			 * Jos voittorivillä osuu symboolit yhteen
			 */
			if(connected>0) {
				/*
				 * Jos first_is_wild boolean on pysynyt TRUE
				 * muodossa, on voittorivin kaikki symboolit
				 * wild-symbooleja, jolloin asetetaan voittosymbooliksi
				 * highestPayer
				 */
				if(first_is_wild)
					symbol = highestPayer;
				multiply_bet+=symbol.getMultipliers()[connected];
			}

			String text = "Connections: "+connected;
			if(symbol!=null)
				text+=" "+symbol.getName();
			System.out.println(text);
		}

		System.out.println("Bonus: "+bonus+"\nScatter: "+scatter);
		System.out.println("Panos kerrotaan: "+multiply_bet);
		if(launch_bonus)
			System.out.println("LAUNCH BONUS");
		if(freespins>0)
			System.out.println(freespins+" FREESPINS");

		double win = bet * multiply_bet;
		if(win>0f)
			Database.increaseCreditBalance(win);
		return win;
	}

	public void fatalError() throws NullPointerException {
		/*
		 * Tämä metodi heittäisi käyttäjälle ilmoituksen virheestä
		 * ja ohjelma palaa pois pelistä
		 */
		throw new NullPointerException("Virhe avatessa peliä.");
	}

	public void launchBonus() {

	}

}

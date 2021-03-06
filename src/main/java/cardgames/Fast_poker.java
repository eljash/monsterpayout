package cardgames;

import java.util.Arrays;

/**
 * Tämä luokka sisältää FastPoker pelin sisäisen (model) logiikan
 * @author Samuel Laisaar
 * @version 12.12.2021
 */
public class Fast_poker {

	private boolean play = false;
	private boolean doubles = false;
	private int winningHand;
	private DeckOfCards deck;

	public Fast_poker() {}

	public void makeDeck() {
		deck = new DeckOfCards();
	}

	/**
	 * Luodaan Cards luokasta 6 kortin pakka
	 * @return 6 Kortin array
	 */
	public Card[] take6() {
		Card[] cards  = new Card[6];
		for(int i = 0; i < cards.length; i++) {
			cards[i] = deck.takeCard();
		}
		return cards;
	}

	public boolean isPlay() {
		return play;
	}

	public void setPlay(Boolean play) {
		this.play = play;
	}

	public boolean isDoubles() {
		return doubles;
	}

	public void setDoubles(Boolean doubles) {
		this.doubles = doubles;
	}

	public void getWinningCards(Card[] wCards) {
		winningHand = checkWinnings(wCards);
	}

	public int getWinningHand() {
		return winningHand;
	}

	/**
	 * Tarkistetaan voittiko pelaaja tuplauksen
	 * @param wCards
	 * @param i
	 * @return boolean arvo, joka kertoo tuplauksen tuloksen
	 */
	public boolean checkDouble(Card[] wCards, int i) {
		if(wCards[i].getRank() == 1) { //Jos klikattu kortti on ässä, tuplauksen voittaa aina.
			return true;
		}
		if(wCards[0].getRank() == 1) {
			return i != 0 && wCards[i].getRank() == 14; //Jos tuplauksessa tulee ässä, niin on saatava ässä jotta tuplauksen voittaa.
		}
		else
			return i != 0 && wCards[i].getRank() >= wCards[0].getRank(); // muu kortti kuin ässä
	}

	/**
	 * Tarkistetaan kaikki mahdolliset pokerikädet.
	 * @param wCards
	 * @return kokonaisluku, joka kertoo voitetun pokerikäden
	 */
	public int checkWinnings(Card[] wCards) {
		Arrays.sort(wCards);
		for (Card wCard : wCards) {
			System.out.println("Sorted: " + wCard);
		}
		//Kuningasvärisuora
		if(flush(wCards) && royalStraight(wCards))
			return 8;
		//Värisuora
		else if(flush(wCards) && straight(wCards))
			return 7;
		//Neljä samaa
		else if(wCards[0].getRank() == wCards[1].getRank() && wCards[1].getRank() == wCards[2].getRank() && wCards[2].getRank() == wCards[3].getRank() ||
				wCards[1].getRank() == wCards[2].getRank() && wCards[2].getRank() == wCards[3].getRank() && wCards[3].getRank() == wCards[4].getRank())
			return 6;
		//Täyskäsi
		else if(wCards[0].getRank() == wCards[1].getRank() && wCards[1].getRank() == wCards[2].getRank() && wCards[3].getRank() == wCards[4].getRank() ||
				wCards[0].getRank() == wCards[1].getRank() && wCards[2].getRank() == wCards[3].getRank() && wCards[3].getRank() == wCards[4].getRank())
			return 5;
		//Väri
		else if(flush(wCards))
			return 4;
		//Suora
		else if(straight(wCards))
			return 3;
		//Kolmoset
		else if(wCards[0].getRank() == wCards[1].getRank() && wCards[1].getRank() == wCards[2].getRank() || wCards[1].getRank() == wCards[2].getRank() && wCards[2].getRank() == wCards[3].getRank() ||
				wCards[2].getRank() == wCards[3].getRank() && wCards[3].getRank() == wCards[4].getRank())
			return 2;
		//Kaks paria
		else if(wCards[0].getRank() == wCards[1].getRank() && wCards[2].getRank() == wCards[3].getRank() || wCards[1].getRank() == wCards[2].getRank() && wCards[3].getRank() == wCards[4].getRank() ||
				wCards[0].getRank() == wCards[1].getRank() && wCards[3].getRank() == wCards[4].getRank())
			return 1;
		//Pari
		else if((wCards[0].getRank() == wCards[1].getRank() && wCards[1].getRank() >= 10 || wCards[1].getRank() == 1) || (wCards[1].getRank() == wCards[2].getRank() && wCards[2].getRank() >= 10 || wCards[2].getRank() == 1) || (wCards[2].getRank() == wCards[3].getRank() && wCards[3].getRank() >= 10 || wCards[3].getRank() == 1) || (wCards[3].getRank() == wCards[4].getRank() && wCards[4].getRank() >= 10 || wCards[4].getRank() == 1))
			return 0;
		//ei voittoa
		else
			return -1;
	}

	private boolean straight(Card[] wCards) {
		if(wCards[0].getRank() != 1) {//Tarkistetaan onko korteissa ässää
			if(wCards[0].getRank()+1 == wCards[1].getRank() && wCards[1].getRank()+1 == wCards[2].getRank() && wCards[2].getRank()+1 == wCards[3].getRank() && wCards[3].getRank()+1 == wCards[4].getRank())
				return true;
		}
		else { //Jos ässä on suorassa. Eli kuningas suora
			return royalStraight(wCards);
		}
		return false;
	}

	private boolean royalStraight(Card[] wCards) {
		if(wCards[1].getRank()+1 == wCards[2].getRank() && wCards[2].getRank()+1 == wCards[3].getRank() && wCards[3].getRank()+1 == wCards[4].getRank() && wCards[4].getRank()+1 == wCards[0].getRank()+13)
			return true;
		return false;
	}

	private boolean flush(Card[] wCards) {
		if(wCards[0].getSuit() == wCards[1].getSuit() && wCards[1].getSuit() == wCards[2].getSuit() && wCards[2].getSuit() == wCards[3].getSuit() && wCards[3].getSuit() == wCards[4].getSuit())
			return true;
		return false;
	}
}
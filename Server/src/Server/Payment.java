package Server;

import java.time.ZonedDateTime;

public class Payment {
	String name;
	String surname;
	String adress;
	String cardNumber;
	int cvv;
	double payment;
	ZonedDateTime time;
	public String getName() {
		return name;
	}
	public void set(String name, String surname, String adress, String cardNumber, int cvv, double payment, ZonedDateTime time) {
		this.name = name;
		this.surname = surname;
		this.adress = adress;
		this.cardNumber = cardNumber;
		this.cvv = cvv;
		this.payment = payment;
		this.time = time;
	}
	public String getSurname() {
		return surname;
	}
	public String getAdress() {
		return adress;
	}
	public String getCardNumber() {
		return cardNumber;
	}
	public int getCvv() {
		return cvv;
	}	
	public double getPayment() {
		return payment;
	}
	public ZonedDateTime getTime() {
		return time;
	}
	
}

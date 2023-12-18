package Server;

public class RegisteredUser {
	String username;
	String password;
	String name;
	String surname;
	String jmbg;
	String cardNumber;
	String email;
	
	public void setRegUser(String username, String password, String name, String surname, String jmbg, String cardNumber, String email) {
		this.username = username;
		this.password = password;
		this.name = name;
		this.surname = surname;
		this.jmbg = jmbg;
		this.cardNumber = cardNumber;
		this.email = email;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getName() {
		return name;
	}

	public String getSurname() {
		return surname;
	}

	public String getJmbg() {
		return jmbg;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public String getEmail() {
		return email;
	}
	
	
	
}

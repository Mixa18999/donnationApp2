package Server;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;



public class ClientHandler extends Thread{

	Socket commsSocket = null;
	Socket fileTransportSocket = null;
	BufferedReader clientInputStream=null;
	PrintStream clientOutputStream=null;
	DataOutputStream dataOutStream = null;
	DataInputStream dataInStream = null;
	String dateFormat = "dd.MM.yyyy. HH:mm:ss";
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
	String username;
	String input;
	
	public ClientHandler(Socket commsSocket, Socket fileTransportSocket) {
		this.commsSocket = commsSocket;
		this.fileTransportSocket = fileTransportSocket;
	}
	
	public void run() {
	
		try {
			clientInputStream = new BufferedReader(new InputStreamReader(commsSocket.getInputStream()));
			clientOutputStream = new PrintStream(commsSocket.getOutputStream());
			dataInStream = new DataInputStream(fileTransportSocket.getInputStream());
			dataOutStream = new DataOutputStream(fileTransportSocket.getOutputStream());
			clientOutputStream.println("Konekcija je uspesno uspostavljena!");
			
			
			do{
				userMenu();
				input = clientInputStream.readLine();
				switch (input) {
				case "1": {
					String brKartice;
					String ime;
					String prezime;
					double iznos = 0;
					int cvv = 0;
					ZonedDateTime vremeUplate = ZonedDateTime.now();
					do {
						clientOutputStream.println("Unesite vase ime:");
						ime = clientInputStream.readLine();
					}while (ime.equals("") || !ime.matches("^[a-zA-Z]*$") || ime.length() < 1);
					do {
						clientOutputStream.println("Unesite vase prezime:");
						prezime = clientInputStream.readLine();
					}while (prezime.equals("") || !prezime.matches("^[a-zA-Z]*$") || prezime.length() < 1);
					clientOutputStream.println("Unesite vasu adresu:");
					String adresa = clientInputStream.readLine();
					do {
						clientOutputStream.println("Unesite broj vase platne kartice:");
						brKartice = clientInputStream.readLine();
						while (!isValidCardNumber(brKartice)) {
							clientOutputStream.println("Greska u unosu, broj kartice mora biti u formatu xxxx-xxxx-xxxx-xxxx. Pokusajte ponovo:");//TODO svedi na do-while
							clientOutputStream.println();
							brKartice = clientInputStream.readLine();
						}
						do {
							clientOutputStream.println("Unesite vas CVV broj[CVV broj mora imati tri cifre]:");
							String s = clientInputStream.readLine();
							if(s.matches("\\d+")) {
								cvv = Integer.parseInt(s);
							}
							else {
								clientOutputStream.println("CVV moze sadrzati samo cifre!");
							}
						}while(!numberHasThreeDigits(cvv));
					}while(!checkCardNumberCVV(brKartice, cvv));
					do {
						clientOutputStream.println("Unesite iznos za uplatu(Minimalan iznos je 200 dinara):");
						String unosIznosa = clientInputStream.readLine();
						if(unosIznosa.matches("[0-9.]+")) {
							iznos = Double.parseDouble(unosIznosa);
						}else {
							clientOutputStream.println("Iznos moze sadrzati samo cifre i tacku!");
						}
						
					}while(iznos<200);
					
					generatePayment(ime, prezime, adresa, brKartice, cvv, iznos, vremeUplate);
					Payment uplata = new Payment();
					uplata.set(ime, prezime, adresa, brKartice, cvv, iznos, vremeUplate);
					modifyPaymentDB(uplata);
					sendFile("D:/Program Files (x86)/Eclipse/workplace/Server/Uplata.txt");//NE PREMESTAJ!!!!
					break;
				}
				case "2":
					totalFunds();
					break;
				case "3":
					registration();
					break;
				case "4":
					logIn();
					break;
				case "5":{
					clientOutputStream.println("Dovidjenja");
					dataInStream.close();
					dataOutStream.close();
					break;
				}
				default:
					clientOutputStream.println("Neocekivan unos, izaberite jednu od ponudjenih opcija:");
				}

			}while(!input.equals("5"));
		} catch (IOException e) {
			System.out.println("Klijent je iznenada prekinuo vezu!");
		}
	}
	
	public void userMenu() {
		clientOutputStream.println("Izaberite zeljenu opciju:");
		clientOutputStream.println("1.Uplata novca u dobrotvorne svrhe");
		clientOutputStream.println("2.Iznos dosad prikupljenih sredstava");
		clientOutputStream.println("3.Registracija");
		clientOutputStream.println("4.Prijava");
		clientOutputStream.println("5.Izlaz");
	}
	
	private boolean isValidCardNumber(String cardNumber) {
		return cardNumber.matches("[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{4}");
	}

	public boolean numberHasThreeDigits(int number) {
		if(number>99 && number<1000)
			return true;
		else return false;
	}
	
	public boolean checkCardNumberCVV(String cardNumber, int cvv) {
		try (
			FileReader fR = new FileReader("CardDB.txt");
			BufferedReader in = new BufferedReader(fR)){
			boolean kraj = false;
			String s = "";
			while(!kraj) {
				String pom = in.readLine();
				if(pom==null) kraj=true;
				else s=pom;
				String[] separate = s.split(" ");
				if(separate[0].equals(cardNumber)&&Integer.parseInt(separate[1])==cvv) {
					return true;
				}
				else s="";
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		clientOutputStream.println("Broj kartice ne odgovara CVV broju! Izvrsite unos ponovo!");
		return false;
	}
	
	public void generatePayment(String name, String surname, String adress, String cardNumber, int cvv, double payment, ZonedDateTime time){
		try (FileWriter fOut = new FileWriter("Uplata.txt");
				BufferedWriter bwOut = new BufferedWriter(fOut);
				PrintWriter out = new PrintWriter(bwOut)){
			out.println("\t\t\t\t ------Potvrda o uplati u dobrotvorne svrhe------");
			out.println("\t\t\t\t\t\t "+ time.format(formatter));
			out.println("Ime: " + name);
			out.println("Prezime: " + surname);
			out.println("Adersa: " + adress );
			out.println("Broj kartice: " + cardNumber + "   CVV broj: " + cvv);
			out.println("Iznos uplate: " + String.format("%,.2f", payment) + " din");
			out.println("\t\t\t\t ------Hvala vam na donaciji------");
		} catch (Exception e) {
			System.out.println("Greska:" + e.getMessage());
		}
		
	}
	
	public void sendFile(String path) throws IOException {
        List<String> fileLines = Files.readAllLines(Paths.get(path));
        String fileContent = String.join("\n", fileLines);
        dataOutStream.writeUTF(fileContent);
        clientOutputStream.println("Uplatnica je poslata.");
	}
	
	public void modifyPaymentDB(Payment payment) {
		try (FileWriter fOut = new FileWriter("Payments.txt",true); ///try with resources
				BufferedWriter bwOut = new BufferedWriter(fOut);
				PrintWriter out = new PrintWriter(bwOut)){
			out.println(payment.getName()+";"+payment.getSurname()+";"+payment.getAdress()+";"+payment.getCardNumber()+";"+payment.getCvv()+";"+String.format("%,.2f", payment.getPayment())+";"+payment.getTime().format(formatter));
		} catch (Exception e) {
			System.out.println("Greska:" + e.getMessage());
		}
	}
	
	public void totalFunds() {
		double total = 0;
		try (
				FileReader fR = new FileReader("Payments.txt");
				BufferedReader in = new BufferedReader(fR)){
				boolean kraj = false;
				String s = "";
				while(!kraj) {
					String pom = in.readLine();
					if(pom==null) kraj=true;
					else {
						s=pom;
						String[] separate = s.split(";");
						total+=Double.parseDouble(separate[5]); 
					}
				}
				clientOutputStream.println("Ukupno je prikupljeno: " + String.format("%,.2f", total) + " dinara");
			} catch (FileNotFoundException e) {
				clientOutputStream.println("Nema prikupljenih novcanih sredstava.");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	}

	public void registration() throws IOException {//TODO hendluj exception
		String cardNumber;
		String jmbg;
		String name;
		String surname;
		String email;
		do {
			clientOutputStream.println("Unesite korisnicko ime:");
			username = clientInputStream.readLine();
		}while(!checkUsername(username));
		clientOutputStream.println("Unesite lozinku:");
		String password = clientInputStream.readLine();
		do {
			clientOutputStream.println("Unesite vase ime:");
			name = clientInputStream.readLine();
		}while (name.equals("") || !name.matches("^[a-zA-Z]*$") || name.length() < 1);
		do {
			clientOutputStream.println("Unesite vase prezime:");
			surname = clientInputStream.readLine();
		}while(surname.equals("") || !surname.matches("^[a-zA-Z]*$") || surname.length() < 1);
		do {
			clientOutputStream.println("Unesite vas JMBG[mora imati 13 brojeva]:");
			jmbg = clientInputStream.readLine();
		}while(!checkJMBG(jmbg));
		do {
			clientOutputStream.println("Unesite broj vase platne kartice u formatu [xxxx-xxxx-xxxx-xxxx], gde je x broj:");
			cardNumber = clientInputStream.readLine();
		}while (!(isValidCardNumber(cardNumber)&&checkCardNumberInDB(cardNumber)));
		do {
			clientOutputStream.println("Unesite vas email:");
			email = clientInputStream.readLine();
		}while(!email.matches("^(.+)@(.+)$"));
		RegisteredUser regUser = new RegisteredUser();
		regUser.setRegUser(username, password, name, surname, jmbg, cardNumber, email);
		modifyRegUsersDB(regUser);
		clientOutputStream.println("Uspesno ste se registrovali!");
	}

	public boolean checkUsername(String username) {
		try (
				FileReader fR = new FileReader("RegUsers.txt");
				BufferedReader in = new BufferedReader(fR)){
				boolean kraj = false;
				String s = "";
				while(!kraj) {
					String pom = in.readLine();
					if(pom==null) kraj=true;
					else {
						s=pom;
						String[] separate = s.split(";");
						if(separate[0].equals(username)) {
							clientOutputStream.println("Uneto korisnicko ime vec postoji!");
							return false;
						}
					}
				}
			} catch (FileNotFoundException e) {
				return true;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		return true;
	}
	
	public boolean checkJMBG(String jmbg) {
		return jmbg.matches("[0-9]{13}");
	}
	
	public void modifyRegUsersDB(RegisteredUser regUser) {
		try (FileWriter fOut = new FileWriter("RegUsers.txt",true); ///try with resources
				BufferedWriter bwOut = new BufferedWriter(fOut);
				PrintWriter out = new PrintWriter(bwOut)){
			out.println(regUser.getUsername()+";"+regUser.getPassword()+";"+regUser.getName()+";"+regUser.getSurname()+";"+regUser.getJmbg()+";"+regUser.getCardNumber()+";"+regUser.getEmail());
		} catch (Exception e) {
			System.out.println("Greska:" + e.getMessage());
		}
	}

	public void logIn() throws IOException {//TODO hendluj exception
		String username;
		String password;
		do {
			clientOutputStream.println("Unesite vase korisnicko ime:");
			username = clientInputStream.readLine();
			clientOutputStream.println("Unesite vasu lozinku:");
			password = clientInputStream.readLine();
		}while(!validation(username, password));
		//Menu for Registered users
		String inputRMenu;
		do {
			regUserMenu();
			inputRMenu = clientInputStream.readLine();
			switch(inputRMenu) {
				case "1":{
					double iznos = 0;
					int cvv = 0;
					ZonedDateTime vremeUplate = ZonedDateTime.now();
					RegisteredUser regUser = new RegisteredUser();
					regUser = returnRegUserParameters(username);
					clientOutputStream.println("Unesite vasu adresu:");
					String adresa = clientInputStream.readLine();
					do {
						do {
							clientOutputStream.println("Unesite vas CVV broj[CVV broj mora imati tri cifre]:");
							String s = clientInputStream.readLine();
							if(s.matches("\\d+")) {
								cvv = Integer.parseInt(s);
							}
							else {
								clientOutputStream.println("CVV moze sadrzati samo cifre!");
							}
						}while(!numberHasThreeDigits(cvv));
					}while(!checkCardNumberCVV(regUser.getCardNumber(), cvv));
					do {
						clientOutputStream.println("Unesite iznos za uplatu(Minimalan iznos je 200 dinara):");
						String unosIznosa = clientInputStream.readLine();
						if(unosIznosa.matches("[0-9.]+")) {
							iznos = Double.parseDouble(unosIznosa);
						}else {
							clientOutputStream.println("Iznos moze sadrzati samo cifre i tacku!");
						}
					}while(iznos<200);
				
					generatePayment(regUser.getName(), regUser.getSurname(), adresa, regUser.getCardNumber(), cvv, iznos, vremeUplate);
					Payment uplata = new Payment();
					uplata.set(regUser.getName(), regUser.getSurname(), adresa, regUser.getCardNumber(), cvv, iznos, vremeUplate);
					modifyPaymentDB(uplata);
					sendFile("D:/Program Files (x86)/Eclipse/workplace/Server/Uplata.txt");//NE PREMESTAJ!!!!
					break;
				}
				case "2":{
					totalFunds();
					break;
				}
				case "3":{
					lastTenPayments();
					break;
				}
				case "4":{
					break;
				}
				default:{
					clientOutputStream.println("Neocekivan unos, izaberite jednu od ponudjenih opcija:");
				}
		}
		}while(!inputRMenu.equals("4"));
	}
	
	public boolean validation(String username, String password) {
			try (
					FileReader fR = new FileReader("RegUsers.txt");
					BufferedReader in = new BufferedReader(fR)){
					boolean kraj = false;
					String s = "";
					while(!kraj) {
						String pom = in.readLine();
						if(pom==null) kraj=true;
						else {
							s=pom;
							String[] separate = s.split(";");
							if(separate[0].equals(username)&&separate[1].equals(password)) {
								return true;
							}
						}
					}
				} catch (FileNotFoundException e) {
					clientOutputStream.println("Nema registrovanih klijenata! Ne mozete da izvrsite prijavu dok se ne registrujete!");
					return false;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			return false;
	}

	public void regUserMenu() {
		clientOutputStream.println("Izaberite zeljenu opciju:");
		clientOutputStream.println("1.Uplata novca u dobrotvorne svrhe");
		clientOutputStream.println("2.Iznos dosad prikupljenih sredstava");
		clientOutputStream.println("3.Prikaz poslednjih deset uplata");
		clientOutputStream.println("4.Odjava");
	}
	
	public RegisteredUser returnRegUserParameters(String username) {
		RegisteredUser regUser = new RegisteredUser();
		try (
				FileReader fR = new FileReader("RegUsers.txt");
				BufferedReader in = new BufferedReader(fR)){
				boolean kraj = false;
				String s = "";
				while(!kraj) {
					String pom = in.readLine();
					if(pom==null) kraj=true;
					else {
						s=pom;
						String[] separate = s.split(";");
						if(separate[0].equals(username)) {
							regUser.setRegUser(username, separate[1], separate[2], separate[3], separate[4], separate[5], separate[6]);
						}
					}
				}
			} catch (FileNotFoundException e) {
				//TODO
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		return regUser;
	}

	public boolean checkCardNumberInDB(String cardNumber) {
		try (
			FileReader fR = new FileReader("CardDB.txt");
			BufferedReader in = new BufferedReader(fR)){
			boolean kraj = false;
			String s = "";
			while(!kraj) {
				String pom = in.readLine();
				if(pom==null) kraj=true;
				else s=pom;
				String[] separate = s.split(" ");
				if(separate[0].equals(cardNumber)) {
					return true;
				}
				else s="";
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		clientOutputStream.println("Uneli ste broj nepostojece kartice!");
		return false;
	}
	
	public void lastTenPayments() {
		try (
				FileReader fR = new FileReader("Payments.txt");
				BufferedReader in = new BufferedReader(fR);
				Stream<String> fileStream = Files.lines(Paths.get("Payments.txt"))){
				int n = (int) fileStream.count();
				String s = "";
				for(int i = 1;i<=n;i++) {
					if(n==0) {
						clientOutputStream.println("Nema uplata za prikaz!");
						break;
					}
					s = in.readLine();
					String[] separate = s.split(";");
					if(n>0 && n<=10) {//TODO proveri n=0
						clientOutputStream.println(i + ". Ime: " + separate[0] + " Prezime: " + separate[1] + " Datum i vreme uplate: " + separate[6] + " Iznos: " + separate[5]  + " dinara");
					}
					if(i>(n-10)) {
						clientOutputStream.println(i + ". Ime: " + separate[0] + " Prezime: " + separate[1] + " Datum i vreme uplate: " + separate[6] + " Iznos: " + separate[5] + " dinara");
					}
				}
				
			}catch (FileNotFoundException e) {
				clientOutputStream.println("Nema uplata za prikaz!");
			} 
			catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	}
}

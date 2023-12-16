package Server;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.List;
//import java.util.LinkedList;

public class ClientHandler extends Thread{

	Socket commsSocket = null;
	Socket fileTransportSocket = null;
	BufferedReader clientInputStream=null;
	PrintStream clientOutputStream=null;
	DataOutputStream dataOutStream = null;
	DataInputStream dataInStream = null;
	
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
			
			String input;
			do{
				userMenu();
				input = clientInputStream.readLine();
				switch (input) {
				case "1": {
					double iznos = 0;
					int cvv = 0;
					ZonedDateTime vremeUplate = ZonedDateTime.now();
					clientOutputStream.println("Unesite vase ime:");
					String ime = clientInputStream.readLine();
					clientOutputStream.println("Unesite vase prezime:");
					String prezime = clientInputStream.readLine();
					clientOutputStream.println("Unesite vasu adresu:");
					String adresa = clientInputStream.readLine();
					clientOutputStream.println("Unesite broj vase platne kartice:");
					String brKartice = clientInputStream.readLine();
					while (!isValidCardNumber(brKartice)) {
						clientOutputStream.println("Greska u unosu, broj kartice mora biti u formatu xxxx-xxxx-xxxx-xxxx. Pokusajte ponovo:");
						clientOutputStream.println();
						brKartice = clientInputStream.readLine();
					}
					do {
					clientOutputStream.println("Unesite vas CVV broj[CVV broj mora imati tri cifre]:");
					String s = clientInputStream.readLine();
					cvv = Integer.parseInt(s);//STA AKO UKUCA SLOVO!!!!!
					}while(!numberHasThreeDigits(cvv));
					do {
						clientOutputStream.println("Unesite iznos za uplatu(Minimalan iznos je 200 dinara):");
						iznos = Double.parseDouble(clientInputStream.readLine());
					}while(iznos<200);
					
					generatePayment(ime, prezime, adresa, brKartice, cvv, iznos, vremeUplate);
					dataInStream.close();
					dataOutStream.close();
					break;
				}
				case "4":{
					clientOutputStream.println("Dovidjenja");
					break;
				}
				default:
					throw new IllegalArgumentException("Unexpected value: " + input);
				}

			}while(!input.equals("4"));
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void userMenu() {
		clientOutputStream.println("Izaberite zeljenu opciju:");
		clientOutputStream.println("1.Uplata novca u dobrotvorne svrhe");
		clientOutputStream.println("4.Izlaz");
	}
	
	private boolean isValidCardNumber(String cardNumber) {
		return cardNumber.matches("[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{4}");
	}

	public boolean numberHasThreeDigits(int number) {
		if(number>99 && number<1000)
			return true;
		else return false;
	}
	
	public void generatePayment(String name, String surname, String adress, String cardNumber, int cvv, double payment, ZonedDateTime time) throws IOException {
		
		try (FileWriter fOut = new FileWriter("Uplata.txt");
				BufferedWriter bwOut = new BufferedWriter(fOut);
				PrintWriter out = new PrintWriter(bwOut)){
			out.println("\t\t\t\t ------Potvrda o uplati u dobrotvorne svrhe------");
			out.println("\t\t\t\t\t\t "+ time.getDayOfMonth()+"."+time.getMonthValue()+"."+time.getYear()+". godine"+ " " + time.getHour()+":"+time.getMinute()+":"+ time.getSecond());
			out.println("Ime: " + name);
			out.println("Prezime: " + surname);
			out.println("Adersa: " + adress );
			out.println("Broj kartice: " + cardNumber + "   CVV broj: " + cvv);
			out.println("Iznos uplate: " + payment + " din");
			out.println("\t\t\t\t ------Hvala vam na donaciji------");
		} catch (Exception e) {
			System.out.println("Greska:" + e.getMessage());
		}
		sendFile("D:/Program Files (x86)/Eclipse/workplace/Server/Uplata.txt");
	}
	
	public void sendFile(String path) throws IOException {
        List<String> fileLines = Files.readAllLines(Paths.get(path));
        String fileContent = String.join("\n", fileLines);
        dataOutStream.writeUTF(fileContent);
	}
}

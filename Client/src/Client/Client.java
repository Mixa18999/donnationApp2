package Client;

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
import java.net.UnknownHostException;

public class Client implements Runnable {

	static Socket commsSocket = null;
	static Socket transportSocket = null;
	static BufferedReader serverInputStream = null;
	static PrintStream serverOutputStream = null;
	static BufferedReader keyboardInput = null;
	static DataOutputStream dataOutStream = null;
	static DataInputStream dataInStream = null;
	static String input;

	
	
	public static void main(String[] args) {
		
		try {
			commsSocket = new Socket("localhost", 7272);
			transportSocket = new Socket("localhost", 7373);
			serverInputStream = new BufferedReader(new InputStreamReader(commsSocket.getInputStream()));
			serverOutputStream = new PrintStream(commsSocket.getOutputStream());
			keyboardInput = new BufferedReader(new InputStreamReader(System.in));
			dataInStream = new DataInputStream(transportSocket.getInputStream());
			dataOutStream = new DataOutputStream(transportSocket.getOutputStream());
			new Thread(new Client()).start();
			while(true) {
				input = serverInputStream.readLine();
				System.out.println(input);
				if (input.equals("Dovidjenja")) {
					break;
				}
			}
			receiveFile();
			dataInStream.close();
			dataOutStream.close();
			transportSocket.close();
			commsSocket.close();
		} catch (UnknownHostException e) {
			System.out.println("Nepoznat host!");
		} catch (IOException e) {
			System.out.println("Server je pao!");
		}
		
		
	}

	
	public void run() {
		String poruka;

		while (true) {

			try {
				poruka = keyboardInput.readLine();
				
				serverOutputStream.println(poruka);

				if (poruka.equals("4")) {
					break;
				}
			} catch (IOException e) {
				System.out.println("Greska pri unosu!");
			}
		}
		
	}
	
	public static void receiveFile() throws IOException{
			String fileContent = dataInStream.readUTF();
			try (FileWriter fOut = new FileWriter("Uplata.txt");
					BufferedWriter bwOut = new BufferedWriter(fOut);
					PrintWriter out = new PrintWriter(bwOut)){
				out.println(fileContent);
			} catch (Exception e) {
				System.out.println("Greska:" + e.getMessage());
			}
	}

}

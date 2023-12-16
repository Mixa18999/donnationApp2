package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
//import java.util.LinkedList;

public class Server {
	//public static LinkedList<ClientHandler> listOfClients = new LinkedList<ClientHandler>();
	public static void main(String[] args) {
		int portNumber = 7272;
		int portFileNumber = 7373;
		ServerSocket serverSocket = null;
		ServerSocket serverSocketFile = null;
		Socket commsSocket = null;
		Socket fileTransportSocket = null;
		
		try {
			serverSocket = new ServerSocket(portNumber);
			serverSocketFile = new ServerSocket(portFileNumber);
			while(true) {
				System.out.println("Cekam na konekciju....");
				commsSocket = serverSocket.accept();
				fileTransportSocket = serverSocketFile.accept();
				System.out.println("Konekcija je uspostavljena!");
				ClientHandler newClient = new ClientHandler(commsSocket,fileTransportSocket);	
				//listOfClients.add(newClient);
				newClient.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}

package org.apache.jmeter.visualizers.backend.rest;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ResultsServer {
	public void startServer () {
		try {
			@SuppressWarnings("resource")
			ServerSocket serverSocket = new ServerSocket(4207);
			while(true){
				Socket socket = serverSocket.accept();
				new HttpServer(socket).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

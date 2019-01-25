/*******************************************************************************
 * The MIT License
 *
 * Copyright (c) 2018 knokko
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *  
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *******************************************************************************/
package nl.knokko.bo.server.auth;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import nl.knokko.bo.server.auth.data.AuthDataManager;
import nl.knokko.util.random.CrazyRandom;
import nl.knokko.util.random.PseudoRandom;
import nl.knokko.util.random.Random;

public class AuthServer {

	private static AuthConsole console;
	private static AuthWebServer webServer;
	private static AuthTCPServer tcpServer;
	private static AuthDataManager dataManager;

	private static Random simpleRandom;
	private static CrazyRandom crazyRandom;

	public static void main(String[] args) {
		console = new AuthConsole();
		dataManager = new AuthDataManager(new File("auth"));
		simpleRandom = new PseudoRandom(PseudoRandom.Configuration.LEGACY);
		dataManager.load();
		new Thread(console).start();
		// TODO add timer to update realm states and reset failed login attempts
		if (args.length > 0) {
			try {
				startConnection(InetAddress.getLocalHost().getHostAddress(), Integer.parseInt(args[0]),
						Integer.parseInt(args[1]));
			} catch (UnknownHostException ex) {
				console.println("Can't resolve own host address");
			}
		}
	}

	public static void startConnection(String host, int webPort, int tcpPort) {
		console.println("Host address is " + host);
		webServer = new AuthWebServer(host, webPort);
		tcpServer = new AuthTCPServer();
		new Thread(webServer).start();
		tcpServer.start(tcpPort);
	}

	public static AuthConsole getConsole() {
		return console;
	}

	public static AuthWebServer getWebServer() {
		return webServer;
	}

	public static AuthTCPServer getTCPServer() {
		return tcpServer;
	}

	public static AuthDataManager getDataManager() {
		return dataManager;
	}

	public static Random getSimpleRandom() {
		return simpleRandom;
	}

	public static CrazyRandom getRandom() {
		return crazyRandom;
	}

	public static void setCrazyRandom(CrazyRandom random) {
		if (crazyRandom == null)
			crazyRandom = random;
		else
			throw new IllegalStateException("The CrazyRandom has been set already");
	}

	public static void stop() {
		console.println("Stopping server...");
		dataManager.save();
		try {
			webServer.stop();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		tcpServer.stop();
		console.setStopping();
	}
}
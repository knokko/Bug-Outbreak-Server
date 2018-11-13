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

import java.net.InetAddress;
import java.net.UnknownHostException;
import nl.knokko.bo.server.auth.data.UserData;
import nl.knokko.util.console.Console;

public class AuthConsole extends Console {

	@Override
	protected void stop() {
		AuthServer.stop();
	}

	@Override
	protected void executeCommand(String[] command) {
		if (command.length == 0) {
			println("Empty command?");
			return;
		}
		if (command[0].equals("stop") || command[0].equals("exit")) {
			AuthServer.stop();
		} else if (command[0].equals("start")) {
			if (AuthServer.getWebServer() == null) {
				if (command.length == 3) {// port number on localhost
					try {
						int webPort = Integer.parseInt(command[1]);
						try {
							int tcpPort = Integer.parseInt(command[2]);
							String hostName = InetAddress.getLocalHost().getHostAddress();
							AuthServer.startConnection(hostName, webPort, tcpPort);
						} catch (NumberFormatException nfe) {
							println("Couldn't parse tcp port number (" + command[2] + ")");
						} catch (UnknownHostException ex) {
							println("Can't resolve own host address: " + ex.getMessage());
						}
					} catch (NumberFormatException nfe) {
						println("Couldn't parse web port number (" + command[1] + ")");
					}
				} else {
					println("Use start <web port> <tcp port>");
				}
			} else {
				println("The connection has already started");
			}
		} else if (command[0].equals("setProfileIP")) {
			if (command.length > 1) {
				try {
					byte[] address = new byte[command.length - 1];
					for (int index = 0; index < address.length; index++) {
						int b = Integer.parseInt(command[index + 1]);
						if (b >= 0 && b < 256) {
							address[index] = (byte) b;
						} else {
							println("Byte " + index + " is out of range: " + b);
							return;
						}
					}
					AuthServer.getDataManager().setProfileIP(address);
				} catch (NumberFormatException nfe) {
					println("At least one of the bytes of the address couldn't be parsed: " + nfe.getMessage());
				}
			} else {
				println("Use setProfileIP <address...>");
			}
		} else if (command[0].equals("addRealm")) {
			if (command.length > 2) {
				if (AuthServer.getDataManager().getRealm(command[1]) != null) {
					println("There is already a realm with name " + command[1]);
					return;
				}
				try {
					byte[] address = new byte[command.length - 2];
					for (int index = 0; index < address.length; index++) {
						int b = Integer.parseInt(command[index + 2]);
						if (b >= 0 && b < 256) {
							address[index] = (byte) b;
						} else {
							println("Byte " + index + " is out of range: " + b);
							return;
						}
					}
					AuthServer.getDataManager().addRealm(command[1], address);
				} catch (NumberFormatException nfe) {
					println("At least one of the bytes of the address couldn't be parsed: " + nfe.getMessage());
				}
			} else {
				println("Use addRealm <name> <address...>");
			}
		} else if (command[0].equals("promote")) {
			if (command.length == 2) {
				try {
					long id = Long.parseLong(command[1]);
					UserData data = AuthServer.getDataManager().getUserData(id);
					if (data != null) {
						if (!data.isOP()) {
							data.setOP(true);
							println("Promoted " + data.getUsername());
						} else {
							println(data.getUsername() + " was already an operator");
						}
					} else {
						println("There is no account with id " + id);
					}
				} catch (NumberFormatException nfe) {
					println("Couldn't parse the id (" + command[1] + ")");
				}
			} else {
				println("Use promote <id>");
			}
		} else if (command[0].equals("demote")) {
			if (command.length == 2) {
				try {
					long id = Long.parseLong(command[1]);
					UserData data = AuthServer.getDataManager().getUserData(id);
					if (data != null) {
						if (data.isOP()) {
							data.setOP(false);
							println("Demoted " + data.getUsername());
						} else {
							println(data.getUsername() + " was no operator");
						}
					} else {
						println("There is no account with id " + id);
					}
				} catch (NumberFormatException nfe) {
					println("Couldn't parse the id (" + command[1] + ")");
				}
			} else {
				println("Use demote <id>");
			}
		} else if (command[0].equals("terminate")) {
			println("Terminating server...");
			System.exit(0);
		} else {
			println("Unknown command");
		}
	}

	void setStopping() {
		stopping = true;
	}
}
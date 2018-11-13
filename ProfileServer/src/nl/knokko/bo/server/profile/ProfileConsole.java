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
package nl.knokko.bo.server.profile;

import java.net.InetAddress;
import java.net.UnknownHostException;
import nl.knokko.util.console.Console;

public class ProfileConsole extends Console {

	@Override
	protected void stop() {
		ProfileServer.stop();
	}

	@Override
	protected void executeCommand(String[] command) {
		if (command[0].equals("start")) {
			if (ProfileServer.getWebServer() == null) {
				if (command.length == 4) {
					try {
						int ownWebPort = Integer.parseInt(command[1]);
						try {
							int authTCPPort = Integer.parseInt(command[3]);
							try {
								String host = InetAddress.getLocalHost().getHostAddress();
								ProfileServer.startConnection(host, ownWebPort, command[2], authTCPPort);
							} catch (UnknownHostException uhe) {
								println("Can't find own host name");
							}
						} catch (NumberFormatException nfe) {
							println("Can't parse auth tcp port number");
						}
					} catch (NumberFormatException nfe) {
						println("Can't parse own web port number");
					}
				} else {
					println("Use start <web port> <auth host> <auth tcp port>");
				}
			} else {
				println("The connection has already started");
			}
		} else if (command[0].equals("stop") || command[0].equals("exit")) {
			stop();
		} else if (command[0].equals("terminate")) {
			System.exit(0);
		}
	}

	void setStopping() {
		stopping = true;
	}
}
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
package nl.knokko.bo.server.auth.protocol.tcp;

import java.util.Arrays;

import nl.knokko.bo.server.auth.AuthServer;
import nl.knokko.bo.server.auth.AuthTCPServer;
import nl.knokko.bo.server.auth.AuthTCPServer.State;
import nl.knokko.bo.server.protocol.AuthProfileCode.StC;
import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitOutput;
import nl.knokko.util.protocol.BitProtocol;
import static nl.knokko.bo.server.auth.protocol.tcp.ProtocolProfileStart.denyStart;

public class ProtocolProfileVerify implements BitProtocol<AuthTCPServer.Handler> {

	@Override
	public void process(BitInput input, AuthTCPServer.Handler handler) {
		if (handler.getState().getState() == State.STATE_PROFILE_LOGGING_IN) {
			System.out.println("The profile server tries to verify itself.");
			synchronized (AuthServer.getDataManager().getProfileLock()) {
				if (AuthServer.getDataManager().getProfileServer() != null) {
					if (Arrays.equals(AuthServer.getDataManager().getProfileServer().getIP(),
							handler.getClient().getInetAddress().getAddress())) {
						if (!AuthServer.getDataManager().getProfileServer().isOnline()) {
							int[] tempHasher = handler.getState().getTempHasher();
							int[] fromClient = input.readInts(50);
							// TODO fix this later
							/*
							HashResult fromServer = ServerHasher
									.tempHash(AuthServer.getDataManager().getProfileServer().getPassword(), tempHasher);
							if (Arrays.equals(fromClient, fromServer.get())) {
								int port = input.readChar();// port range is the same as char value range
								AuthServer.getDataManager().getProfileServer().setOnline(port);
								AuthServer.getTCPServer().getSpeaker().setProfileHandler(handler);
								BitOutput output = handler.createOutput();
								output.addNumber(StC.APPROVE_START, StC.BITCOUNT, false);
								AuthServer.getConsole().println("The profile server connected succesfully");
								handler.getState().clearTempHasher();
								handler.getState().setState(State.STATE_PROFILE_LOGGED_IN);
								output.terminate();
							} else {
								denyStart(handler, StC.RefuseStart.WRONG_PASSWORD);
							}
							*/
						} else {
							denyStart(handler, StC.RefuseStart.ALREADY_STARTED);
						}
					} else {
						denyStart(handler, StC.RefuseStart.WRONG_IP);
					}
				} else {
					denyStart(handler, StC.RefuseStart.NO_PROFILE_SERVER);
				}
			}
		} else {
			handler.stop("Tried to finish profile login from wrong state");
		}
	}
}
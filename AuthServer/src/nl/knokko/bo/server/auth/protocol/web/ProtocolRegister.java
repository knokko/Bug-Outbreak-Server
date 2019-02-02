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
package nl.knokko.bo.server.auth.protocol.web;

import nl.knokko.bo.server.auth.AuthServer;
import nl.knokko.bo.server.auth.AuthWebServer;
import nl.knokko.bo.server.auth.AuthWebServer.State;
import nl.knokko.bo.server.auth.data.IPData;
import nl.knokko.bo.server.auth.data.UserData;
import nl.knokko.bo.server.auth.protocol.web.ConnectionCode.StC;
import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitOutput;
import nl.knokko.util.protocol.BitProtocol;

public class ProtocolRegister implements BitProtocol<AuthWebServer.Handler> {

	public void process(BitInput input, AuthWebServer.Handler handler) {
		
		// Only allow registration if the client comes from the nothing state
		if (handler.getState().getAuthState() == State.AUTH_STATE_NOTHING) {
			BitOutput output = handler.createOutput();
			
			// No funny concurrency
			synchronized (AuthServer.getDataManager().getRegisterLock()) {
				IPData ipData = AuthServer.getDataManager().getIPData(handler.getAddress());
				
				// Only 10 accounts per address
				if (ipData.getCreatedAccounts() < 10) {
					String username = input.readString(ConnectionCode.MAX_USERNAME_LENGTH);
					
					// Only 1 account per username
					if (AuthServer.getDataManager().getUserData(username) == null) {
						
						// Read the data from the client
						String salt = input.readString(ConnectionCode.MAX_USERNAME_LENGTH + 20);
						byte[] testPayload = input.readBytes(300);
						int[] serverStartSeed = input.readInts(24);
						int[] clientSessionSeed = input.readInts(24);
						int[] serverSessionSeed = input.readInts(24);
						
						// Register account
						AuthServer.getDataManager().register(new UserData(username, salt, testPayload, serverStartSeed, clientSessionSeed, serverSessionSeed, handler.getAddress()));
						ipData.increaseCreatedAccounts();
						
						// Reset auth state so that the user can log in with the new account
						handler.getState().clearAuthState();
						output.addNumber(StC.REGISTER, StC.BITCOUNT, false);
						output.terminate();
					} else {
						
						// Name is already in use
						output.addNumber(StC.REGISTER_FAILED, StC.BITCOUNT, false);
						output.addNumber(StC.RegisterFail.NAME_IN_USE, StC.RegisterFail.BITCOUNT, false);
						output.terminate();
						handler.getState().clearAuthState();
					}
				} else {
					
					// Already maximum number of accounts on this ip address
					output.addNumber(StC.REGISTER_FAILED, StC.BITCOUNT, false);
					output.addNumber(StC.RegisterFail.IP_LIMIT_EXCEEDED, StC.RegisterFail.BITCOUNT, false);
					output.terminate();
					handler.getState().clearAuthState();
				}
			}
		} else {
			handler.uglyStop("Registering with active auth state");
		}
	}
}
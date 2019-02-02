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
import nl.knokko.bo.server.auth.data.UserData;
import nl.knokko.bo.server.auth.protocol.web.ConnectionCode.StC;
import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitOutput;
import nl.knokko.util.protocol.BitProtocol;

public class ProtocolLogin1 implements BitProtocol<AuthWebServer.Handler> {

	@Override
	public void process(BitInput input, AuthWebServer.Handler handler) {
		if (handler.getState().getAuthState() == State.AUTH_STATE_NOTHING) {
			String username = input.readString(ConnectionCode.MAX_USERNAME_LENGTH);
			UserData account = AuthServer.getDataManager().getUserData(username);
			BitOutput output = handler.createOutput();
			if (account != null) {
				synchronized (account) {
					if (!account.isLoggedIn()) {
						if (account.getFailedAttempts() >= 120 && AuthServer.getDataManager()
								.getIPData(handler.getAddress()).getFailedLoginAttempts() > 2) {
							output.addNumber(StC.LOGIN_1_FAILED, StC.BITCOUNT, false);
							output.addNumber(StC.LoginFail1.UNDER_ATTACK, StC.LoginFail1.BITCOUNT, false);
							output.terminate();
						} else {
							int[] halfServerSeed = input.readInts(24);
							int[] halfClientSeed = AuthServer.getSimpleRandom().nextInts(24);
							output.addNumber(StC.LOGIN_1, StC.BITCOUNT, false);
							output.addString(account.getSalt());
							output.addInts(halfClientSeed);
							output.terminate();
							handler.getState().setLoggingIn(account.getID(), halfClientSeed, halfServerSeed);
						}
					} else {
						output.addNumber(StC.LOGIN_1_FAILED, StC.BITCOUNT, false);
						output.addNumber(StC.LoginFail1.ALREADY_LOGGED_IN, StC.LoginFail1.BITCOUNT, false);
						output.terminate();
					}
				}
			} else {
				output.addNumber(StC.LOGIN_1_FAILED, StC.BITCOUNT, false);
				output.addNumber(StC.LoginFail1.NO_USERNAME, StC.LoginFail1.BITCOUNT, false);
				output.terminate();
			}
		} else {// should only happen with a corrupted client
			handler.uglyStop("Tried to log in with auth state " + handler.getState().getAuthState());
		}
	}
}
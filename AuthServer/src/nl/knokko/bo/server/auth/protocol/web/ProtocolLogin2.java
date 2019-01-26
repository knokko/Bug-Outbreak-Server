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
import nl.knokko.util.hashing.result.HashResult;
import nl.knokko.util.hashing.Hasher;
import nl.knokko.util.protocol.BitProtocol;

public class ProtocolLogin2 implements BitProtocol<AuthWebServer.Handler> {

	@Override
	public void process(BitInput input, AuthWebServer.Handler handler) {
		if (handler.getState().getAuthState() == State.AUTH_STATE_LOGGING_IN) {
			HashResult tempHashResult = new HashResult(input.readInts(20));
			int[] encryptor = input.readInts(20);
			UserData account = AuthServer.getDataManager().getUserData(handler.getState().getAccountID());
			BitOutput output = handler.createOutput();
			synchronized (account) {
				if (!account.isLoggedIn()) {// someone else managed to log in between the first and second login message
					HashResult encryptedClientHashResult = account.getEncryptedHash();
					HashResult clientHashResult = Hasher.decrypt(encryptedClientHashResult, encryptor);
					int[] tempHasher = handler.getState().getTempHasher();
					HashResult tempHashResult2 = Hasher.tempHash(clientHashResult, tempHasher[0], tempHasher[1],
							tempHasher[2], tempHasher[3]);
					if (tempHashResult.equals(tempHashResult2)) {// if these 2 are equal, the client logged in
						handler.getState().setLoggedIn();
						account.setLoggedIn();
						output.addNumber(StC.LOGIN_2, StC.BITCOUNT, false);
						output.addBoolean(account.isOP());
						output.terminate();
					} else {
						handler.getState().clearAuthState();
						AuthServer.getDataManager().getIPData(handler.getAddress()).increaseFailedAttempts();
						output.addNumber(StC.LOGIN_2_FAILED, StC.BITCOUNT, false);
						output.addNumber(StC.LoginFail2.WRONG_PASSWORD, StC.LoginFail2.BITCOUNT, false);
						output.terminate();
					}
				} else {
					handler.getState().clearAuthState();
					output.addNumber(StC.LOGIN_2_FAILED, StC.BITCOUNT, false);
					output.addNumber(StC.LoginFail2.ALREADY_LOGGED_IN, StC.LoginFail2.BITCOUNT, false);
					output.terminate();
				}
			}
		} else {// corrupted client
			handler.uglyStop("Skipped first part of login");
		}
	}
}
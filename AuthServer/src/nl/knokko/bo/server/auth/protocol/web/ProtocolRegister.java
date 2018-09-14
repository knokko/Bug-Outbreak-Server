/* 
 * The MIT License
 *
 * Copyright 2018 20182191.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
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
 */
package nl.knokko.bo.server.auth.protocol.web;

import nl.knokko.bo.server.auth.AuthServer;
import nl.knokko.bo.server.auth.AuthWebServer;
import nl.knokko.bo.server.auth.AuthWebServer.State;
import nl.knokko.bo.server.auth.data.IPData;
import nl.knokko.bo.server.auth.data.UserData;
import nl.knokko.bo.server.auth.protocol.web.ConnectionCode.StC;
import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitOutput;
import nl.knokko.util.hashing.HashResult;
import nl.knokko.util.protocol.BitProtocol;

public class ProtocolRegister implements BitProtocol<AuthWebServer.Handler> {

	public void process(BitInput input, AuthWebServer.Handler handler) {
		if(handler.getState().getAuthState() == State.AUTH_STATE_NOTHING){
			BitOutput output = handler.createOutput();
			synchronized(AuthServer.getDataManager().getRegisterLock()){
				IPData ipData = AuthServer.getDataManager().getIPData(handler.getAddress());
				if(ipData.getCreatedAccounts() < 10){
					String username = input.readJavaString(ConnectionCode.MAX_USERNAME_LENGTH);
					if(AuthServer.getDataManager().getUserData(username) == null){
						String salt = input.readJavaString(ConnectionCode.MAX_USERNAME_LENGTH + 20);
						HashResult encryptedClientHashResult = new HashResult(input.readInts(20));
						UserData account = new UserData(username, salt, encryptedClientHashResult, handler.getAddress());
						AuthServer.getDataManager().register(account);
						ipData.increaseCreatedAccounts();
						handler.getState().setRegistered(account.getID());
						output.addNumber(StC.REGISTER, StC.BITCOUNT, false);
						output.terminate();
					}
					else {
						output.addNumber(StC.REGISTER_FAILED, StC.BITCOUNT, false);
						output.addNumber(StC.RegisterFail.NAME_IN_USE, StC.RegisterFail.BITCOUNT, false);
						output.terminate();
						handler.getState().clearAuthState();
					}
				}
				else {
					output.addNumber(StC.REGISTER_FAILED, StC.BITCOUNT, false);
					output.addNumber(StC.RegisterFail.IP_LIMIT_EXCEEDED, StC.RegisterFail.BITCOUNT, false);
					output.terminate();
					handler.getState().clearAuthState();
				}
			}
		}
		else {
			handler.uglyStop("Registering with active auth state");
		}
	}
}
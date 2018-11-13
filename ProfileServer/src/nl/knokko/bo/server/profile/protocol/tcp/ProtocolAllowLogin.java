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
package nl.knokko.bo.server.profile.protocol.tcp;

import nl.knokko.bo.server.profile.ProfileServer;
import nl.knokko.bo.server.profile.ProfileTCPClient;
import nl.knokko.bo.server.profile.ProfileTCPClient.State;
import nl.knokko.bo.server.protocol.AuthProfileCode.CtS;
import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitOutput;
import nl.knokko.util.protocol.BitProtocol;
import nl.knokko.util.socket.client.TCPClientSocket;

public class ProtocolAllowLogin implements BitProtocol<TCPClientSocket<ProfileTCPClient.State>> {

	@Override
	public void process(BitInput input, TCPClientSocket<ProfileTCPClient.State> handler) {
		if (handler.getState().getAuthState() == State.AUTH_STATE_COMPLETE) {
			long userID = input.readLong();
			byte[] clientAddress = input.readByteArray();
			int[] loginKey = input.readInts(8);
			if (!ProfileServer.getDataManager().getUserData(userID).isLoggedIn()) {
				if (ProfileServer.getWebServer().getLoginManager().allowLogin(userID, clientAddress, loginKey)) {
					BitOutput output = handler.createOutput();
					output.addNumber(CtS.WILL_ALLOW_LOGIN, CtS.BITCOUNT, false);
					output.addLong(userID);
					output.terminate();
				} else {
					BitOutput output = handler.createOutput();
					output.addNumber(CtS.WONT_ALLOW_LOGIN, CtS.BITCOUNT, false);
					output.addLong(userID);
					output.addNumber(CtS.RefuseLogin.ALREADY_WAITING, CtS.RefuseLogin.BITCOUNT, false);
					output.terminate();
				}
			} else {
				BitOutput output = handler.createOutput();
				output.addNumber(CtS.WONT_ALLOW_LOGIN, CtS.BITCOUNT, false);
				output.addLong(userID);
				output.addNumber(CtS.RefuseLogin.ALREADY_LOGGED_IN, CtS.RefuseLogin.BITCOUNT, false);
				output.terminate();
			}
		} else {
			handler.close("Auth server sent TCPCode.StC.ALLOW_LOGIN at the wrong moment");
		}
	}
}
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

import nl.knokko.bo.server.profile.ProfileTCPClient;
import nl.knokko.bo.server.profile.ProfileTCPClient.State;
import nl.knokko.util.bits.BitInput;
import nl.knokko.util.protocol.BitProtocol;
import nl.knokko.util.socket.client.TCPClientSocket;

public class ProtocolApproveStart implements BitProtocol<TCPClientSocket<ProfileTCPClient.State>> {

	@Override
	public void process(BitInput input, TCPClientSocket<ProfileTCPClient.State> handler) {
		if (handler.getState().getAuthState() == State.AUTH_STATE_WAITING) {
			System.out.println("The auth server approved my start");
			handler.getState().setAuthState(State.AUTH_STATE_COMPLETE);
		} else {
			handler.close("Received TCPCode.StC.APPROVE_START at the wrong moment");
		}
	}
}
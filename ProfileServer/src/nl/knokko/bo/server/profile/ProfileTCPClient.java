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

import nl.knokko.util.socket.client.TCPClientSocket;
import nl.knokko.bo.server.profile.protocol.tcp.TCPProfileProtocol;

import java.io.IOException;
import nl.knokko.bo.server.protocol.AuthProfileCode.CtS;
import nl.knokko.util.bits.BitOutput;

public class ProfileTCPClient extends TCPClientSocket<ProfileTCPClient.State> {

	public ProfileTCPClient() {
		super(new TCPProfileProtocol(), new State());
	}

	@Override
	protected void onError(IOException error) {
		ProfileServer.getConsole().println("Disconnected from auth server: " + error.getMessage());
	}

	@Override
	protected void onConnect() {
		ProfileServer.getConsole().println("Connected with auth server");
		BitOutput output = this.createOutput();
		output.addNumber(CtS.START, CtS.BITCOUNT, false);
		output.terminate();
	}

	@Override
	protected void onClose() {
		ProfileServer.getConsole().println("Connection with auth server has been closed");
	}

	public static class State {

		public static final byte AUTH_STATE_START = 0;
		public static final byte AUTH_STATE_WAITING = 1;
		public static final byte AUTH_STATE_COMPLETE = 2;

		private byte authState = AUTH_STATE_START;

		public State() {

		}

		public byte getAuthState() {
			return authState;
		}

		public void setAuthState(byte newState) {
			authState = newState;
		}
	}
}
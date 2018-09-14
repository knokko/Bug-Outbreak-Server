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
package nl.knokko.bo.server.auth.protocol.tcp;

import nl.knokko.bo.server.auth.AuthServer;
import nl.knokko.bo.server.auth.AuthTCPServer;
import nl.knokko.bo.server.auth.AuthTCPServer.State;
import nl.knokko.bo.server.protocol.AuthProfileCode.CtS;
import nl.knokko.util.bits.BitInput;
import nl.knokko.util.protocol.BitProtocol;

public class ProtocolProfileRefuseLogin implements BitProtocol<AuthTCPServer.Handler> {

	@Override
	public void process(BitInput input, AuthTCPServer.Handler handler) {
            System.out.println("The profile server won't let the user log in");
		if(handler.getState().getState() == State.STATE_PROFILE_LOGGED_IN){
			if(AuthServer.getTCPServer().getSpeaker().refuseProfileLogin(input.readLong(), input.readNumber(CtS.RefuseLogin.BITCOUNT, false)) == null){
				handler.stop("The profile server refused to connect with a client that didn't ask to connect");
			}
		}
		else {
			handler.stop("A TCP server tried to refuse a profile login before logging in itself");
		}
	}
}
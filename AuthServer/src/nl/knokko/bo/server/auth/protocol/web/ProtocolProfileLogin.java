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
import nl.knokko.bo.server.auth.AuthTCPServer.Speaker;
import nl.knokko.bo.server.auth.AuthWebServer.Handler;
import nl.knokko.bo.server.auth.protocol.web.ConnectionCode.StC;
import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitOutput;
import nl.knokko.util.protocol.BitProtocol;

public class ProtocolProfileLogin implements BitProtocol<Handler> {

	@Override
	public void process(BitInput input, Handler handler) {
		if(handler.getState().isLoggedIn()){
			BitOutput output = handler.createOutput();
			Speaker speaker = AuthServer.getTCPServer().getSpeaker();
			synchronized(speaker.getProfileHandlerLock()){
				if(speaker.getProfileHandler() != null){
					speaker.requestProfileLogin(handler);
				}
				else {
					output.addNumber(StC.PROFILE_LOGIN_FAILED, StC.BITCOUNT, false);
					output.addNumber(StC.ProfileFail.SERVER_DOWN, StC.ProfileFail.BITCOUNT, false);
					output.terminate();
				}
			}
		}
		else {
			handler.uglyStop("Tried to get profile before logging in");
		}
	}
}
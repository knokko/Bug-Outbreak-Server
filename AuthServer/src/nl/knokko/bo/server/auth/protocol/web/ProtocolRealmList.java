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
import nl.knokko.bo.server.auth.AuthWebServer.Handler;
import nl.knokko.bo.server.auth.AuthWebServer.State;
import nl.knokko.bo.server.auth.protocol.web.ConnectionCode.StC;
import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitOutput;
import nl.knokko.util.protocol.BitProtocol;

public class ProtocolRealmList implements BitProtocol<AuthWebServer.Handler> {

	@Override
	public void process(BitInput input, Handler handler) {
		if (handler.getState().getAuthState() == State.AUTH_STATE_LOGGED_IN) {
			if (handler.getState().getActionState() == State.ACTION_STATE_NOTHING) {
				String[] realms = AuthServer.getDataManager().getRealmList();
				BitOutput output = handler.createOutput();
				output.addNumber(StC.REALM_LIST, StC.BITCOUNT, false);
				output.addInt(realms.length);
				for (String realm : realms)
					output.addJavaString(realm);
				output.terminate();
			} else {
				handler.uglyStop("Requested realm list while waiting for another request");
			}
		} else {
			handler.uglyStop("Requested realm list before logging in");
		}
	}
}
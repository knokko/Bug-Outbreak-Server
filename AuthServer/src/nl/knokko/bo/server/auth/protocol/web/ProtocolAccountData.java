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
import nl.knokko.bo.server.auth.data.UserData;
import nl.knokko.bo.server.auth.protocol.web.ConnectionCode.StC;
import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitOutput;
import nl.knokko.util.protocol.BitProtocol;

public class ProtocolAccountData implements BitProtocol<AuthWebServer.Handler> {

	@Override
	public void process(BitInput input, Handler handler) {
		if (handler.getState().isLoggedIn()) {
			if (handler.getState().getActionState() == State.ACTION_STATE_NOTHING) {
				UserData accountData = AuthServer.getDataManager().getUserData(handler.getState().getAccountID());
				BitOutput output = handler.createOutput();
				output.addNumber(StC.ACCOUNT_DATA, StC.BITCOUNT, false);
				output.addJavaString(Long.toString(accountData.getID()));// javascript doesn't have long integers
				output.addJavaString(accountData.getUsername());
				output.addByteArray(accountData.getCreationIP());
				output.terminate();
			} else {
				handler.uglyStop("Requested account data while another request is being processed");
			}
		} else {
			handler.uglyStop("Requested account data before logging in");
		}
	}
}
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
package nl.knokko.bo.server.profile.protocol.web;

import nl.knokko.bo.server.profile.ProfileServer;
import nl.knokko.bo.server.profile.ProfileWebServer;
import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitOutput;
import nl.knokko.util.protocol.BitProtocol;

public class ProtocolLogin implements BitProtocol<ProfileWebServer.Handler> {

	@Override
	public void process(BitInput input, ProfileWebServer.Handler handler) {
		if (!handler.getState().isLoggedIn()) {
			int[] providedAuthKey = input.readInts(8);
			long id = ProfileServer.getWebServer().getLoginManager().login(handler.getAddress().getAddress(),
					providedAuthKey);
			if (id != -1) {
				handler.getState().setLoggedIn(id);
				BitOutput output = handler.createOutput();
				output.addNumber(WebCode.StC.LOGIN, WebCode.StC.BITCOUNT, false);
				output.terminate();
			} else {
				handler.uglyStop("Failed to log in");
			}
		} else {
			handler.uglyStop("Tried to log in twice");
		}
	}
}
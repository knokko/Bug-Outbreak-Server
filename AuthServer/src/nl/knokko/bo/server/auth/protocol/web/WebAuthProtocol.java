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

import nl.knokko.bo.server.auth.AuthWebServer;
import nl.knokko.bo.server.auth.protocol.web.ConnectionCode.CtS;
import nl.knokko.util.protocol.DomainBitProtocol;

public class WebAuthProtocol extends DomainBitProtocol<AuthWebServer.Handler> {

	public WebAuthProtocol() {
		super(CtS.AMOUNT, CtS.BITCOUNT);
		register(CtS.LOGIN_1, new ProtocolLogin1());
		register(CtS.LOGIN_2, new ProtocolLogin2());
		register(CtS.REGISTER, new ProtocolRegister());
		register(CtS.REALM_LIST, new ProtocolRealmList());
		register(CtS.ACCOUNT_DATA, new ProtocolAccountData());
		register(CtS.PROFILE, new ProtocolProfileLogin());
		register(CtS.REALM_INFO, new ProtocolRealmInfo());
	}
}
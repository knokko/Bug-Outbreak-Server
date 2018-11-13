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

import java.util.Calendar;
import nl.knokko.bo.server.auth.AuthServer;
import nl.knokko.bo.server.auth.AuthWebServer;
import nl.knokko.bo.server.auth.AuthWebServer.State;
import nl.knokko.bo.server.auth.external.RealmServer;
import nl.knokko.bo.server.auth.protocol.web.ConnectionCode.StC;
import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitOutput;
import nl.knokko.util.protocol.BitProtocol;

public class ProtocolRealmInfo implements BitProtocol<AuthWebServer.Handler> {

	@Override
	public void process(BitInput input, AuthWebServer.Handler handler) {
		if (handler.getState().getAuthState() == State.AUTH_STATE_LOGGED_IN) {
			String realmName = input.readJavaString(50);
			RealmServer realm = AuthServer.getDataManager().getRealm(realmName);
			BitOutput output = handler.createOutput();
			output.addNumber(StC.REALM_INFO, StC.BITCOUNT, false);
			if (realm != null) {
				output.addBoolean(true);
				output.addBoolean(realm.isOnline());
				output.addInt(realm.getOnlinePlayers());
				output.addInt(realm.getMaxPlayers());
				output.addInt(realm.getTotalPlayers());
				Calendar createdAt = new Calendar.Builder().setInstant(realm.getCreatedAt()).build();
				output.addJavaString(createdAt.get(Calendar.DAY_OF_MONTH) + " " + createdAt.get(Calendar.HOUR_OF_DAY)
						+ " " + createdAt.get(Calendar.YEAR));
			} else {
				output.addBoolean(false);
			}
			output.terminate();
		} else {
			handler.uglyStop("Requested realm info before logging in");
		}
	}
}
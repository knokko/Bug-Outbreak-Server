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

import java.util.Arrays;

import nl.knokko.bo.server.auth.AuthServer;
import nl.knokko.bo.server.auth.AuthWebServer;
import nl.knokko.bo.server.auth.AuthWebServer.State;
import nl.knokko.bo.server.auth.data.UserData;
import nl.knokko.bo.server.auth.protocol.web.ConnectionCode.StC;
import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitOutput;
import nl.knokko.util.bits.ByteArrayBitOutput;
import nl.knokko.util.hashing.Hasher;
import nl.knokko.util.hashing.encryptor.Encryptor;
import nl.knokko.util.hashing.encryptor.SimpleEncryptor;
import nl.knokko.util.protocol.BitProtocol;
import nl.knokko.util.random.PseudoRandom;
import nl.knokko.util.random.RandomArray;

public class ProtocolLogin2 implements BitProtocol<AuthWebServer.Handler> {

	@Override
	public void process(BitInput input, AuthWebServer.Handler handler) {
		
		// Check that this is a proper client
		if (handler.getState().getAuthState() == State.AUTH_STATE_LOGGING_IN) {
			
			State state = handler.getState();
			UserData account = AuthServer.getDataManager().getUserData(state.getAccountID());
			BitOutput output = handler.createOutput();
			
			// No funny concurrency
			synchronized (account) {
				
				// It is possible that someone else managed to log in between first login message
				// and this message
				if (!account.isLoggedIn()) {
					byte[] encryptedClientSeed = input.readByteArray();
					
					// Decrypt the received clientStartSeed
					Encryptor clientSeedDecryptor = new SimpleEncryptor(Hasher.createRandom(account.getServerStartSeed(), state.getHalfClientSeed()));
					byte[] clientStartSeed = clientSeedDecryptor.decrypt(encryptedClientSeed);
					
					byte[] checkTestPayload = null;
					
					// clientStartSeed is probably null if the password is incorrect
					if (clientStartSeed != null) {
						checkTestPayload = RandomArray.createPseudo(PseudoRandom.Configuration.LEGACY, clientStartSeed).nextBytes(300);
					}
					
					// If those 2 are equal, the client proved he owns the account
					if (Arrays.equals(checkTestPayload, account.getTestPayload())) {
						
						int[] halfSeed1 = AuthServer.getRandom().nextInts(24);
						int[] halfSeed2 = AuthServer.getRandom().nextInts(24);
						
						// Alright, now its time to prove that I am the server
						Encryptor encryptor = new SimpleEncryptor(Hasher.createRandom(account.getServerStartSeed(), state.getHalfServerSeed()));
						ByteArrayBitOutput payloadOutput = new ByteArrayBitOutput(348);
						payloadOutput.addInts(halfSeed1);
						payloadOutput.addInts(halfSeed2);
						payloadOutput.addBytes(checkTestPayload);
						byte[] encryptedPayload = encryptor.encrypt(payloadOutput.getRawBytes());
						
						// Send to the client
						output.addNumber(StC.LOGIN_2, StC.BITCOUNT, false);
						output.addBoolean(account.isOP());
						output.addByteArray(encryptedPayload);
						output.terminate();
						
						// Mark as logged in
						handler.getState().setLoggedIn();
						account.setLoggedIn();
						
						// The decryptor of the server has the same seed as the encryptor of the client
						handler.setEncryptor(new SimpleEncryptor(Hasher.createRandom(halfSeed2, account.getServerSessionSeed())));
						handler.setDecryptor(new SimpleEncryptor(Hasher.createRandom(halfSeed1, account.getClientSessionSeed())));
					} else {
						
						// Password was incorrect
						handler.getState().clearAuthState();
						AuthServer.getDataManager().getIPData(handler.getAddress()).increaseFailedAttempts();
						output.addNumber(StC.LOGIN_2_FAILED, StC.BITCOUNT, false);
						output.addNumber(StC.LoginFail2.WRONG_PASSWORD, StC.LoginFail2.BITCOUNT, false);
						output.terminate();
					}
				} else {
					
					// Account is already logged in
					handler.getState().clearAuthState();
					output.addNumber(StC.LOGIN_2_FAILED, StC.BITCOUNT, false);
					output.addNumber(StC.LoginFail2.ALREADY_LOGGED_IN, StC.LoginFail2.BITCOUNT, false);
					output.terminate();
				}
			}
		} else {// corrupted client
			handler.uglyStop("Skipped first part of login");
		}
	}
}
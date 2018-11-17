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
package nl.knokko.bo.server.auth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import nl.knokko.util.bits.BitOutput;
import nl.knokko.util.socket.server.TCPServerSocket;
import nl.knokko.bo.server.auth.AuthTCPServer.State;
import nl.knokko.bo.server.auth.protocol.tcp.TCPAuthProtocol;
import nl.knokko.bo.server.protocol.AuthProfileCode.CtS;
import nl.knokko.bo.server.protocol.AuthProfileCode.StC;
import nl.knokko.bo.server.auth.protocol.web.ConnectionCode;

public class AuthTCPServer extends TCPServerSocket<State> {

	private final Speaker speaker;

	public AuthTCPServer() {
		super(new TCPAuthProtocol());
		speaker = new Speaker();
	}

	@Override
	protected State createState() {
		return new State();
	}

	@Override
	protected boolean shouldAccept(byte[] clientAddress) {
		if (AuthServer.getDataManager().getRealm(clientAddress) != null)
			return true;
		return AuthServer.getDataManager().getProfileServer() != null
				&& Arrays.equals(AuthServer.getDataManager().getProfileServer().getIP(), clientAddress);
	}

	@Override
	protected void onOpen() {
		AuthServer.getConsole().println("Started TCP server at port " + getLocalPort());
	}

	@Override
	protected void onError(IOException ioex) {
		AuthServer.getConsole().println("An IO error occured with the TCP server: " + ioex.getMessage());
	}

	@Override
	protected void onClose() {
		AuthServer.getConsole().println("Stopped TCP server");
	}

	@Override
	protected void onHandlerOpen(Handler handler) {
		AuthServer.getConsole().println("Opened a connection with " + handler.getClient().getInetAddress());
	}

	@Override
	protected void onHandlerError(Handler handler, IOException ioex) {
		AuthServer.getConsole()
				.println("An IO error occured with " + handler.getClient().getInetAddress() + ": " + ioex.getMessage());
	}

	@Override
	protected void onHandlerClose(Handler handler) {
		AuthServer.getConsole().println("Closed connection with " + handler.getClient().getInetAddress());
		if (handler.getState().getState() == State.STATE_PROFILE_LOGGED_IN) {
			speaker.clearProfileHandler();
			AuthServer.getDataManager().getProfileServer().setOffline();
		}
		if (handler.getState().getState() == State.STATE_REALM_LOGGED_IN) {
			AuthServer.getDataManager().setRealmOffline(handler.getState().getRealm());
		}
	}

	public Speaker getSpeaker() {
		return speaker;
	}

	public class Speaker {

		private Handler profileHandler;

		private final Collection<AuthWebServer.Handler> profileLoginRequests;

		private final Object profileHandlerLock;

		private Speaker() {
			profileLoginRequests = new ArrayList<AuthWebServer.Handler>();
			profileHandlerLock = new Object();
		}

		public Object getProfileHandlerLock() {
			return profileHandlerLock;
		}

		public void setProfileHandler(Handler handler) {
			synchronized (profileHandlerLock) {
				if (profileHandler != null)
					throw new IllegalStateException("profile handler is already set");
				profileHandler = handler;
			}
		}

		public Handler getProfileHandler() {
			synchronized (profileHandlerLock) {
				return profileHandler;
			}
		}

		private void clearProfileHandler() {
			synchronized (profileHandlerLock) {
				if (profileHandler == null)
					throw new IllegalStateException("Profile handler is not set");
				profileHandler = null;
			}
		}

		public void requestProfileLogin(AuthWebServer.Handler handler) {
			BitOutput output;
			synchronized (profileHandlerLock) {
				if (profileHandler == null)
					throw new IllegalStateException("No profile handler");
				output = profileHandler.createOutput();
			}
			synchronized (profileLoginRequests) {
				if (profileLoginRequests.contains(handler))
					throw new IllegalStateException("Handler (" + handler + ")is already requesting profile login");
				profileLoginRequests.add(handler);
			}
			output.addNumber(StC.ALLOW_LOGIN, StC.BITCOUNT, false);
			output.addLong(handler.getState().getAccountID());
			output.addByteArray(handler.getAddress().getAddress());
			int[] loginKey = AuthServer.getRandom().nextInts(8);
			output.addInts(loginKey);
			handler.getState().setProfileLoginKey(loginKey);
			output.terminate();
		}

		public AuthWebServer.Handler approveProfileLogin(long id) {
			AuthWebServer.Handler handler = null;
			synchronized (profileLoginRequests) {
				Iterator<AuthWebServer.Handler> iterator = profileLoginRequests.iterator();
				while (iterator.hasNext()) {
					AuthWebServer.Handler next = iterator.next();
					if (next.getState().getAccountID() == id) {
						handler = next;
						iterator.remove();
						break;
					}
				}
			}
			if (handler != null && handler.isConnected() && handler.getState().isLoggedIn()) {
				synchronized (profileHandlerLock) {
					if (profileHandlerLock != null) {
						int[] key = handler.getState().getProfileLoginKey();
						BitOutput output = handler.createOutput();
						output.addNumber(ConnectionCode.StC.PROFILE_LOGIN, ConnectionCode.StC.BITCOUNT, false);
						output.addByteArray(profileHandler.getClient().getInetAddress().getAddress());
						output.addChar((char) AuthServer.getDataManager().getProfileServer().getPort());
						output.addInts(key);
						output.terminate();
						System.out.println("Profile server port is " + AuthServer.getDataManager().getProfileServer().getPort());
					} else {
						BitOutput output = handler.createOutput();
						output.addNumber(ConnectionCode.StC.PROFILE_LOGIN_FAILED, ConnectionCode.StC.BITCOUNT, false);
						output.addNumber(ConnectionCode.StC.ProfileFail.SERVER_DOWN,
								ConnectionCode.StC.ProfileFail.BITCOUNT, false);
						output.terminate();
					}
				}
			} else {
				System.out.println("Handler isn't ready to log in to the profile server");
			}
			return handler;
		}

		public AuthWebServer.Handler refuseProfileLogin(long id, long reason) {
			AuthWebServer.Handler handler = null;
			synchronized (profileLoginRequests) {
				Iterator<AuthWebServer.Handler> iterator = profileLoginRequests.iterator();
				while (iterator.hasNext()) {
					AuthWebServer.Handler next = iterator.next();
					if (next.getState().getAccountID() == id) {
						handler = next;
						iterator.remove();
						break;
					}
				}
			}
			if (handler != null && handler.isConnected() && handler.getState().isLoggedIn()) {
				synchronized (profileHandlerLock) {
					if (profileHandlerLock != null) {
						int[] key = handler.getState().getProfileLoginKey();
						BitOutput output = handler.createOutput();
						output.addNumber(ConnectionCode.StC.PROFILE_LOGIN, ConnectionCode.StC.BITCOUNT, false);
						output.addByteArray(profileHandler.getClient().getInetAddress().getAddress());
						output.addChar((char) profileHandler.getClient().getPort());
						output.addInts(key);
						output.terminate();
					} else {
						BitOutput output = handler.createOutput();
						output.addNumber(ConnectionCode.StC.PROFILE_LOGIN_FAILED, ConnectionCode.StC.BITCOUNT, false);
						if (reason == CtS.RefuseLogin.ALREADY_LOGGED_IN)
							output.addNumber(ConnectionCode.StC.ProfileFail.ALREADY_LOGGED_IN,
									ConnectionCode.StC.ProfileFail.BITCOUNT, false);
						else {
							profileHandler.stop("Unknown reason to refuse client profile login: " + reason);
							output.addNumber(ConnectionCode.StC.ProfileFail.SERVER_DOWN,
									ConnectionCode.StC.ProfileFail.BITCOUNT, false);
						}
						output.terminate();
					}
				}
			}
			return handler;
		}
	}

	public static class State {

		public static final byte STATE_DEFAULT = 0;
		public static final byte STATE_REALM_LOGGING_IN = 1;
		public static final byte STATE_REALM_LOGGED_IN = 2;
		public static final byte STATE_PROFILE_LOGGING_IN = 3;
		public static final byte STATE_PROFILE_LOGGED_IN = 4;

		private byte state;

		private int[] tempHasher;

		private String realmName;

		public State() {
			state = STATE_DEFAULT;
		}

		public byte getState() {
			return state;
		}

		public void setState(byte newState) {
			state = newState;
		}

		public void setTempHasher(int[] hasher) {
			tempHasher = hasher;
		}

		public int[] getTempHasher() {
			return tempHasher;
		}

		public void clearTempHasher() {
			tempHasher = null;
		}

		public void setRealm(String realm) {
			realmName = realm;
		}

		public String getRealm() {
			return realmName;
		}
	}
}
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
package nl.knokko.bo.server.auth;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import nl.knokko.bo.server.auth.protocol.web.WebAuthProtocol;
import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitOutput;
import nl.knokko.util.bits.ByteArrayBitInput;
import nl.knokko.util.bits.ByteArrayBitOutput;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class AuthWebServer extends WebSocketServer {
	
	private static final WebAuthProtocol PROTOCOL = new WebAuthProtocol();

	public AuthWebServer(String host, int port) {
		super(new InetSocketAddress(host, port));
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
            if(AuthServer.getDataManager().getIPData(conn.getRemoteSocketAddress().getAddress()).getFailedLoginAttempts() >= 100){
		conn.closeConnection(1002, "Too many failed attempts");
            }
            else {
		conn.setAttachment(new Handler(conn));
            }
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		Handler handler = conn.getAttachment();
		if(handler.getState().isLoggedIn()){
			AuthServer.getDataManager().getUserData(handler.getState().getAccountID()).setLoggedOut();
		}
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		System.out.println("Received string message?");
		conn.close();
	}
	
	@Override
	public void onMessage(WebSocket conn, ByteBuffer message){
		byte[] bytes = new byte[message.capacity()];
		message.get(bytes);
		BitInput input = new ByteArrayBitInput(bytes);
		Handler handler = conn.getAttachment();
		try {
			PROTOCOL.process(input, handler);
		} catch(Exception ex){
			handler.uglyStop("An exception was thrown: " + ex.getMessage());//printing the exception could result in spam attacks
		}
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		System.out.println("websocket error occured: " + ex.getMessage());
	}

	@Override
	public void onStart() {
		System.out.println("Started web server at port " + getPort());
	}
	
	public static class Handler {
		
		private final WebSocket ws;
		
		private final State state;
		
		public Handler(WebSocket ws){
			this.ws = ws;
			state = new State();
		}
		
		public State getState(){
			return state;
		}
		
		public void uglyStop(String reason){
			ws.closeConnection(1002, reason);
		}
		
		public InetAddress getAddress(){
			return ws.getRemoteSocketAddress().getAddress();
		}
		
		public boolean isConnected(){
			return ws.isOpen();
		}
		
		public BitOutput createOutput(){
			return new Output();
		}
		
		private class Output extends ByteArrayBitOutput {
			
			@Override
			public void terminate(){
				ws.send(getBytes());
			}
		}
	}
	
	public static class State {
		
		public static final byte AUTH_STATE_NOTHING = 0;
		public static final byte AUTH_STATE_LOGGING_IN = 1;
		public static final byte AUTH_STATE_LOGGED_IN = 2;
		
		public static final byte ACTION_STATE_NOTHING = 0;
		public static final byte ACTION_STATE_PROFILE_LOGIN = 1;
		
		private byte authState = AUTH_STATE_NOTHING;
		private byte actionState = ACTION_STATE_NOTHING;
		
		private int[] tempLoginHasher;
		private int[] profileLoginKey;
		
		private long accountID = -1;
		
		public byte getAuthState(){
			return authState;
		}
		
		public byte getActionState(){
			return actionState;
		}
		
		public boolean isLoggedIn(){
			return authState == AUTH_STATE_LOGGED_IN;
		}
		
		public void clearAuthState(){
			authState = AUTH_STATE_NOTHING;
			tempLoginHasher = null;
			accountID = -1;
		}
		
		public void setLoggingIn(long id, int[] hasher){
			authState = AUTH_STATE_LOGGING_IN;
			accountID = id;
			tempLoginHasher = hasher;
		}
		
		public void setLoggedIn(){
			authState = AUTH_STATE_LOGGED_IN;
			tempLoginHasher = null;
		}
		
		public void setRegistered(long id){
			authState = AUTH_STATE_LOGGED_IN;
			accountID = id;
		}
		
		public int[] getTempHasher(){
			return tempLoginHasher;
		}
		
		public long getAccountID(){
			return accountID;
		}
		
		public void setProfileLoginKey(int[] loginKey){
			if(actionState != ACTION_STATE_NOTHING) throw new IllegalStateException("Another action is being performed now");
			profileLoginKey = loginKey;
			actionState = ACTION_STATE_PROFILE_LOGIN;
		}
		
		public int[] getProfileLoginKey(){
			if(actionState != ACTION_STATE_PROFILE_LOGIN) throw new IllegalStateException("This handler is not attempting a profile login");
			int[] key = profileLoginKey;
			profileLoginKey = null;
			actionState = ACTION_STATE_NOTHING;
			return key;
		}
	}
}
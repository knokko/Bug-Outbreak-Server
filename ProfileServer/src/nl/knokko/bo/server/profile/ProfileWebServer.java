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
package nl.knokko.bo.server.profile;

import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import nl.knokko.bo.server.profile.protocol.web.WebProfileProtocol;
import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.ByteArrayBitInput;
import nl.knokko.util.bits.BitOutput;
import nl.knokko.util.bits.ByteArrayBitOutput;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class ProfileWebServer extends WebSocketServer {
	
	private static final WebProfileProtocol PROTOCOL = new WebProfileProtocol();
	
	private final LoginManager loginManager;

	public ProfileWebServer(String host, int port) {
		super(new InetSocketAddress(host, port));
		loginManager = new LoginManager();
	}
	
	public LoginManager getLoginManager(){
		return loginManager;
	}
        
        @Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		if(!loginManager.couldJoin(conn.getRemoteSocketAddress().getAddress().getAddress())){
			conn.closeConnection(10002, "This IP address is not allowed to join now.");
		}
		else {
			conn.setAttachment(new Handler(conn));
		}
	}
        
        @Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                Handler handler = conn.getAttachment();
                if(handler.getState().isLoggedIn()){
                    ProfileServer.getDataManager().getUserData(handler.getState().getUserID()).setLoggedOut();
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
		PROTOCOL.process(input, handler);
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
            
            private long userID;
            
            public State(){
                userID = -1;
            }
            
            public void setLoggedIn(long id){
                userID = id;
            }
            
            public boolean isLoggedIn(){
                return userID != -1;
            }
            
            public long getUserID(){
                return userID;
            }
        }
	
	public static class LoginManager {
		
		private static class Entry {
			
			private final byte[] address;
			private final int[] loginKey;
			
			private final long id;
			
			private Entry(byte[] address, int[] loginKey, long id){
				this.address = address;
				this.loginKey = loginKey;
				this.id = id;
			}
		}
		
		private final Collection<Entry> joinEntries;
		
		private LoginManager(){
			joinEntries = new ArrayList<Entry>();
		}
		
		public boolean couldJoin(byte[] ip){
			synchronized(joinEntries){
				for(Entry entry : joinEntries)
					if(Arrays.equals(ip, entry.address))
						return true;
				return false;
			}
		}
		
		public long login(byte[] address, int[] loginKey){
			synchronized(joinEntries){
				Iterator<Entry> iterator = joinEntries.iterator();
				while(iterator.hasNext()){
					Entry entry = iterator.next();
					if(Arrays.equals(address, entry.address) && Arrays.equals(loginKey, entry.loginKey)){
						iterator.remove();
						return entry.id;
					}
				}
				return -1;
			}
		}
                
                public boolean allowLogin(long id, byte[] address, int[] loginKey){
                    synchronized(joinEntries){
                        for(Entry entry : joinEntries)
                            if(entry.id == id)
                                return false;
                        joinEntries.add(new Entry(address, loginKey, id));
                        return true;
                    }
                }
	}
}
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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import nl.knokko.bo.server.profile.data.ProfileDataManager;

public class ProfileServer {
	
	private static ProfileConsole console;
        private static ProfileDataManager dataManager;
	private static ProfileWebServer webServer;
        private static ProfileTCPClient tcpClient;
        
        private static int webPort;

	public static void main(String[] args) {
		console = new ProfileConsole();
		dataManager = new ProfileDataManager();
		dataManager.load();
		new Thread(console).start();//TODO finally test the profile server...
                if(args.length > 0){
                    try {
                        startConnection(InetAddress.getLocalHost().getHostAddress(), Integer.parseInt(args[0]), args[1], Integer.parseInt(args[2]));
                    } catch(UnknownHostException uhe){
                        console.println("Can't resolve own host address: " + uhe.getMessage());
                    }
                }
	}
        
        public static ProfileConsole getConsole(){
            return console;
        }
        
        public static ProfileDataManager getDataManager(){
            return dataManager;
        }
        
        public static void startConnection(String ownHost, int ownPort, String authHost, int authPort){
            console.println("Starting connection with IP " + ownHost);
            webPort = ownPort;
            webServer = new ProfileWebServer(ownHost, ownPort);
            new Thread(webServer).start();
            tcpClient = new ProfileTCPClient();
            tcpClient.start(authHost, authPort);
        }
        
        public static int getWebPort(){
            return webPort;
        }
        
        public static ProfileWebServer getWebServer(){
            return webServer;
        }
	
	public static void stop(){
		console.println("Stopping server...");
		dataManager.save();
                if(tcpClient != null)
                    tcpClient.close("Server is stopping");
                if(webServer != null){
                    try {
                        webServer.stop();
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
		console.setStopping();
	}
}
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
package nl.knokko.bo.server.protocol;

public class AuthProfileCode {
	
	public static class CtS {//profile server to the auth server
		
		public static final byte START = 0;
		public static final byte VERIFY = 1;
		public static final byte WILL_ALLOW_LOGIN = 2;
		public static final byte WONT_ALLOW_LOGIN = 3;
		
		public static final byte AMOUNT = 4;
		public static final byte BITCOUNT = 2;
		
		public static class RefuseLogin {
			
			public static final byte ALREADY_LOGGED_IN = 0;
                        public static final byte ALREADY_WAITING = 0;
			
			public static final byte AMOUNT = 2;
			public static final byte BITCOUNT = 1;
		}
	}
	
	public static class StC {//auth server to profile server
		
		public static final byte VERIFY = 0;
		public static final byte APPROVE_START = 1;
		public static final byte DENY_START = 2;
		public static final byte ALLOW_LOGIN = 3;
		
		public static final byte AMOUNT = 4;
		public static final byte BITCOUNT = 2;
		
		public static class RefuseStart {
			
			public static final byte WRONG_PASSWORD = 0;
			public static final byte ALREADY_STARTED = 1;
			public static final byte WRONG_IP = 2;
			public static final byte NO_PROFILE_SERVER = 3;
			
			public static final byte AMOUNT = 4;
			public static final byte BITCOUNT = 2;
		}
	}
}
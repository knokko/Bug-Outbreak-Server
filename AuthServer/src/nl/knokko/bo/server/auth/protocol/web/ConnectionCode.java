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

public class ConnectionCode {

	public static final int MAX_USERNAME_LENGTH = 30;

	public static class CtS {

		public static final byte LOGIN_1 = 0;
		public static final byte LOGIN_2 = 1;
		public static final byte REGISTER = 2;
		public static final byte REALM_LIST = 3;
		public static final byte ACCOUNT_DATA = 4;
		public static final byte PROFILE = 5;
		public static final byte REALM_INFO = 6;

		public static final byte BITCOUNT = 3;
		public static final byte AMOUNT = 7;
	}

	public static class StC {

		public static final byte LOGIN_1 = 0;
		public static final byte LOGIN_1_FAILED = 1;
		public static final byte LOGIN_2 = 2;
		public static final byte LOGIN_2_FAILED = 3;
		public static final byte REGISTER = 4;
		public static final byte REGISTER_FAILED = 5;
		public static final byte REALM_LIST = 6;
		public static final byte ACCOUNT_DATA = 7;
		public static final byte PROFILE_LOGIN = 8;
		public static final byte PROFILE_LOGIN_FAILED = 9;
		public static final byte REALM_INFO = 10;

		public static final byte BITCOUNT = 4;
		public static final byte AMOUNT = 11;

		public static class LoginFail1 {

			public static final byte NO_USERNAME = 0;
			public static final byte ALREADY_LOGGED_IN = 1;
			public static final byte UNDER_ATTACK = 2;

			public static final byte BITCOUNT = 2;
		}

		public static class LoginFail2 {

			public static final byte WRONG_PASSWORD = 0;
			public static final byte ALREADY_LOGGED_IN = 1;// it is possible that someone else logged in in the meantime

			public static final byte BITCOUNT = 1;
		}

		public static class RegisterFail {

			public static final byte NAME_IN_USE = 0;
			public static final byte IP_LIMIT_EXCEEDED = 1;

			public static final byte BITCOUNT = 1;
		}

		public static class ProfileFail {

			public static final byte SERVER_DOWN = 0;
			public static final byte ALREADY_LOGGED_IN = 1;

			public static final byte BITCOUNT = 1;
		}
	}
}
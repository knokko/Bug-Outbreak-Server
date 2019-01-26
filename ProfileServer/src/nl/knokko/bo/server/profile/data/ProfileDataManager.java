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
package nl.knokko.bo.server.profile.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitOutput;
import nl.knokko.util.bits.BitInputStream;
import nl.knokko.util.bits.BitOutputStream;

import nl.knokko.bo.server.profile.ProfileServer;
import nl.knokko.usermanager.UserDataManager;

public class ProfileDataManager extends UserDataManager<ProfileUserData> {

	private final File baseDirectory;
	private final File passwordDirectory;

	private int[] authPassword;

	private boolean shouldSave;

	public ProfileDataManager() {
		super(ProfileUserData.class, new File("profile/users"), 500, 300);
		baseDirectory = new File("profile");
		passwordDirectory = new File(baseDirectory + "/passwords");
		baseDirectory.mkdirs();
		passwordDirectory.mkdir();
		shouldSave = true;
	}

	public synchronized void save() {
		if (!shouldSave) {
			ProfileServer.getConsole().println("Saving has been skipped");
			return;
		}
		super.save();
		passwordDirectory.mkdirs();
		try {
			BitOutput output = new BitOutputStream(new FileOutputStream(getAuthPasswordFile()));
			output.addIntArray(authPassword);
			output.terminate();
		} catch (IOException ioex) {
			ProfileServer.getConsole()
					.println("An IO error occured while saving the password for the auth server: " + ioex.getMessage());
		}
	}

	public void load() {
		try {
			BitInput input = new BitInputStream(new FileInputStream(getAuthPasswordFile()));
			authPassword = input.readIntArray();
			input.terminate();
		} catch (IOException ioex) {
			ProfileServer.getConsole().println(
					"An IO error occured while loading the password for the auth server: " + ioex.getMessage());
			ProfileServer.getConsole().println(
					"Copy the password from the auth server to the profile server and start the profile server again.");
			try {
				ProfileServer.getConsole().println("IP should be " + InetAddress.getLocalHost().getHostAddress());
			} catch (UnknownHostException ex) {
				ProfileServer.getConsole().println("The host address of the server socket is unknown");
			}
			shouldSave = false;
			ProfileServer.stop();
			return;
		}
	}

	private File getAuthPasswordFile() {
		return new File(passwordDirectory + "/auth.pw");
	}

	public int[] getAuthPassword() {
		return authPassword;
	}
}
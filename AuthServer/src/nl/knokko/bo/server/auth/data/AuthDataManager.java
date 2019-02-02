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
package nl.knokko.bo.server.auth.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.Arrays;

import nl.knokko.bo.server.auth.AuthServer;
import nl.knokko.bo.server.auth.external.ProfileServer;
import nl.knokko.bo.server.auth.external.RealmServer;
import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitInputStream;
import nl.knokko.util.bits.BitOutput;
import nl.knokko.util.bits.BitOutputStream;
import nl.knokko.util.bits.ByteArrayBitInput;
import nl.knokko.util.bits.ByteArrayBitOutput;
import nl.knokko.util.random.CrazyRandom;

public class AuthDataManager {

	private final File baseDirectory;
	private final File userDirectory;
	private final File ipDirectory;
	private final File idDirectory;
	private final File passwordDirectory;

	private final UsersFile[] loadedFiles;
	private final IPsFile[] loadedIPs;
	private IDsFile[] loadedIDs;
	private RealmServer[] realmServers;
	private ProfileServer profileServer;

	private final Object idLock;// loadedIDs can be replaced by a bigger array, so it's not a safe lock
	private final Object registerLock;// the account can't be locked during registering because it doesn't have an
										// instance yet
	private final Object profileLock;
	private final Object realmLock;

	private boolean shouldSave;

	private long nextID;

	public AuthDataManager(File baseDirectory) {
		this.baseDirectory = baseDirectory;
		this.userDirectory = new File(baseDirectory + "/users");
		this.ipDirectory = new File(baseDirectory + "/ips");
		this.idDirectory = new File(baseDirectory + "/ids");
		this.passwordDirectory = new File(baseDirectory + "/passwords");
		this.loadedFiles = new UsersFile[256];
		this.loadedIPs = new IPsFile[256];
		this.loadedIDs = new IDsFile[0];
		idLock = new Object();
		registerLock = new Object();
		profileLock = new Object();
		realmLock = new Object();
		shouldSave = true;
	}

	public synchronized void load() {
		try {
			BitInput input = new BitInputStream(new FileInputStream(getGeneralFile()));
			nextID = input.readLong();
			if (input.readBoolean())
				profileServer = new ProfileServer(input);
			realmServers = new RealmServer[input.readInt()];
			for (int index = 0; index < realmServers.length; index++) {
				realmServers[index] = new RealmServer(input);
			}
			input.terminate();
		} catch (IOException ioex) {
			AuthServer.getConsole().println("Couldn't load general file manager data: " + ioex.getMessage());
			AuthServer.getConsole().println("Terminate the server if this isn't the first time!");
			realmServers = new RealmServer[0];
		}
		if (profileServer != null) {
			try {
				BitInput profilePass = new BitInputStream(new FileInputStream(getProfilePasswordFile()));
				profileServer.loadPassword(profilePass);
				profilePass.terminate();
			} catch (IOException ioex) {
				AuthServer.getConsole()
						.println("Couldn't load the password of the profile server: " + ioex.getMessage());
				shouldSave = false;
				AuthServer.stop();
				return;
			}
		}
		for (RealmServer realm : realmServers) {
			try {
				BitInput input = new BitInputStream(new FileInputStream(getRealmPasswordFile(realm)));
				realm.loadPassword(input);
				input.terminate();
			} catch (IOException ioex) {
				AuthServer.getConsole().println(
						"Couldn't load the password of the realm server " + realm.getName() + ": " + ioex.getMessage());
				shouldSave = false;
				AuthServer.stop();
				return;
			}
		}
		try {
			AuthServer.setCrazyRandom(CrazyRandom.fromFile(getRandomFile()));
		} catch (IOException ioex) {
			AuthServer.getConsole().println("Couldn't load crazy random data: " + ioex.getMessage());
			AuthServer.getConsole().println("You should probably terminate the server!");
			AuthServer.getConsole().println("A weak seed will be generated for the CrazyRandom...");
			CrazyRandom random = new CrazyRandom(AuthServer.getSimpleRandom().nextBooleans(32000), 12456);
			AuthServer.setCrazyRandom(random);
		}
	}

	public void setProfileIP(byte[] ip) {
		synchronized (profileLock) {
			if (profileServer == null) {
				try {
					passwordDirectory.mkdirs();
					File passwordFile = getProfilePasswordFile();
					BitOutput output = new BitOutputStream(new FileOutputStream(passwordFile));
					output.addIntArray(AuthServer.getRandom().nextInts(15));
					output.terminate();
					BitInput input = new BitInputStream(new FileInputStream(passwordFile));
					profileServer = new ProfileServer(ip);
					profileServer.loadPassword(input);
					input.terminate();
					AuthServer.getConsole()
							.println("The address of the profile server has been set to " + Arrays.toString(ip));
					AuthServer.getConsole()
							.println("The password of the profile server has been saved as " + passwordFile);
				} catch (IOException ioex) {
					AuthServer.getConsole()
							.println("An IO error occured while creating the password for the profile server: "
									+ ioex.getMessage());
				}
			} else {
				profileServer.setIP(ip);
				AuthServer.getConsole()
						.println("The address of the profile server has been changed to " + Arrays.toString(ip));
			}
		}
	}

	public void addRealm(String name, byte[] ip) {
		synchronized (realmLock) {
			RealmServer[] newRealms = Arrays.copyOf(realmServers, realmServers.length + 1);
			RealmServer realm = new RealmServer(name, ip);
			newRealms[realmServers.length] = realm;
			try {
				passwordDirectory.mkdirs();
				File passwordFile = getRealmPasswordFile(realm);
				BitOutput output = new BitOutputStream(new FileOutputStream(passwordFile));
				output.addIntArray(AuthServer.getRandom().nextInts(16));
				output.terminate();
				BitInput input = new BitInputStream(new FileInputStream(passwordFile));
				realm.loadPassword(input);
				input.terminate();
				realmServers = newRealms;
				AuthServer.getConsole().println("The realm " + name
						+ " has been added succesfully and it's password has been saved as " + passwordFile);
			} catch (IOException ioex) {
				AuthServer.getConsole().println("Failed to set the password of the realm: " + ioex.getMessage());
			}
		}
	}

	public void setRealmOffline(String name) {
		synchronized (realmLock) {
			for (RealmServer realm : realmServers) {
				if (realm.getName().equals(name)) {
					realm.setOffline();
					AuthServer.getConsole().println("The realm server " + name + " disconnected");
					return;
				}
			}
		}
		AuthServer.getConsole().println("Tried to set the realm server " + name + " offline, but couldn't find it");
	}

	public RealmServer getRealm(String name) {
		synchronized (realmLock) {
			for (RealmServer realm : realmServers)
				if (realm.getName().equals(name))
					return realm;
			return null;
		}
	}

	public RealmServer getRealm(byte[] ip) {
		synchronized (realmLock) {
			for (RealmServer realm : realmServers)
				if (Arrays.equals(ip, realm.getIP()))
					return realm;
			return null;
		}
	}

	public String[] getRealmList() {
		synchronized (realmLock) {
			int length = 0;
			for (RealmServer realm : realmServers)
				if (realm.isOnline())
					length++;
			String[] names = new String[length];
			int nameIndex = 0;
			for (RealmServer realm : realmServers)
				if (realm.isOnline())
					names[nameIndex++] = realm.getName();
			return names;
		}
	}

	public ProfileServer getProfileServer() {
		synchronized (profileLock) {
			return profileServer;
		}
	}

	public synchronized void save() {
		if (!shouldSave)
			return;
		baseDirectory.mkdirs();
		try {
			BitOutput output = new BitOutputStream(new FileOutputStream(getGeneralFile()));
			output.addLong(nextID);
			if (profileServer != null) {
				output.addBoolean(true);
				profileServer.save(output);
			} else
				output.addBoolean(false);
			output.addInt(realmServers.length);
			for (RealmServer realm : realmServers)
				realm.save(output);
			output.terminate();
		} catch (IOException ioex) {
			AuthServer.getConsole().println("Failed to save general data: " + ioex.getMessage());
		}
		passwordDirectory.mkdir();
		if (profileServer != null) {
			try {
				BitOutput output = new BitOutputStream(new FileOutputStream(getProfilePasswordFile()));
				profileServer.savePassword(output);
				output.terminate();
			} catch (IOException ioex) {
				AuthServer.getConsole()
						.println("Failed to save the password of the profile server: " + ioex.getMessage());
			}
		}
		for (RealmServer realm : realmServers) {
			try {
				BitOutput output = new BitOutputStream(new FileOutputStream(getRealmPasswordFile(realm)));
				realm.savePassword(output);
				output.terminate();
			} catch (IOException ioex) {
				AuthServer.getConsole().println(
						"Failed to save the password of the realm " + realm.getName() + ": " + ioex.getMessage());
			}
		}
		try {
			AuthServer.getRandom().saveToFile(getRandomFile());
		} catch (IOException ioex) {
			AuthServer.getConsole().println("Couldn't save CrazyRandom: " + ioex.getMessage());
		}
		userDirectory.mkdir();
		for (UsersFile usersFile : loadedFiles) {
			if (usersFile != null) {
				try {
					ByteArrayBitOutput output = new ByteArrayBitOutput(2000);
					usersFile.save(output);
					output.terminate();
					OutputStream fileOutput = Files.newOutputStream(getUsersFileFor(usersFile.getHash()).toPath());
					fileOutput.write(output.getBytes());
					fileOutput.flush();
					fileOutput.close();
				} catch (IOException ioex) {
					AuthServer.getConsole()
							.println("Failed to save usersFile " + usersFile.getHash() + ": " + ioex.getMessage());
				}
			}
		}
		ipDirectory.mkdir();
		for (IPsFile ipsFile : loadedIPs) {
			if (ipsFile != null) {
				try {
					BitOutput output = new BitOutputStream(new FileOutputStream(getIPsFileFor(ipsFile.getHash())));
					ipsFile.save(output);
					output.terminate();
				} catch (IOException ioex) {
					AuthServer.getConsole()
							.println("Failed to save ipsFile " + ipsFile.getHash() + ": " + ioex.getMessage());
				}
			}
		}

		idDirectory.mkdir();
		for (IDsFile idsFile : loadedIDs) {
			try {
				BitOutput output = new BitOutputStream(new FileOutputStream(getIDsFileFor(idsFile.getIndex())));
				idsFile.save(output);
				output.terminate();
			} catch (IOException ioex) {
				AuthServer.getConsole()
						.println("Failed to save idsFile " + idsFile.getIndex() + ": " + ioex.getMessage());
			}
		}
	}

	public Object getRegisterLock() {
		return registerLock;
	}

	public Object getProfileLock() {
		return profileLock;
	}

	private File getGeneralFile() {
		return new File(baseDirectory + "/general.data");
	}

	private File getProfilePasswordFile() {
		return new File(passwordDirectory + "/profile.pw");
	}

	private File getRealmPasswordFile(RealmServer realm) {
		return new File(passwordDirectory + "/" + realm.getName() + ".pw");
	}

	private File getRandomFile() {
		return new File(baseDirectory + "/crazy.random");
	}

	/**
	 * Tries to find the data of the user with the specified name. This method will
	 * either return the UserData for the specified username or null if there is no
	 * user with that name.
	 * 
	 * @param username The username of the requested account
	 * @return The UserData of the account with the specified username or null if
	 *         there is no account with that name
	 */
	public UserData getUserData(String username) {
		return getUsersFile(username).getUser(username);
	}

	public UserData getUserData(long id) {
		String username = getUsernameByID(id);
		if (username == null)
			return null;
		return getUserData(username);
	}

	/**
	 * Register a new user. The checks if the user can be registered should be done
	 * before calling this method
	 * 
	 * @param data
	 */
	public void register(UserData data) {
		if (!getUsersFile(data.getUsername()).addUser(data))
			throw new IllegalStateException("There is already an account with name " + data.getUsername());
		data.assignID(nextID++);
		setID(data.getID(), data.getUsername());
	}

	private UsersFile getUsersFile(String username) {
		byte hash = (byte) username.hashCode();
		synchronized (loadedFiles) {
			UsersFile users = loadedFiles[hash & 0xFF];
			if (users != null)
				return users;
			File file = getUsersFileFor(hash);
			if (file.exists()) {
				try {
					BitInput input = ByteArrayBitInput.fromFile(file);
					users = new UsersFile(hash, input);
					input.terminate();
				} catch (IOException ioex) {
					AuthServer.getConsole().println("Strange IO exception occured: " + ioex.getMessage());
					users = new UsersFile(hash);
				}
			} else {
				users = new UsersFile(hash);
			}
			loadedFiles[hash & 0xFF] = users;
			return users;
		}
	}

	private File getUsersFileFor(byte hash) {
		return new File(userDirectory + "/u" + hash + ".users");
	}

	public IPData getIPData(InetAddress address) {
		return getIPData(address.getAddress());
	}

	public IPData getIPData(byte[] ip) {
		synchronized (loadedIPs) {
			IPsFile file = getIPsFile(ip);
			IPData data = file.getIP(ip);
			if (data == null) {
				data = new IPData(ip);
				file.addIP(data);
			}
			return data;
		}
	}

	private static byte hashIP(byte[] ip) {
		byte hash = (byte) ip.length;
		for (byte i : ip)
			hash += i;
		return hash;
	}

	private IPsFile getIPsFile(byte[] ip) {
		byte hash = hashIP(ip);
		IPsFile ipsFile = loadedIPs[hash & 0xFF];
		if (ipsFile != null)
			return ipsFile;
		File file = getIPsFileFor(hash);
		if (file.exists()) {
			try {
				BitInput input;
				if (file.length() <= Integer.MAX_VALUE) {// This is the expected scenario
					input = ByteArrayBitInput.fromFile(file);
				} else {
					input = new BitInputStream(new FileInputStream(file));
				}
				ipsFile = new IPsFile(hash, input);
				input.terminate();
			} catch (IOException ioex) {
				AuthServer.getConsole().println(
						"Strange IO Exception occured when loading IPs File " + file + ": " + ioex.getMessage());
				ipsFile = new IPsFile(hash);
			}
		} else {
			ipsFile = new IPsFile(hash);
		}
		loadedIPs[hash & 0xFF] = ipsFile;
		return ipsFile;
	}

	private File getIPsFileFor(byte hash) {
		return new File(ipDirectory + "/i" + hash + ".ips");
	}

	private String getUsernameByID(long id) {
		return getIDsFile(id).getUsernameForID(id);
	}

	private void setID(long id, String username) {
		getIDsFile(id).register(id, username);
	}

	private IDsFile getIDsFile(long id) {
		if (id < 0)
			throw new IllegalArgumentException("ID must be positive, so it can't be " + id);
		long indexL = id / IDsFile.AMOUNT;
		if (indexL > Integer.MAX_VALUE)
			throw new IllegalStateException("Too many users!");
		int index = (int) indexL;
		synchronized (idLock) {
			if (index < loadedIDs.length) {
				if (loadedIDs[index] != null)
					return loadedIDs[index];
				// load the file from disk after the else block
			} else {
				loadedIDs = Arrays.copyOf(loadedIDs, index + 1);
			}
			// try to load the file from disk
			File file = getIDsFileFor(index);
			if (file.exists()) {
				try {
					BitInput input;
					if (file.length() <= Integer.MAX_VALUE) {// expected scenario
						input = ByteArrayBitInput.fromFile(file);
					} else {
						input = new BitInputStream(new FileInputStream(file));
					}
					IDsFile idsFile = new IDsFile(index, input);
					input.terminate();
					loadedIDs[index] = idsFile;
					return idsFile;
				} catch (IOException ioex) {
					AuthServer.getConsole().println("Strange IO exception occured while trying to load ids with index "
							+ index + ": " + ioex.getMessage());
				}
			}
			// create a new instance
			IDsFile idsFile = new IDsFile(index);
			loadedIDs[index] = idsFile;
			return idsFile;
		}
	}

	private File getIDsFileFor(int index) {
		return new File(idDirectory + "/i" + index + ".ids");
	}
}
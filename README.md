# Bug-Outbreak-Server
The server/backend codes for my work in progress game Bug Outbreak

I am trying to split the tasks of the server to 4 types of servers:

-The Auth(entication) server that handles all the authentication. 
Every client has to connect to this server before they can connect to any other servers. 
After a successful authentication, the clients can request this server to connect them with the other servers.
When that happens, this server will send a random and temporarily code to both the client and other server that the client can join with that code.
And thus, the other servers won't need to know anything about the account of the client.

-The Profile server keeps track of game related account information of players. 
At the moment, it only stores their personal character models.

-The Development server is supposed to be accessible only for accounts that are marked as developer. 
It should allow them to edit and create tiles and worlds.
I haven't even started with this server.

-The Realm servers are the servers where the actual game is supposed to be played. 
Every realm server should have its own world, players and progress.
I haven't even started with this server.

I am writing the server codes in Eclipse. The sources are divided into 4 java projects (will be more in the future):

-AuthServer

-ProfileServer

-AuthProfileProtocol

-EntityModels

I suppose other IDEA's should have something similar to java projects, but I don't know all of them.

Each java project has its own dependencies. 
Since I am too lazy to learn any serious dependency management, they are all simply added to the build path.

The complete list of dependencies are:

-AuthProfileProtocol (part of this repository)

-EntityModels (part of this repository)

-BitHelper (github.com/knokko/BitHelper)

-BitProtocol (github.com/knokko/Bit-Protocol)

-Console (github.com/knokko/Console)

-Hashing (github.com/knokko/Hashing)

-Random (github.com/knokko/Random)

-TCPClient (github.com/knokko/TCP-Client)

-TCPServer (github.com/knokko/TCP-Server)

-JavaWebSocket (github.com/TooTallNate/Java-WebSocket)

Dependencies of AuthServer:

-AuthProfileProtocol

-BitProtocol

-BitHelper

-Console

-Hashing

-Random

-TCPServer

Dependencies of ProfileServer:

-AuthProfileProtocol

-BitProtocol

-BitHelper

-Console

-EntityModels

-Hashing

-Random

-TCPClient

Dependencies of EntityModels:

-BitHelper

AuthProfileProtocol doesn't have dependencies


After setting up the project, do the following steps to start the servers:

-Start the AuthServer (nl.knokko.bo.server.auth.AuthServer.java)

You will get a few complains in the console about missing files because it is the first time. Also a warning about a weak random seed, but that's something to thing about when you actually get to the point of publishing (which may or may not happen).

-Start the ProfileServer (nl.knokko.bo.server.profile.ProfileServer.java)

You will get a few complains again and the server will not even start properly. This is no problem at all, you only needed to do this because it will show you the ip address you will have to use in the auth server.

-Run the command 'setProfileIP <ip1> <ip2> <ip3> <ip4> ... possibly more' where the ips are supposed to represent the IP address that was shown when you tried to start the profile server.

An example would be 'setProfileIP 132 94 37 11'. You should just enter the command in the console of the running AuthServer.

-Copy (and rename the destination file) 'auth\passwords\profile.pw' that should have been generated just now to 'profile\passwords\auth.pw'. All required folders should have been created automatically, but you might have to refresh your folders.

-Run the command 'start <web port number> <tcp port number>' in the AuthServer console.

The <web port number> is the port number where the auth server will listen for web socket connections from the clients. You can choose this number yourself, but there are a few requirements. It musts be an integer between 0 and 65535 (both inclusive) and it must not be used for any other service.
The <tcp port number> is the port number where the auth server will listen for connections from the other servers. Again, you can choose the number yourself, but the same requirements apply.
You will need these port numbers later on.

-Start the ProfileServer again

-Run the command 'start <websocket port number> <auth server ip> <auth tcp port number>' in the ProfileServer console.
  
The <websocket port number> is the port number where the websocket server of the profile server will be listening on.
The <auth server ip> is the representation of the auth server like ip1.ip2.ip3.ip4 .
The <auth tcp port number> is the port number where the auth server is listening for the other servers. The auth ip and port should be displayed in the console of the auth server. You can choose the <websocket port number> yourself.
  
In the unlikely case everything just works the first time, you would have started both servers succesfully. Now that you managed to do that, it's time to start the client. See github.com/knokko/Bug-Outbreak-Client for instructions to start the client.

You can stop both servers by running the command 'stop' or 'exit' in both consoles. (Use 'terminate' if you want an ugly stop. )The next time you start the servers, you no longer need to run the setProfileIP in the auth server, but you will need to use the start commands every time you run the server.

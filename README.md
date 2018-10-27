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
BitHelper (github.com/knokko/BitHelper)
BitProtocol (github.com/knokko/Bit-Protocol)
Console (github.com/knokko/Console)
Hashing (github.com/knokko/Hashing)
Random (github.com/knokko/Random)
TCPClient (github.com/knokko/TCP-Client)
TCPServer (github.com/knokko/TCP-Server)
JavaWebSocket (I will find source soon)

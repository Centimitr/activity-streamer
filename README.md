# COMP90015 Project 1: Activity Streamer

A simple distributed system; Clients can send and receive activity objects from a stream with the help of multiple servers. TCP + JSON.


## Build

Developed and tested using Intellij IDEA under Java 10 environment.

This project does not use Java 10's local-variable type interface (var), and use Java 9's module system to organize code files. So it should be able to build with JDK 9+. The project is fully configured under Intellij IDEA so the build process can be easily executed by using Compile option in the menu. The two Jar packages are also set to be generated in the build process. Client GUI part is built with React and does not depend on more preprocessing tools.


## Usages

`ActivityStreamerClient.jar` and `ActivityStreamerServer.jar` are provided to work as client and server.

__Server__

```
java -jar ActivityStreamerServer.jar -rp <remote port> -rh <remote host> -s <secret> -lp <local port> -lh <local host>
```

A server can connect to another server. It listens to clients' requests and also allow for more servers to connect to expand the network.

```
-lp                              local port number
-rp                              remote port number
-rh                              remote hostname
-lh                              local hostname
-a                               activity interval in milliseconds
-s                               secret for the server to use
```
A server maintains mainly three groups of connections. Clients, children servers and its parent server. Although this mechanism makes servers forming a tree, there is no central server in this system. 

__Client__
```
java -jar ActivityStreamerClient.jar -rp <remote port> -rh <remote host> -u <username> -s <secret>
```

Clients should login to a server and then they can broadcast activity objects.
```
-u                              username
-rp                             remote port number
-rh                             remote hostname
-s                              secret for username
```   
A client maintains a 1 to 1 connection to a specific server and a GUI interface is provided. The interface is built with React + MobX which embeded in a JavaFx application.

## Main Ideas

- 3 modules: Client, Server, Shared
- Message Routing
- Strong message type definitions
- Responder pattern, split initiative and passive parts
- States management at different scales

## Project Stucture

```
Activity Streamer
├── Client         
├───── App.java                         # CLI entrance              
├───── View.java                        # GUI interface, using JavaFX webview to load the compiled React app               
├───── Client.java                      # inherit from ClientResponder, create connection with a server
├───── ClientResponder.java             # server messages handling
├───── ClientAgent.java                 # perform actions and manage states for client
├───── Frontend                         # store compiled React app for GUI
├── Server                              
├───── App.java                         # CLI entrance
├───── Server.java                      # inherit from ServerResponder, maintain connections with clients and servers
├───── ServerResponder.java             # server/client messages handling
├───── ConnectivityManager.java         # maintains connections by groups and corresponding router
├───── RegisterManager.java             # states for register feature
├───── Users.java                       # record user information
├───── Servers.java                     # record server information (e.g loads)  
├── Shared            
├───── Listener.java                    # wraps ServerSocket
├───── Connectivity.java                # wraps Socket
├───── MessageRouter.java               # simple routing for the system
├───── MessageContext.java              # context for message handlers
├───── ... 10+ Java files               # for async/sync, connection management and message definitions 
└── FrontEnd                            # GUI application source code
```

<br />  
<sup>Sat, 28 April 2018</sup> 

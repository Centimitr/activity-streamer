# COMP90015

## Shared
The `Shared` module includes concept classes for this project.

- `Connectivity` wraps a Socket and provide I/O functions with String and Objects.
- `Listener` wraps a ServerSocket and is only useful for server code.
- `MessageRouter` is a abstract router for allocate different messages to many handle functions, which can help the program to be clear.
- `MessageContext` is used in the handle functions registered to a MessageRouter. It can help features decoupling.
- `MessageCommands` enums the possible commands' type.

## Client
The singleton Client has 1 C/S connection and a GUI instance.

It manages the connection with a Connectivity instance and redirect its input stream to a MessageRouter instance.

## Server
The singleton Server is actually the Control instance. It has an array of C/S connections and might have a S/S connection.

Similarly, it manages the connections with Connectivity instances and redirect their input to either the serverMessageRouter or the clientMessageRouter.

# Description
This application is an online version of the classic 2-player game, Pong, played over the network by providing an IP address. The first person to start the program will act as the "server" and the second person to start the program will act as the "client" and must specify the server's IP address in order to play against the first person. Note that the default setting for this game is set up so that the client attempts to connect to the localhost (i.e. IP Address: 127.0.0.1). In order to play this game online with someone else, change the string "localhost" under PhysicsThread's startClient() method in NetworkPong.java to the IP address of the server you wish to connect to.

#Language
Written in Java using the NetBeans IDE

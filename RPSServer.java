/*
 * Name: RPSServer.java
 * Author: Sharabh Shukla
 *
 * To compile on an allv machine, type
 *       javac RPSServer.java
 *
 * To run an allv machine, type
 *       java RPSServer YOUR_PORT_NUMBER
 *
 * This program is the server side of the single player rock, paper, scissors game 
 * played between a user through the client side and the server (this side). In the server,
 * this program handles connection with client, starting a game, interpretting user's input,
 * keeping the game on a loop, and closing the socket to the client when the user wants to logout.
 */

import java.io.*; // Provides for system input and output through data
import java.net.*; // Provides the classes for implementing networking
import java.util.*; // Provides for Scanner class to read input from user

// TCP Server Class
class RPSServer {
    public static void main(String argv[]) throws Exception{
        // get the port number assigned from the command line
        int lisPort = Integer.parseInt(argv[0]);
        
        // create a server socket (TCP)
        ServerSocket listener = new ServerSocket(lisPort);
        
        while(true){
            // Wait and accept client connection
            Socket connectionSocket = listener.accept();
            
            //create an input stream from the socket input stream
            InputStream inFromClient = connectionSocket.getInputStream();
            
            // create an output stream from the socket output stream
            OutputStream outToClient = connectionSocket.getOutputStream();
            
            Scanner inputClient = new Scanner(inFromClient);
            PrintWriter outputClient = new PrintWriter(outToClient, true);
            
            // read a line from the input stream for ID and print the ID
            String clientID = inputClient.nextLine();
            System.out.println("LOGIN 200 OK\nClient ID: " + clientID);
            
            // create counters of keeping stats
            int totalGame = 0, playerWin = 0, totalTimeOut = 0;
            
            // counter to make sure the loop runs as long as the user doesn't logout
            String keepPlaying = "play";
            
            while(keepPlaying.equalsIgnoreCase("play")){
                // prompt the start of game and send client a heads up
                System.out.println("\nNew Game");
                outputClient.println("New Game");
                
                // read a line from the input stream for client's pick
                String clientPick = inputClient.nextLine();
                
                // get the appropriate response message
                String responseCode = serverResponseCode(clientPick);
                
                // check if client picked logout and close session
                if(clientPick.equalsIgnoreCase("logout")){
                    //update loop status
                    keepPlaying = "stop";
                    
                    // indicate to client that the user picked logout
                    outputClient.println("logout");
                    
                    // print client's final session stats
                    System.out.println(responseCode);
                    
                    // close the connection socket
                    connectionSocket.close();
                }
                // start a new game if the user picked login as their command
                else if(clientPick.equalsIgnoreCase("login")){
                    //update loop status
                    keepPlaying = "play";
                    
                    // indicate to client that the user picked help
                    outputClient.println("login");
                    
                    continue;
                }
                // print client's pick as long as it is not help or logout
                else if(clientPick.equalsIgnoreCase("")){
                    //update loop status
                    keepPlaying = "play";
                    
                    // indicate to client that the pick timed out
                    outputClient.println("time out");
                    
                    System.out.println(responseCode);
                    totalTimeOut++; totalGame++;
                }
                // print client's pick as long as it is not help or logout
                else if(!clientPick.equalsIgnoreCase("help") && !clientPick.equalsIgnoreCase("logout")){
                    // print out client pick along with the response code
                    System.out.println(responseCode);
                    
                    // indicate to client that the user picked rock, paper, or scissors
                    // outputClient.println("play");
                    if(clientPick.equalsIgnoreCase("rock")){
                        outputClient.println("rock");
                    }
                    else if(clientPick.equalsIgnoreCase("paper")){
                        outputClient.println("paper");
                    }
                    else{
                        outputClient.println("scissors");
                    }
                    
                    
                    // create a server pick and send it to client
                    String serverPick = serverPick();
                    
                    //System.out.println("Server Pick: " + serverPick);
                    outputClient.println(serverPick);
                    
                    // determine the result of the game and send result to client
                    String result = gameOutcome(serverPick, clientPick);
                    outputClient.println(result);
                    
                    // add to the counters!
                    if(result.equalsIgnoreCase("User Loses!")){
                        totalGame++;
                    }
                    else if(result.equalsIgnoreCase("User Wins!")){
                        totalGame++; playerWin++;
                    }
                    else{
                        continue;
                    }
            
                    //update loop status
                    keepPlaying = "play";
                }
                else{
                    //update loop status
                    keepPlaying = "play";
                    
                    // indicate to client that the user picked help
                    outputClient.println("help");
                    
                    continue;
                }
            }
            // close the connection socket
            connectionSocket.close();
        }
    }
    // A fuction to return a random number between 1-3 for the server to make a pick between rock, paper, or scissors
    public static String serverPick(){
        int rand = (int) (Math.random() * ((3-1)+1)) + 1;
        String serverPick;
        
        if(rand == 1)
            serverPick = "rock";
        else if(rand == 2)
            serverPick = "paper";
        else
            serverPick = "scissors";
        
        return serverPick;
    }
    /* A function to determine the outcome of the rock-paper-scissors game between the client and server.
     * It takes two string parameters and returns a string as the result of the game
     */
    public static String gameOutcome(String serverPick, String clientPick){
        String result = null;
        
        if(serverPick.equalsIgnoreCase(clientPick)){
            result = "Tie";
        }
        else if((serverPick.equalsIgnoreCase("rock") && clientPick.equalsIgnoreCase("paper")) || (serverPick.equalsIgnoreCase("paper") && clientPick.equalsIgnoreCase("scissors")) || (serverPick.equalsIgnoreCase("scissors") && clientPick.equalsIgnoreCase("rock"))){
            result = "User Wins!";
        }
        else{
            result = "User Loses!";
        }
        
        return result;
    }
    // A function to determine which acknowledgement status code to use by the sever
    public static String serverResponseCode(String clientPick){
        String response = null;
        
        if(clientPick.equalsIgnoreCase("login"))
            response = "LOGIN 200 OK";
        else if(clientPick.equalsIgnoreCase("rock"))
            response = "USER ROCK 200 OK";
        else if(clientPick.equalsIgnoreCase("paper"))
            response = "USER PAPER 200 OK";
        else if(clientPick.equalsIgnoreCase("scissors"))
            response = "USER SCISSORS 200 OK";
        else if(clientPick.equalsIgnoreCase("logout"))
            response = "LOGOUT 200 OK";
        else if(clientPick.equalsIgnoreCase(""))
            response = "TIMEOUT 200 OK";
        else
            response = "";

        return response;
    }
}
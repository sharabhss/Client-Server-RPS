/*
 * Name: RPSClient.java
 * Author: Sharabh Shukla
 *
 * To compile on an allv machine, type
 *       javac RPSClient.java
 *
 * To run on a local machine, type
    javac RPSClient localhost YOUR_PORT_NUMBER
 *
 * This program is the client side of the single player rock, paper, scissors game
 * played between a user through the client side (this side) and the server side. In the client,
 * this program handles communication with user by providing the command line interface for a user to
 * interact with the game. A user can login, ask for help, play rock, paper, scissor, and logout and
 * and end session as they like.
 */

import java.io.*; // Provides for system input and output through data
import java.net.*; // Provides the classes for implementing networking
import java.util.*; // Provides for Scanner class to read input from user

// TCP Client class
class RPSClient {
    public static void main(String argv[]) throws Exception
    {
        // get the server port form command line
        int lisPort = Integer.parseInt(argv[1]);
        
        // create a client socket (TCP) and connect to server
        Socket clientSocket = new Socket(argv[0], lisPort);
        
        // create an input stream from the socket input stream
        InputStream inFromServer = clientSocket.getInputStream();
        
        // create an output stream from the socket output stream
        PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
        
        // create an input stream for user inputs
        Scanner serverInput = new Scanner(inFromServer);
        
        // prompt user for loginID and read a line form the standard input
        System.out.print("Login ID: ");
        Scanner input = new Scanner(System.in);
        String loginID = input.nextLine();
        
        // send the loginID read to the server
        outToServer.println(loginID);
        
        // create counters of keeping stats
        int totalGame = 0, playerWin = 0, totalTimeOut = 0;
        
        //The command line game play starts here
        while(serverInput.hasNextLine()){
            // receive indication of new game and indicate start of a new game to user
            String newGame = serverInput.nextLine();
            if(newGame.equals("New Game")){
               System.out.println("\nNEW GAME 200 OK");
            }
            //prompt user to make their pick
            System.out.print("Client Pick: ");
            
            String clientPick = "";
            
            // error checking for invalid commands entered and handeling timeouts
            while(true){
                // create an input stream from System.in
                BufferedReader userPick = new BufferedReader(new InputStreamReader(System.in));
                
                // create start time from when to start counting the 5 second time out
                long startTime = System.currentTimeMillis();
                
                // waits 5 seconds from start time as long as System.in is empty
                while((System.currentTimeMillis() - startTime) < 5000 && !userPick.ready()){
                }
                
                // check if there was input in System.in or call the game a time out
                if(userPick.ready()){
                    clientPick = userPick.readLine();
                }
                else{
                    System.out.println("TIME OUT 200 OK");
                    
                    // add to the total game and total time out counters
                    totalTimeOut++; totalGame++;
                    
                    // end checking for user command
                    break;
                }
                
                // if the correct command is entered, move on. If not, get the user to input again
                if(clientPick.equalsIgnoreCase("rock") || clientPick.equalsIgnoreCase("paper") || clientPick.equalsIgnoreCase("scissors") || clientPick.equalsIgnoreCase("logout")|| clientPick.equalsIgnoreCase("help") || clientPick.equalsIgnoreCase("login")){
                    break;
                }
                else{
                    System.out.print("Invalid input, enter again: ");
                }
            }
            // send the game response read to the server
            outToServer.println(clientPick);
            
            // receive server instruction on what to do with client's pick
            String nextStep = serverInput.nextLine();
            
            // get appropriate respose code to go with the server message
            String responseCode = clientResponseCode(nextStep);
            
            // if there was a time out, start a new game
            if(nextStep.equals("time out")){
                continue;
            }
            // if the user input login, do nothing and start a new game
            else if(nextStep.equals("login")){
                continue;
            }
            // logout check and display stats and terminate session
            else if(nextStep.equals("logout")){
                // print stat
                System.out.println("\nG = " + totalGame + " W = " + playerWin + " O = " + totalTimeOut);
                
                // close the connection socket
                clientSocket.close();
                System.exit(0);
            }
            // if user input help, then print the help argument with all the commands and its descriptions and a new game is started afterwards
            else if(nextStep.equals("help")){
                System.out.println("Available Commands");
                System.out.println("help\t\t: Takes no argument. It prints a list of supported commands");
                System.out.println("login\t\t: Takes no argument. Currently not implemented");
                System.out.println("rock\t\t: Takes no argument. Used by the player to throw a rock");
                System.out.println("paper\t\t: Takes no argument. Used by the player to throw a paper");
                System.out.println("scissors\t: Takes no argument. Used by the player to throw a scissors");
                System.out.println("logout\t\t: Takes no argument. Used to display player stats and terminated the session");
            }
            // game is played here
            else{
                // get server pick from server
                String serverPick = serverInput.nextLine();
                
                // print acknowledgement that client reveiced server pick
                System.out.println(responseCode);
                
                // get the result of the picks
                String gameResult = serverInput.nextLine();
                
                // print acknowledgement that client reveiced game results from server
                if(gameResult.equals("Tie"))
                {
                    System.out.println("TIE 200 OK");
                }
                else{
                    // add to total game counter if its a win or loss only
                    totalGame++;
                    
                    // if the user wins, add to the total win counter
                    if(gameResult.equals("User Wins!")){
                        playerWin++;
                    }
                    
                    //print the result of the game as acknowledgements from server
                    if(gameResult.equalsIgnoreCase("User Wins!")){
                            System.out.println("USER WIN 200 OK");
                    }
                    else{
                        System.out.println("USER LOSS 200 OK");
                    }
                    
                }
            }
        }
        // close the socket
        clientSocket.close();
    }
    // A function to determine which acknowledgement status code to use by the sever
    public static String clientResponseCode(String nextStep){
        String response = null;
        
        if(nextStep.equalsIgnoreCase("login"))
            response = "LOGIN 200 OK\n";
        else if(nextStep.equalsIgnoreCase("logout"))
            response = "LOGOUT 200 OK\n";
        else if(nextStep.equalsIgnoreCase("time out"))
            response = "TIMEOUT 200 OK\n";
        else if(nextStep.equalsIgnoreCase("help"))
            response = "HELP 200 OK\n";
        else if(nextStep.equalsIgnoreCase("rock"))
            response = "SERVER ROCK 200 OK";
        else if(nextStep.equalsIgnoreCase("paper"))
            response = "SERVER PAPER 200 OK";
        else if(nextStep.equalsIgnoreCase("scissors"))
            response = "SERVER SCISSORS 200 OK";
        else
            response = "";
        
        return response;
    }
}

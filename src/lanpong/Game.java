/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lanpong;
import javax.swing.*;
import java.awt.*;
import java.net.*;
import java.io.*;
/**
 *
 * @author Matt
 */
public class Game extends JFrame {
    // Widgets go here
    JPanel panel_game;
    
    // Server variables
    BufferedWriter out;
    BufferedReader in;
    int port;
    String ip_address;
    
    // Game variables
    private int pos_player;
    private int pos_enemy;
    private int pos_ball_x;
    private int pos_ball_y;
    private final int paddleSpeed = 20;
    private int speed_ball_x;
    private int speed_ball_y;
    private final int height_paddle = 60;
    private final int width_paddle = 10;
    private final int size_ball = 20;
    private int score_player = 0;
    private int score_enemy = 0;
    private boolean roundStart;
    private boolean playerReady;
    private boolean enemyReady;
    
    
    public Game(int port) {
        this.port = port;
        initComponents();
    }
    
    public Game(String ip_address, int port) {
        this.ip_address = ip_address;
        this.port = port;
        initComponents();
    }
    
    void initComponents() {
        // Initial window settings
        panel_game = new Canvas();
        panel_game.setPreferredSize(new Dimension(800,600));
        panel_game.setFocusable(true);
        this.add(panel_game);
        this.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
        
        
        // Post-init settings
        int alignCenter = (panel_game.getHeight() + height_paddle)/2;
        pos_player = alignCenter;
        pos_enemy = alignCenter;
        
        panel_game.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                KeyPressed(e);
            }
        });
        panel_game.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                KeyReleased(e);
            }
        });
        
        
        // Start thread
        new Thread((Canvas)panel_game).start();
        new Thread(new PhysicsThread()).start();
    }
    
    
    /* NETWORKING FUNCTIONS */
    /* Update the positions of objects on the screen and check for collisions
    */
    public class PhysicsThread implements Runnable {
        /* Set up a connection between the client and server sockets
           @note This method is executed by the start() method of this class's template Runnable
        */
        @Override
        public void run() {
            try {
                // Attempt to connect to a server, if server found, connect to it
                startClient(ip_address, port);
                // Set up the first round
                resetRound("client");
                // Update the player paddle, enemy paddle, and ball's position
                while (true) {
                    updateFrame("client");
                    // Limit the game to 100 FPS
                    Thread.sleep(10);
                }
            } catch (IOException | InterruptedException e) {
                try {
                    // If no server could be connected to, start a server
                    startServer(port);
                    // Set up the first round
                    resetRound("server");
                    // Update the player paddle, enemy paddle, and ball's position
                    while (true) {
                        updateFrame("server");
                        // Limit the game to 100 FPS
                        Thread.sleep(10);
                    }
                } catch (IOException | InterruptedException except) {}
            }
        }
        /* Create a server that looks for client requests to connect to
        */
        public void startServer(int port) throws IOException {
                // Start a server on Port 5000
                ServerSocket serverSocket = new ServerSocket(port);
                System.out.println("No server found. Starting server on Port " + port);
                // Look for a connection to the client server
                Socket socket = serverSocket.accept();
                // Output information to the client
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                // Read in information from the client
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        /* Create a client socket that connects to a server
        */
        public void startClient(String ip_address, int port) throws IOException{
            // Start client request on Port 5000, and if a server exists on that Port, connect to it
            Socket clientSocket = new Socket(ip_address, port);
            System.out.println("Server found. Connected on Port " + port);
            // Output information to the server
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            // Read in information from the server
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
        }
        /* Updates the positions of all objects on the screen
           @param side - Corresponds with which side (server or client) is being updated
        */
        public void updateFrame(String side) throws IOException {
            // Output this player's current position
            out.write(Integer.toString(pos_player));
            out.newLine();
            out.flush();
            // Read in the enemy's movements from the server
            String enemyStatus = in.readLine();
            // Check if the enemy is ready to start a new round
            if (!roundStart && enemyStatus.equals("ready"))
                enemyReady = true;
            // Check the enemy's position
            else 
                pos_enemy = Integer.parseInt(enemyStatus);
            // Round starts once both players are ready
            if (enemyReady && playerReady)
                roundStart = true;
            // Move the ball
            if (roundStart) {
                pos_ball_x += speed_ball_x;
                pos_ball_y += speed_ball_y;
                // Check if the ball has collided with any objects
                checkCollision();
                // Goal has been scored and the round is over
                int winner = checkGoal();
                if (winner != 0) {
                    // Reset round
                    
                    resetRound(side, winner);
                }
            }
        }
        /* Checks if the ball has collided with a paddle or wall
        */
        public boolean checkCollision() {
            boolean flag = false;
            // Ball collides with enemy's paddle
            if (pos_ball_x + size_ball > panel_game.getWidth() - 2*width_paddle && pos_ball_x < panel_game.getWidth() - width_paddle
                    && pos_ball_y < pos_enemy + height_paddle && pos_ball_y + size_ball > pos_enemy) {
                speed_ball_x = -speed_ball_x;
                flag = true;
            }
            // Ball collides with player's paddle
            else if (pos_ball_x < 2*width_paddle && pos_ball_x + size_ball > width_paddle && pos_ball_y < pos_player + height_paddle && pos_ball_y + size_ball > pos_player) {
                speed_ball_x = -speed_ball_x;
                flag = true;
            }
            // Ball collides with wall
            else if (pos_ball_y < 0 || pos_ball_y + size_ball > panel_game.getHeight()) {
                speed_ball_y = -speed_ball_y;
                flag = true;
            }
            return flag;
        }
        /* Checks if a player has scored
           @note This function returns -1 if the enemy has scored, 1 if the player has scored, and 0 if otherwise
        */
        public int checkGoal() {
            // Player scored
            if (pos_ball_x + size_ball > panel_game.getWidth()) {
                score_player++;
                return 1;
            }
            // Enemy scored
            else if (pos_ball_x < 0) {
                score_enemy++;
                return -1;
            }
            return 0;
        }
        /* Reset player and ball positions for the next round
           @param side - Corresponds with which side (server or client) is being reset
        */
        public void resetRound(String side) {
            roundStart = false;
            playerReady = false;
            enemyReady = false;
            pos_player = (panel_game.getHeight() - height_paddle) / 2;
            pos_enemy = (panel_game.getHeight() - height_paddle) / 2;
            pos_ball_x = (panel_game.getWidth()-size_ball)/2;
            pos_ball_y = (panel_game.getHeight()-size_ball)/2;
            // Flip the direction of the ball depending on whose screen it will be displayed on
            if (side.equals("server"))
                speed_ball_x = -3;
            else
                speed_ball_x = 3;
            speed_ball_y = 3;
        }
        /* Reset player and ball positions, but reset the ball depending on who won the last round
           @param side - Corresponds with which side (server of client) is being reset
           @param winner - Corresponds with which player won the last round
        */
        public void resetRound(String side, int winner) {
            resetRound(side);
            // Serve the ball to the player who lost the last round
            if (side.equals("server")) {
                if (winner == 1)
                    speed_ball_x = -speed_ball_x;
            } else {
                if (winner == -1)
                    speed_ball_x = -speed_ball_x;
            } 
        }
    }
    
    
    /* GRAPHICS ANIMATIONS */
    public class Canvas extends JPanel implements Runnable {
        // Width of the center line on the field
        int width_line = 5;
        /* Paints the ball, paddles, and scores onto the screen
           @param g - The object to be drawn onto
        */
        @Override
        public void paintComponent(Graphics g) {
            // Black background
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
            
            // Center field line
            g.setColor(Color.WHITE);
            g.fillRect(this.getWidth()/2 - width_line/2, 0, width_line, this.getHeight());
            
            // Draw paddles
            g.fillRect(width_paddle, pos_player, width_paddle, height_paddle);
            g.fillRect(this.getWidth() - 2*width_paddle, pos_enemy, width_paddle, height_paddle);
            
            // Draw ball
            g.fillRect(pos_ball_x, pos_ball_y, size_ball, size_ball);
            
            // Draw scoreboard
            g.drawString(Integer.toString(score_player), this.getWidth() * 1/4, 50);
            g.drawString(Integer.toString(score_enemy), this.getWidth() * 3/4, 50);
            // If a player is ready to start the next round, show READY on the screen
            if (!roundStart) {
                if (playerReady)
                    g.drawString("READY", 100, 100);
                if (enemyReady)
                    g.drawString("READY", this.getWidth() - 100, 100);
            }
        }
        /* Update the screen every 10ms
        */
        @Override
        public void run() {
            while(true) {
                // Repaint the scene
                repaint();
                try {
                    // Maximum 100 FPS
                    Thread.sleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        /* Updates the position of the player's paddle
           @param direction - The direction (-1 or 1) that the player is traveling in
        */
        public void updatePos(int direction) {
            pos_player += direction * paddleSpeed;     
        }
    }
    
    
//    /* GAME STATE CHECKING */ 
//    /* Checks if the ball has collided with a paddle or wall
//    */
//    public boolean checkCollision() {
//        boolean flag = false;
//        // Ball collides with enemy's paddle
//        if (pos_ball_x + size_ball > panel_game.getWidth() - 2*width_paddle && pos_ball_x < panel_game.getWidth() - width_paddle
//                && pos_ball_y < pos_enemy + height_paddle && pos_ball_y + size_ball > pos_enemy) {
//            speed_ball_x = -speed_ball_x;
//            flag = true;
//        }
//        // Ball collides with player's paddle
//        else if (pos_ball_x < 2*width_paddle && pos_ball_x + size_ball > width_paddle && pos_ball_y < pos_player + height_paddle && pos_ball_y + size_ball > pos_player) {
//            speed_ball_x = -speed_ball_x;
//            flag = true;
//        }
//        // Ball collides with wall
//        else if (pos_ball_y < 0 || pos_ball_y + size_ball > panel_game.getHeight()) {
//            speed_ball_y = -speed_ball_y;
//            flag = true;
//        }
//        return flag;
//    }
//    /* Checks if a player has scored
//       @note This function returns -1 if the enemy has scored, 1 if the player has scored, and 0 if otherwise
//    */
//    public int checkGoal() {
//        // Player scored
//        if (pos_ball_x + size_ball > panel_game.getWidth()) {
//            score_player++;
//            return 1;
//        }
//        // Enemy scored
//        else if (pos_ball_x < 0) {
//            score_enemy++;
//            return -1;
//        }
//        return 0;
//    }
//    /* Reset player and ball positions for the next round
//       @param side - Corresponds with which side (server or client) is being reset
//    */
//    public void resetRound(String side) {
//        roundStart = false;
//        playerReady = false;
//        enemyReady = false;
//        pos_player = (panel_game.getHeight() - height_paddle) / 2;
//        pos_enemy = (panel_game.getHeight() - height_paddle) / 2;
//        pos_ball_x = (panel_game.getWidth()-size_ball)/2;
//        pos_ball_y = (panel_game.getHeight()-size_ball)/2;
//        // Flip the direction of the ball depending on whose screen it will be displayed on
//        if (side.equals("server"))
//            speed_ball_x = -3;
//        else
//            speed_ball_x = 3;
//        speed_ball_y = 3;
//    }        
//    /* Reset player and ball positions, but reset the ball depending on who won the last round
//        @param side - Corresponds with which side (server of client) is being reset
//        @param winner - Corresponds with which player won the last round
//    */
//    public void resetRound(String side, int winner) {
//        resetRound(side);
//        // Serve the ball to the player who lost the last round
//        if (side.equals("server")) {
//            if (winner == 1)
//                speed_ball_y = -speed_ball_y;
//        } else {
//            if (winner == -1)
//                speed_ball_x = -speed_ball_x;
//        } 
//    }

    /* KEY EVENT HANDLING */
    private void KeyPressed(java.awt.event.KeyEvent e) {
        Canvas canvas = (Canvas)panel_game;
        // Down key pressed
        if (e.getKeyCode() == 40 && pos_player + height_paddle < panel_game.getHeight()) {
            canvas.updatePos(1);
        } 
        // Up key pressed
        else if (e.getKeyCode() == 38 && pos_player > 0) {
            canvas.updatePos(-1);
        }
        // Space pressed indicates that a player is ready to start the next round
        else if (e.getKeyCode() == 32) {
            playerReady = true;
            try {
                out.write("ready");
                out.newLine();
                out.flush();
            } catch (Exception ex) {}
        }
    }
    /*  Stop moving the paddle if the user isn't pressing a key
        @param e - This is the key released by the user
    */
    private void KeyReleased(java.awt.event.KeyEvent e) {
        Canvas canvas = (Canvas)panel_game;
        canvas.updatePos(0);
    }
    
    
}

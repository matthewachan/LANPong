/* @file NetworkPong.java
 * @author Matthew Chan
 * @date 3/5/2016
 * @brief This application is a 2-player game of pong, played over the network
 * @detail The default setting for this game is set up to connect to the same computer.
 *         In order to play this game on the network with someone else, change the string
 *         "localhost" under PhysicsThread's startClient() method to the IP address you wish
 *         to connect to.
 */
package network.pong;
// GUI imports
import java.awt.*;
import javax.swing.*;
// Networking imports
import java.net.*;
// I/O Stream imports
import java.io.*;

public class NetworkPong extends javax.swing.JFrame {
    private int paddlePos;
    private int enemyPos;
    private int ballPosX;
    private int ballPosY;
    private final int paddleSpeed = 20;
    private int ballSpeedX;
    private int ballSpeedY;
    private final int paddleHeight = 60;
    private final int paddleWidth = 10;
    private final int ballSize = 20;
    private int playerScore = 0;
    private int enemyScore = 0;
    private boolean roundStart;
    private boolean playerReady;
    private boolean enemyReady;
    private BufferedReader in;
    private BufferedWriter out;
    
    /* Set up the UI */
    public NetworkPong() {
        initComponents();
        // Set up objects on the screen
        paddlePos = (jPanel1.getHeight()-paddleHeight)/2;
        enemyPos = (jPanel1.getHeight()-paddleHeight)/2;
        ballPosX = (jPanel1.getWidth()-ballSize)/2;
        ballPosY = (jPanel1.getHeight()-ballSize)/2;
        // Allow the game to accept keyboard input
        jPanel1.requestFocus();
        // Start a thread to animate the game
        new Thread((Canvas)jPanel1).start();
        // Start a thread to handle the collisions and physics
        new Thread(new PhysicsThread()).start(); 
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new Canvas();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                pongKeyPressed(e);
            }
        });
        jPanel1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                pongKeyReleased(e);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 965, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 590, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    /*  Move the paddle if the user presses up/down arrows
        @param e - This is the key pressed by the user
    */
    private void pongKeyPressed(java.awt.event.KeyEvent e) {
        Canvas canvas = (Canvas)jPanel1;
        // Down key pressed
        if (e.getKeyCode() == 40 && paddlePos + paddleHeight < jPanel1.getHeight()) {
            canvas.updatePaddlePos(1);
        } 
        // Up key pressed
        else if (e.getKeyCode() == 38 && paddlePos > 0) {
            canvas.updatePaddlePos(-1);
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
    private void pongKeyReleased(java.awt.event.KeyEvent e) {
        Canvas canvas = (Canvas)jPanel1;
        canvas.updatePaddlePos(0);
        
    }
    
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(NetworkPong.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(NetworkPong.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(NetworkPong.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(NetworkPong.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new NetworkPong().setVisible(true);
            }
        }); 
    }
    
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
                startClient();
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
                    startServer();
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
        public void startServer() throws IOException {
                // Start a server on Port 5000
                ServerSocket serverSocket = new ServerSocket(5000);
                System.out.println("No server found. Starting server on Port 5000.");
                // Look for a connection to the client server
                Socket socket = serverSocket.accept();
                // Output information to the client
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                // Read in information from the client
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        /* Create a client socket that connects to a server
        */
        public void startClient() throws IOException{
            // Start client request on Port 5000, and if a server exists on that Port, connect to it
            Socket clientSocket = new Socket("localhost", 5000);
            System.out.println("Server found. Connected on Port 5000.");
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
            out.write(Integer.toString(paddlePos));
            out.newLine();
            out.flush();
            // Read in the enemy's movements from the server
            String enemyStatus = in.readLine();
            // Check if the enemy is ready to start a new round
            if (!roundStart && enemyStatus.equals("ready"))
                enemyReady = true;
            // Check the enemy's position
            else 
                enemyPos = Integer.parseInt(enemyStatus);
            // Round starts once both players are ready
            if (enemyReady && playerReady)
                roundStart = true;
            // Move the ball
            if (roundStart) {
                ballPosX += ballSpeedX;
                ballPosY += ballSpeedY;
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
            if (ballPosX + ballSize > jPanel1.getWidth() - 2*paddleWidth && ballPosX < jPanel1.getWidth() - paddleWidth
                    && ballPosY < enemyPos + paddleHeight && ballPosY + ballSize > enemyPos) {
                ballSpeedX = -ballSpeedX;
                flag = true;
            }
            // Ball collides with player's paddle
            else if (ballPosX < 2*paddleWidth && ballPosX + ballSize > paddleWidth && ballPosY < paddlePos + paddleHeight && ballPosY + ballSize > paddlePos) {
                ballSpeedX = -ballSpeedX;
                flag = true;
            }
            // Ball collides with wall
            else if (ballPosY < 0 || ballPosY + ballSize > jPanel1.getHeight()) {
                ballSpeedY = -ballSpeedY;
                flag = true;
            }
            return flag;
        }
        /* Checks if a player has scored
           @note This function returns -1 if the enemy has scored, 1 if the player has scored, and 0 if otherwise
        */
        public int checkGoal() {
            // Player scored
            if (ballPosX + ballSize > jPanel1.getWidth()) {
                playerScore++;
                return 1;
            }
            // Enemy scored
            else if (ballPosX < 0) {
                enemyScore++;
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
            paddlePos = (jPanel1.getHeight() - paddleHeight) / 2;
            enemyPos = (jPanel1.getHeight() - paddleHeight) / 2;
            ballPosX = (jPanel1.getWidth()-ballSize)/2;
            ballPosY = (jPanel1.getHeight()-ballSize)/2;
            // Flip the direction of the ball depending on whose screen it will be displayed on
            if (side.equals("server"))
                ballSpeedX = -3;
            else
                ballSpeedX = 3;
            ballSpeedY = 3;
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
                    ballSpeedX = -ballSpeedX;
            } else {
                if (winner == -1)
                    ballSpeedX = -ballSpeedX;
            } 
        }
    }
    /* Animate the JPanel
    */
    public class Canvas extends JPanel implements Runnable {
        // Width of the center line on the field
        int lineWidth = 5;
        /* Paints the ball, paddles, and scores onto the screen
           @param g - The object to be drawn onto
        */
        @Override
        public void paintComponent(Graphics g) {
            // Set the background to black
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
            // Draw the center line
            g.setColor(Color.WHITE);
            g.fillRect(this.getWidth()/2 - lineWidth/2, 0, lineWidth, this.getHeight());
            // Draw player paddle
            g.fillRect(paddleWidth, paddlePos, paddleWidth, paddleHeight);
            // Draw enemy paddle
            g.fillRect(this.getWidth() - 2*paddleWidth, enemyPos, paddleWidth, paddleHeight);
            // Draw the ball
            g.fillRect(ballPosX, ballPosY, ballSize, ballSize);
            // Draw scoreboard
            g.drawString(Integer.toString(playerScore), this.getWidth() * 1/4, 50);
            g.drawString(Integer.toString(enemyScore), this.getWidth() * 3/4, 50);
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
        public void updatePaddlePos(int direction) {
            paddlePos += direction * paddleSpeed;     
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}

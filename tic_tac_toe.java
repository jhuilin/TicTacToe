import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;

public class TicTacToe extends JFrame{
   private enum GameState{ STARTING, PLAYING, DRAW, CROSS_WON, CIRCLE_WON};      //indicate current game state
   private GameState currentState = GameState.STARTING;

   private enum Seed{ EMPTY, CROSS, CIRCLE};              //indicate current turn
   private Seed currentPlayer = Seed.CIRCLE;
   private Seed board[][] = new Seed[3][3];

   private enum opponent{ VS_player, VS_dumbAI, VS_smartAI }; //indicate current opponent
   private opponent vs = opponent.VS_player;

   private enum turn{ PLAYER, AI}                      //indicate player turn or AI(if vs. AI)
   private turn currentTurn = turn.PLAYER;

   private JPanel leftPanel;              //reset and opponent choice
   private JPanel gamePanel;              //game board
   private JLabel currentStatus = new JLabel("O's Turn");

   private Dimension d = new Dimension(100,100);     //button size
   Font font = new Font("Arial", Font.BOLD, 50);     //button font
   private JButton buttons[] = new JButton[9];          //9 buttons

   //right panel
   private JPanel rightPanel;
   private JPanel opponentPanel;
   private JRadioButton player;
   private JRadioButton dumbAI;
   private JRadioButton smartAI;
   private ButtonGroup opponents;

   private JPanel resetPanel;
   private JButton reset;

   //dumbAI
   private Random r = new Random();

   private int move = 0;

   public TicTacToe(){              //window
      setTitle("Tic Tac Toe");
      setSize(350,350);
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setLayout(new GridLayout());
      new gameBoardPanel();        //build game board: 9 Jbuttons
      new buildRightPanel();       //build right panel
      initGame();                   //initial game status
      add(leftPanel);
      add(rightPanel);
      pack();
      setVisible(true);
   }

    private void initGame() {
         for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 3; ++col) {
               board[row][col] = Seed.EMPTY; // all cells empty
            }
         }
         currentState = GameState.STARTING; // ready to play
         currentPlayer = Seed.CIRCLE;       // cross plays first
         move = 0;
    }

    private void resetButtuns(){
       for(int i=0; i<9; i++){
            buttons[i].setText("");             //set all button with no 'X' or 'O'
            buttons[i].setEnabled(true);      //enable all buttons
       }
       currentStatus.setForeground(Color.black); //set status bar back to black color
       currentStatus.setText("O's Turn");
    }

class gameBoardPanel extends JPanel implements ActionListener{    //build game board

   public gameBoardPanel(){
      leftPanel =  new JPanel();
      leftPanel.setLayout(new BorderLayout());
      gamePanel = new JPanel();
      gamePanel.setLayout(new GridLayout(3,3));        //for 9 buttons
      for(int i=0; i<9; i++)
         buttons[i] = new JButton("");              //create all buttons with no text
      for(int i=0; i<9; i++){
         gamePanel.add(buttons[i]);          //add to panel
         buttons[i].setPreferredSize(d);          //set size for each button
      }
      for(int i=0; i<9; i++)
         buttons[i].addActionListener(this);       //add listener for each buttons
      leftPanel.add(gamePanel, BorderLayout.CENTER);
      currentStatus.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 12));
      leftPanel.add(currentStatus, BorderLayout.SOUTH);
   }

   public void actionPerformed(ActionEvent e){
      JButton currentButton = (JButton) e.getSource();
      int row=0, col=0;
      for(int i=0; i<9; i++)
         if(currentButton == buttons[i]){      //if clicked a button update the board
            board[i/3][i%3] = currentPlayer;
            row = i/3;
            col = i%3;
         }
      if(currentState == GameState.PLAYING || currentState == GameState.STARTING){      //if game has result cannot click button
         if(currentPlayer == Seed.CIRCLE){              //circle turn
            currentPlayer = Seed.CROSS;
            currentButton.setFont(font);
            currentButton.setText("O");
            currentButton.setEnabled(false);            //make it disable after clicking once
            currentStatus.setText("X's Turn");          //change to 'X' turn
            currentState = GameState.PLAYING;
         }else{
            currentPlayer = Seed.CIRCLE;               //cross turn
            currentButton.setFont(font);
            currentButton.setText("X");
            currentButton.setEnabled(false);
            currentStatus.setText("O's Turn");          //change to 'O' turn
            currentState = GameState.PLAYING;
         }
         if(vs != opponent.VS_player)            //if not vs. other player change to AI turn
            currentTurn = turn.AI;
         move++;             //for smartAI counting
         updateGame(board[row][col], row, col);    //check the current game status after one button clicked
         if(vs == opponent.VS_dumbAI && currentTurn == turn.AI)    //if vs dumbAI call the method for next move
            dumbAImove();
         else if(vs == opponent.VS_smartAI && currentTurn == turn.AI){  //if vs smartAI call the method for next move
            move++;
            smartAImove();
         }
      }
   }

   private void dumbAImove(){       //chose random button if that button is empty
      int b;
      do{
         b = r.nextInt(9);
      }while(board[b/3][b%3] != Seed.EMPTY);
      chooseButton(b);
      updateGame(board[b/3][b%3], b/3, b%3);
   }

   private void smartAImove(){                //calculate next move, aiming for win if not possible > preventing player to win;
      int x, row=0, col=0, button=0;          //if both condition not met get a random move
      boolean no_solution = true;             //if both condition not met
      if(move <= 2){
         if(board[1][1] == Seed.EMPTY ){       //if [1][1] is empty always get the position because by getting middle point can prevent
            chooseButton(4);               //4 possible winning by player and increase winning rate
            updateGame(board[1][1], 1, 1);
         }else if(board[1][1] != Seed.EMPTY){   //else if middle is not empty put it in corner prevent an other trick for player winning
            chooseButton(0);
            updateGame(board[0][0], 0, 0);
         }
      }
      if(move >2){
         for(int r=0;r<9; r+=3){
            x = checkRow(r/3, Seed.CIRCLE);             //check circle row to prevent player to win
            if(x != -1 && no_solution){                //if found no solution don't do anything or found a solution for winning(winning>preventing)
               button = r+x;
               row = r/3;
               col = x;
               no_solution = false;
            }
            x = checkCol(r/3, Seed.CIRCLE);             //check circle column
            if(x != -1 && no_solution){
               for(int c=0; c<9; c++)
                  if(c/3 == x && c%3 == r/3)
                     button = c;
               row = x;
               col = r/3;
               no_solution = false;
            }
            x = checkDia(Seed.CIRCLE);             //check circle diagonal
            if(x != -1 && no_solution){
               button = x;
               row = x/3;
               col = x%3;
               no_solution = false;
            }
            x = checkOpDia(Seed.CIRCLE);         //check circle opposite-diagonal
            if(x != -1 && no_solution){
               button = x;
               row = x/3;
               col = x%3;
               no_solution = false;
            }


            x = checkRow(r/3, Seed.CROSS);          //check cross row for winning possibility
            if(x != -1){
               button = r+x;
               row = r/3;
               col = x;
               no_solution = false;
            }
            x = checkCol(r/3, Seed.CROSS);       //check cross column
            if(x != -1){
               for(int c=0; c<9; c++)
                  if(c/3 == x && c%3 == r/3)
                     button = c;
               row = x;
               col = r/3;
               no_solution = false;
            }
            x = checkDia(Seed.CROSS);           //check cross diagonal
            if(x != -1){
               button = x;
               row = x/3;
               col = x%3;
               no_solution = false;
            }
            x = checkOpDia(Seed.CROSS);             //check cross opposite-diagonal
            if(x != -1){
               button = x;
               row = x/3;
               col = x%3;
               no_solution = false;
            }
         }
         if(no_solution){         //if don't find prevention for possible for win generate a random move
            int b;
            do{
               b = r.nextInt(9);
            }while(board[b/3][b%3] != Seed.EMPTY);
            button = b;
            row = b/3;
            col = b%3;
         }
         chooseButton(button);
         updateGame(Seed.CROSS, row, col);
      }
   }

   private int checkRow(int row, Seed theSeed){         //check if there is two in same row, if yes return the missing row else just return -1
      if(board[row][0] == Seed.EMPTY && board[row][1] == theSeed && board[row][2] == theSeed)
         return 0;
      else if(board[row][0] == theSeed && board[row][1] == Seed.EMPTY && board[row][2] == theSeed)
         return 1;
      else if(board[row][0] == theSeed && board[row][1] == theSeed && board[row][2] == Seed.EMPTY)
         return 2;
      return -1;
   }

   private int checkCol(int col, Seed theSeed){      //check if there is two in same column, if yes return the missing column else just return -1
      if(board[0][col] == Seed.EMPTY && board[1][col] == theSeed && board[2][col] == theSeed)
         return 0;
      else if(board[0][col] == theSeed && board[1][col] == Seed.EMPTY && board[2][col] == theSeed)
         return 1;
      else if(board[0][col] == theSeed && board[1][col] == theSeed && board[2][col] == Seed.EMPTY)
         return 2;
      return -1;
   }

   private int checkDia(Seed theSeed){       //check if there is two in same diagonal, if yes return the missing diagonal else just return -1
      if(board[0][0] == Seed.EMPTY && board[1][1] == theSeed && board[2][2] == theSeed)
         return 0;
      if(board[0][0] == theSeed && board[1][1] == Seed.EMPTY && board[2][2] == theSeed)
         return 4;
      if(board[0][0] == theSeed && board[1][1] == theSeed && board[2][2] == Seed.EMPTY)
         return 8;
      return -1;

   }

   private int checkOpDia(Seed theSeed){  //check if there is two in same opposite-diagonal, if yes return the missing one else just return -1
      if(board[0][2] == Seed.EMPTY && board[1][1] == theSeed && board[2][0] == theSeed)
         return 2;
      if(board[0][2] == theSeed && board[1][1] == Seed.EMPTY && board[2][0] == theSeed)
         return 4;
      if(board[0][2] == theSeed && board[1][1] == theSeed && board[2][0] == Seed.EMPTY)
         return 6;
      return -1;
   }

   private void chooseButton(int i){     //set the status for the chosen button from AI
      board[i/3][i%3] = currentPlayer;
      buttons[i].setEnabled(false);
      currentPlayer = Seed.CIRCLE;
      buttons[i].setFont(font);
      buttons[i].setText("X");
      currentStatus.setText("O's Turn");
      currentState = GameState.PLAYING;
      currentTurn = turn.PLAYER;
   }

   private void updateGame(Seed theSeed, int row, int col){   //update game status
      if(hasWon(theSeed, row, col)){       //check is there a winner
         if(theSeed == Seed.CROSS){
            currentState = GameState.CROSS_WON;
            currentStatus.setForeground(Color.red);
            currentStatus.setText("'X' Won! Click restart to play again.");
            currentTurn = turn.PLAYER;
         }else{
            currentState = GameState.CIRCLE_WON;
            currentStatus.setForeground(Color.red);
            currentStatus.setText("'O' Won! Click restart to play again.");
            currentTurn = turn.PLAYER;
         }
      }else if(isDraw()){             //esle check is there a draw
         currentState = GameState.DRAW;
         currentStatus.setForeground(Color.red);
         currentStatus.setText("DRAW! Click restart to play again.");
         currentTurn = turn.PLAYER;
      }
   }

   private boolean hasWon(Seed theSeed, int row, int col){          //check 3 in a row or 3 in column or 3 in diagonal or 3 in opposite-diagonal
      return((board[row][0] == theSeed && board[row][1] == theSeed && board[row][2] == theSeed)
            || (board[0][col] == theSeed && board[1][col] == theSeed && board[2][col] == theSeed)
            || (board[0][0] == theSeed && board[1][1] == theSeed && board[2][2] == theSeed)
            || (board[0][2] == theSeed && board[1][1] == theSeed && board[2][0] == theSeed));
   }

   private boolean isDraw(){           //check is there any empty button
      for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 3; ++col)
               if (board[row][col] == Seed.EMPTY)
                  return false; // an empty button found, not draw, exit
      return true;
   }
}

class buildRightPanel extends JPanel implements ActionListener{       //right panel

   public buildRightPanel(){
      rightPanel = new JPanel();
      rightPanel.setLayout(new BorderLayout());

      resetPanel = new JPanel();
      reset = new JButton("Restart");             //create a restart button
      reset.addActionListener(this);
      resetPanel.add(reset);
      new buildOpponentPanel();           //build opponent list
      rightPanel.add(resetPanel, BorderLayout.CENTER);
   }


   public void actionPerformed(ActionEvent e){
   if(e.getSource() == reset){
         initGame();
         resetButtuns();
      }
   }

   class buildOpponentPanel extends JPanel implements ActionListener{

      public buildOpponentPanel(){
      opponentPanel = new JPanel();
      opponentPanel.setLayout(new GridLayout(3,1));
      player = new JRadioButton("Player vs. Player",true);         //Default opponent is player vs. player
      dumbAI = new JRadioButton("Player vs. dumb AI");
      smartAI = new JRadioButton("Player vs. smartAI");
      opponents = new ButtonGroup();                      //group all radio button
      opponents.add(player);
      opponents.add(dumbAI);
      opponents.add(smartAI);
      player.addActionListener(this);
      dumbAI.addActionListener(this);
      smartAI.addActionListener(this);
      opponentPanel.add(player);
      opponentPanel.add(dumbAI);
      opponentPanel.add(smartAI);
      rightPanel.add(opponentPanel, BorderLayout.NORTH);
   }

   public void actionPerformed(ActionEvent e) {      //if game is playing don't allow to change the opponent
         if(currentState == GameState.PLAYING){
            JOptionPane.showMessageDialog(null, "Cannot change the opponent during a game\nClick 'ok' to continue");
            if(vs == opponent.VS_player)
               player.setSelected(true);
            else if(vs == opponent.VS_dumbAI)
               dumbAI.setSelected(true);
            else
               smartAI.setSelected(true);
         }
         else if(e.getSource() == player)
            vs = opponent.VS_player;
         else if(e.getSource() == dumbAI)
            vs = opponent.VS_dumbAI;
         else
            vs = opponent.VS_smartAI;

      }
   }
}

   public static void main(String[] args){
      new TicTacToe();
   }
}

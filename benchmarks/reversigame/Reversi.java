/**
Copyright (c) 2011-present - Luu Gia Thuy

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.
*/

/*
* The original file is available at:
* https://github.com/luugiathuy/ReversiGame
*
* Modifications:
* (1) Squashed java files into a single java file.
* (2) Modify the Man-machine war to Machine to war. Delete flag "mIsCompTurn".
* (3) Modify some flags, such as "mIsBlackTurn = false" to "mIsBlackTurn = true.
*     - The ruler says the black is first. so modify the "mIsBlackTurn = true".
*     - Because both sides have no pieces to end the game. Add the flag "mNoMove"
*       to record whether the other party can not move pieces.
*       If the second party can't move pieces, end the game.
* (4) Add test function "timeReversi", "verifyReversi" for test.
*/
package benchmarks.reversigame;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Vector;

import benchmarks.reversigame.Agent.MoveCoord;
import benchmarks.reversigame.Agent.MoveScore;

// CHECKSTYLE.OFF: .*
class Evaluation {
	
	private static final int sBOARD_SIZE = Reversi.sBOARD_SIZE;
	
	private static int[][] sBOARD_VALUE = {
		{100, -1, 5, 2, 2, 5, -1, 100},
		{-1, -10,1, 1, 1, 1,-10, -1},
		{5 , 1,  1, 1, 1, 1,  1,  5},
		{2 , 1,  1, 0, 0, 1,  1,  2},
		{2 , 1,  1, 0, 0, 1,  1,  2},
		{5 , 1,  1, 1, 1, 1,  1,  5},
		{-1,-10, 1, 1, 1, 1,-10, -1},
		{100, -1, 5, 2, 2, 5, -1, 100}};
	
	public static int evaluateBoard(char[][] board, char piece, char oppPiece) {
		int score = 0;
		for (int r = 0; r < sBOARD_SIZE; ++r) {
			for (int c = 0; c < sBOARD_SIZE; ++c) {
				if (board[r][c] == piece)
					score += sBOARD_VALUE[r][c];
				else if (board[r][c] == oppPiece)
					score -= sBOARD_VALUE[r][c];
			}
		}
		return score;
	}
	
	public static ArrayList<MoveCoord> genPriorityMoves(char[][] board, char piece) {
		ArrayList<MoveCoord> moveList = Reversi.findValidMove(board, piece, false);
		PriorityQueue<MoveScore> moveQueue = new PriorityQueue<MoveScore>();
		
		for (int i=0; i < moveList.size(); ++i) {
			MoveCoord move = moveList.get(i);
			MoveScore moveScore = new MoveScore(move, sBOARD_VALUE[move.getRow()][move.getCol()]);
			moveQueue.add(moveScore);
		}
		
		moveList = new ArrayList<MoveCoord>();
		while (!moveQueue.isEmpty()) {
			moveList.add(moveQueue.poll().getMove());
		}
		
		return moveList;
	}
	
	
}

/**
 * This is an interface for ReversiAgents to work with the GUI interface.
 */
interface Agent {

	/**
	 * Method to make a move. game state is stored in the class variables
	 *
	 * Method for you to implement. You want to make your modifications here.
	 *
	 * @return Returns a move.
	 */
	MoveCoord findMove(char[][] board, char piece);

	/**
	 * class that implements a pair of integer coordinates
	 */
	public class MoveCoord {
		private int row;

		private int col;

		/**
		 * constructor for a Pair of coordinates
		 */
		public MoveCoord(int row, int col) {
			this.row = row;
			this.col = col;
		}

		/** accessor methods */
		public int getRow() {
			return this.row;
		}

		public int getCol() {
			return this.col;
		}

		/** mutation methods */
		public void setRow(int row) {
			this.row = row;
		}

		public void setCol(int col) {
			this.col = col;
		}


		/** takes a pair of x,y coordinates, converts to standard board notation */
		public static String encode(int row, int col) {
			return ("" + new Character((char) ('A' + col)) + (row + 1));
		}
	}
	
	/**
	* Class for presenting the move and the score together
	*/
	public class MoveScore implements Comparable<MoveScore>{
		private MoveCoord move ;
	    private int score ;
	    
	    public MoveScore(MoveCoord move, int score){
	        this.move = move;
	        this.score = score;
	    }
	    
	    public int getScore(){ 
	    	return score ;
	    }
	    
	    public MoveCoord getMove(){ 
		  	return move ;
	   }

		@Override
		public int compareTo(MoveScore o) {
			if(o.score > this.score)
				return 1;
			else if (o.score < this.score)
				return -1;
			else
				return 0;
		}
	}
}

class NegaScoutAgent implements Agent{
        
    static final int INFINITY = 1000000;
    
    private int mMaxPly = 5;
	
	@Override
	public MoveCoord findMove(char[][] board, char piece) {
		return abNegascoutDecision(board, piece);
	}
	
	public MoveCoord abNegascoutDecision(char[][] board, char piece){
    	MoveScore moveScore = abNegascout(board,0,-INFINITY,INFINITY,piece);
    	return moveScore.getMove();
    }
    
    /**
     * Searching the move using NegaScout
     * @param board the state of game
     * @param ply the depth of searching
     * @param alpha the boundary
     * @param beta the boundary
     * @param player the player will find the move
     * @return the pair of move and score
     */
    public MoveScore abNegascout(char[][] board, int ply, int alpha, int beta, char piece){
    	char oppPiece = (piece == Reversi.sBLACK_PIECE) ? Reversi.sWHITE_PIECE : Reversi.sBLACK_PIECE;
    	
    	// Check if we have done recursing
    	if (ply==mMaxPly){
            return new MoveScore(null, Evaluation.evaluateBoard(board, piece, oppPiece));
        }
    		
    	int currentScore;
    	int bestScore = -INFINITY;
    	MoveCoord bestMove = null;
    	int adaptiveBeta = beta; 	// Keep track the test window value
    	
    	// Generates all possible moves
    	ArrayList<MoveCoord> moveList = Evaluation.genPriorityMoves(board, piece);
    	if (moveList.isEmpty())
    		return new MoveScore(null, bestScore);
    	bestMove = moveList.get(0);
    	
    	// Go through each move
    	for(int i=0;i<moveList.size();i++){
    		MoveCoord move = moveList.get(i);
    		char[][] newBoard = new char[8][8];
    		for (int r = 0; r < 8; ++r)
    			for (int c=0; c < 8; ++c)
    				newBoard[r][c] = board[r][c];
    		
    		// Recurse
    		MoveScore current = abNegascout(newBoard, ply+1, -adaptiveBeta, - Math.max(alpha,bestScore), oppPiece);
    		
    		currentScore = - current.getScore();
    		
    		// Update bestScore
    		if (currentScore>bestScore){
    			// if in 'narrow-mode' then widen and do a regular AB negamax search
    			if (adaptiveBeta == beta || ply>=(mMaxPly-2)){
    				bestScore = currentScore;
					bestMove = move;
    			}else{ // otherwise, we can do a Test
    				current = abNegascout(newBoard, ply+1, -beta, -currentScore, oppPiece);
    				bestScore = - current.getScore();
    				bestMove = move;
    			}
    			
    			// If we are outside the bounds, the prune: exit immediately
        		if(bestScore>=beta){
        			return new MoveScore(bestMove,bestScore);
        		}
        		
        		// Otherwise, update the window location
        		adaptiveBeta = Math.max(alpha, bestScore) + 1;
    		}
    	}
    	return new MoveScore(bestMove,bestScore);
    }
}

/**
 * Game logic
 * @author luugiathuy
 *
 */
public class Reversi {
	
	/** Game State */
	public static final int PLAYING = 0;
	public static final int ENDED = 1;

	/** number of rows */
	public static final int sBOARD_SIZE = 8;
	
	/** piece represents black */
	public static final char sBLACK_PIECE = 'b';
	
	/** piece represents white */
	public static final char sWHITE_PIECE = 'w';
	
	/** susggest piece for black */
	public static final char sSUGGEST_BLACK_PIECE = 'p';
	
	/** susggest piece for white */
	public static final char sSUGGEST_WHITE_PIECE = 'u';
	
	/** empty piece */
	public static final char sEMPTY_PIECE = '-';
	
	/** move offset for row */
	private static final int[] sOFFSET_MOVE_ROW = {-1, -1, -1,  0,  0,  1,  1,  1};
	
	/** move offset for column */
	private static final int[] sOFFSET_MOVE_COL = {-1,  0,  1, -1,  1, -1,  0,  1};
	
	/** board init */
	private static final char[][] sINIT_BOARD = {	{ sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE },	// 1
													{ sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE },	// 2
													{ sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE }, // 3
													{ sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sBLACK_PIECE, sWHITE_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE },	// 4
													{ sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sWHITE_PIECE, sBLACK_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE }, // 5
													{ sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE }, // 6
													{ sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE }, // 7
													{ sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE, sEMPTY_PIECE }};// 8
													// 		a    		  b    			c    	      d    		 	e    	  	  f    			g    		  h
	
	/** whether it is black's turn to move */
	private boolean mIsBlackTurn = false;
	
	/** whether player move */
	private boolean mNoMove = false;
	
	/** the board */
	private char[][] mBoard;
	
	/** score of black piece */
	private int mBlackScore;
	
	/** score of white piece */
	private int mWhiteScore;
	
	/** state of the game */
	private int mState;
	
	/** AI agent */
	private Agent mAIAgent;
	
	/** new piece position */
	private int mNewPieceRow;
	private int mNewPieceCol;
	
	/** whether a piece is changed*/
	private boolean[][] mIsEffectedPiece;
	
	private Vector<String> mMoveList;
	
	/** Private constructor */
	public Reversi() {
		init();
	}
	
	/** Initialize the board */
    private void init() {
    	// init board
		mBoard = new char[sBOARD_SIZE][sBOARD_SIZE];
		
		// init effected pieces
		mIsEffectedPiece = new boolean[sBOARD_SIZE][sBOARD_SIZE];
		
		// init move list
		mMoveList = new Vector<String>();
		
		// set up AI agent
		mAIAgent = new NegaScoutAgent();
		
		// set default
		mNoMove = false;
	}
    
    public char[][] getBoard() {
    	return mBoard;
    }
    
    /** Gets game state */
    public int getGameState() {
    	return mState;
    }
    
    /** Sets game state */
    public void setGameState(int state) {
    	mState = state;
    }
    
    /** Get white's score */
    public int getWhiteScore() {
    	return mWhiteScore;
    }
    
    /** Get black's score */
    public int getBlackScore() {
    	return mBlackScore;
    }
    
    public boolean isNewPiece(int row, int col) {
    	return (mNewPieceRow == row && mNewPieceCol == col);
    }
    
    public Vector<String> getMoveList() {
    	return mMoveList;
    }
    
    /** New game */
	public void newGame() {
		// reset the board
		resetBoard();		
		// reset effected pieces
		resetEffectedPieces();
		// black piece starts first for ruler
		mIsBlackTurn = true;
		// set state
		mState = PLAYING;
                // Because both sides have no pieces to end the game.
                // "mNoMove" is used to record whether the other party can not
                // move pieces. If the second party can't move pieces, end the game.
                mNoMove = false;
		stateChange();
		
	}
    
    /** Reset the board */
	private void resetBoard() {
		for (int i=0; i < sBOARD_SIZE; ++i)
			for (int j=0; j < sBOARD_SIZE; ++j)
				mBoard[i][j] = sINIT_BOARD[i][j];
		
		mBlackScore = 2;
		mWhiteScore = 2;
		
		mNewPieceRow = -1;
		mNewPieceCol = -1;
		
		mMoveList.removeAllElements();
	}
	
	public void resetEffectedPieces() {
		for (int i=0; i < sBOARD_SIZE; ++i)
			for (int j=0; j < sBOARD_SIZE; ++j)
				mIsEffectedPiece[i][j] = false;
	}
	
	public void setEffectedPiece(int row, int col) {
		mIsEffectedPiece[row][col] = true;
	}
	
	public boolean isEffectedPiece(int row, int col) {
		return mIsEffectedPiece[row][col];
	}
	
	/** Get next move */
	private void getNextMove() {
			char piece = (mIsBlackTurn) ? sBLACK_PIECE : sWHITE_PIECE;
			
			// clear all suggested pieces
			for (int i=0 ;i < sBOARD_SIZE; ++i)
				for (int j=0; j < sBOARD_SIZE; ++j)
					if (mBoard[i][j] == sSUGGEST_BLACK_PIECE || mBoard[i][j] == sSUGGEST_WHITE_PIECE)
						mBoard[i][j] = sEMPTY_PIECE;
			
			// copy board to temp
			char[][] tempBoard = new char[8][8];
			for (int i=0; i< sBOARD_SIZE; ++i)
				for (int j=0; j < sBOARD_SIZE; ++j)
					tempBoard[i][j] = mBoard[i][j];
			
			// find optimal move
			MoveCoord move = mAIAgent.findMove(tempBoard, piece);
			if (move != null)
			{
                                mNoMove = false;
				effectMove(mBoard, piece, move.getRow(), move.getCol());
				addToMoveList(piece, move.getRow(), move.getCol());
				mNewPieceRow = move.getRow();
				mNewPieceCol = move.getCol();
				stateChange();
			} else {
                                if (mNoMove) {
                                        mState = ENDED;
                                } else {
                                        mNoMove = true;
                                }
                        }
			
			// next move
			changeTurn();
	}
	
	/** add a move to move list */
	private void addToMoveList(char piece, int row, int col) {
		String str = String.format("%s:\t%s", String.valueOf(piece).toUpperCase(), MoveCoord.encode(row, col));
		mMoveList.add(str);
	}
	
	/** change turn of playing */
	private void changeTurn() {
		mIsBlackTurn = !mIsBlackTurn;
	}
	
	/** Calculate score */
	private void calScore() {
		mBlackScore = 0;
		mWhiteScore = 0;
		for (int i = 0; i < sBOARD_SIZE; ++i)
			for (int j = 0; j < sBOARD_SIZE; ++j)
			{
				if (mBoard[i][j] == sBLACK_PIECE)
					++mBlackScore;
				else if (mBoard[i][j] == sWHITE_PIECE)
					++mWhiteScore;
			}
                if (mBlackScore + mWhiteScore >= sBOARD_SIZE*sBOARD_SIZE) {
                        mState = ENDED;
                }
	}
	
	/**
	 * Finds valid moves for specific piece
	 * @param board the board
	 * @param piece the piece need to find move
	 * @param isSuggest true to indicate suggested pieces on the board
	 * @return an array list of moves
	 */
	public static ArrayList<MoveCoord> findValidMove(char[][] board, char piece, boolean isSuggest) {
		char suggestPiece = (piece == sBLACK_PIECE) ? sSUGGEST_BLACK_PIECE : sSUGGEST_WHITE_PIECE;
		
		ArrayList<MoveCoord> moveList = new ArrayList<MoveCoord>();
		for (int i = 0; i < 8; ++i)
			for (int j = 0; j < 8; ++j) {
				// clean the suggest piece before
				if (board[i][j] == sSUGGEST_BLACK_PIECE || board[i][j] == sSUGGEST_WHITE_PIECE)
					board[i][j] = sEMPTY_PIECE;
				
				if (isValidMove(board,piece, i, j))
				{
					moveList.add(new MoveCoord(i, j));
					
					// if we want suggestion, mark on board
					if (isSuggest)
						board[i][j] = suggestPiece;
				}
			}
		
		return moveList;
	}
	
	/**
	 * Check whether a move is valid
	 * @param board the board
	 * @param piece the piece need to check
	 * @param row row of the move
	 * @param col column of the move
	 * @return true if the move is valid, false otherwise
	 */
	public static boolean isValidMove(char[][] board, char piece, int row, int col) {
		// check whether this square is empty
		if (board[row][col] != sEMPTY_PIECE)
			return false;
		
		char oppPiece = (piece == sBLACK_PIECE) ? sWHITE_PIECE : sBLACK_PIECE;
		
		boolean isValid = false;
		// check 8 directions
		for (int i = 0; i < 8; ++i) {
			int curRow = row + sOFFSET_MOVE_ROW[i];
			int curCol = col + sOFFSET_MOVE_COL[i];
			boolean hasOppPieceBetween = false;
			while (curRow >=0 && curRow < 8 && curCol >= 0 && curCol < 8) {
				
				if (board[curRow][curCol] == oppPiece)
					hasOppPieceBetween = true;
				else if ((board[curRow][curCol] == piece) && hasOppPieceBetween)
				{
					isValid = true;
					break;
				}
				else
					break;
				
				curRow += sOFFSET_MOVE_ROW[i];
				curCol += sOFFSET_MOVE_COL[i];
			}
			if (isValid)
				break;
		}
		
		return isValid;
	}
	
	/**
	 * Effect the move
	 * @param board the board
	 * @param piece the piece of move
	 * @param row row of the move
	 * @param col column of the move
	 * @return the new board after the move is affected
	 */
	public char[][] effectMove(char[][] board, char piece, int row, int col) {
		board[row][col] = piece;
		
		resetEffectedPieces();
		
		// check 8 directions
		for (int i = 0; i < 8; ++i) {
			int curRow = row + sOFFSET_MOVE_ROW[i];
			int curCol = col + sOFFSET_MOVE_COL[i];
			boolean hasOppPieceBetween = false;
			while (curRow >=0 && curRow < 8 && curCol >= 0 && curCol < 8) {
				// if empty square, break
				if (board[curRow][curCol] == sEMPTY_PIECE)
					break;
				
				if (board[curRow][curCol] != piece)
					hasOppPieceBetween = true;
				
				if ((board[curRow][curCol] == piece) && hasOppPieceBetween)
				{
					int effectPieceRow = row + sOFFSET_MOVE_ROW[i];
					int effectPieceCol = col + sOFFSET_MOVE_COL[i];
					while (effectPieceRow != curRow || effectPieceCol != curCol)
					{
						setEffectedPiece(effectPieceRow, effectPieceCol);
						board[effectPieceRow][effectPieceCol] = piece;
						effectPieceRow += sOFFSET_MOVE_ROW[i];
						effectPieceCol += sOFFSET_MOVE_COL[i];
					}
					 
					break;
				}
				
				curRow += sOFFSET_MOVE_ROW[i];
				curCol += sOFFSET_MOVE_COL[i];
			}
		}
		
		return board;
	}
	
	/**
	 * human move piece
	 * @param row row of the move
	 * @param col column of the move
	 */
	public void movePiece(int row, int col) {
		char piece = (mIsBlackTurn) ? sBLACK_PIECE : sWHITE_PIECE;
		char suggestPiece = (mIsBlackTurn) ? sSUGGEST_BLACK_PIECE : sSUGGEST_WHITE_PIECE;
		if (mBoard[row][col] == suggestPiece)
		{
			effectMove(mBoard, piece, row, col);
			mNewPieceRow = row;
			mNewPieceCol = col;
			
			// add to move list
			addToMoveList(piece, row, col);
			// notify the observer
			stateChange();
			
			// change turn
			changeTurn();
			getNextMove();
		}
	}
    
	private void stateChange() {
		calScore();
	}
// CHECKSTYLE.ON: .*

  public void timeReversi(int iterations) {
    for (int iter = 0; iter < iterations; iter++) {
      newGame();
      while (getGameState() == PLAYING) {
        getNextMove();
      }
    }
  }

  public boolean verifyReversi() {
    newGame();
    while (getGameState() == PLAYING) {
      getNextMove();
    }

    int blackScore = getBlackScore();
    int whiteScore = getWhiteScore();

    if (blackScore != 15) {
      System.out.println("ERROR: blackScore should be 15");
      return false;
    }

    if (whiteScore != 49) {
      System.out.println("ERROR: whiteScore should be 49");
      return false;
    }

    return true;
  }

  public static void main(String []argv) {
    int rc = 0;
    Reversi obj = new Reversi();

    long before = System.currentTimeMillis();
    obj.timeReversi(3);
    long after = System.currentTimeMillis();

    System.out.println("benchmarks/algorithm/Reversi: " + (after - before));

    if (!obj.verifyReversi()) {
      rc++;
    }

    System.exit(rc);
  }
}

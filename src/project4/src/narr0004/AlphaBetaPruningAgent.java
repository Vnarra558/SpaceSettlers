package narr0004;

import spacesettlers.game.*;

/**
 * This agent utilizes the Alpha-Beta Pruning technique to make decisions
 * on the TicTacToe2D and 3D game.
 */
public class AlphaBetaPruningAgent extends AbstractGameAgent {

    // variable to store my player id.
    private int myPlayer = 0;
    // variable to store my opponent player id.
    private int opponentPlayer = 0;
    // Alpha value for the Alpha-Beta Pruning.
    private double alpha = Double.NEGATIVE_INFINITY;
    // Beta value for the Alpha-Beta Pruning.
    private double beta = Double.POSITIVE_INFINITY;


    public AlphaBetaPruningAgent() {
    }

    /**
     * Gives the next move for the game, either in 2D or 3D based on the Asteroid collected .
     *
     * @param game The current game state.
     * @return The next action to perform.
     */
    @Override
    public AbstractGameAction getNextMove(AbstractGame game) {

        if (game.getClass() == TicTacToe2D.class) {
            return getNextMove2D(game);
        } else {
            return getNextMove3D(game);
        }
    }
    /**
     * Determines the next move for the TicTacToe2D game.
     * and also available moves and chooses the best one using the Alpha-Beta Pruning technique.
     *
     * @param game The current 2D TicTacToe game.
     * @return  best action to perform.
     */
    public AbstractGameAction getNextMove2D(AbstractGame game) {
        // get the player number assigned to us.
        if (myPlayer != super.getPlayer()) {
            myPlayer = super.getPlayer();
            if (myPlayer == 1) {
                opponentPlayer = 2;
            } else {
                opponentPlayer = 1;
            }
        }
        System.out.println(" My Player Id:" + myPlayer);
        System.out.println(" Opponent Player id: " + opponentPlayer);


        TicTacToe2DBoard currentBoard = (TicTacToe2DBoard) game.getBoard();

        int[][] boardCopy = currentBoard.getBoard();
        System.out.println(" Using AlphaBetaPruning algorithm to check which move to make:");
        TicTacToe2DAction bestMove = new TicTacToe2DAction(0, 0);
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if(boardCopy[i][j] == 0) {
                    TicTacToe2DAction action = new TicTacToe2DAction(i, j );
                    currentBoard.makeMove(action,myPlayer);

                    double score = AlphaBetaPruningAlgorithm2D(boardCopy, 0, false, alpha, beta);
                    if(score > bestScore) {
                        bestScore = score;
                        bestMove = action;
                    }
                    currentBoard.unMakeMove(action);
                }
            }
        }

        System.out.println(" Finally, making this move on board at this location : \t" + bestMove.getRow() + "\t" + bestMove.getCol());
        return bestMove;
    }
    /**
     * The Alpha-Beta Pruning algorithm2D to evaluate the best move for the TicTacToe2D game.
     * It recursively evaluates the every possible move using the alpha and beta values to prune
     * branches that don't need to be explored.
     *
     * @param boardCopy The current state of the game board.
     * @param depth The current depth of the game tree.
     * @param isMaxPlayer Flag to say current player is maximizing or minimizing.
     * @param alpha  alpha value.
     * @param beta  beta value.
     * @return value of the best move.
     */
    public double AlphaBetaPruningAlgorithm2D(int[][] boardCopy, int depth, boolean isMaxPlayer,double alpha, double beta) {
        int positionsAvailableOnBoard = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if(boardCopy[i][j] == 0) {
                    positionsAvailableOnBoard++;
                }
            }
        }
        if(positionsAvailableOnBoard == 0) {
            System.out.println(" No available position on board. So, returning 0 as best value." );
            return 0;
        }

        //Setting the current board to see who is wining for the current move.
        TicTacToe2DBoard currentBoard = new TicTacToe2DBoard();
        currentBoard.setBoard(boardCopy);

        if(currentBoard.getWinningPlayer() != myPlayer && currentBoard.getWinningPlayer() != 0) {
            System.out.println(" After checking the game board, we see opponent player is winning. So, returning -1 as value for the move.");
            return -1;
        }
        else if(currentBoard.getWinningPlayer() == myPlayer ) {
            System.out.println(" After checking the game board, we see my player is winning. So, returning 1 as value for the move.");
            return 1;
        }
        else {
            if(depth >=2) return 0;
        }

        if(isMaxPlayer) {
            //System.out.println(" Chance of MAX player. ");
            double bestScore = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if(boardCopy[i][j] == 0) {
                        TicTacToe2DAction action = new TicTacToe2DAction(i,j);
                        currentBoard.makeMove(action,myPlayer);
                        double score = AlphaBetaPruningAlgorithm2D(boardCopy, depth + 1, false,alpha,beta);
                        bestScore = Math.max(score, bestScore);
                        currentBoard.unMakeMove(action);

                        alpha = Math.max(alpha, bestScore);

                        if(beta <= alpha){
                            return bestScore;
                        }

                    }
                }
            }
            return bestScore;
        }
        else {
            double bestScore = Double.POSITIVE_INFINITY;
            //System.out.println(" Chance of MIN player. ");
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if(boardCopy[i][j] == 0) {
                        TicTacToe2DAction action = new TicTacToe2DAction(i,j);
                        currentBoard.makeMove(action,opponentPlayer);

                        double score = AlphaBetaPruningAlgorithm2D(boardCopy, depth + 1, true,alpha,beta);
                        bestScore = Math.min(score, bestScore);
                        currentBoard.unMakeMove(action);

                        beta = Math.min(beta, bestScore);

                        if(beta <= alpha){
                            return bestScore;
                        }
                    }
                }
            }
            return bestScore;
        }
    }
    /**
     * Determines the next move for the TicTacToe3D game.
     * and also available moves and chooses the best one using the Alpha-Beta Pruning technique.
     *
     * @param game The current 3D TicTacToe game.
     * @return  best action to perform.
     */
    public AbstractGameAction getNextMove3D(AbstractGame game) {
        // get the player number assigned to us.
        if (myPlayer != super.getPlayer()) {
            myPlayer = super.getPlayer();
            if (myPlayer == 1) {
                opponentPlayer = 2;
            } else {
                opponentPlayer = 1;
            }
        }
        System.out.println(" My Player Id:" + myPlayer);
        System.out.println(" Opponent Player id: " + opponentPlayer);

        TicTacToe3DBoard currentBoard = (TicTacToe3DBoard) game.getBoard();

        int[][][] boardCopy = currentBoard.getBoard();
        System.out.println(" Using Minimax algorithm to check which move to make:");
        TicTacToe3DAction bestMove = new TicTacToe3DAction(0, 0, 0);
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    if(boardCopy[i][j][k] == 0) {
                        TicTacToe3DAction action = new TicTacToe3DAction(i, j, k);
                        currentBoard.makeMove(action,myPlayer);
                        double score = AlphaBetaPruningAlgorithm3D(boardCopy, 0, false,alpha, beta);
                        if(score > bestScore) {
                            bestScore = score;
                            bestMove = action;
                        }
                        currentBoard.unMakeMove(action);
                    }
                }
            }
        }
        System.out.println(" Finally, making this move on board at this location : \t" + bestMove.getRow() + "\t" + bestMove.getCol() + "\t" + bestMove.getDepth());
        return bestMove;
    }
    /**
     * The Alpha-Beta Pruning algorithm3D to evaluate the best move for the TicTacToe3D game.
     * It recursively evaluates the every possible move using the alpha and beta values to prune
     * branches that don't need to be explored.
     *
     * @param boardCopy The current state of the game board.
     * @param depth The current depth of the game tree.
     * @param isMaxPlayer Flag to say current player is maximizing or minimizing.
     * @param alpha  alpha value.
     * @param beta  beta value.
     * @return value of the best move.
     */
    public double AlphaBetaPruningAlgorithm3D(int[][][] boardCopy, int depth, boolean isMaxPlayer, double alpha, double beta) {
        int positionsAvailableOnBoard = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    if(boardCopy[i][j][k] == 0) {
                        positionsAvailableOnBoard++;
                    }
                }
            }
        }
        if(positionsAvailableOnBoard == 0) {
            System.out.println(" No available position on board. So, returning 0 as best value." );
            return 0;
        }

        //Setting the current board to see who is wining for the current move.
        TicTacToe3DBoard currentBoard = new TicTacToe3DBoard();
        currentBoard.setBoard(boardCopy);

        if(currentBoard.getWinningPlayer() != myPlayer && currentBoard.getWinningPlayer() != 0) {
            //System.out.println(" After checking the game board, we see opponent player is winning. So, returning -1 as value for the move.");
            return -1;
        }
        else if(currentBoard.getWinningPlayer() == myPlayer ) {
            //System.out.println(" After checking the game board, we see my player is winning. So, returning 1 as value for the move.");
            return 1;
        }
        else {
            if(depth >=3) return 0;
        }

        if(isMaxPlayer) {
            //System.out.println(" Chance of MAX player. ");
            double bestScore = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    for (int k = 0; k < 3; k++) {
                        if(boardCopy[i][j][k] == 0) {
                            TicTacToe3DAction action = new TicTacToe3DAction(i,j,k);
                            currentBoard.makeMove(action,myPlayer);
                            double score = AlphaBetaPruningAlgorithm3D(boardCopy, depth + 1, false,alpha,beta);
                            bestScore = Math.max(score, bestScore);
                            currentBoard.unMakeMove(action);

                            alpha = Math.max(alpha, bestScore);

                            if(beta <= alpha){
                                return bestScore;
                            }
                        }
                    }
                }
            }
            return bestScore;
        }
        else {
            double bestScore = Double.POSITIVE_INFINITY;
            //System.out.println(" Chance of MIN player. ");
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    for (int k = 0; k < 3; k++) {
                        if(boardCopy[i][j][k] == 0) {
                            TicTacToe3DAction action = new TicTacToe3DAction(i,j,k);
                            currentBoard.makeMove(action,opponentPlayer);
                            double score = AlphaBetaPruningAlgorithm3D(boardCopy, depth + 1, true,alpha,beta);
                            bestScore = Math.min(score, bestScore);
                            currentBoard.unMakeMove(action);

                            beta = Math.min(beta, bestScore);

                            if(beta <= alpha){
                                return bestScore;
                            }
                        }
                    }
                }
            }
            return bestScore;
        }
    }

}

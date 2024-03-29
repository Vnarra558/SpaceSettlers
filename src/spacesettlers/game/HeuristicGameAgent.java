package spacesettlers.game;

/**
 * Plays the game provided by the factory by calling the right actual heuristic agent
 */
public class HeuristicGameAgent extends AbstractGameAgent{
    HeuristicTicTacToe3DGameAgent heuristic3DTTTPlayer;
    HeuristicTicTacToe2DGameAgent heuristic2DTTTPlayer;

    public HeuristicGameAgent() {
        heuristic2DTTTPlayer = new HeuristicTicTacToe2DGameAgent();
        heuristic3DTTTPlayer = new HeuristicTicTacToe3DGameAgent();
    }

    /**
     * Returns the right move depending on which game is passed in
     *
     * @param game any of the games generated by the gaming factory
     * @return the next move for the heuristic agent
     */
    @Override
    public AbstractGameAction getNextMove(AbstractGame game) {
        if (game.getClass() == TicTacToe2D.class) {
            //System.out.println("Getting a move for the 2D game\n");
            heuristic2DTTTPlayer.setPlayer(this.getPlayer());
            return heuristic2DTTTPlayer.getNextMove(game);
        } else {
            //System.out.println("Getting a move for the 3D game\n");
            heuristic3DTTTPlayer.setPlayer(this.getPlayer());
            return heuristic3DTTTPlayer.getNextMove(game);
        }
    }
}

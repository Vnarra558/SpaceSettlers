package narr0004;

import spacesettlers.utilities.Position;

import java.util.List;


/**
 * To create vertex for a graph
 * position position of vertex
 * isStart is start vertex
 * isGoal is goal vertex
 * edgesList list of edges for that vertex
 * 
 */
import spacesettlers.utilities.Position;

import java.util.List;

/**
 * This class will arrange the vertex position, starting vertex and
 * goal vertex and checks if the node is visited or not for arranging the path.
 *
 */

public class Vertex {
    private Position position;
    private Boolean isStart;
    private Boolean isGoal;
    private Boolean isVisited;
    private List<Edge> edgesList;

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Boolean getIsStart() {
        return isStart;
    }

    public void setIsStart(Boolean isStart) {
        this.isStart = isStart;
    }

    public Boolean getIsGoal() {
        return isGoal;
    }

    public void setIsGoal(Boolean isGoal) {
        this.isGoal = isGoal;
    }

    public List<Edge> getEdgesList() {
        return edgesList;
    }

    public void setEdgesList(List<Edge> edgesList) {
        this.edgesList = edgesList;
    }

    public Boolean getVisited() {
        return isVisited;
    }

    public void setVisited(Boolean visited) {
        isVisited = visited;
    }
}

package narr0004;

import spacesettlers.utilities.Position;

import java.util.List;

/**
 * create vertex for the graph
 * flags to check whether start, goal vertex
 * flag to check whether the vertex is visted or not in search
 * list to store edges connected to that vertex
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

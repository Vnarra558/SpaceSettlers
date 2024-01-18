package narr0004;

import java.util.ArrayList;
import java.util.List;

/**
 * Initialize the Graph with all Vertices and edges.
 *
 */
public class Graph {
    List<Vertex> allVertices = new ArrayList<>();
    List<Edge> allEdges = new ArrayList<>();

    public List<Vertex> getAllVertices() {
        return allVertices;
    }

    public void setAllVertices(List<Vertex> allVertices) {
        this.allVertices = allVertices;
    }

    public List<Edge> getAllEdges() {
        return allEdges;
    }

    public void setAllEdges(List<Edge> allEdges) {
        this.allEdges = allEdges;
    }
}

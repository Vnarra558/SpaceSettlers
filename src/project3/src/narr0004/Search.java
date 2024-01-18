package narr0004;


/**
 *  search class used in search algorthims while traversing
 */

public class Search {

    private Vertex vertex;
    private Edge edge;

    private double gCost;

    private double hCost;

    private double fCost;

    public Vertex getVertex() {
        return vertex;
    }

    public void setVertex(Vertex vertex) {
        this.vertex = vertex;
    }

    public Edge getEdge() {
        return edge;
    }

    public void setEdge(Edge edge) {
        this.edge = edge;
    }

    public double getgCost() {
        return gCost;
    }

    public void setgCost(double gCost) {
        this.gCost = gCost;
    }

    public double gethCost() {
        return hCost;
    }

    public void sethCost(double hCost) {
        this.hCost = hCost;
    }

    public double getfCost() {
        return fCost;
    }

    public void setfCost(double fCost) {
        this.fCost = fCost;
    }
}

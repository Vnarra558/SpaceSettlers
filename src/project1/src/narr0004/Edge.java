package narr0004;

/**
 * to create edge for a graph and finds distances between the edges.
 * vertex1 
 * vertex2 
 * 
 */
public class Edge {
    private Vertex vertex1;
    private Vertex vertex2;
    private double distance;

    public Vertex getVertex1() {
        return vertex1;
    }

    public void setVertex1(Vertex vertex1) {
        this.vertex1 = vertex1;
    }

    public Vertex getVertex2() {
        return vertex2;
    }

    public void setVertex2(Vertex vertex2) {
        this.vertex2 = vertex2;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
